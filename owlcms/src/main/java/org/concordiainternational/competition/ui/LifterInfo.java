/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.decision.Sound;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.timer.CountdownTimerListener;
import org.concordiainternational.competition.ui.AnnouncerView.Mode;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.components.TimerControls;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.concordiainternational.competition.ui.generators.TryFormatter;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.notifique.Notifique;
import org.vaadin.notifique.Notifique.Message;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.Notification;

/**
 * Display information regarding the current lifter
 * 
 * REFACTOR This class badly needs refactoring, as it is used in 6 different settings (3
 * announcer views, the result editing page, the lifter display, the lifter card
 * editor).
 * 
 * @author jflamy
 * 
 */
public class LifterInfo extends VerticalLayout implements 
	CountdownTimerListener,
	DecisionEventListener,
	ApplicationView,
	Notifyable
	{
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
	private SessionData sessionData;

	protected DecisionEvent prevEvent;
	
    @SuppressWarnings("serial")
    public LifterInfo(String identifier, final SessionData groupData, AnnouncerView.Mode mode, Component parentView) {
        final CompetitionApplication currentApp = CompetitionApplication.getCurrent();
        this.app = currentApp;
        this.locale = currentApp.getLocale();
        this.identifier = identifier;
        this.groupData = groupData;
        this.parentView = parentView;
        this.mode = mode;
        this.sessionData = groupData;

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
        
		// URI handler must remain, so is not part of the register/unRegister pair
		app.getMainWindow().addURIHandler(this);
        registerAsListener();
    }

    @Override
    public String toString() {
        return identifier + "_" + System.identityHashCode(this); //$NON-NLS-1$
    }

    /**
     * Display the information about the lifter Also displays the buttons to
     * manage timer.
     * 
     * @param lifter1
     * @param groupData1
     */
    public void loadLifter(final Lifter lifter1, final SessionData groupData1) {
        logger.debug("LifterInfo.loadLifter() begin: newLifter = {} previousLifter = {}", lifter1, prevLifter); //$NON-NLS-1$

        synchronized (app) {
			// make sure that groupData listens to changes relating to the lifter
			// since the buttons trigger changes within the data (e.g.
			// successfulLift)
			if (parentView instanceof AnnouncerView) {
				if ((((AnnouncerView) parentView).mode == Mode.ANNOUNCER && isTop())) { //$NON-NLS-1$
					groupData1.trackEditors(lifter1, this.lifter, this);
				}
			}
			// don't work for nothing, avoid stutter on the screen.
			// we have to compare the attributes as last displayed.
			if (lifter1 != null) {
				if (lifter1 == prevLifter
						&& lifter1.getAttemptsDone() == prevAttempt
						&& lifter1.getNextAttemptRequestedWeight() == prevWeight) {
					return; // we are already showing correct information.
				}
				if (isTop()) {
				    lifter1.check15_20kiloRule(false,(Notifyable)parentView);
				}
				prevLifter = lifter1;
				prevAttempt = lifter1.getAttemptsDone();
				prevWeight = lifter1.getNextAttemptRequestedWeight();
			}
			// prepare new display.
			this.removeAllComponents();
			if (lifter1 == null)
				return;
			updateDisplayBoard(lifter1, groupData1);
			StringBuilder sb = new StringBuilder();
			boolean done = getHTMLLifterInfo(lifter1,
					isBottom(), sb); //$NON-NLS-1$
			final Label label = new Label(sb.toString(), Label.CONTENT_XHTML);
			label.addStyleName("zoomable");
			label.setData("lifter");
			this.addComponent(label);
			this.setSpacing(true);
			if (isBottom()) { 
				bottomDisplayOptions(lifter1, groupData1);
			}
			if (done)
				return; // lifter has already performed all lifts.
			if (lifter1.isCurrentLifter() && isTop()) { //$NON-NLS-1$
				topDisplayOptions(lifter1, groupData1);
			} else if (isDisplay()) { //$NON-NLS-1$
				currentLifterDisplayOptions(lifter1, groupData1);
			}
		}
        app.push();

        this.lifter = lifter1;
        logger.debug("LifterInfo.loadLifter() end: " + lifter1.getLastName()); //$NON-NLS-1$
    }


    /**
     * @param sb
     * @return
     */
    public boolean getHTMLLifterInfo(Lifter lifter1, boolean alwaysShowName, StringBuilder sb) {
        final int currentTry = 1 + (lifter1.getAttemptsDone() >= 3 ? lifter1.getCleanJerkAttemptsDone() : lifter1.getSnatchAttemptsDone());
        boolean done = currentTry > 3;

        // display requested weight
        if (done) {
            appendDiv(sb, "break", "&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$
            done = true;
        } else {
            appendDiv(sb, "weight", lifter1.getNextAttemptRequestedWeight() + Messages.getString("Common.kg", locale)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        appendDiv(sb, "break", "&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$

        // display lifter name and affiliation
        if (!done || alwaysShowName) {
            appendDiv(sb, lifter1.getLastName().toUpperCase());
            appendDiv(sb, lifter1.getFirstName());
            appendDiv(sb, lifter1.getClub());
            appendDiv(sb, "break", "&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // display current attempt number
        if (done) {
            CompetitionSession currentSession = sessionData.getCurrentSession();
			if (currentSession != null) {
				// we're done, write a "finished" message and return.
				appendDiv(sb, MessageFormat.format(Messages.getString("LifterInfo.Done", locale),currentSession.getName() )); //$NON-NLS-1$
			} else {
				// do nothing.
			}
        } else {
            //appendDiv(sb, lifter.getNextAttemptRequestedWeight()+Messages.getString("Common.kg",locale)); //$NON-NLS-1$
            String tryInfo = TryFormatter.formatTry(lifter1, locale, currentTry);
            appendDiv(sb, tryInfo);
        }
        return done;
    }
    
    /**
     * @param lifter1
     * @param groupData1
     */
    private void updateDisplayBoard(final Lifter lifter1, final SessionData groupData1) {
        logger.trace("loadLifter prior to updateNEC {} {} {} ", new Object[] { identifier, parentView, mode }); //$NON-NLS-1$
        if (isTop() && parentView == groupData1.getAnnouncerView() && mode == Mode.ANNOUNCER) { //$NON-NLS-1$
            updateNECOnWeightChange(lifter1, groupData1);
        }
        logger.trace("loadLifter after updateNEC"); //$NON-NLS-1$
    }

    /**
     * Update the NEC Display.
     * 
     * Changed weight.
     * 
     * @param lifter1
     * @param groupData1
     */
    private void updateNECOnWeightChange(final Lifter lifter1, final SessionData groupData1) {
        // top part of announcer view drives electronic display
        if (groupData1.needToUpdateNEC) {
            if (WebApplicationConfiguration.NECShowsLifterImmediately) {
                groupData1.displayLifterInfo(lifter1);
            } else {
                final Lifter currentLifter = groupData1.getNECDisplay().getCurrentLifter();
                logger.trace("lifter = {}  currentLifter={}", lifter1, currentLifter); //$NON-NLS-1$
                if (currentLifter != null && currentLifter.equals(lifter1)) {
                    groupData1.displayLifterInfo(lifter1);
                } else {
                    groupData1.displayWeight(lifter1);
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
                    timerControls.showTimerControls(groupData.getTimer().isRunning());
                } else {
                    automaticStartTime.setVisible(false);
                    timerControls.hideTimerControls();
                    groupData.setStartTimeAutomatically(false);
                }
            }

        });
        automaticStartTime.addListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                startTimeAutomatically = (Boolean) event.getProperty().getValue();
                if (startTimeAutomatically) {
                    groupData.setStartTimeAutomatically(true);
                } else {
                    groupData.setStartTimeAutomatically(false);
                }
            }
        });

        options.setWidth("30ex"); //$NON-NLS-1$
        options.addComponent(actAsTimekeeper);
        options.addComponent(automaticStartTime);
        return options;
    }


    TimerControls timerControls;
    private boolean blocked = true;

    /**
     * Display remaining time and timer controls
     * 
     * @param lifter1
     * @param groupData1
     */
    private void topDisplayOptions(final Lifter lifter1, final SessionData groupData1) {

        createTimerDisplay(groupData1);
        if (timerControls != null) timerControls.unregisterListeners();
        timerControls = new TimerControls(lifter1, groupData1, true, mode, this, showTimerControls, app);
        this.addComponent(new Label());
        this.addComponent(timerControls);
        if (mode == Mode.ANNOUNCER) {
            FormLayout timekeeperOptions = timekeeperOptions();
            this.addComponent(timekeeperOptions);
        }
    }

    /**
     * Additional things to display in the lifter editor.
     * 
     * @param groupData1
     */
    @SuppressWarnings("serial")
	private void bottomDisplayOptions(final Lifter lifter1, final SessionData groupData1) {
    	if (parentView instanceof ResultView) {
	    		FormLayout customScoreFL = new FormLayout();
	    		TextField field = new TextField(Messages.getString("LifterInfo.customScore",CompetitionApplication.getCurrentLocale()));
	    		field.setWidth("5em");
	    		customScoreFL.addComponent(field);
	    		field.setPropertyDataSource(new MethodProperty<Lifter>(lifter1, "customScore"));
	    		field.setImmediate(true);
	    		field.addListener(new ValueChangeListener() {
					
					@Override
					public void valueChange(ValueChangeEvent event) {
			    		((ResultView)parentView).getResultList().getGroupData().persistPojo(lifter1);
					}
				});
	    		this.addComponent(customScoreFL);
    	}
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
     * @param groupData1
     */
    private void createTimerDisplay(final SessionData groupData1) {
        timerDisplay = new Label();
        timerDisplay.addStyleName("zoomable");
        timerDisplay.addStyleName("timerDisplay");

        // we set the value to the time allowed for the current lifter as
        // computed by groupData
        int timeAllowed = groupData1.getDisplayTime();  // was getTimeAllowed();
        final CountdownTimer timer = groupData1.getTimer();
        final boolean running = timer.isRunning();
        logger.debug("timeAllowed={} timer.isRunning()={}", timeAllowed, running); //$NON-NLS-1$
        if (!running) {
            timerDisplay.setValue(TimeFormatter.formatAsSeconds(timeAllowed));
            timerDisplay.setEnabled(false); // greyed out.
        }
        this.addComponent(timerDisplay);
    }


    int prevTimeRemaining = 0;
	protected boolean shown;

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
        logger.trace("final warning, {}", isMasterConsole(masterData));
        if (timerDisplay == null) return;
        prevTimeRemaining = remaining;

        synchronized (app) {
            if (!isBlocked()) {
                timerDisplay.setValue(TimeFormatter.formatAsSeconds(remaining));
//                final ClassResource resource = new ClassResource("/sounds/finalWarning.mp3", app); //$NON-NLS-1$
//                playSound(resource);
                playSound("/sounds/finalWarning2.wav");
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
//                final ClassResource resource = new ClassResource("/sounds/initialWarning.mp3", app); //$NON-NLS-1$
//                playSound(resource);
                playSound("/sounds/initialWarning2.wav");
            }
            setBlocked(false);
        }
        app.push();
    }

//    /**
//     * @param resource
//     */
//    @SuppressWarnings("unused")
//	private void playSound(final ClassResource resource) {
//        SessionData masterData = app.getMasterData(app.getPlatformName());
//        if (isMasterConsole(masterData)) {
//            // we are not the master application; do not play
//            app.getBuzzer().play(resource);
//            logger.debug("! {} is master, playing sound", app); //$NON-NLS-1$
//        } else {
//            logger.debug("- {} not master, not playing sound", app); //$NON-NLS-1$
//        }
//
//    }
    
    private void playSound(String soundName) {
        SessionData masterData = app.getMasterData(app.getPlatformName());
        if (isMasterConsole(masterData)) {
            new Sound(masterData.getPlatform().getMixer(),soundName).emit();
            logger.debug("! {} is master, playing sound", app); //$NON-NLS-1$
        } else {
            // we are not the master application; do not play
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
//                final ClassResource resource = new ClassResource("/sounds/timeOver.mp3", app); //$NON-NLS-1$
//                playSound(resource);
                playSound("/sounds/timeOver2.wav");
                if (timerDisplay != null) {
                    timerDisplay.setEnabled(false);
                }
            }
            setBlocked(false);
        }
        app.push();
    }

    @Override
    public void forceTimeRemaining(int remaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
        if (timerDisplay == null) return;
        prevTimeRemaining = remaining;

        synchronized (app) {
        	timerDisplay.setEnabled(false); // show that timer has stopped.
            timerDisplay.setValue(TimeFormatter.formatAsSeconds(remaining));
            timerControls.enableStopStart(false);
            setBlocked(false);
        }
        showNotification(originatingApp, reason);
        app.push();

    }

    @Override
    public void pause(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
    	
        setBlocked(true); // don't process the next update from the timer.
        synchronized (app) {
	        if (timerControls != null) {
	        	timerControls.enableStopStart(false);
	        }
	        if (timerDisplay != null) {
	            timerDisplay.setEnabled(false);
	        }
        }
        showNotification(originatingApp, reason);
        app.push();
    }

	/**
	 * Show a notification on other consoles.
	 * Notification is shown for actors other than the time keeper (when the announcer or marshall stops the time
	 * through a weight change).
	 * 
	 * @param originatingApp
	 * @param reason
	 */
	private void showNotification(CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {

		if (isTop() && app != originatingApp) {
			CompetitionApplication receivingApp = app;
			
			// defensive
			String originatingPlatformName = originatingApp.components.getPlatformName();
			String receivingPlatformName = receivingApp.components.getPlatformName();
			if (! originatingPlatformName.equals(receivingPlatformName)) {
				logger.error("event from platform {} sent to {}",originatingPlatformName, receivingPlatformName);
				traceBack(originatingApp);
			}
        	
			if (receivingApp.components.currentView instanceof AnnouncerView && reason != TimeStoppedNotificationReason.UNKNOWN) {
				AnnouncerView receivingView = (AnnouncerView) receivingApp.components.currentView;
				ApplicationView originatingAppView = originatingApp.components.currentView;
				if (originatingAppView instanceof AnnouncerView) {
					AnnouncerView originatingView = (AnnouncerView)originatingAppView;
					if (originatingView.mode != receivingView.mode 
							&& originatingView.mode != Mode.TIMEKEEPER) {
						traceBack(originatingApp);
						receivingView.displayNotification(originatingView.mode,reason);
					}
				} else {
					traceBack(originatingApp);
					receivingView.displayNotification(null,reason);
				}
        	}
        }
	}

	/**
	 * @param originatingApp
	 */
	private void traceBack(CompetitionApplication originatingApp) {
//		logger.trace("showNotification in {} from {}",app,originatingApp);
//		LoggerUtils.logException(logger, new Exception("where"));
	}

    @Override
    public void start(int timeRemaining) {
        setBlocked(false);
        synchronized (app) {
	        if (timerControls != null) timerControls.enableStopStart(true);
	        if (timerDisplay != null) {
	            timerDisplay.setEnabled(true);
	        }
        }
        app.push();
    }


    @Override
    public void stop(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {

        setBlocked(true); // don't process the next update from the timer.
        synchronized (app) {
	        if (timerControls != null) timerControls.enableStopStart(false);
	        if (timerDisplay != null) {
	            timerDisplay.setEnabled(false);
	        }
        }
        showNotification(originatingApp, reason);
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
            // logger.trace("app {} blocked={}",app,blocked);
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

    private void appendDiv(StringBuilder sb, String string) {
        sb.append("<div>") //$NON-NLS-1$
                .append(string).append("</div>"); //$NON-NLS-1$
    }

    private void appendDiv(StringBuilder sb, String cssClass, String string) {
        sb.append("<div class='" + cssClass + "'>") //$NON-NLS-1$ //$NON-NLS-2$
                .append(string).append("</div>"); //$NON-NLS-1$
    }

	/**
	 * Display referee decisions
	 * @see org.concordiainternational.competition.decision.DecisionEventListener#updateEvent(org.concordiainternational.competition.decision.DecisionEvent)
	 */
	@Override
	public void updateEvent(final DecisionEvent updateEvent) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// show a notification
				// only the master console is registered for these events.
				logger.trace("received event {}",updateEvent);
				switch (updateEvent.getType()) {
				case SHOW:
					 shown = true;
					 displayNotification(updateEvent);
					 if (timerControls != null) { timerControls.hideLiftControls(); }
					 break;
					 // go on to UPDATE;
				case UPDATE:
					if (shown) {
						displayNotification(updateEvent);
					}
					break;
				case RESET:
					shown = false;
					prevEvent = null;
					if (timerControls != null) { timerControls.showLiftControls(); }
					break;
				}				
			}

			/**
			 * @param newEvent
			 */
			protected void displayNotification(final DecisionEvent newEvent) {
				synchronized (app) {
					final ApplicationView currentView = app.components.currentView;
					if (currentView instanceof AnnouncerView) {
//						if (stutteringEvent(newEvent,prevEvent)) {
//							prevEvent = newEvent;
//							logger.trace("A prevented notification for {}",newEvent);
//							logger.trace("A prevEvent={}",prevEvent);
//							return;
//						}
//						prevEvent = newEvent;
//						logger.trace("B prevEvent={}",prevEvent);
						
						final AnnouncerView announcerView = (AnnouncerView)currentView;
						Notifique notifications = announcerView.getNotifications();
						String style;
						String message;
						final Boolean accepted = newEvent.isAccepted();
						logger.trace("B YES notification for {} accepted={}",newEvent,accepted);
						if (accepted != null) {
							final Lifter lifter2 = newEvent.getLifter();
							final String name = (lifter2 != null ?lifter2.getLastName().toUpperCase()+" "+lifter2.getFirstName() : " «?» ");
							Integer attemptedWeight = newEvent.getAttemptedWeight();
							attemptedWeight = (attemptedWeight != null ? attemptedWeight : 0 );
                            if (accepted) {
								style = "owlcms-white";
								message = MessageFormat.format(Messages.getString("Decision.lift", locale),name,attemptedWeight);
							} else {
								style = "owlcms-red";
								message = MessageFormat.format(Messages.getString("Decision.noLift", locale),name,attemptedWeight);
							}
							final Message addedMessage = notifications.add((Resource)null,message,true,style,true);
							announcerView.scheduleMessageRemoval(addedMessage, 10000);
						}
					}
				}
				app.push();
			}

			/**
			 * @param curEvent
			 * @param prevEvent1
			 * @return true if the two events concern the same lifter and the same attempt and give the same decision
			 */
			@SuppressWarnings("unused")
            private boolean stutteringEvent(DecisionEvent curEvent,
					DecisionEvent prevEvent1) {
				logger.trace("curEvent={} prevEvent={}",curEvent,prevEvent1);
				if (curEvent != null && prevEvent1 != null) {
					Lifter cur = updateEvent.getLifter();
					Lifter prev = prevEvent1.getLifter();
					if (cur != null && prev != null) {
						if (cur != prev) {
							return false;	
						}
						logger.trace("same lifter");
						Integer curAtt = cur.getAttemptsDone();
						Integer prevAtt = cur.getAttemptsDone();
						if (curAtt != null && prevAtt != null) {
							if (!curAtt.equals(prevAtt)){
								return false;
							}
							logger.trace("same attempt");
							Boolean prevDecision = prevEvent1.isAccepted();
							Boolean curDecision = curEvent.isAccepted();
							if (prevDecision != null && curDecision != null) {
								logger.trace("prevDecision={} curDecision={}",prevDecision,curDecision);
								return prevDecision.equals(curDecision);
							} else {
								final boolean b = prevDecision == null && curDecision == null;
								logger.trace("either decision is null prevDecision={} curDecision={}",prevDecision,curDecision);
								return b;
							}
						} else {
							final boolean b = curAtt == null && prevAtt == null;
							logger.trace("either attempt is null prevAtt={} curAtt={}",prevAtt,curAtt);
							return b;
						}
					} else {
						final boolean b = cur == null && prev == null;
						logger.trace("either lifter is null prev={} cur={}",prev,cur);
						return b;
					}
				}  else {
					final boolean b = curEvent == null && prevEvent1 == null;
					logger.trace("either event is null prevEvent1={} curEvent={}",prevEvent1,curEvent);
					return b;
				}
			}
				
			
		}).start();
	}

	/**
	 * @return
	 */
	private boolean isTop() {
		return identifier.startsWith("top");
	}
	
	/**
	 * @return
	 */
	private boolean isBottom() {
		return identifier.startsWith("bottom");
	}

	/**
	 * @return
	 */
	private boolean isDisplay() {
		return identifier.startsWith("display");
	}

	
	/**
	 * Register as listener to various events.
	 */
	@Override
	public void registerAsListener() {
		// window close
        final Window mainWindow = app.getMainWindow();
		logger.debug("window: {} register for {} {}",
				new Object[]{mainWindow,identifier,this});
		mainWindow.addListener(this);
        
		final CompetitionApplication masterApplication = groupData.getMasterApplication();
		CountdownTimer timer = groupData.getTimer();
		
		// if several instances of lifter information are shown on page, only top one buzzes.
		if (masterApplication == app && isTop()) {
			// down signal (for buzzer)
			groupData.getRefereeDecisionController().addListener(this);
			// timer will buzz on this console
			// TODO: now obsolete? - javax.sound is used from server
			if (timer != null) timer.setMasterBuzzer(this);
        }
		// timer countdown events; bottom information does not show timer.
		// if already master buzzer, do not register twice.
        if (timer != null && ! isBottom() && !(timer.getMasterBuzzer() == this)) {
        	timer.addListener(this);
        } 

	}

	@Override
	public void unregisterAsListener() {
		// window close
		final Window mainWindow = app.getMainWindow();
		logger.debug("window: {} UNregister for {} {}",
				new Object[]{mainWindow,identifier,this});
		mainWindow.removeListener(this);
		
		// cleanup referee decision listening.
		CountdownTimer timer = groupData.getTimer();	
		groupData.getRefereeDecisionController().removeListener(this);
		if (timer != null && timer.getMasterBuzzer() == this) timer.setMasterBuzzer(null);
		// timer countdown events
		if (timer != null) timer.removeListener(this);
	}


	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();
	}

	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		logger.trace("registering listeners");
		// called on refresh
		registerAsListener();
		return null;
	}

	@Override
	public void refresh() {
	}

	@Override
	public boolean needsMenu() {
		return false;
	}

	@Override
	public void setParametersFromFragment() {
	}

	@Override
	public String getFragment() {
		return null;
	}

    @Override
    public void showNotificationForLifter(Lifter lifter1, Notification notification, boolean unlessCurrent) {
        logger.warn("lifter {} unlessCurrent{}",lifter1,unlessCurrent);
        if (!unlessCurrent) {
            // always show notification
            app.getMainWindow().showNotification(notification);
        } else if (lifter1 != groupData.getCurrentLifter()) {
            // not the current lifter, show the notification
            app.getMainWindow().showNotification(notification);
        }
    }


}
