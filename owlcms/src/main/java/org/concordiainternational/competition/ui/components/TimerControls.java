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
import org.concordiainternational.competition.utils.LoggerUtils;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;

public class TimerControls extends GridLayout {

    private static final String ANNOUNCER_BUTTON_WIDTH = "9em";
    private static final String ANNOUNCER_SMALL_BUTTON_WIDTH = "4em";
    private static final long serialVersionUID = 4075226732120553473L;

    private static final Logger logger = LoggerFactory.getLogger(TimerControls.class);
    private static final Logger timingLogger = LoggerFactory.getLogger("timing." + TimerControls.class.getSimpleName()); //$NON-NLS-1$
    private static final Logger buttonLogger = LoggerFactory.getLogger("buttons." + TimerControls.class.getSimpleName()); //$NON-NLS-1$
    //    private static Logger listenerLogger = LoggerFactory.getLogger("listeners."+TimerControls.class.getSimpleName()); //$NON-NLS-1$

    /**
     * a click that take place less than MIN_CLICK_DELAY milliseconds after an initial click on the Ok or Failed button is ignored. This
     * usually means that the user double clicked by mistake, or pressed a second time becaus he was unsure that recalculation had taken
     * place and we definitely don't want to register two successful or failed lifts.
     */
    protected static final long MIN_CLICK_DELAY = 1000;

    final public Button announce = new Button();
    final public Button changeWeight = new Button();
    // final public Button stopStart = new Button();
    final public Button start = new Button();
    final public Button stop = new Button();
    final public Button oneMinute = new Button();
    final public Button twoMinutes = new Button();
    final public Button okLift = new Button();
    final public Button noLift = new Button();
    final public Button stopTimeBottom = new Button();
    private Mode mode;
    private LifterInfo lifterInfo;
    private boolean timerVisible = false;

    // private boolean timerShortcutsEnabled = false;
    // private ShortcutActionListener startAction;
    // private ShortcutActionListener stopAction;
    // private ShortcutActionListener oneMinuteAction;
    // private ShortcutActionListener twoMinutesAction;

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

//        logger.warn(buttonLogger.getName());
        enableButtons(groupData, "init");
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
            // configureStopStart(lifter, groupData, locale);
            configureStart(lifter, groupData, locale);
            configureStop(lifter, groupData, locale);
            configureOneMinute(lifter, groupData, locale);
            configureTwoMinutes(lifter, groupData, locale);
            configureOkLift(lifter, groupData, locale);
            configureFailedLift(lifter, groupData, locale);

            registerListeners(lifter, groupData);

            this.addComponent(announce, 0, 0, 1, 0);
            this.addComponent(changeWeight, 2, 0, 3, 0);
            this.addComponent(start, 0, 1, 0, 1);
            this.addComponent(stop, 1, 1, 1, 1);

            this.addComponent(oneMinute, 2, 1, 2, 1);
            this.addComponent(twoMinutes, 3, 1, 3, 1);
            this.addComponent(okLift, 0, 2, 1, 2);
            this.addComponent(noLift, 2, 2, 3, 2);

            okLift.addStyleName("okLift"); //$NON-NLS-1$
            noLift.addStyleName("failedLift"); //$NON-NLS-1$

