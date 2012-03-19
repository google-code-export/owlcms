/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.components;

import java.util.Locale;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.ui.AnnouncerView;
import org.concordiainternational.competition.ui.AnnouncerView.Mode;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.LifterInfo;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.TimeStoppedNotificationReason;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ActionManager;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;

public class TimerControls extends GridLayout implements ShortcutAction.Notifier {

    private static final String ANNOUNCER_BUTTON_WIDTH = "9em";
	private static final String ANNOUNCER_SMALL_BUTTON_WIDTH = "4em";
	private static final long serialVersionUID = 4075226732120553473L;
    static final Logger logger = LoggerFactory.getLogger(TimerControls.class);
    static final Logger timingLogger = LoggerFactory
            .getLogger("org.concordiainternational.competition.timer.TimingLogger"); //$NON-NLS-1$

    /**
     * a click that take place less than MIN_CLICK_DELAY milliseconds after an
     * initial click on the Ok or Failed button is ignored. This usually means
     * that the user double clicked by mistake, or pressed a second time becaus
     * he was unsure that recalculation had taken place and we definitely don't
     * want to register two successful or failed lifts.
     */
    protected static final long MIN_CLICK_DELAY = 1000;


    final public Button announce = new Button();
    final public Button changeWeight = new Button();
    //final public Button stopStart = new Button();
    final public Button start = new Button();
    final public Button stop = new Button();
    final public Button oneMinute = new Button();
    final public Button twoMinutes = new Button();
    final public Button okLift = new Button();
    final public Button failedLift = new Button();
    final public Button stopTimeBottom = new Button();
    private Mode mode;
    private LifterInfo lifterInfo;
    private boolean timerVisible = false;
    
	private boolean timerShortcutsEnabled = false;
	private ShortcutAction actionStart;
	private ShortcutAction actionStop;
	private ShortcutAction actionOneMinute;
	private ShortcutAction actionTwoMinutes;
	private ActionManager actionManager;
	
