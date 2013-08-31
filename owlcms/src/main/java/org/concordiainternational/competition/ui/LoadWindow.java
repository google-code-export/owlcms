/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.util.Locale;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.PlatesInfoEvent.PlatesInfoListener;
import org.concordiainternational.competition.ui.SessionData.UpdateEvent;
import org.concordiainternational.competition.ui.SessionData.UpdateEventListener;
import org.concordiainternational.competition.ui.components.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.incubator.dashlayout.ui.HorDashLayout;
import com.vaadin.incubator.dashlayout.ui.VerDashLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class LoadWindow extends Window implements Property.ValueChangeListener,
        Window.CloseListener, PlatesInfoListener {
    private static final long serialVersionUID = 4907861433698426676L;

    private static final Logger logger = LoggerFactory.getLogger(LoadWindow.class);

    private GridLayout grid = null;
    LifterInfo lifterCardIdentification;
    boolean ignoreChanges;
    private Item availablePlates;
    private SessionData masterData;
    private LoadImage imageArea;

    private UpdateEventListener groupDataListener;

    private Menu menu;

	CompetitionApplication app;

    public LoadWindow(Menu menu) {
        super();
        this.menu = menu;

        final Locale locale = CompetitionApplication.getCurrentLocale();
        app = CompetitionApplication.getCurrent();
        
        masterData = app.getMasterData();
        
        display(locale);
        position();

        registerAsListener(locale);
        
        logger.debug("window {} created",this);
    }

    private void position() {
        setPositionX(600);
        setPositionY(100);
    }

    /**
     * @param resizeButton
     */
    private void resize(final Button resizeButton) {
        if (grid.isVisible()) {
            resizeButton.setCaption("-");
            setWidth("45em");
            setHeight("63ex");
        } else {
            resizeButton.setCaption("+");
            setWidth("30em");
            setHeight("38ex");
        }
    }

    /**
     * Listen to various events.
     * {@link #unregisterAsListener()} undoes the registrations.
     * @param locale
     */
    private void registerAsListener(final Locale locale) {
    	// subwindow closes
        addListener((CloseListener)this);
        
        // main window closes
        app.getMainWindow().addListener((CloseListener)this);
        
        // changes to current lifter
        groupDataListener = new SessionData.UpdateEventListener() {
            @Override
            public void updateEvent(UpdateEvent updateEvent) {
                new Thread(new Runnable() {
					@Override
					public void run() {
						display(locale);
					}
				}).start();
            }
        };
        masterData.addListener(groupDataListener);
        
        // changes to plates available
        masterData.addBlackBoardListener(this);
    }

    /**
     * Clean-up listener registrations.
     * Removes all listeners registered by {@link #registerAsListener(Locale)}
     */
    public void unregisterAsListener() {
	    masterData.removeListener(groupDataListener);
	    CompetitionApplication.getCurrent().getMainWindow().removeListener((CloseListener)this);
	    masterData.removeBlackBoardListener(this);
	    menu.setLoadComputerWindow(null);
	}

	/**
     * @param locale
     */
    @SuppressWarnings("serial")
	private void display(final Locale locale) {
        CompetitionApplication app1 = CompetitionApplication.getCurrent();
        Platform platform = masterData.getPlatform();
        logger.debug("diplaying platform {} {}",platform.getName(),platform);
		availablePlates = new BeanItem<Platform>(platform);
		synchronized (app1) {
            boolean gridIsVisible = (grid == null ? false : grid.isVisible());
            removeAllComponents();
            final int expectedBarWeight = computeOfficialBarWeight();
            this.setSizeFull();

            VerticalLayout root = new VerDashLayout();
            HorizontalLayout top = new HorDashLayout();

            imageArea = new LoadImage(this);

            final Button resizeButton = new Button();
            resizeButton.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    grid.setVisible(!grid.isVisible());
                    resize(resizeButton);
                }

            });

            Form liftGrid = createGrid(locale, expectedBarWeight);
            grid.setVisible(gridIsVisible);
            grid.setMargin(false);
            root.addComponent(top);
            root.addComponent(liftGrid);
            imageArea.computeImageArea(masterData, platform, true);
            resize(resizeButton);

            top.setSizeFull();
            top.addComponent(imageArea);
            top.setComponentAlignment(imageArea, Alignment.MIDDLE_CENTER);
            top.addComponent(resizeButton);
            top.setComponentAlignment(resizeButton, Alignment.BOTTOM_LEFT);
            top.setExpandRatio(resizeButton, 0);
            top.setMargin(true);

            top.setExpandRatio(imageArea, 100);
            this.addComponent(root);
            this.addStyleName("light");
            this.addStyleName("platesPopup");
        }
        app1.push();
    }

    @Override
    public void close() {
        super.close();
        unregisterAsListener();
    }

    /**
     * Create the grid where the number of available plates is shown.
     * 
     * @param locale
     * @param expectedBarWeight
     * @return
     */
    private Form createGrid(Locale locale, int expectedBarWeight) {
        grid = new GridLayout(10, 5);
        Form liftGrid = new Form(grid);
        createGridContents(locale, expectedBarWeight);
        liftGrid.setWriteThrough(true);
        grid.setSpacing(true);
        return liftGrid;
    }

    /**
     * Create the header row and the header column.
     * 
     * @param locale
     * @param expectedBarWeight
     */
    private void createGridContents(Locale locale, int expectedBarWeight) {
        // first row
        int column = 1;
        int row = 0;
        addLabel(new Label("0,5"), column++, row); //$NON-NLS-1$
        addLabel(new Label("1"), column++, row); //$NON-NLS-1$
        addLabel(new Label("1,5"), column++, row); //$NON-NLS-1$
        addLabel(new Label("2"), column++, row); //$NON-NLS-1$
        addLabel(new Label("2.5"), column++, row); //$NON-NLS-1$
        addLabel(new Label("5"), column++, row); //$NON-NLS-1$
        addLabel(new Label(Messages.getString("LoadComputer.Collar", locale)), column++, row); //$NON-NLS-1$

        // second row
        column = 1;
        row = 1;
        // metal plates
        bindGridCell("nbS_0_5", column++, row); //$NON-NLS-1$
        bindGridCell("nbS_1", column++, row); //$NON-NLS-1$
        bindGridCell("nbS_1_5", column++, row); //$NON-NLS-1$
        bindGridCell("nbS_2", column++, row); //$NON-NLS-1$
        bindGridCell("nbS_2_5", column++, row); //$NON-NLS-1$
        bindGridCell("nbS_5", column++, row); //$NON-NLS-1$
        // collar
        bindGridCell("nbC_2_5", column++, row); //$NON-NLS-1$

        // third row
        column = 1;
        row = 2;
        addLabel(new Label("2,5"), column++, row); //$NON-NLS-1$
        addLabel(new Label("5"), column++, row); //$NON-NLS-1$
        addLabel(new Label("10"), column++, row); //$NON-NLS-1$
        addLabel(new Label("15"), column++, row); //$NON-NLS-1$
        addLabel(new Label("20"), column++, row); //$NON-NLS-1$
        addLabel(new Label("25"), column++, row); //$NON-NLS-1$
        addLabel(new Label(Messages.getString("LoadComputer.Bar", locale)), column++, row); //$NON-NLS-1$
        addLabel(new Label(Messages.getString("LoadComputer.NonStandardBar", locale)), column++, row); //$NON-NLS-1$

        // bumper plates
        column = 1;
        row = 3;
        bindGridCell("nbL_2_5", column++, row); //$NON-NLS-1$
        bindGridCell("nbL_5", column++, row); //$NON-NLS-1$
        bindGridCell("nbL_10", column++, row); //$NON-NLS-1$
        bindGridCell("nbL_15", column++, row); //$NON-NLS-1$
        bindGridCell("nbL_20", column++, row); //$NON-NLS-1$
        bindGridCell("nbL_25", column++, row); //$NON-NLS-1$
        // bar
        Field officialBar = bindGridCell("officialBar", column++, row); //$NON-NLS-1$
        Field lightBar = bindGridCell("lightBar", column++, row); //$NON-NLS-1
        if (Integer.parseInt((String) lightBar.getValue()) > 0) {
            officialBar.setValue("0");
        } else {
            officialBar.setValue(Integer.toString(expectedBarWeight));
        }

        // first column
        addLabel(new Label(Messages.getString("LoadComputer.MetalPlates", locale)), 0, 1); //$NON-NLS-1$
        addLabel(new Label(Messages.getString("LoadComputer.BumperPlates", locale)), 0, 3); //$NON-NLS-1$
        // 15 characters wide.
        addLabel(new Label(), 0, 0);
        grid.getComponent(0, 0).setWidth("15ex"); //$NON-NLS-1$

    }

    private void addLabel(Label label, int column, int row) {
        grid.addComponent(label, column, row);
    }

    /**
     * Create empty fields for all the cells.
     * 
     * @param app
     */
    private Field createIntegerField(int column, int row, Locale locale) {
        Field field = null;
        field = new TextField();
        ((TextField) field).setWidth("2.5em"); //$NON-NLS-1$
        field.addValidator(new IntegerValidator(Messages.getString("LifterCardEditor.integerExpected", locale) //$NON-NLS-1$
                ));
        grid.addComponent(field, column, row);
        return field;
    }

    /**
     * Utility routine to associate a grid cell with an availablePlates field through a
     * property.
     * 
     * @param propertyId
     * @param column
     * @param row
     */
    private Field bindGridCell(Object propertyId, int column, int row) {
        Field component = (Field) grid.getComponent(column, row);
        if (component == null) {
            component = createIntegerField(column, row, CompetitionApplication.getCurrentLocale());
        }
        component.setPropertyDataSource(availablePlates.getItemProperty(propertyId));
        component.addListener((ValueChangeListener) this);
        component.setWriteThrough(true);
        component.setReadOnly(false);
        ((TextField) component).setImmediate(true);
        return component;
    }

    /*
     * Event received when any of the cells in the form has changed. We need to
     * recompute the dependent cells (non-Javadoc)
     * 
     * @see
     * com.vaadin.data.Property.ValueChangeListener#valueChange(com.vaadin.data
     * .Property.ValueChangeEvent)
     */
    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        // prevent triggering if called recursively or during initial display
        // prior to editing.

        if (ignoreChanges) {
        	logger.debug("listener in window {} ignoring event",this);
        	return;
        }
		boolean prevIgnoreChanges = ignoreChanges;
    	try {
			logger.debug("listener in window {} notified",this);
			ignoreChanges = true;

			// this means that an editable property has changed in the form; refresh
			// all the computed ones.
			if (event != null) logger.debug(event.getProperty().toString());

			// manage field interdependencies.
			final Integer lightBar = (Integer) availablePlates.getItemProperty("lightBar").getValue();
			if (lightBar != 0) {
			    availablePlates.getItemProperty("officialBar").setValue("0");
			} else {
			    availablePlates.getItemProperty("officialBar").setValue(computeOfficialBarWeight());
			}
			CompetitionApplication.getCurrent().getHbnSession().merge(masterData.getPlatform());
			//logger.debug("value change, after merge, collars: {}",masterData.getPlatform().getNbC_2_5());
			imageArea.computeImageArea(masterData, masterData.getPlatform(), true);
			masterData.fireBlackBoardEvent(new PlatesInfoEvent(this));
		} finally {
			ignoreChanges = prevIgnoreChanges;
		}

    }

    /**
     * @return
     */
    private Integer computeOfficialBarWeight() {
        final Lifter currentLifter = masterData.getCurrentLifter();
        String gender = "M";
        if (currentLifter != null) {
            gender = currentLifter.getGender();
        }
        final int expectedBarWeight = "M".equals(gender) ? 20 : 15;
        return expectedBarWeight;
    }

    @Override
    public void windowClose(CloseEvent e) {
    	logger.debug("plates subwindow closed");
        if (e.getWindow() == this) {
            unregisterAsListener();
        }
    }

    /* Refresh when someone has updated plate loading information in another window.
	 * @see org.concordiainternational.competition.ui.PlatesInfoEvent.PlatesInfoListener#plateLoadingUpdate(org.concordiainternational.competition.ui.PlatesInfoEvent)
	 */
	@Override
	public void plateLoadingUpdate(PlatesInfoEvent event) {
		
		if (event.source != this) {
			logger.debug("event from {} received in {}",event.source,this);
			boolean prevIgnoreChanges = ignoreChanges;
			try {
				ignoreChanges = true;
				display(app.getLocale());
			} finally {
				ignoreChanges = prevIgnoreChanges;
			}
		} else {
			logger.debug("event from {} ignored by self.",event.source);
		}
	}
    



}
