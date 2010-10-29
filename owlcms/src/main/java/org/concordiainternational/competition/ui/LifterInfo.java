/* 
 * Copyright ©2009 Jean-François Lamy
 * 
 * Licensed under the Open Software Licence, Version 3.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.opensource.org/licenses/osl-3.0.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.concordiainternational.competition.ui;

import java.util.Locale;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.timer.CountdownTimerListener;
import org.concordiainternational.competition.ui.AnnouncerView.Mode;
import org.concordiainternational.competition.ui.components.TimerControls;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Display information regarding the current lifter
 * 
 * REFACTOR Refactorize this class, as it is used in 6 different settings (3
 * announcer views, the result editing page, the lifter display, the lifter card
 * editor).
 * 
 * @author jflamy
 * 
 */
public class LifterInfo extends VerticalLayout implements CountdownTimerListener {
    static final Logger logger = LoggerFactory.getLogger(LifterInfo.class);
    static final Logger timingLogger = LoggerFactory
            .getLogger("org.concordiainternational.competition.timer.TimingLogger"); //$NON-NLS-1$

    private static final long serialVersionUID = -3687013148334708795L;
    public Locale locale;

    public String identifier;
    private SessionData groupData;
    private Label timerDisplay;
    private CompetitionApplication app;
    private Mode mode;
    private CheckBox actAsTimekeeper = new CheckBox();
    private CheckBox automaticStartTime = new CheckBox(); //$NON-NLS-1$
    private Boolean showTimerControls = false;
    private Boolean startTimeAutomatically = false;
    private Component parentView;
    private Lifter lifter = null;

    private Lifter prevLifter = null;
    private Integer prevAttempt = null;
    private Integer prevWeight = null;

    private long lastOkButtonClick = 0L;
    private long lastFailedButtonClick = 0L;