    public TimerControls(final Lifter lifter, final SessionData groupData, boolean top, AnnouncerView.Mode mode,
            LifterInfo lifterInfo, boolean timerVisible, CompetitionApplication app) {
        super(4, 3);
        this.mode = mode;
        this.lifterInfo = lifterInfo;
        this.timerVisible = timerVisible;
        this.setMargin(false);
        this.setSpacing(false);

        Locale locale = app.getLocale();

        if (top) {
            top(lifter, groupData, locale);
        } else {
            bottom(lifter, groupData, locale);
        }

        this.setSpacing(true);
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     * @throws OverlapsException
     * @throws OutOfBoundsException
     */
    private void top(final Lifter lifter, final SessionData groupData, Locale locale) throws OverlapsException,
            OutOfBoundsException {
        if (mode == AnnouncerView.Mode.ANNOUNCER) {
            configureAnnounceButton(lifter, groupData, locale);
            configureWeightChangeButton(lifter, groupData, locale);
            //configureStopStart(lifter, groupData, locale);
            configureStart(lifter, groupData, locale);
            configureStop(lifter, groupData, locale);
            configureOneMinute(lifter, groupData, locale);
            configureTwoMinutes(lifter, groupData, locale);
            configureOkLift(lifter, groupData, locale);
            configureFailedLift(lifter, groupData, locale);
            
            setActions(lifter,groupData);
//            CompetitionApplication.getCurrent().getMainWindow().addActionHandler(this);

            this.addComponent(announce, 0, 0, 1, 0);
            this.addComponent(changeWeight, 2, 0, 3, 0);
            //this.addComponent(stopStart, 0, 1, 1, 1);
            this.addComponent(start, 0, 1, 0, 1);
            this.addComponent(stop, 1, 1, 1, 1);
            
            this.addComponent(oneMinute, 2, 1, 2, 1);
            this.addComponent(twoMinutes, 3, 1, 3, 1);
            this.addComponent(okLift, 0, 2, 1, 2);
            this.addComponent(failedLift, 2, 2, 3, 2);

            okLift.addStyleName("okLift"); //$NON-NLS-1$
            failedLift.addStyleName("failedLift"); //$NON-NLS-1$

            final boolean announced = !groupData.getNeedToAnnounce();
            if (!WebApplicationConfiguration.NECShowsLifterImmediately) {
                announce.setEnabled(true); // allow announcer to call lifter at
                                           // will
                changeWeight.setEnabled(true); // always allow changes
                enableStopStart(true);
                //stopStart.setEnabled(announced);
            } else {
                announce.setEnabled(!announced && groupData.getAnnouncerEnabled()); // if lifter has been
                                                 // announced, disable this
                                                 // button
                changeWeight.setEnabled(true); // always allow changes
                enableStopStart(groupData.getTimer().isRunning());
                //stopStart.setEnabled(announced);
            }
            // okLift.setEnabled(false);
            // failedLift.setEnabled(false);

            if (timerVisible) {
                showTimerControls(groupData.getTimer().isRunning());
            } else {
                hideTimerControls();
            }
        } else if (mode == AnnouncerView.Mode.MARSHALL) {
            configureWeightChangeButton(lifter, groupData, locale);
            this.addComponent(changeWeight, 0, 0, 1, 0);
            // changeWeight.setEnabled(false);
        } else if (mode == AnnouncerView.Mode.TIMEKEEPER) {
            //configureStopStart(lifter, groupData, locale);
        	configureStart(lifter, groupData, locale);
        	configureStop(lifter, groupData, locale);
            configureOneMinute(lifter, groupData, locale);
            configureTwoMinutes(lifter, groupData, locale);
            this.addComponent(start, 0, 1, 0, 1);
            this.addComponent(stop, 1, 1, 1, 1);     
            this.addComponent(oneMinute, 2, 1, 2, 1);
            this.addComponent(twoMinutes, 3, 1, 3, 1);
            enableStopStart(groupData.getTimer().isRunning());
        }
    }



	public void enableStopStart(boolean running) {
    	final boolean isTimeKeeper = mode == Mode.TIMEKEEPER;
    	if (!running) {
    		stop.setEnabled(false);
    		stop.removeStyleName("primary");
    		
			if (isTimeKeeper) {
				stop.removeClickShortcut();
			}
    		
    		start.setEnabled(true);    		
    		if (isTimeKeeper) {
    			start.setClickShortcut(KeyCode.ENTER);
    		} else {
    			start.focus();
    		}
    		start.addStyleName("primary");
    	} else {
    		start.setEnabled(false);
    		start.removeStyleName("primary");
    		if (isTimeKeeper) {
    			start.removeClickShortcut();
    		}
    		
    		stop.setEnabled(true);    		
    		if (isTimeKeeper) {
    			stop.setClickShortcut(KeyCode.ENTER);
    		} else {
    			stop.focus();
    		}
    		stop.addStyleName("primary");
    	} 
	}

	/**
     * @param groupData
     * @param locale
     */
    private void bottom(Lifter lifter, final SessionData groupData, Locale locale) {

        addStopTimeBottomListener(lifter, groupData, locale);

        // if the timer is running, we allow scorekeeper to stop it.
        // otherwise, button will get enabled by the timer when it is started
        final boolean enabled = groupData.getTimer().isRunning();
        logger.debug("timer is running = {}", enabled); //$NON-NLS-1$
        // stopTimeBottom.setEnabled(enabled);
        this.addComponent(stopTimeBottom);
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void configureFailedLift(final Lifter lifter, final SessionData groupData, Locale locale) {

        final Button.ClickListener failedLiftListener = new Button.ClickListener() {
            private static final long serialVersionUID = 5693610077500773431L;

            @Override
            public void buttonClick(ClickEvent event) {
                final long currentTimeMillis = System.currentTimeMillis();
                // ignore two clicks on the same button in quick succession
                if (currentTimeMillis - lifterInfo.getLastFailedButtonClick() > MIN_CLICK_DELAY) {
                    logger.debug("Failed: délai acceptable: {}", currentTimeMillis
                        - lifterInfo.getLastFailedButtonClick());
                    lifterInfo.setLastFailedButtonClick(currentTimeMillis);
                    timingLogger.debug("failedLift"); //$NON-NLS-1$
                    // call group data first because this resets the timers
                    logger.info("failed lift for {} {}", lifter.getLastName(), lifter.getFirstName()); //$NON-NLS-1$
                    lifterInfo.setBlocked(true);
                    groupData.liftDone(lifter, false);
                    lifter.failedLift();
                } else {
                    logger.debug("Failed: délai INacceptable: {}", currentTimeMillis
                        - lifterInfo.getLastFailedButtonClick());
                }
            }
        };
        failedLift.addListener(failedLiftListener);
        failedLift.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        failedLift.setCaption(Messages.getString("LifterInfo.Failed", locale)); //$NON-NLS-1$
        failedLift.setEnabled(groupData.getAnnouncerEnabled());
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void configureOkLift(final Lifter lifter, final SessionData groupData, Locale locale) {
        final Button.ClickListener okLiftListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -2582860566509880474L;

            @Override
            public void buttonClick(ClickEvent event) {
                okLiftDoIt(lifter, groupData);
            }
        };
        okLift.addListener(okLiftListener);
        okLift.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        okLift.setCaption(Messages.getString("LifterInfo.Successful", locale)); //$NON-NLS-1$
        okLift.setEnabled(groupData.getAnnouncerEnabled());
    }

//    /**
//     * @param lifter
//     * @param groupData
//     * @param locale
//     */
//    private void configureStopStart(final Lifter lifter, final SessionData groupData, Locale locale) {
//        final Button.ClickListener stopStartListener = new Button.ClickListener() { //$NON-NLS-1$
//            private static final long serialVersionUID = -2582860566509880474L;
//
//            @Override
//            public void buttonClick(ClickEvent event) {
//                logger.debug("stop/start clicked");
//                final CountdownTimer timer = groupData.getTimer();
//                groupData.manageTimerOwner(lifter, groupData, timer);
//
//                final boolean running = timer.isRunning();
//                timingLogger.debug("stop/start timer.isRunning()={}", running); //$NON-NLS-1$
//                if (running) {
//                    lifterInfo.setBlocked(true);
//                    timer.pause(TimeStoppedNotificationReason.STOP_START_BUTTON); // pause() does not clear the associated
//                                   // lifter
//                } else {
//                    lifterInfo.setBlocked(false); // !!!!
//                    timer.restart();
//                    groupData.setLifterAsHavingStarted(lifter);
//                    groupData.getRefereeDecisionController().setBlocked(false);
//                }
//                // announce.setEnabled(false);
//                // changeWeight.setEnabled(false);
//            }
//        };
////        stopStart.addListener(stopStartListener);
////        stopStart.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
////        stopStart.setCaption(Messages.getString("LifterInfo.StopStartTime", locale)); //$NON-NLS-1$
//    }
    
    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void configureStart(final Lifter lifter, final SessionData groupData, Locale locale) {
        @SuppressWarnings("serial")
		final Button.ClickListener startListener = new Button.ClickListener() { //$NON-NLS-1$

            @Override
            public void buttonClick(ClickEvent event) {
                startDoIt(lifter, groupData);
            }
        };
        start.addListener(startListener);
        //start.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        start.setIcon(new ThemeResource("icons/16/playTriangle.png"));
        //start.setCaption(Messages.getString("LifterInfo.StartTime", locale)); //$NON-NLS-1$
    }
    
    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void configureStop(final Lifter lifter, final SessionData groupData, Locale locale) {
        final Button.ClickListener stopListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -2582860566509880474L;

            @Override
            public void buttonClick(ClickEvent event) {
                stopDoIt(lifter, groupData);
            }
        };
        stop.addListener(stopListener);
        //stop.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        stop.setIcon(new ThemeResource("icons/16/pause.png"));
        //stop.setCaption(Messages.getString("LifterInfo.StopTime", locale)); //$NON-NLS-1$
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void configureOneMinute(final Lifter lifter, final SessionData groupData, Locale locale) {

        final Button.ClickListener oneMinuteListener = new Button.ClickListener() {
            private static final long serialVersionUID = 5693610077500773431L;

            @Override
            public void buttonClick(ClickEvent event) {
                oneMinuteDoIt(lifter, groupData);

            }
        };
        oneMinute.addListener(oneMinuteListener);
        oneMinute.setWidth(ANNOUNCER_SMALL_BUTTON_WIDTH);
        oneMinute.setCaption(Messages.getString("LifterInfo.OneMinute", locale)); //$NON-NLS-1$
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void configureTwoMinutes(final Lifter lifter, final SessionData groupData, Locale locale) {

        final Button.ClickListener twoMinutesListener = new Button.ClickListener() {
            private static final long serialVersionUID = 5693610077500773431L;

            @Override
            public void buttonClick(ClickEvent event) {
                twoMinutesDoIt(lifter, groupData);
            }

        };
        twoMinutes.addListener(twoMinutesListener);
        twoMinutes.setWidth(ANNOUNCER_SMALL_BUTTON_WIDTH);
        twoMinutes.setCaption(Messages.getString("LifterInfo.TwoMinutes", locale)); //$NON-NLS-1$
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void configureWeightChangeButton(final Lifter lifter, final SessionData groupData, Locale locale) {
        final Button.ClickListener changeWeightListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -2582860566509880474L;

            @Override
            public void buttonClick(ClickEvent event) {
                timingLogger.debug("weightChangeButton clicked"); //$NON-NLS-1$
                logger.info("WEIGHT CHANGE button clicked");
                groupData.getTimer().pause(TimeStoppedNotificationReason.CURRENT_LIFTER_CHANGE);
                if (mode == Mode.ANNOUNCER || mode == Mode.MARSHALL) {
                    // if
                    // (!WebApplicationConfiguration.NECShowsLifterImmediately)
                    // {
                    // groupData.displayWeight(lifter);
                    // } else {
                    // groupData.displayLifterInfo(lifter);
                    // }
                    AnnouncerView announcerView = (AnnouncerView) CompetitionApplication.getCurrent().components.currentView;
                    announcerView.setStickyEditor(false, false);
                    announcerView.editFirstLifterInfo(groupData, WebApplicationConfiguration.DEFAULT_STICKINESS);
                    changeWeight.setEnabled(true);
                    if (mode == Mode.ANNOUNCER) {
                        announce.setEnabled(true);
                        announcerView.selectFirstLifter();
                    }
                }

                // okLift.setEnabled(false);
                // failedLift.setEnabled(false);
                // stopStart.setEnabled(false);
            }
        };
        changeWeight.addListener(changeWeightListener);
        changeWeight.setWidth(ANNOUNCER_BUTTON_WIDTH); 
        changeWeight.setCaption(Messages.getString("LifterInfo.WeightChange", locale)); //$NON-NLS-1$
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void configureAnnounceButton(final Lifter lifter, final SessionData groupData, final Locale locale) {
        final Button.ClickListener announceListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -2582860566509880474L;

            @Override
            public void buttonClick(ClickEvent event) {
                timingLogger.debug("announce"); //$NON-NLS-1$
                checkDecisionHasBeenDisplayed(groupData, locale);
                groupData.callLifter(lifter); // will call start which will
                                              // cause the timer buttons to do
                                              // their thing.
                enableStopStart(groupData.getTimer().isRunning());
                announce.setEnabled(false);
                changeWeight.setEnabled(true);
                groupData.getRefereeDecisionController().setBlocked(false);
                // okLift.setEnabled(true);
                // failedLift.setEnabled(true);
            }
        };

        announce.addListener(announceListener);
        announce.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        announce.setCaption(Messages.getString("LifterInfo.Announce", locale)); //$NON-NLS-1$
        announce.setEnabled(groupData.getAnnouncerEnabled());
    }

    protected void checkDecisionHasBeenDisplayed(SessionData groupData, Locale locale) {
        if (!groupData.getAnnouncerEnabled()) {
            throw new RuntimeException(Messages.getString("LifterInfo.Busy", locale)); //$NON-NLS-1$
        }
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void addStopTimeBottomListener(final Lifter lifter, final SessionData groupData, Locale locale) {
        // we need a way to stop the timer if the current lifter requests a
        // change.
        final Button.ClickListener stopTimeBottomListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -2582860566509880474L;

            @Override
            public void buttonClick(ClickEvent event) {
                timingLogger.debug("stopTimeBottom"); //$NON-NLS-1$
                groupData.getTimer().pause(TimeStoppedNotificationReason.CURRENT_LIFTER_CHANGE);
            }
        };
        stopTimeBottom.addListener(stopTimeBottomListener);
        stopTimeBottom.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        stopTimeBottom.setCaption(Messages.getString("LifterInfo.WeightChange", locale)); //$NON-NLS-1$
    }

    public void showTimerControls(boolean running) {
        //stopStart.setVisible(true);
    	stop.setVisible(true);
    	start.setVisible(true);
    	enableStopStart(running);
        oneMinute.setVisible(true);
        twoMinutes.setVisible(true);
        setTimerShortcutsEnabled(true);
    }

	private void setTimerShortcutsEnabled(boolean b) {
		this.timerShortcutsEnabled = b;
	}

	public void hideTimerControls() {
        start.setVisible(false);
        stop.setVisible(false);
        oneMinute.setVisible(false);
        twoMinutes.setVisible(false);
        setTimerShortcutsEnabled(false);
    }

    public void showLiftControls() {
    	logger.trace("showing announcer decision buttons");
    	announce.setEnabled(true);
        okLift.setEnabled(true);
        failedLift.setEnabled(true);
    }

    public void hideLiftControls() {
    	logger.trace("hiding announcer decision buttons");
    	announce.setEnabled(false);
        okLift.setEnabled(false);
        failedLift.setEnabled(false);
    }

	private void okLiftDoIt(final Lifter lifter, final SessionData groupData) {
		final long currentTimeMillis = System.currentTimeMillis();
		// ignore two clicks on the same button in quick succession
		if ((currentTimeMillis - lifterInfo.getLastOkButtonClick()) > MIN_CLICK_DELAY) {
		    logger.debug("Ok: délai acceptable: {}-{}={}",
		        new Object[] { currentTimeMillis, lifterInfo.getLastOkButtonClick(),
		                (currentTimeMillis - lifterInfo.getLastOkButtonClick()) });
		    lifterInfo.setLastOkButtonClick(currentTimeMillis);
		    logger.debug("Ok: dernier click accepté: {} {}", (lifterInfo.getLastOkButtonClick()), lifterInfo);
		    timingLogger.debug("okLift"); //$NON-NLS-1$
		    // call group data first because this resets the timers
		    logger.info("successful lift for {} {}", lifter.getLastName(), lifter.getFirstName()); //$NON-NLS-1$
		    lifterInfo.setBlocked(true);
		    groupData.liftDone(lifter, true);
		    lifter.successfulLift();
		} else {
		    logger.debug("Ok: délai INacceptable: {}", currentTimeMillis - lifterInfo.getLastOkButtonClick());
		}
	}

	private void startDoIt(final Lifter lifter, final SessionData groupData) {
		logger.info("start clicked");
		final CountdownTimer timer = groupData.getTimer();
		groupData.manageTimerOwner(lifter, groupData, timer);

		final boolean running = timer.isRunning();
		timingLogger.debug("start timer.isRunning()={}", running); //$NON-NLS-1$
		if (running) {
			// do nothing
		} else {
		    lifterInfo.setBlocked(false); // !!!!
		    timer.restart();
		    groupData.setLifterAsHavingStarted(lifter);
		    groupData.getRefereeDecisionController().setBlocked(false);
		    enableStopStart(true);
		}
	}

	private void stopDoIt(final Lifter lifter, final SessionData groupData) {
		logger.info("stop clicked");
		final CountdownTimer timer = groupData.getTimer();
		groupData.manageTimerOwner(lifter, groupData, timer);

		final boolean running = timer.isRunning();
		timingLogger.debug("stop timer.isRunning()={}", running); //$NON-NLS-1$
		if (running) {
		    lifterInfo.setBlocked(true);
		    timer.pause(TimeStoppedNotificationReason.STOP_START_BUTTON); // pause() does not clear the associated
		                   // lifter
		    enableStopStart(false);
		} else {
			// do nothing.
		}
	}

	private void oneMinuteDoIt(final Lifter lifter, final SessionData groupData) {
		final CountdownTimer timer = groupData.getTimer();
		final boolean running = timer.isRunning();
		timingLogger.debug("oneMinute"); //$NON-NLS-1$

		// call group data first because this resets the timers
		logger.info("resetting to one minute for {}", lifter); //$NON-NLS-1$
		if (running) {
		    timer.forceTimeRemaining(60000); // pause() does not clear
		                                     // the associated lifter
		}
		groupData.setForcedByTimekeeper(true, 60000);
		enableStopStart(false);
	}
	
	private void twoMinutesDoIt(final Lifter lifter,
			final SessionData groupData) {
		final CountdownTimer timer = groupData.getTimer();
        final boolean running = timer.isRunning();
        timingLogger.debug("twoMinutes"); //$NON-NLS-1$

        // call group data first because this resets the timers
        logger.info("resetting to two minutes for {}", lifter); //$NON-NLS-1$
        if (running) {
            timer.forceTimeRemaining(120000); // pause() does not clear
                                              // the associated lifter
        }

        groupData.setForcedByTimekeeper(true, 120000);
        enableStopStart(false);
	}
	
    private void setActions(final Lifter lifter, final SessionData groupData) {
    	actionManager = new ActionManager();
    	
    	//FIXME - implement Action.Listener in shortcut actions, binding to lifter and groupData.
    	
    	actionStart = new ShortcutAction("start",ShortcutAction.KeyCode.G, new int[]{ShortcutAction.SHORTHAND_CHAR_ALT});
    	actionStop = new ShortcutAction("stop",ShortcutAction.KeyCode.P, new int[]{ShortcutAction.SHORTHAND_CHAR_ALT});
    	actionOneMinute = new ShortcutAction("stop",ShortcutAction.KeyCode.NUM1, new int[]{ShortcutAction.SHORTHAND_CHAR_ALT});
    	actionTwoMinutes = new ShortcutAction("stop",ShortcutAction.KeyCode.NUM2, new int[]{ShortcutAction.SHORTHAND_CHAR_ALT});
	}

	@Override
	public void addActionHandler(Handler actionHandler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeActionHandler(Handler actionHandler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends Action & com.vaadin.event.Action.Listener> void addAction(
			T action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends Action & com.vaadin.event.Action.Listener> void removeAction(
			T action) {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public Action[] getActions(Object target, Object sender) {
//		return new Action[]{actionStart,actionStop,actionOneMinute,actionTwoMinutes};
//	}
//
//	@Override
//	public void handleAction(Action action, Object sender, Object target) {
//		if (action == actionStart) startDoIt(lifter, groupData);
//		else if (action == actionStart) stopDoIt(lifter, groupData);
//		else if (action == actionOneMinute) oneMinuteDoIt(lifter, groupData);
//		else if (action == actionTwoMinutes) twoMinutesDoIt(lifter, groupData);
//	}

}
