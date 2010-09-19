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

package org.concordiainternational.competition.ui.components;

import java.util.Locale;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.ui.AnnouncerView;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.GroupData;
import org.concordiainternational.competition.ui.LifterInfo;
import org.concordiainternational.competition.ui.AnnouncerView.Mode;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Button.ClickEvent;

public class TimerControls extends GridLayout {

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
    final public Button stopStart = new Button();
    final public Button oneMinute = new Button();
    final public Button twoMinutes = new Button();
    final public Button okLift = new Button();
    final public Button failedLift = new Button();
    final public Button stopTimeBottom = new Button();
    private Mode mode;
    private LifterInfo lifterInfo;
    private boolean timerVisible = false;

    public TimerControls(final Lifter lifter, final GroupData groupData, boolean top, AnnouncerView.Mode mode,
            LifterInfo lifterInfo, boolean timerVisible, CompetitionApplication app) {
        super(3, 3);
        this.mode = mode;
        this.lifterInfo = lifterInfo;
        this.timerVisible = timerVisible;

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
    private void top(final Lifter lifter, final GroupData groupData, Locale locale) throws OverlapsException,
            OutOfBoundsException {
        if (mode == AnnouncerView.Mode.ANNOUNCER) {
            configureAnnounceButton(lifter, groupData, locale);
            configureWeightChangeButton(lifter, groupData, locale);
            configureStopStart(lifter, groupData, locale);
            configureOneMinute(lifter, groupData, locale);
            configureTwoMinutes(lifter, groupData, locale);
            configureOkLift(lifter, groupData, locale);
            configureFailedLift(lifter, groupData, locale);

            this.addComponent(announce, 0, 0);
            this.addComponent(changeWeight, 1, 0, 2, 0);
            this.addComponent(stopStart, 0, 1);
            this.addComponent(oneMinute, 1, 1);
            this.addComponent(twoMinutes, 2, 1);
            this.addComponent(okLift, 0, 2);
            this.addComponent(failedLift, 1, 2, 2, 2);

            okLift.addStyleName("okLift"); //$NON-NLS-1$
            failedLift.addStyleName("failedLift"); //$NON-NLS-1$

            final boolean announced = !groupData.getNeedToAnnounce();
            if (!WebApplicationConfiguration.NECShowsLifterImmediately) {
                announce.setEnabled(true); // allow announcer to call lifter at
                                           // will
                changeWeight.setEnabled(true); // always allow changes
                stopStart.setEnabled(announced);
            } else {
                announce.setEnabled(!announced); // if lifter has been
                                                 // announced, disable this
                                                 // button
                changeWeight.setEnabled(true); // always allow changes
                stopStart.setEnabled(announced);
            }
            // okLift.setEnabled(false);
            // failedLift.setEnabled(false);

            if (timerVisible) {
                showTimerControls();
            } else {
                hideTimerControls();
            }
        } else if (mode == AnnouncerView.Mode.MARSHALL) {
            configureWeightChangeButton(lifter, groupData, locale);
            this.addComponent(changeWeight, 0, 0, 1, 0);
            // changeWeight.setEnabled(false);
        } else if (mode == AnnouncerView.Mode.TIMEKEEPER) {
            configureStopStart(lifter, groupData, locale);
            configureOneMinute(lifter, groupData, locale);
            configureTwoMinutes(lifter, groupData, locale);
            this.addComponent(stopStart, 0, 1);
            this.addComponent(oneMinute, 1, 1);
            this.addComponent(twoMinutes, 2, 1);
            // stopStart.setEnabled(false);
        }
    }

    /**
     * @param groupData
     * @param locale
     */
    private void bottom(Lifter lifter, final GroupData groupData, Locale locale) {

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
    private void configureFailedLift(final Lifter lifter, final GroupData groupData, Locale locale) {

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
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void configureOkLift(final Lifter lifter, final GroupData groupData, Locale locale) {
        final Button.ClickListener okLiftListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -2582860566509880474L;

            @Override
            public void buttonClick(ClickEvent event) {
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
    private void configureStopStart(final Lifter lifter, final GroupData groupData, Locale locale) {
        final Button.ClickListener stopStartListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -2582860566509880474L;

            @Override
            public void buttonClick(ClickEvent event) {
                logger.warn("stop/start clicked");
                final CountdownTimer timer = groupData.getTimer();
                groupData.manageTimerOwner(lifter, groupData, timer);

                final boolean running = timer.isRunning();
                timingLogger.debug("stop/start timer.isRunning()={}", running); //$NON-NLS-1$
                if (running) {
                    lifterInfo.setBlocked(true);
                    timer.pause(); // pause() does not clear the associated
                                   // lifter
                } else {
                    lifterInfo.setBlocked(false); // !!!!
                    timer.restart();
                    groupData.setLifterAsHavingStarted(lifter);
                }
                // announce.setEnabled(false);
                // changeWeight.setEnabled(false);
            }
        };
        stopStart.addListener(stopStartListener);
        stopStart.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        stopStart.setCaption(Messages.getString("LifterInfo.StopStartTime", locale)); //$NON-NLS-1$
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void configureOneMinute(final Lifter lifter, final GroupData groupData, Locale locale) {

        final Button.ClickListener oneMinuteListener = new Button.ClickListener() {
            private static final long serialVersionUID = 5693610077500773431L;

            @Override
            public void buttonClick(ClickEvent event) {
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
    private void configureTwoMinutes(final Lifter lifter, final GroupData groupData, Locale locale) {

        final Button.ClickListener twoMinutesListener = new Button.ClickListener() {
            private static final long serialVersionUID = 5693610077500773431L;

            @Override
            public void buttonClick(ClickEvent event) {
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
    private void configureWeightChangeButton(final Lifter lifter, final GroupData groupData, Locale locale) {
        final Button.ClickListener changeWeightListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -2582860566509880474L;

            @Override
            public void buttonClick(ClickEvent event) {
                timingLogger.debug("weightChangeButton clicked"); //$NON-NLS-1$
                logger.info("WEIGHT CHANGE button clicked");
                groupData.getTimer().pause();
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
    private void configureAnnounceButton(final Lifter lifter, final GroupData groupData, final Locale locale) {
        final Button.ClickListener announceListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -2582860566509880474L;

            @Override
            public void buttonClick(ClickEvent event) {
                timingLogger.debug("announce"); //$NON-NLS-1$
                checkDecisionHasBeenDisplayed(groupData, locale);
                groupData.callLifter(lifter); // will call start which will
                                              // cause the timer buttons to do
                                              // their thing.
                stopStart.setEnabled(true);
                announce.setEnabled(false);
                changeWeight.setEnabled(true);
                // okLift.setEnabled(true);
                // failedLift.setEnabled(true);
            }
        };

        announce.addListener(announceListener);
        announce.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        announce.setCaption(Messages.getString("LifterInfo.Announce", locale)); //$NON-NLS-1$
    }

    protected void checkDecisionHasBeenDisplayed(GroupData groupData, Locale locale) {
        if (!groupData.getAnnouncerEnabled()) {
            throw new RuntimeException(Messages.getString("LifterInfo.Busy", locale)); //$NON-NLS-1$
        }
    }

    /**
     * @param lifter
     * @param groupData
     * @param locale
     */
    private void addStopTimeBottomListener(final Lifter lifter, final GroupData groupData, Locale locale) {
        // we need a way to stop the timer if the current lifter requests a
        // change.
        final Button.ClickListener stopTimeBottomListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -2582860566509880474L;

            @Override
            public void buttonClick(ClickEvent event) {
                timingLogger.debug("stopTimeBottom"); //$NON-NLS-1$
                groupData.getTimer().pause();
            }
        };
        stopTimeBottom.addListener(stopTimeBottomListener);
        stopTimeBottom.setWidth(ANNOUNCER_BUTTON_WIDTH); //$NON-NLS-1$
        stopTimeBottom.setCaption(Messages.getString("LifterInfo.WeightChange", locale)); //$NON-NLS-1$
    }

    public void showTimerControls() {
        stopStart.setVisible(true);
        oneMinute.setVisible(true);
        twoMinutes.setVisible(true);
    }

    public void hideTimerControls() {
        stopStart.setVisible(false);
        oneMinute.setVisible(false);
        twoMinutes.setVisible(false);
    }

}
