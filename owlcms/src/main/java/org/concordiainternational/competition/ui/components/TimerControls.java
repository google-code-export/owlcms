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
import org.concordiainternational.competition.ui.AnnouncerView;
import org.concordiainternational.competition.ui.AnnouncerView.Mode;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.InteractionNotificationReason;
import org.concordiainternational.competition.ui.LifterInfo;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;

public class TimerControls extends GridLayout {

	private static final String ANNOUNCER_BUTTON_WIDTH = "9em";
	private static final String ANNOUNCER_SMALL_BUTTON_WIDTH = "4em";
	private static final long serialVersionUID = 4075226732120553473L;
	static final Logger logger = LoggerFactory.getLogger(TimerControls.class);
	static final Logger timingLogger = LoggerFactory
			.getLogger("org.concordiainternational.competition.timer.TimingLogger"); //$NON-NLS-1$
	static final Logger buttonLogger = LoggerFactory
	            .getLogger("org.concordiainternational.competition.ButtonsLogger"); //$NON-NLS-1$

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

//	private boolean timerShortcutsEnabled = false;
//	private ShortcutActionListener startAction;
//	private ShortcutActionListener stopAction;
//	private ShortcutActionListener oneMinuteAction;
//	private ShortcutActionListener twoMinutesAction;

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

			registerListeners(lifter,groupData);

			this.addComponent(announce, 0, 0, 1, 0);
			this.addComponent(changeWeight, 2, 0, 3, 0);
			this.addComponent(start, 0, 1, 0, 1);
			this.addComponent(stop, 1, 1, 1, 1);

			this.addComponent(oneMinute, 2, 1, 2, 1);
			this.addComponent(twoMinutes, 3, 1, 3, 1);
			this.addComponent(okLift, 0, 2, 1, 2);
			this.addComponent(failedLift, 2, 2, 3, 2);

			okLift.addStyleName("okLift"); //$NON-NLS-1$
			failedLift.addStyleName("failedLift"); //$NON-NLS-1$

//			final boolean announced = !groupData.getNeedToAnnounce();
			if (!WebApplicationConfiguration.ShowLifterImmediately) {
//				announce.setEnabled(true); // allow announcer to call lifter at will
//				buttonLogger.debug("announce: "+"do not show lifter immediately"+": {}", announce.isEnabled() );
				changeWeight.setEnabled(true); // always allow changes
				enableStopStart(true);
			} else {
			    // if lifter has been announced, disable this button
//				announce.setEnabled(!announced /*&& groupData.getAnnouncerEnabled()*/);
//				buttonLogger.debug("announce: "+"!announced "+": {}", announce.isEnabled() );
				changeWeight.setEnabled(true); // always allow changes
				enableStopStart(groupData.getTimer().isRunning());
			}

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

            registerListeners(lifter,groupData);
//			setTimerShortcutsEnabled(true);
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
				failedLiftDoIt(lifter, groupData);
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
		start.setIcon(new ThemeResource("icons/16/playTriangle.png"));
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
		stop.setIcon(new ThemeResource("icons/16/pause.png"));
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
				groupData.getTimer().pause(InteractionNotificationReason.CURRENT_LIFTER_CHANGE_STARTED);
				if (mode == Mode.ANNOUNCER || mode == Mode.MARSHALL) {
					AnnouncerView announcerView = (AnnouncerView) CompetitionApplication.getCurrent().components.currentView;
					announcerView.setStickyEditor(false, false);
					announcerView.editFirstLifterInfo(groupData, WebApplicationConfiguration.DEFAULT_STICKINESS);
					changeWeight.setEnabled(true);
					if (mode == Mode.ANNOUNCER) {
//						announce.setEnabled(true);
//						buttonLogger.debug("announce:"+"Mode.ANNOUNCER"+": {}", announce.isEnabled() );
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
				groupData.callLifter(lifter); // will call start which will cause the timer buttons to do their thing.
				enableStopStart(groupData.getTimer().isRunning());
//				announce.setEnabled(false);
//				buttonLogger.debug("announce:"+"Mode.ANNOUNCER"+": {}", announce.isEnabled());
				changeWeight.setEnabled(true);
				groupData.getRefereeDecisionController().setBlocked(false);
				// okLift.setEnabled(true);
				// failedLift.setEnabled(true);
			}
		};