    @SuppressWarnings("serial")
    public LifterInfo(String identifier, final SessionData groupData, AnnouncerView.Mode mode, Component parentView) {
        final CompetitionApplication currentApp = CompetitionApplication.getCurrent();
        this.app = currentApp;
        this.locale = currentApp.getLocale();
        this.identifier = identifier;
        this.groupData = groupData;
        this.parentView = parentView;
        this.mode = mode;

        this.addListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {
                // if the announcer clicks on the name, then send again to NEC
                // display
                // test for announcer view done in updateDisplayBoard.
                Component child = event.getChildComponent();
                if (child != null && child instanceof Label && "lifter".equals(((AbstractComponent) child).getData())) {
                    updateDisplayBoard(lifter, groupData);
                }
            }
        });
    }

    @Override
    public String toString() {
        return identifier + "_" + System.identityHashCode(this); //$NON-NLS-1$
    }

    /**
     * Display the information about the lifter Also displays the buttons to
     * manage timer.
     * 
     * @param lifter
     * @param groupData
     */
    public void loadLifter(final Lifter lifter, final SessionData groupData) {
        logger.debug("LifterInfo.loadLifter() begin: newLifter = {} previousLifter = {}", lifter, prevLifter); //$NON-NLS-1$

        synchronized (app) {
			// make sure that groupData listens to changes relating to the lifter
			// since the buttons trigger changes within the data (e.g.
			// successfulLift)
			if (parentView instanceof AnnouncerView) {
				if ((((AnnouncerView) parentView).mode == Mode.ANNOUNCER && identifier
						.startsWith("top"))) { //$NON-NLS-1$
					groupData.trackEditors(lifter, this.lifter, this);
				}
			}
			// don't work for nothing, avoid stutter on the screen.
			// we have to compare the attributes as last displayed.
			if (lifter != null) {
				if (lifter == prevLifter
						&& lifter.getAttemptsDone() == prevAttempt
						&& lifter.getNextAttemptRequestedWeight() == prevWeight) {
					return; // we are already showing correct information.
				}
				prevLifter = lifter;
				prevAttempt = lifter.getAttemptsDone();
				prevWeight = lifter.getNextAttemptRequestedWeight();
			}
			// prepare new display.
			this.removeAllComponents();
			if (lifter == null)
				return;
			updateDisplayBoard(lifter, groupData);
			StringBuilder sb = new StringBuilder();
			boolean done = lifter.getHTMLLifterInfo(
					identifier.startsWith("bottom"), sb); //$NON-NLS-1$
			final Label label = new Label(sb.toString(), Label.CONTENT_XHTML);
			label.addStyleName("zoomable");
			label.setData("lifter");
			this.addComponent(label);
			this.setSpacing(true);
			if (done)
				return; // lifter has already performed all lifts.
			if (lifter.isCurrentLifter() && identifier.startsWith("top")) { //$NON-NLS-1$
				topDisplayOptions(lifter, groupData);
			} else if (lifter.isCurrentLifter()
					&& identifier.startsWith("bottom")) { //$NON-NLS-1$
				bottomDisplayOptions(lifter, groupData);
			} else if (identifier.startsWith("display")) { //$NON-NLS-1$
				currentLifterDisplayOptions(lifter, groupData);
			}
		}
        app.push();

        this.lifter = lifter;
        logger.debug("LifterInfo.loadLifter() end: " + lifter.getLastName()); //$NON-NLS-1$
    }

    /**
     * @param lifter
     * @param groupData
     */
    private void updateDisplayBoard(final Lifter lifter, final SessionData groupData) {
        logger.trace("loadLifter prior to updateNEC {} {} {} ", new Object[] { identifier, parentView, mode }); //$NON-NLS-1$
        if (identifier.startsWith("top") && parentView == groupData.getAnnouncerView() && mode == Mode.ANNOUNCER) { //$NON-NLS-1$
            updateNECOnWeightChange(lifter, groupData);
        }
        logger.trace("loadLifter after updateNEC"); //$NON-NLS-1$
    }

    /**
     * Update the NEC Display.
     * 
     * Changed weight.
     * 
     * @param lifter
     * @param groupData
     */
    private void updateNECOnWeightChange(final Lifter lifter, final SessionData groupData) {
        // top part of announcer view drives electronic display
        if (groupData.needToUpdateNEC) {
            if (WebApplicationConfiguration.NECShowsLifterImmediately) {
                groupData.displayLifterInfo(lifter);
            } else {
                final Lifter currentLifter = groupData.getNECDisplay().getCurrentLifter();
                logger.trace("lifter = {}  currentLifter={}", lifter, currentLifter); //$NON-NLS-1$
                if (currentLifter != null && currentLifter.equals(lifter)) {
                    groupData.displayLifterInfo(lifter);
                } else {
                    groupData.displayWeight(lifter);
                }
            }
        }
    }

    private FormLayout timekeeperOptions() {
        actAsTimekeeper.setCaption(Messages.getString("LifterInfo.actAsTimeKeeper", locale)); //$NON-NLS-1$
        automaticStartTime.setCaption(Messages.getString("LifterInfo.automaticStartTime", locale)); //$NON-NLS-1$
        automaticStartTime.setValue(groupData.getStartTimeAutomatically());
        actAsTimekeeper.setValue(showTimerControls);
        FormLayout options = new FormLayout();
        actAsTimekeeper.setImmediate(true);
        automaticStartTime.setImmediate(true);
        automaticStartTime.setVisible(showTimerControls);
        actAsTimekeeper.addListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                showTimerControls = (Boolean) event.getProperty().getValue();
                if (showTimerControls) {
                    automaticStartTime.setValue(false);
                    automaticStartTime.setVisible(showTimerControls);
                    buttons.showTimerControls();
                } else {
                    automaticStartTime.setVisible(false);
                    buttons.hideTimerControls();
                    groupData.startTimeAutomatically(false);
                }
            }

        });
        automaticStartTime.addListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                startTimeAutomatically = (Boolean) event.getProperty().getValue();
                if (startTimeAutomatically) {
                    groupData.startTimeAutomatically(true);
                } else {
                    groupData.startTimeAutomatically(false);
                }
            }

        });
        options.setWidth("30ex"); //$NON-NLS-1$
        options.addComponent(actAsTimekeeper);
        options.addComponent(automaticStartTime);
        return options;
    }

    TimerControls buttons;
    private boolean blocked = true;

    /**
     * Display remaining time and timer controls
     * 
     * @param lifter
     * @param groupData
     */
    private void topDisplayOptions(final Lifter lifter, final SessionData groupData) {

        createTimerDisplay(groupData);

        buttons = new TimerControls(lifter, groupData, true, mode, this, showTimerControls, app);
        this.addComponent(new Label());
        this.addComponent(buttons);
        if (mode == Mode.ANNOUNCER) {
            FormLayout timekeeperOptions = timekeeperOptions();
            this.addComponent(timekeeperOptions);
        }
    }

    /**
     * Nothing to display in the lifter editor.
     * 
     * @param groupData
     */
    private void bottomDisplayOptions(Lifter lifter, final SessionData groupData) {
    }

    /**
     * Display remaining time
     * 
     * @param lifter2
     * @param groupData2
     */
    private void currentLifterDisplayOptions(Lifter lifter2, SessionData groupData2) {
        createTimerDisplay(groupData2);
    }

    /**
     * @param groupData
     */
    private void createTimerDisplay(final SessionData groupData) {
        timerDisplay = new Label();
        timerDisplay.addStyleName("zoomable");
        timerDisplay.addStyleName("timerDisplay");

        // we set the value to the time allowed for the current lifter as
        // computed by groupData
        int timeAllowed = groupData.getTimeAllowed();
        final CountdownTimer timer = groupData.getTimer();
        final boolean running = timer.isRunning();
        logger.debug("timeAllowed={} timer.isRunning()={}", timeAllowed, running); //$NON-NLS-1$
        if (!running) {
            timerDisplay.setValue(TimeFormatter.formatAsSeconds(timeAllowed));
            timerDisplay.setEnabled(false); // greyed out.
        }

        CompetitionApplication masterApplication = groupData.getMasterApplication();
//        logger.warn("masterApplication = {}, app={}", masterApplication, app);
        if (masterApplication == app) {
            timer.setMasterBuzzer(this);
        } else {
            timer.addListener(this);
        }
        this.addComponent(timerDisplay);
    }

    int prevTimeRemaining = 0;

    @Override
    public void normalTick(int timeRemaining) {
        if (timerDisplay == null) {
            setBlocked(false);
            return;
        } else if (TimeFormatter.getSeconds(prevTimeRemaining) == TimeFormatter.getSeconds(timeRemaining)) {
            prevTimeRemaining = timeRemaining;
            setBlocked(false);
            return;
        } else {
            prevTimeRemaining = timeRemaining;
        }

        synchronized (app) {
            if (!isBlocked()) {
                timerDisplay.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
                timerDisplay.setEnabled(true);
            }
            setBlocked(false);
        }
        app.push();
    }

    @Override
    public void finalWarning(int remaining) {
        SessionData masterData = app.getMasterData(app.getPlatformName());
        logger.warn("final warning, {}", isMasterConsole(masterData));
        if (timerDisplay == null) return;
        prevTimeRemaining = remaining;

        synchronized (app) {
            if (!isBlocked()) {
                timerDisplay.setValue(TimeFormatter.formatAsSeconds(remaining));
                final ClassResource resource = new ClassResource("/sounds/finalWarning.mp3", app); //$NON-NLS-1$
                playSound(resource);
            }
            setBlocked(false);
        }
        app.push();
    }

    @Override
    public void initialWarning(int remaining) {
        if (timerDisplay == null) return;
        prevTimeRemaining = remaining;

        synchronized (app) {
            if (!isBlocked()) {
                timerDisplay.setValue(TimeFormatter.formatAsSeconds(remaining));
                final ClassResource resource = new ClassResource("/sounds/initialWarning.mp3", app); //$NON-NLS-1$
                playSound(resource);
            }
            setBlocked(false);
        }
        app.push();
    }

    /**
     * @param resource
     */
    private void playSound(final ClassResource resource) {
        SessionData masterData = app.getMasterData(app.getPlatformName());
        if (isMasterConsole(masterData)) {
            // we are not the master application; do not play
            app.getBuzzer().play(resource);
            logger.debug("! {} is master, playing sound", app); //$NON-NLS-1$
        } else {
            logger.debug("- {} not master, not playing sound", app); //$NON-NLS-1$
        }

    }

    /**
     * @param masterData
     * @return
     */
    private boolean isMasterConsole(SessionData masterData) {
        return app == masterData.getMasterApplication();
    }

    @Override
    public void noTimeLeft(int remaining) {
        if (timerDisplay == null) return;
        prevTimeRemaining = remaining;

        synchronized (app) {
            if (!isBlocked()) {
                timerDisplay.setValue(TimeFormatter.formatAsSeconds(remaining));
                final ClassResource resource = new ClassResource("/sounds/timeOver.mp3", app); //$NON-NLS-1$
                playSound(resource);
                if (timerDisplay != null) {
                    timerDisplay.setEnabled(false);
                }
            }
            setBlocked(false);
        }
        app.push();
    }

    @Override
    public void forceTimeRemaining(int remaining) {
        if (timerDisplay == null) return;
        prevTimeRemaining = remaining;

        synchronized (app) {
        	timerDisplay.setEnabled(false); // show that timer has stopped.
            timerDisplay.setValue(TimeFormatter.formatAsSeconds(remaining));
            setBlocked(false);
        }
        app.push();

    }

    @Override
    public void pause(int timeRemaining) {
    	
        setBlocked(true); // don't process the next update from the timer.
        synchronized (app) {
	        if (buttons != null) buttons.stopStart.setEnabled(true);
	        if (timerDisplay != null) {
	            timerDisplay.setEnabled(false);
	        }
        }
        app.push();
    }

    @Override
    public void start(int timeRemaining) {
        setBlocked(false);
        synchronized (app) {
	        if (buttons != null) buttons.stopStart.setEnabled(true);
	        if (timerDisplay != null) {
	            timerDisplay.setEnabled(true);
	        }
        }
        app.push();
    }


    @Override
    public void stop(int timeRemaining) {

        setBlocked(true); // don't process the next update from the timer.
        synchronized (app) {
	        if (buttons != null) buttons.stopStart.setEnabled(true);
	        if (timerDisplay != null) {
	            timerDisplay.setEnabled(false);
	        }
        }
        app.push();
    }

    /**
     * Prevent the timer listeners from updating the timer and emitting sounds;
     * Called by the timer controls as soon as the user clicks. This is because
     * of race conditions: - there is a delay in propagating the events - the
     * timer update can occur after the updateList() routine has updated the
     * remaining time - the bell could ring just after the timekeeper has
     * clicked to stop.
     * 
     * @param blocked
     *            if true, stop paying attention to timer
     */
    public void setBlocked(boolean blocked) {
        if (blocked != this.blocked) {
            this.blocked = blocked;
            // logger.warn("app {} blocked={}",app,blocked);
        }
    }

    /**
     * @return the blocked
     */
    private boolean isBlocked() {
        return blocked;
    }

    /**
     * @param lastOkButtonClick
     *            the lastOkButtonClick to set
     */
    public void setLastOkButtonClick(long lastOkButtonClick) {
        this.lastOkButtonClick = lastOkButtonClick;
    }

    /**
     * @return the lastOkButtonClick
     */
    public long getLastOkButtonClick() {
        return lastOkButtonClick;
    }

    /**
     * @param lastFailedButtonClick
     *            the lastFailedButtonClick to set
     */
    public void setLastFailedButtonClick(long lastFailedButtonClick) {
        this.lastFailedButtonClick = lastFailedButtonClick;
    }

    /**
     * @return the lastFailedButtonClick
     */
    public long getLastFailedButtonClick() {
        return lastFailedButtonClick;
    }

}