            if (timerVisible) {
                showTimerControls(groupData);
            } else {
                hideTimerControls();
            }
        } else if (mode == AnnouncerView.Mode.MARSHALL) {
            configureWeightChangeButton(lifter, groupData, locale);
            this.addComponent(changeWeight, 0, 0, 1, 0);
        } else if (mode == AnnouncerView.Mode.TIMEKEEPER) {
            // configureStopStart(lifter, groupData, locale);
            configureStart(lifter, groupData, locale);
            configureStop(lifter, groupData, locale);
            configureOneMinute(lifter, groupData, locale);
            configureTwoMinutes(lifter, groupData, locale);
            this.addComponent(start, 0, 1, 0, 1);
            this.addComponent(stop, 1, 1, 1, 1);
            this.addComponent(oneMinute, 2, 1, 2, 1);
            this.addComponent(twoMinutes, 3, 1, 3, 1);
            registerListeners(lifter, groupData);
        }
    }

    public void enableButtons(SessionData groupData, String whereFrom) {
        boolean announced = groupData.isAnnounced();
        boolean running = groupData.getTimer().isRunning();

        CompetitionApplication app = CompetitionApplication.getCurrent();
        synchronized (app) {

            if (!running) {
                if (announced) {
                    buttonLogger.debug(System.identityHashCode(this) + " {}: announced", whereFrom);
                    start.setEnabled(true);
                    start.addStyleName("primary");
                    stop.setEnabled(false);
                    stop.removeStyleName("primary");
                    // must be able to decide.
                    okLift.setEnabled(true);
                    okLift.addStyleName("primary");
                    noLift.setEnabled(true);
                    noLift.addStyleName("primary");
                } else {
                    buttonLogger.debug(System.identityHashCode(this) + " {}: not announced", whereFrom);
                    start.setEnabled(false);
                    start.removeStyleName("primary");
                    stop.setEnabled(false);
                    stop.removeStyleName("primary");
                    // since someone was announced, must be able to decide...
                    okLift.setEnabled(false);
                    okLift.removeStyleName("primary");
                    noLift.setEnabled(false);
                    noLift.removeStyleName("primary");
                }

            } else {
                buttonLogger.debug(System.identityHashCode(this) + " {}: timer running, {}", whereFrom, (announced ? "announced"
                        : "not announced"));
                start.setEnabled(false);
                start.removeStyleName("primary");
                // timer is running, must be able to stop
                stop.setEnabled(true);
                stop.addStyleName("primary");
                // must be able to decide.
                okLift.setEnabled(true);
                okLift.addStyleName("primary");
                noLift.setEnabled(true);
                noLift.addStyleName("primary");
            }
            changeWeight.setEnabled(true); // always allow changes
        }
        app.push();
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
                LoggerUtils.buttonSetup(groupData);
                failedLiftDoIt(lifter, groupData);
            }
        };
        noLift.addListener(failedLiftListener);
        noLift.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        noLift.setCaption(Messages.getString("LifterInfo.Failed", locale)); //$NON-NLS-1$
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
                LoggerUtils.buttonSetup(groupData);
                okLiftDoIt(lifter, groupData);
            }
        };
        okLift.addListener(okLiftListener);
        okLift.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        okLift.setCaption(Messages.getString("LifterInfo.Successful", locale)); //$NON-NLS-1$
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
                LoggerUtils.buttonSetup(groupData);
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
                LoggerUtils.buttonSetup(groupData);
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
                LoggerUtils.buttonSetup(groupData);
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
                LoggerUtils.buttonSetup(groupData);
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
                LoggerUtils.buttonSetup(groupData);
                timingLogger.debug("weightChangeButton clicked"); //$NON-NLS-1$
                logger.info("WEIGHT CHANGE button clicked");
                groupData.getTimer().pause(InteractionNotificationReason.CURRENT_LIFTER_CHANGE_STARTED);
                if (mode == Mode.ANNOUNCER || mode == Mode.MARSHALL) {
                    AnnouncerView announcerView = (AnnouncerView) CompetitionApplication.getCurrent().components.currentView;
                    announcerView.setStickyEditor(false, false);
                    announcerView.editFirstLifterInfo(groupData, WebApplicationConfiguration.DEFAULT_STICKINESS);

                    if (mode == Mode.ANNOUNCER) {
                        announcerView.selectFirstLifter();
                    }
                }
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
                LoggerUtils.buttonSetup(groupData);

                timingLogger.debug("announce"); //$NON-NLS-1$
                checkDecisionHasBeenDisplayed(groupData, locale);
                groupData.callLifter(lifter); // will call start which will cause the timer buttons to do their thing.

                groupData.getRefereeDecisionController().setBlocked(false);
            }
        };

        announce.addListener(announceListener);
        announce.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        announce.setCaption(Messages.getString("LifterInfo.Announce", locale)); //$NON-NLS-1$
    }

    protected void checkDecisionHasBeenDisplayed(SessionData groupData, Locale locale) {
        // if (!groupData.getAnnouncerEnabled()) {
        //			throw new RuntimeException(Messages.getString("LifterInfo.Busy", locale)); //$NON-NLS-1$
        // }
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
                LoggerUtils.buttonSetup(groupData);
                timingLogger.debug("stopTimeBottom"); //$NON-NLS-1$
                groupData.getTimer().pause(InteractionNotificationReason.CURRENT_LIFTER_CHANGE_DONE);
            }
        };
        stopTimeBottom.addListener(stopTimeBottomListener);
        stopTimeBottom.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        stopTimeBottom.setCaption(Messages.getString("LifterInfo.WeightChange", locale)); //$NON-NLS-1$
    }

    public void showTimerControls(SessionData groupData) {
        stop.setVisible(true);
        start.setVisible(true);
        oneMinute.setVisible(true);
        twoMinutes.setVisible(true);
        enableButtons(groupData, "showTimerControls");
    }

    public void hideTimerControls() {
        start.setVisible(false);
        stop.setVisible(false);
        oneMinute.setVisible(false);
        twoMinutes.setVisible(false);
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
            lifterInfo.doDisplayDecision(true, lifter.getNextAttemptRequestedWeight(), lifter);
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
            lifterInfo.doDisplayDecision(false, lifter.getNextAttemptRequestedWeight(), lifter);
            groupData.noLiftUpdateModel();

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
            timingLogger.debug("start timer.isRunning()={}", false); //$NON-NLS-1$
            lifterInfo.setBlocked(false); // !!!!
            groupData.getRefereeDecisionController().setBlocked(false);
            groupData.startUpdateModel();
            // enableButtons(groupData,"start onClick");
        }
    }

    private void stopDoIt(final Lifter lifter, final SessionData groupData) {
        logger.info("stop clicked");
        groupData.setTimeKeepingInUse(true);
        if (groupData.getTimer().isRunning()) {
            timingLogger.debug("stop timer.isRunning()={}", true); //$NON-NLS-1$
            lifterInfo.setBlocked(true);
            groupData.stopUpdateModel();
            // enableButtons(groupData, "stop onClick");
        } else {
            timingLogger.debug("stop timer.isRunning()={}", false); //$NON-NLS-1$
            // do nothing.
        }
    }

    private void oneMinuteDoIt(final Lifter lifter, final SessionData groupData) {
        timingLogger.debug("oneMinute"); //$NON-NLS-1$
        logger.info("resetting to one minute for {}", lifter); //$NON-NLS-1$
        groupData.oneMinuteUpdateModel();
        // enableButtons(groupData, "oneMinute onClick");
    }

    private void twoMinutesDoIt(final Lifter lifter, final SessionData groupData) {
        timingLogger.debug("twoMinutes"); //$NON-NLS-1$
        logger.info("resetting to two minutes for {}", lifter); //$NON-NLS-1$
        groupData.twoMinuteUpdateModel();
        // enableButtons(groupData,"twoMinute onClick");
    }

    public void unregisterListeners() {
    }

    public void registerListeners(Lifter lifter, SessionData groupData) {
    }

}