		announce.addListener(announceListener);
		announce.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
		announce.setCaption(Messages.getString("LifterInfo.Announce", locale)); //$NON-NLS-1$
//		announce.setEnabled(groupData.getAnnouncerEnabled());
//		buttonLogger.debug("announce:"+"groupData.getAnnouncerEnabled()"+": {}", announce.isEnabled() );
	}

	protected void checkDecisionHasBeenDisplayed(SessionData groupData, Locale locale) {
//		if (!groupData.getAnnouncerEnabled()) {
//			throw new RuntimeException(Messages.getString("LifterInfo.Busy", locale)); //$NON-NLS-1$
//		}
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
				groupData.getTimer().pause(InteractionNotificationReason.CURRENT_LIFTER_CHANGE_DONE);
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
//		setTimerShortcutsEnabled(true);
	}

//	public void setTimerShortcutsEnabled(boolean b) {
//		this.timerShortcutsEnabled = b;
//	}

	public void hideTimerControls() {
		start.setVisible(false);
		stop.setVisible(false);
		oneMinute.setVisible(false);
		twoMinutes.setVisible(false);
//		setTimerShortcutsEnabled(false);
	}

	public void showLiftControls() {
		logger.trace("showing announcer decision buttons");
//		announce.setEnabled(true);
//		buttonLogger.debug("announce:"+"showLiftControls"+": {}", announce.isEnabled() );
		okLift.setEnabled(true);
		failedLift.setEnabled(true);
	}

	public void hideLiftControls() {
		logger.trace("hiding announcer decision buttons");
//		announce.setEnabled(false);
//		buttonLogger.debug("announce:"+"hideLiftControls"+": {}", announce.isEnabled() );
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
			
	        groupData.notifyPrematureDecision();
			timingLogger.debug("okLift"); //$NON-NLS-1$	
			// call group data first because this resets the timers
			logger.info("successful lift for {} {}", lifter.getLastName(), lifter.getFirstName()); //$NON-NLS-1$
			lifterInfo.setBlocked(true);		
			groupData.okLiftUpdateModel();
			
		} else {
			logger.debug("Ok: délai Inacceptable: {}", currentTimeMillis - lifterInfo.getLastOkButtonClick());
		}
	}

    private void failedLiftDoIt(final Lifter lifter, final SessionData groupData) {
        final long currentTimeMillis = System.currentTimeMillis();
        // ignore two clicks on the same button in quick succession
        if (currentTimeMillis - lifterInfo.getLastFailedButtonClick() > MIN_CLICK_DELAY) {
            logger.debug("Failed: délai acceptable: {}", currentTimeMillis
                    - lifterInfo.getLastFailedButtonClick());
            lifterInfo.setLastFailedButtonClick(currentTimeMillis);
            
            groupData.notifyPrematureDecision();
            timingLogger.debug("failedLift"); //$NON-NLS-1$
            // call group data first because this resets the timers
            logger.info("failed lift for {} {}", lifter.getLastName(), lifter.getFirstName()); //$NON-NLS-1$
            lifterInfo.setBlocked(true);
            
            groupData.failedListUpdateModel();

        } else {
            logger.debug("Failed: délai Inacceptable: {}", currentTimeMillis
                    - lifterInfo.getLastFailedButtonClick());
        }
    }

	public void startDoIt(final Lifter lifter, final SessionData groupData) {
		logger.info("start clicked");
		groupData.setTimeKeepingInUse(true);
		if (groupData.getTimer().isRunning()) {
		    // do nothing
		    timingLogger.debug("start timer.isRunning()={}", true); //$NON-NLS-1$
		} else {
		    lifterInfo.setBlocked(false); // !!!!
	        enableStopStart(true);
		    timingLogger.debug("start timer.isRunning()={}", false); //$NON-NLS-1$
		    groupData.startUpdateModel();
		    groupData.getRefereeDecisionController().setBlocked(false);
		}
	}

	private void stopDoIt(final Lifter lifter, final SessionData groupData) {
		logger.info("stop clicked");
		groupData.setTimeKeepingInUse(true);
		if (groupData.getTimer().isRunning()) {
		    timingLogger.debug("stop timer.isRunning()={}", true); //$NON-NLS-1$
			lifterInfo.setBlocked(true);
			groupData.stopUpdateModel();
			enableStopStart(false);
		} else {
		    timingLogger.debug("stop timer.isRunning()={}", false); //$NON-NLS-1$
			// do nothing.
		}
	}

	private void oneMinuteDoIt(final Lifter lifter, final SessionData groupData) {
		timingLogger.debug("oneMinute"); //$NON-NLS-1$

		// call group data first because this resets the timers
		logger.info("resetting to one minute for {}", lifter); //$NON-NLS-1$
		groupData.oneMinuteUpdateModel();
		enableStopStart(false);
	}

	private void twoMinutesDoIt(final Lifter lifter, final SessionData groupData) {
		timingLogger.debug("twoMinutes"); //$NON-NLS-1$

		// call group data first because this resets the timers
		logger.info("resetting to two minutes for {}", lifter); //$NON-NLS-1$
		groupData.twoMinuteUpdateModel();
		enableStopStart(false);
	}

