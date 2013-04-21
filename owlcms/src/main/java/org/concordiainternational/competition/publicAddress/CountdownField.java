/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.publicAddress;

import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.publicAddress.IntermissionTimerEvent.IntermissionTimerListener;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.components.ISO8601DateField;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
public class CountdownField extends CustomField implements IntermissionTimerListener {
	
	private static Logger logger = LoggerFactory.getLogger(CountdownField.class);

	private IntermissionTimer timer;
	private Label remainingSecondsDisplay;
	private DateField endTime;
	private DurationField requestedSeconds;
	private BeanItem<IntermissionTimer> timerItem;
	private CompetitionApplication app;

	public CountdownField() {
		app = CompetitionApplication.getCurrent();
		setCompositionRoot(createLayout());
		setInternalValue(null);
	}

	/**
	 * @param item
	 */
	private Layout createLayout() {
		GridLayout grid = new GridLayout(4,3);
		addIntermissionTimerToLayout(grid, 0);
		addEndTimeToLayout(grid, 1);
		addRequestedSecondsToLayout(grid, 2);
		grid.setSpacing(true);
		return grid;
	}

	/**
	 * @param grid
	 * @param row which row of the grid
	 */
	private void addIntermissionTimerToLayout(GridLayout grid, int row) {
		grid.addComponent(new Label(Messages.getString("Field.CountdownField.runningTimer", app.getLocale())),0,row);
		remainingSecondsDisplay = new Label();
		grid.addComponent(remainingSecondsDisplay,1,row);
		HorizontalLayout timerButtons = new HorizontalLayout();
		Button start = new Button(Messages.getString("Field.CountdownField.start", app.getLocale()), new ClickListener() {	
			@Override
			public void buttonClick(ClickEvent event) {
				timer.restart(); // start from the current remaining number, or from scratch if none.
			}
		});
		timerButtons.addComponent(start);
		Button stop = new Button(Messages.getString("Field.CountdownField.stop", app.getLocale()), new ClickListener() {	
			@Override
			public void buttonClick(ClickEvent event) {
				timer.pause();
			}
		});
		timerButtons.addComponent(stop);
        Button clear = new Button(Messages.getString("Field.CountdownField.clear", app.getLocale()), new ClickListener() {    
            @Override
            public void buttonClick(ClickEvent event) {
                requestedSeconds.setValue(0);
            }
        });
        timerButtons.addComponent(clear);		
		grid.addComponent(timerButtons,2,row);
	}
	
	/**
	 * @param grid
	 * @param row which row of the grid
	 */
	private void addEndTimeToLayout(GridLayout grid, int row) {
		Label label = new Label(Messages.getString("Field.CountdownField.endTime", app.getLocale()));
		label.setDescription(Messages.getString("Field.CountdownField.endTimeDescription", app.getLocale()));
		grid.addComponent(label,0,row);
		endTime = new ISO8601DateField();
		endTime.setReadOnly(false);
		endTime.setResolution(DateField.RESOLUTION_MIN);
		endTime.setImmediate(false);
		endTime.setWriteThrough(true);
		grid.addComponent(endTime,1,row);
		
		HorizontalLayout buttons = new HorizontalLayout();
		Button set = new Button(Messages.getString("Field.CountdownField.set", app.getLocale()), new ClickListener() {	
			@Override
			public void buttonClick(ClickEvent event) {
				endTime.commit(); // write to the underlying bean.
			}
		});
		buttons.addComponent(set);
		grid.addComponent(buttons,2,row);
	}
	
	/**
	 * @param grid
	 * @param row which row of the grid
	 */
	private void addRequestedSecondsToLayout(GridLayout grid, int row) {
		Label label = new Label(Messages.getString("Field.CountdownField.requestedSeconds", app.getLocale()));
		label.setDescription(Messages.getString("Field.CountdownField.requestedSecondsDescription", app.getLocale()));
		grid.addComponent(label,0,row);
		
		// wrap a text field to handle the hh:mm:ss and mm:ss formats, returning milliseconds
		final TextField rawField = new TextField();
        requestedSeconds = new DurationField(rawField,Integer.class);
		requestedSeconds.setImmediate(true);
		requestedSeconds.setWriteThrough(true);
		grid.addComponent(requestedSeconds,1,row);
		
		HorizontalLayout buttons = new HorizontalLayout();
		Button set = new Button(Messages.getString("Field.CountdownField.set", app.getLocale()), new ClickListener() {	
			@Override
			public void buttonClick(ClickEvent event) {
				logger.debug("requestedSeconds prior to commit {}, rawfield={}",requestedSeconds.getValue(),rawField.getValue());
				requestedSeconds.commit(); // write to the underlying bean.
			}
		});
		buttons.addComponent(set);
		grid.addComponent(buttons,2,row);
	}
	
    @Override
    public void setInternalValue(Object newValue) throws ReadOnlyException,
            ConversionException {
        // use the official timer -- in our case no one should override.
    	timer = (newValue instanceof IntermissionTimer) ?
    				(IntermissionTimer) newValue
    				: CompetitionApplication.getCurrent().getMasterData().getIntermissionTimer();
  
    	timer = CompetitionApplication.getCurrent().getMasterData().getIntermissionTimer();
        super.setInternalValue(timer);
        timerItem = new BeanItem<IntermissionTimer>(timer);
        
		Property endTimeProperty = timerItem.getItemProperty("endTime");
		endTime.setPropertyDataSource(endTimeProperty);
		
		Property requestedSecondsProperty = timerItem.getItemProperty("requestedSeconds");
		requestedSeconds.setPropertyDataSource(requestedSecondsProperty);
		
		remainingSecondsDisplay.setValue(TimeFormatter.formatAsSeconds(timer.getRemainingMilliseconds()));
    }

	
	@Override
	public Object getValue() {
		return timer;
	}
	
    @Override
	public Class<?> getType() {
		return IntermissionTimer.class;
	}

	@Override
	public String toString() {
		return TimeFormatter.formatAsSeconds(timer.getRemainingMilliseconds());
	}

	@Override
	public void intermissionTimerUpdate(IntermissionTimerEvent event) {
		Integer remainingMilliseconds = event.getRemainingMilliseconds();
		int seconds = TimeFormatter.getSeconds(remainingMilliseconds);
		logger.debug("received update this={} event={}",this.toString(),seconds);
		synchronized (app) {
			remainingSecondsDisplay.setValue(TimeFormatter.formatAsSeconds(remainingMilliseconds));
		}
		app.push();
	}
	
}
