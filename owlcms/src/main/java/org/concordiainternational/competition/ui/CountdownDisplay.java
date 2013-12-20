/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.net.MalformedURLException;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.decision.IDecisionController;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.publicAddress.IntermissionTimerEvent;
import org.concordiainternational.competition.publicAddress.IntermissionTimerEvent.IntermissionTimerListener;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.timer.CountdownTimerListener;
import org.concordiainternational.competition.ui.SessionData.UpdateEvent;
import org.concordiainternational.competition.ui.SessionData.UpdateEventListener;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.components.DecisionLightsWindow;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

public class CountdownDisplay extends VerticalLayout implements
        ApplicationView,
        CountdownTimerListener,
        DecisionEventListener,
        IntermissionTimerListener,
        CloseListener
{
    public final static Logger logger = LoggerFactory.getLogger(CountdownDisplay.class);
    private static final long serialVersionUID = 1437157542240297372L;

    private String platformName;
    private SessionData masterData;
    private CompetitionApplication app;
    private Label timeDisplay = new Label();
    private int lastTimeRemaining;
    private String viewName;
    private Window popUp = null;
    private DecisionLightsWindow decisionLights;
    private UpdateEventListener updateEventListener;
    protected boolean shown;

    private ShortcutActionListener action1ok;
    private ShortcutActionListener action1fail;
    private ShortcutActionListener action2ok;
    private ShortcutActionListener action2fail;
    private ShortcutActionListener action3ok;
    private ShortcutActionListener action3fail;
    private ShortcutActionListener startAction;
    private ShortcutActionListener stopAction;
    private ShortcutActionListener oneMinuteAction;
    private ShortcutActionListener twoMinutesAction;

    @SuppressWarnings("unused")
    private boolean breakTimerShown = false;
    private Label title;

    public CountdownDisplay(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());

        this.app = CompetitionApplication.getCurrent();

        if (platformName == null) {
            // get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        } else if (app.getPlatform() == null) {
            app.setPlatformByName(platformName);
        }

        synchronized (app) {
            boolean prevDisabled = app.getPusherDisabled();
            try {
                app.setPusherDisabled(true);
                create(app, platformName);
                masterData = app.getMasterData(platformName);
                
                registerAsListener();
                display(platformName, masterData);
            } finally {
                app.setPusherDisabled(prevDisabled);
            }
        }
    }

    private void registerAsGroupDataListener(final String platformName1, final SessionData masterData1) {
        // locate the current group data for the platformName
        if (masterData1 != null) {
            logger.debug("{} listening to: {}", platformName1, masterData1); //$NON-NLS-1$	
            //masterData.addListener(SessionData.UpdateEvent.class, this, "update"); //$NON-NLS-1$

            updateEventListener = new SessionData.UpdateEventListener() {

                @Override
                public void updateEvent(UpdateEvent updateEvent) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            display(platformName1, masterData1);
                        }
                    }).start();
                }

            };
            masterData1.addListener(updateEventListener); //$NON-NLS-1$		

        } else {
            logger.debug("{} NOT listening to:  = {}", platformName1, masterData1); //$NON-NLS-1$	
        }
    }

    /**
     * @param app1
     * @param platformName1
     * @throws MalformedURLException
     */
    private void create(UserActions app1, String platformName1) {
        this.setSizeFull();
        this.addStyleName("largeCountdownBackground");

        title = new Label("");
        this.addComponent(title);
        title.setVisible(false);
        title.addStyleName("title");

        timeDisplay = createTimeDisplay();
        this.addComponent(timeDisplay);
        this.setComponentAlignment(timeDisplay, Alignment.MIDDLE_CENTER);

        this.setExpandRatio(timeDisplay, 100);

    }

    /**
     * 
     */
    private Label createTimeDisplay() {
        Label timeDisplay1 = new Label();
        timeDisplay1.setSizeUndefined();
        // timeDisplay1.setHeight("600px");
        timeDisplay1.addStyleName("largeCountdown");
        return timeDisplay1;
    }

    /**
     * @param platformName1
     * @param masterData1
     * @throws RuntimeException
     */
    private void display(final String platformName1, final SessionData masterData1) throws RuntimeException {
        synchronized (app) {
            final Lifter currentLifter = masterData1.getCurrentLifter();
            logger.trace("currentLifter = {}", currentLifter);
            if (currentLifter != null) {
                boolean done = fillLifterInfo(currentLifter);
                logger.trace("done = {}", done);
                updateTime(masterData1);
                timeDisplay.setVisible(!done);
                timeDisplay.removeStyleName("intermission");
            } else {
                timeDisplay.setValue(""); //$NON-NLS-1$
            }
        }

        app.push();
    }

    @Override
    public void refresh() {
        logger.trace("refresh");
        display(platformName, masterData);
    }

    public boolean fillLifterInfo(Lifter lifter) {
        final int currentTry = 1 + (lifter.getAttemptsDone() >= 3 ? lifter.getCleanJerkAttemptsDone() : lifter
                .getSnatchAttemptsDone());
        boolean done = currentTry > 3;

        return done;
    }

    /**
     * @param groupData
     */
    private void updateTime(final SessionData groupData) {
        // we set the value to the time remaining for the current lifter as
        // computed by groupData
        int timeRemaining = groupData.getDisplayTime();
        logger.trace("updateTime {}", timeRemaining);
        pushTime(timeRemaining);
    }

    @Override
    public void finalWarning(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void forceTimeRemaining(int timeRemaining, CompetitionApplication originatingApp, InteractionNotificationReason reason) {
        pushTime(timeRemaining);
    }

    @Override
    public void initialWarning(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void noTimeLeft(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void normalTick(int timeRemaining) {
        pushTime(timeRemaining);
    }

    /**
     * @param timeRemaining
     */
    private void pushTime(int timeRemaining) {
        if (timeDisplay == null)
            return;

        // do not update if no visible change
        if (TimeFormatter.getSeconds(timeRemaining) == TimeFormatter.getSeconds(lastTimeRemaining)) {
            lastTimeRemaining = timeRemaining;
            return;
        } else {
            lastTimeRemaining = timeRemaining;
        }

        synchronized (app) {
            timeDisplay.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
        }
        app.push();
    }

    @Override
    public void pause(int timeRemaining, CompetitionApplication originatingApp, InteractionNotificationReason reason) {
    }

    @Override
    public void start(int timeRemaining) {
    }

    @Override
    public void stop(int timeRemaining, CompetitionApplication originatingApp, InteractionNotificationReason reason) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.components.ApplicationView#needsMenu()
     */
    @Override
    public boolean needsMenu() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public String getFragment() {
        return viewName + "/" + platformName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.components.ApplicationView#setParametersFromFragment(java.lang.String)
     */
    @Override
    public void setParametersFromFragment() {
        String frag = CompetitionApplication.getCurrent().getUriFragmentUtility().getFragment();
        String[] params = frag.split("/");
        if (params.length >= 1) {
            viewName = params[0];
        } else {
            throw new RuleViolationException("Error.ViewNameIsMissing");
        }
        if (params.length >= 2) {
            platformName = params[1];
        } else {
            platformName = CompetitionApplicationComponents.initPlatformName();
        }
    }

    @Override
    public void updateEvent(final DecisionEvent updateEvent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (app) {
                    switch (updateEvent.getType()) {
                    case DOWN:
                        logger.trace("received DOWN event");
                        showLights(updateEvent);
                        break;

                    case SHOW:
                        // if window is not up, show it.
                        shown = true;
                        logger.trace("received SHOW event {}", shown);
                        showLights(updateEvent);
                        break;

                    case RESET:
                        // we are done
                        logger.trace("received RESET event (hiding decision lights)");
                        hideLights(updateEvent);
                        shown = false;
                        break;

                    case WAITING:
                        logger.trace("ignoring WAITING event");
                        break;

                    case UPDATE:
                        logger.trace("received UPDATE event {}", shown);
                        // we need to show that referees have changed their mind.
                        if (shown) {
                            showLights(updateEvent);
                        }
                        break;

                    case BLOCK:
                        logger.trace("received BLOCK event {}", shown);
                        showLights(updateEvent);
                        break;
                    }
                }
                app.push();
            }
        }).start();
    }

    /**
     * Make sure decision lights are shown, and relay the event to the display component.
     * 
     * @param updateEvent
     */
    private void showLights(DecisionEvent updateEvent) {
        // create window
        if (popUp == null) {
            logger.debug("creating window");
            Window mainWindow = app.getMainWindow();
            decisionLights = new DecisionLightsWindow(false, false);
            popUp = new Window(platformName);
            popUp.addStyleName("decisionLightsWindow");
            popUp.setSizeFull();
            mainWindow.addWindow(popUp);
            logger.debug("addWindow {}", popUp);
            popUp.setContent(decisionLights);
        }
        popUp.setVisible(true);

        // relay the event
        logger.debug("relaying");
        decisionLights.updateEvent(updateEvent);

    }

    /**
     * Hide the decision lights.
     * 
     * @param updateEvent
     */
    private void hideLights(DecisionEvent updateEvent) {
        // relay the event (just in case)
        if (decisionLights != null) {
            decisionLights.updateEvent(updateEvent);
        }

        // close window
        if (popUp != null) {
            popUp.setVisible(false);
        }
    }

    /**
     * Resister to all necessary listening events
     */
    @Override
    public void registerAsListener() {
        Window mainWindow = app.getMainWindow();
        mainWindow.addListener((CloseListener) this);

        // listen to changes in the competition data
        logger.debug("listening to session data updates.");
        registerAsGroupDataListener(platformName, masterData);

        // listen to intermission timer events
        masterData.addBlackBoardListener(this);
        logger.debug("listening to intermission timer events.");

        // listen to decisions
        DecisionEventListener decisionListener = (DecisionEventListener) this;
        logger.debug("adding decision listener {}", decisionListener);
        masterData.getRefereeDecisionController().addListener(decisionListener);

        // listen to main timer events
        final CountdownTimer timer = masterData.getTimer();
        timer.setCountdownDisplay(this);
        addActions(mainWindow);
        logger.debug("added action handler");
    }

    /**
     * Undo what registerAsListener did.
     */
    @Override
    public void unregisterAsListener() {
        logger.debug("unregisterAsListener");
        Window mainWindow = app.getMainWindow();
        if (popUp != null) {
            mainWindow.removeWindow(popUp);
            popUp = null;
        }

        // stop listening to intermission timer events
        removeIntermissionTimer();
        masterData.removeBlackBoardListener(this);
        logger.debug("stopped listening to intermission timer events");

        mainWindow.removeListener((CloseListener) this);
        masterData.removeListener(updateEventListener);

        DecisionEventListener decisionListener = (DecisionEventListener) this;
        logger.debug("removing decision listener {}", decisionListener);
        masterData.getRefereeDecisionController().removeListener(decisionListener);
        final CountdownTimer timer = masterData.getTimer();
        if (timer.getCountdownDisplay() == this) {
            timer.setCountdownDisplay(null);
        }

        removeActions(mainWindow);
    }

    //
    // @Override
    // public DownloadStream handleURI(URL context, String relativeUri) {
    // registerAsListener();
    // return null;
    // }

    @Override
    public void windowClose(CloseEvent e) {
        unregisterAsListener();
    }

    public DecisionLightsWindow getDecisionLights() {
        return decisionLights;
    }

    public void setDecisionLights(DecisionLightsWindow decisionLights) {
        this.decisionLights = decisionLights;
    }

    @SuppressWarnings("serial")
    private abstract class ShortcutActionListener extends ShortcutAction implements Action.Listener {

        public ShortcutActionListener(String caption, int kc, int[] m) {
            super(caption, kc, m);
        }

        public ShortcutActionListener(String caption, int kc) {
            super(caption, kc, null);
        }

    }

    @SuppressWarnings("serial")
    private void addActions(Action.Notifier actionNotifier) {
        final IDecisionController refereeDecisionController = masterData.getRefereeDecisionController();
        action1ok = new ShortcutActionListener("1+", ShortcutAction.KeyCode.NUM1) {
            @Override
            public void handleAction(Object sender, Object target) {
                refereeDecisionController.decisionMade(0, true);
            }
        };
        action1fail = new ShortcutActionListener("1-", ShortcutAction.KeyCode.NUM2) {
            @Override
            public void handleAction(Object sender, Object target) {
                refereeDecisionController.decisionMade(0, false);
            }
        };
        action2ok = new ShortcutActionListener("2+", ShortcutAction.KeyCode.NUM3) {
            @Override
            public void handleAction(Object sender, Object target) {
                refereeDecisionController.decisionMade(1, true);
            }
        };
        action2fail = new ShortcutActionListener("2-", ShortcutAction.KeyCode.NUM4) {
            @Override
            public void handleAction(Object sender, Object target) {
                refereeDecisionController.decisionMade(1, false);
            }
        };
        action3ok = new ShortcutActionListener("3+", ShortcutAction.KeyCode.NUM5) {
            @Override
            public void handleAction(Object sender, Object target) {
                refereeDecisionController.decisionMade(2, true);
            }
        };
        action3fail = new ShortcutActionListener("3-", ShortcutAction.KeyCode.NUM6) {
            @Override
            public void handleAction(Object sender, Object target) {
                refereeDecisionController.decisionMade(2, false);
            }
        };
        
        actionNotifier.addAction(action1ok);
        actionNotifier.addAction(action1fail);
        actionNotifier.addAction(action2ok);
        actionNotifier.addAction(action2fail);
        actionNotifier.addAction(action3ok);
        actionNotifier.addAction(action3fail);
    }

    private void removeActions(Action.Notifier actionNotifier) {
        actionNotifier.removeAction(startAction);
        actionNotifier.removeAction(stopAction);
        actionNotifier.removeAction(oneMinuteAction);
        actionNotifier.removeAction(twoMinutesAction);
        actionNotifier.removeAction(action1ok);
        actionNotifier.removeAction(action1fail);
        actionNotifier.removeAction(action2ok);
        actionNotifier.removeAction(action2fail);
        actionNotifier.removeAction(action3ok);
        actionNotifier.removeAction(action3fail);
    }

    @Override
    public void intermissionTimerUpdate(IntermissionTimerEvent event) {
        Integer remainingMilliseconds = event.getRemainingMilliseconds();
        if (remainingMilliseconds != null && remainingMilliseconds > 0) {
            displayIntermissionTimer(remainingMilliseconds);
        } else {
            removeIntermissionTimer();
        }

    }

    /**
     * Hide the break timer
     */
    private void removeIntermissionTimer() {
        logger.debug("removing intermission timer");
        breakTimerShown = false;
        title.setVisible(false);
        // title.setHeight("0%");
        // timeDisplay.setHeight("100%");

        // force update
        lastTimeRemaining = 0;
        refresh();
    }

    /**
     * Display the break timer
     * 
     * @param remainingMilliseconds
     */
    private void displayIntermissionTimer(Integer remainingMilliseconds) {
        synchronized (app) {
            breakTimerShown = true;

            title.setVisible(true);
            title.addStyleName("title");
            title.setValue(Messages.getString("AttemptBoard.Pause", CompetitionApplication.getCurrentLocale()));
            // title.setHeight("15%");

            timeDisplay.setVisible(true);
            timeDisplay.addStyleName("intermission");
            timeDisplay.setValue(TimeFormatter.formatAsSeconds(remainingMilliseconds));
            // timeDisplay.setHeight("85%");
        }
        app.push();
    }

    @Override
    public void showInteractionNotification(CompetitionApplication originatingApp, InteractionNotificationReason reason) {
        // do nothing - notifications are meant for technical officials
    }

    @Override
    public boolean needsBlack() {
        return true;
    }

    private static int classCounter = 0; // per class
    private final int instanceId = classCounter++; // per instance

    @Override
    public String getInstanceId() {
        return Long.toString(instanceId);
    }

    @Override
    public String getLoggingId() {
        return viewName + getInstanceId();
    }

}