//	@SuppressWarnings("serial")
//	private abstract class ShortcutActionListener extends ShortcutAction implements Action.Listener {
//
//		public ShortcutActionListener(String caption, int kc, int[] m) {
//			super(caption, kc, m);
//		}
//
//		public ShortcutActionListener(String caption, int kc) {
//			super(caption, kc, null);
//		}
//
//	}

//	@SuppressWarnings("serial")
//	private void initActions(Action.Notifier actionNotifier, final Lifter lifter, final SessionData groupData) {
//
//		startAction = new ShortcutActionListener("start", ShortcutAction.KeyCode.G){
//
//			@Override
//			public void handleAction(Object sender, Object target) {
//				if (!timerShortcutsEnabled) return;
//				TimerControls.this.startDoIt(lifter,groupData);
//			}
//
//		};
//		stopAction = new ShortcutActionListener("stop",ShortcutAction.KeyCode.P){
//
//			@Override
//			public void handleAction(Object sender, Object target) {
//				if (!timerShortcutsEnabled) return;
//				TimerControls.this.stopDoIt(lifter,groupData);
//			}
//
//		};
//		oneMinuteAction = new ShortcutActionListener("1 minute",ShortcutAction.KeyCode.O){
//
//			@Override
//			public void handleAction(Object sender, Object target) {
//				if (!timerShortcutsEnabled) return;
//				TimerControls.this.oneMinuteDoIt(lifter,groupData);
//			}
//
//		};
//		twoMinutesAction = new ShortcutActionListener("2 minutes",ShortcutAction.KeyCode.T){
//
//			@Override
//			public void handleAction(Object sender, Object target) {
//				if (!timerShortcutsEnabled) return;
//				TimerControls.this.twoMinutesDoIt(lifter,groupData);	
//			}
//
//		};
//
//		actionNotifier.addAction(startAction);
//		actionNotifier.addAction(stopAction);
//		actionNotifier.addAction(oneMinuteAction);
//		actionNotifier.addAction(twoMinutesAction);
//
//	}

	public void unregisterListeners() {
//		Window mainWindow = CompetitionApplication.getCurrent().getMainWindow();
////		mainWindow.removeAction(startAction);
////		mainWindow.removeAction(stopAction);
////		mainWindow.removeAction(oneMinuteAction);
////		mainWindow.removeAction(twoMinutesAction);
	}

	public void registerListeners(Lifter lifter, SessionData groupData) {
//		Window mainWindow = CompetitionApplication.getCurrent().getMainWindow();
////		initActions(mainWindow, lifter, groupData);
	}



}
