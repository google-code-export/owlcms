/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.Platform;
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
import org.concordiainternational.competition.ui.components.Stylable;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.vaadin.weelayout.WeeLayout;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Show information about the current attempt.
 * 
 * @author jflamy
 * 
 */

public class AttemptBoardView extends WeeLayout implements
        ApplicationView,
        CountdownTimerListener,
        IntermissionTimerListener,
        Window.CloseListener,
        URIHandler,
        DecisionEventListener,
        Stylable
{

    public final static Logger logger = LoggerFactory.getLogger(AttemptBoardView.class);
    private static final long serialVersionUID = 1437157542240297372L;

    public String urlString;
    private String platformName;
    private SessionData masterData;
    final private transient CompetitionApplication app;

    private WeeLayout attemptBoardBox = new WeeLayout(Direction.VERTICAL);
    private WeeLayout topRowBox = new WeeLayout(Direction.HORIZONTAL);
    private WeeLayout bottomRowBox = new WeeLayout(Direction.HORIZONTAL);
    private WeeLayout topRightBox = new WeeLayout(Direction.VERTICAL);
    private WeeLayout bottomLeftBox = new WeeLayout(Direction.HORIZONTAL);
    private WeeLayout bottomRightBox = new WeeLayout(Direction.VERTICAL);

    private WeeLayout nameVBox = new WeeLayout(Direction.VERTICAL);
    private WeeLayout startVBox = new WeeLayout(Direction.VERTICAL);
    private WeeLayout teamVBox = new WeeLayout(Direction.VERTICAL);
    private WeeLayout timeVBox = new WeeLayout(Direction.VERTICAL);
    private WeeLayout weightVBox = new WeeLayout(Direction.VERTICAL);
    private WeeLayout attemptVBox = new WeeLayout(Direction.VERTICAL);
    private WeeLayout platesVBox = new WeeLayout(Direction.VERTICAL);

    private WeeLayout nameHBox = new WeeLayout(Direction.HORIZONTAL);
    private WeeLayout startHBox = new WeeLayout(Direction.HORIZONTAL);
    private WeeLayout teamHBox = new WeeLayout(Direction.HORIZONTAL);
    private WeeLayout timeHBox = new WeeLayout(Direction.HORIZONTAL);
    private WeeLayout weightHBox = new WeeLayout(Direction.HORIZONTAL);
    private WeeLayout attemptHBox = new WeeLayout(Direction.HORIZONTAL);
    private WeeLayout platesHBox = new WeeLayout(Direction.HORIZONTAL);

    private Label nameLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label clubLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label startLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label attemptLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label timeLabel = new Label();
    private Label weightLabel = new Label();

    private LoadImage plates;

    private UpdateEventListener updateListener;
    private DecisionLightsWindow decisionLights;
    protected boolean waitingForDecisionLightsReset;

    private boolean showTimer = true;

    public AttemptBoardView(boolean initFromFragment, String viewName, boolean publicFacing, String stylesheetName) {
        super(Direction.VERTICAL);
        logger.trace("entry {} {} {}", new Object[] { initFromFragment, viewName, stylesheetName });
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
            this.publicFacing = publicFacing;
            this.stylesheetName = stylesheetName;
        }
        MDC.put("view", getLoggingId());
        this.app = CompetitionApplication.getCurrent();

        boolean prevDisabledPush = app.getPusherDisabled();
        try {
            app.setPusherDisabled(true);
            if (platformName == null) {
                // get the default platform nameLabel
                platformName = CompetitionApplicationComponents.initPlatformName();
            } else if (app.getPlatform() == null) {
                app.setPlatformByName(platformName);
            }

            doCreate(false);

            // URI handler must remain, so is not part of the register/unRegister pair
            app.getMainWindow().addURIHandler(this);
            registerAsListener();
        } finally {
            app.setPusherDisabled(prevDisabledPush);
        }
        logger.trace("exit");
    }

    public void doCreate(boolean disablePush) {
        logger.trace("entry {}", disablePush);

        // disable push if requested by caller
        synchronized (app) {
            boolean prevDisabled = app.getPusherDisabled();
            try {
                app.setPusherDisabled(disablePush);
                masterData = app.getMasterData(platformName);
                refreshShowTimer();
                createDecisionLights();
                plates = new LoadImage(null);

                create();

                display(platformName, masterData);
            } finally {
                app.setPusherDisabled(prevDisabled);
            }
        }
        logger.trace("exit");
    }

    public void refreshShowTimer() {
        logger.trace("entry");
        // force use of current value.
        Platform curPlatform = masterData.getPlatform();
        if (curPlatform != null) {
            String platformName1 = curPlatform.getName();
            Platform refreshedPlatform = Platform.getByName(platformName1);
            showTimer = refreshedPlatform.getShowTimer();
        }
        logger.trace("exit");
    }

    /**
     * 
     */
    protected void createDecisionLights() {
        logger.trace("entry");
        decisionLights = new DecisionLightsWindow(false, publicFacing);
        decisionLights.setSizeFull();
        decisionLights.setMargin(false);
        logger.trace("exit");
    }

    private UpdateEventListener registerAsListener(final String platformName1, final SessionData masterData1) {
        logger.trace("entry");
        // locate the current group data for the platformName
        if (masterData1 != null) {
            logger.debug("{} listening to: {}", platformName1, masterData1); //$NON-NLS-1$	

            SessionData.UpdateEventListener listener = new SessionData.UpdateEventListener() {

                @Override
                public void updateEvent(UpdateEvent updateEvent) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            logger.trace("entry");
                            // logger.debug("request to display {}",AttemptBoardView.this);
                            if (!waitingForDecisionLightsReset) {
                                display(platformName1, masterData1);
                            }
                            logger.trace("exit");
                        }
                    }).start();
                }

            };
            masterData1.addListener(listener); //$NON-NLS-1$
            logger.trace("exit");
            return listener;

        } else {
            logger.debug("{} NOT listening to:  = {}", platformName1, masterData1); //$NON-NLS-1$
            logger.trace("exit");
            return null;
        }

    }

    /**
     * @param platformName
     * @throws MalformedURLException
     */
    private void create() {
        logger.trace("entry");
        this.setSizeFull();
        this.setMargin(true);

        createAlignedLabel(nameLabel, nameHBox, nameVBox, Alignment.MIDDLE_LEFT, Alignment.MIDDLE_CENTER, "100%", "80%", "name");
        nameLabel.addStyleName("bolded");
        createAlignedLabel(startLabel, startHBox, startVBox, Alignment.MIDDLE_CENTER, Alignment.MIDDLE_CENTER, "50%", "100%", "start");
        createAlignedLabel(clubLabel, teamHBox, teamVBox, Alignment.MIDDLE_RIGHT, Alignment.MIDDLE_RIGHT, "50%", "100%", "text");
        createAlignedLabel(attemptLabel, attemptHBox, attemptVBox, Alignment.MIDDLE_RIGHT, Alignment.MIDDLE_CENTER, "50%", "100%", "text");
        createAlignedLabel(weightLabel, weightHBox, weightVBox, Alignment.MIDDLE_RIGHT, Alignment.MIDDLE_CENTER, "50%", "100%",
                "weightLabel");
        createAlignedLabel(timeLabel, timeHBox, timeVBox, Alignment.MIDDLE_LEFT, Alignment.MIDDLE_CENTER, "100%", "66%", "largeCountdown");

        createAlignmentContainer(plates, platesHBox, platesVBox, Alignment.MIDDLE_CENTER, Alignment.MIDDLE_CENTER, "100%", "33%");

        topRightBox.setWidth("20%");
        topRightBox.setHeight("100%");
        topRightBox.addComponent(startVBox);
        topRightBox.addComponent(teamVBox);

        bottomLeftBox.setWidth("60%");
        bottomLeftBox.setHeight("100%");

        bottomRightBox.setWidth("40%");
        bottomRightBox.setHeight("100%");
        bottomRightBox.addComponent(attemptVBox);
        bottomRightBox.addComponent(weightVBox);

        topRowBox.setSmartRelativeSizes(true);
        topRowBox.addComponent(nameVBox);
        topRowBox.addComponent(topRightBox);
        topRowBox.setHeight("50%");
        topRowBox.setWidth("100%");

        bottomRowBox.setSmartRelativeSizes(true);
        bottomRowBox.addComponent(bottomLeftBox);
        bottomRowBox.addComponent(bottomRightBox);
        bottomRowBox.setHeight("50%");
        bottomRowBox.setWidth("100%");

        attemptBoardBox.setSizeFull();
        attemptBoardBox.setSmartRelativeSizes(true);
        attemptBoardBox.addStyleName("newAttemptBoard");
        attemptBoardBox.addStyleName(stylesheetName);
        attemptBoardBox.addComponent(topRowBox);
        attemptBoardBox.addComponent(bottomRowBox);

        this.addComponent(attemptBoardBox);
        logger.trace("exit");
    }

    private void createAlignedLabel(Label label, WeeLayout hBox, WeeLayout vBox, Alignment hAlignment, Alignment vAlignment, String height,
            String width, String styleName) {
        label.setSizeUndefined();
        label.addStyleName(styleName);
        createAlignmentContainer(label, hBox, vBox, hAlignment, vAlignment, height, width);
    }

    private void createAlignmentContainer(Component component, WeeLayout hBox, WeeLayout vBox, Alignment hAlignment, Alignment vAlignment,
            String height, String width) {
        vBox.addComponent(hBox);
        vBox.setComponentAlignment(hBox, hAlignment);
        vBox.setHeight(height);
        vBox.setWidth(width);
        hBox.setHeight("100%");
        hBox.addComponent(component);
        hBox.setComponentAlignment(component, vAlignment);
    }

    /**
     * @param platformName1
     * @param masterData1
     * @throws RuntimeException
     */
    private void display(final String platformName1, final SessionData masterData1) throws RuntimeException {
        logger.trace("entry {} {}", platformName1, masterData1);
        if (intermissionTimerShown) {
            logger.trace("exit because intermissionTimerShown");
            return;
        }
        synchronized (app) {
            final Lifter currentLifter = masterData1.getCurrentLifter();
            if (currentLifter != null) {
                logger.debug("lifter {}", currentLifter);
                boolean done = fillLifterInfo(currentLifter);
                updateTime(masterData1);
                showDecisionLights(false);
                timeLabel.removeStyleName("intermission");
                timeLabel.addStyleName("largeCountdown");
                timeLabel.setVisible(!done);
                startLabel.setVisible(!done);
            } else {
                logger.info("lifter null");
                hideAttemptBoard();
            }
        }
        app.push();
        logger.trace("exit pusherDisabled={}", app.getPusherDisabled());
    }

    /**
     * 
     */
    protected void hideAttemptBoard() {
        logger.trace("entry");
        nameLabel.setValue(getWaitingMessage()); //$NON-NLS-1$
        showDecisionLights(false);
        timeLabel.setVisible(false);
        clubLabel.setValue("");
        attemptLabel.setValue("");
        weightLabel.setValue("");
        startLabel.setValue("");
        startLabel.setVisible(false);
        if (plates != null)
            plates.setVisible(false);
        logger.trace("exit");
    }

    /**
     * @return message used when Announcer has not selected a group
     */
    private String getWaitingMessage() {
        String message = ""; // Messages.getString("ResultFrame.Waiting", CompetitionApplication.getCurrentLocale());
        // List<Competition> competitions = Competition.getAll();
        // if (competitions.size() > 0) {
        // message = competitions.get(0).getCompetitionName();
        // }
        return message;
    }

    @Override
    public void refresh() {
        logger.trace("entry");
        doCreate(false);
        logger.trace("exit");
    }

    public boolean fillLifterInfo(Lifter lifter) {
        logger.trace("entry");
        final Locale locale = CompetitionApplication.getCurrentLocale();
        final int currentTry = 1 + (lifter.getAttemptsDone() >= 3 ? lifter.getCleanJerkAttemptsDone() : lifter
                .getSnatchAttemptsDone());
        boolean done = currentTry > 3;

        synchronized (app) {
            displayName(lifter, locale, done);
            displayAttemptNumber(lifter, locale, currentTry, done);
            displayRequestedWeight(lifter, locale, done);
        }
        app.push();
        logger.trace("exit");
        return done;
    }

    /**
     * @param lifter
     * @param alwaysShowName
     * @param sb
     * @param done
     */
    private void displayName(Lifter lifter, final Locale locale, boolean done) {
        logger.trace("entry");
        // display lifter name and affiliation
        if (!done) {
            final String lastName = lifter.getLastName();
            final String firstName = lifter.getFirstName();
            final String club = lifter.getClub();
            final Integer startNumber = lifter.getStartNumber();

            nameLabel.setValue(formatName(lastName, firstName));
            clubLabel.setValue(club);
            if (startNumber != null && startNumber > 0) {
                startLabel.setStyleName("start");
                startLabel.setValue(MessageFormat.format(
                        Messages.getString("AttemptBoard.startNumberFormat", locale), startNumber.toString()));
            } else {
                startLabel.setStyleName("text");
                startLabel.setValue("");
            }
        } else {
            nameLabel.setValue(MessageFormat.format(
                    Messages.getString("AttemptBoard.Done", locale), masterData.getCurrentSession().getName())); //$NON-NLS-1$
            clubLabel.setValue("");
            startLabel.setStyleName("text");
            startLabel.setValue("");
        }
        logger.trace("exit");
    }

    public String formatName(final String lastName, final String firstName) {
        int hyphenIndex = lastName.indexOf('-');
        String nLastName = lastName;
        if (hyphenIndex > 0) {
            if (lastName.length() >= 10) {
                String[] parts = lastName.split("-");
                nLastName = parts[0] + "-<br/>" + parts[1];
            }
        }
        return nLastName.toUpperCase() + "<br/>" + firstName;
    }

    private void showDecisionLights(boolean doShow) {
        logger.trace("entry {}", doShow);
        // LoggerUtils.logException(logger, new Exception("stack trace"));

        if (doShow) {
            bottomLeftBox.removeComponent(timeVBox);
            bottomLeftBox.removeComponent(platesVBox);
            bottomLeftBox.addComponent(decisionLights);
            decisionLights.refresh();
        } else {
            bottomLeftBox.removeComponent(decisionLights);
            bottomLeftBox.addComponent(timeVBox);
            bottomLeftBox.addComponent(platesVBox);
            plates.computeImageArea(masterData, masterData.getPlatform(), false);
        }
        logger.trace("exit");
    }

    /**
     * @param lifter
     * @param sb
     * @param locale
     * @param currentTry
     * @param done
     */
    private void displayAttemptNumber(Lifter lifter, final Locale locale, final int currentTry, boolean done) {
        // display current attemptLabel number
        if (!done) {
            final String lift = lifter.getAttemptsDone() >= 3 ? Messages.getString("Common.shortCleanJerk", locale) //$NON-NLS-1$
                    : Messages.getString("Common.shortSnatch", locale);//$NON-NLS-1$
            String tryInfo = MessageFormat.format(Messages.getString("ResultFrame.tryNumber", locale), //$NON-NLS-1$
                    currentTry, lift);
            attemptLabel.setValue(tryInfo);
        } else {
            attemptLabel.setValue("");
        }
        //grid.addComponent(attemptLabel, 3, 3, 3, 3); //$NON-NLS-1$
    }

    /**
     * @param lifter
     * @param sb
     * @param locale
     * @param done
     * @return
     */
    private void displayRequestedWeight(Lifter lifter, final Locale locale, boolean done) {
        // display requested weightLabel
        if (!done) {
            weightLabel.setValue(lifter.getNextAttemptRequestedWeight() + Messages.getString("Common.kg", locale)); //$NON-NLS-1$
        } else {
            weightLabel.setValue(""); //$NON-NLS-1$
        }
    }

    /**
     * @param groupData
     */
    private void updateTime(final SessionData groupData) {
        logger.trace("entry");
        // we set the value to the time remaining for the current lifter as
        // computed by groupData
        int timeRemaining = groupData.getDisplayTime();
        final CountdownTimer timer = groupData.getTimer();
        if (!intermissionTimerShown) {
            showTimeRemaining(timeRemaining);
        }
        timer.addListener(this);
        logger.trace("exit");
    }

    @Override
    public void finalWarning(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void forceTimeRemaining(int timeRemaining, CompetitionApplication originatingApp, InteractionNotificationReason reason) {
        if (!intermissionTimerShown) {
            showTimeRemaining(timeRemaining);
        }
    }

    private void showTimeRemaining(int timeRemaining) {
        logger.trace("entry");
        synchronized (app) {
            if (showTimer) {
                timeLabel.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
            } else {
                timeLabel.setValue("");
            }
        }
        app.push();
        logger.trace("exit");
    }

    @Override
    public void initialWarning(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void noTimeLeft(int timeRemaining) {
        normalTick(timeRemaining);
    }

    int previousTimeRemaining = 0;
    private String viewName;
    protected boolean shown;
    private String stylesheetName;
    private boolean intermissionTimerShown = false;
    private boolean publicFacing;
    private boolean registered;

    @Override
    public void normalTick(int timeRemaining) {
        if (nameLabel == null)
            return;
        if (TimeFormatter.getSeconds(previousTimeRemaining) == TimeFormatter.getSeconds(timeRemaining)) {
            previousTimeRemaining = timeRemaining;
            return;
        } else {
            previousTimeRemaining = timeRemaining;
        }

        showTimeRemaining(timeRemaining);
    }

    @Override
    public void pause(int timeRemaining, CompetitionApplication originatingApp, InteractionNotificationReason reason) {
        logger.trace("entry");
        showTimeRemaining(timeRemaining);
        logger.trace("entry");
    }

    @Override
    public void start(int timeRemaining) {
        showTimeRemaining(timeRemaining);
    }

    @Override
    public void stop(int timeRemaining, CompetitionApplication originatingApp, InteractionNotificationReason reason) {
        showTimeRemaining(timeRemaining);
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
        return viewName + "/" + platformName + "/" + (publicFacing == true ? "public" : "lifter")
                + (stylesheetName != null ? "/" + stylesheetName : "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.components.ApplicationView#setParametersFromFragment(java.lang.String)
     */
    @Override
    public void setParametersFromFragment() {
        logger.trace("entry");
        String frag = CompetitionApplication.getCurrent().getUriFragmentUtility().getFragment();
        String[] params = frag.split("/");
        if (params.length >= 1) {
            viewName = params[0];
        } else {
            throw new RuleViolationException("Error.ViewNameIsMissing");
        }

        if (params.length >= 2) {
            platformName = params[1];
        }
        if (params.length >= 3) {
            String publicFacingString = params[2];
            publicFacing = "public".equals(publicFacingString);
            logger.trace("setting publicFacing to {}", publicFacing);
        }
        if (params.length >= 4) {
            setStylesheetName(params[3]);
            logger.trace("setting stylesheetName to {}", stylesheetName);
        }
        logger.trace("exit");
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
        logger.trace("entry");
        intermissionTimerShown = false;
        refresh();
        logger.trace("exit");
    }

    /**
     * Display the break timer
     * 
     * @param remainingMilliseconds
     */
    private void displayIntermissionTimer(Integer remainingMilliseconds) {
        logger.trace("entry");
        synchronized (app) {
            if (!intermissionTimerShown) {
                hideAttemptBoard();
            }
            intermissionTimerShown = true;
            nameLabel.setValue(Messages.getString("AttemptBoard.Pause", CompetitionApplication.getCurrentLocale()));
            timeLabel.setVisible(true);
            timeLabel.addStyleName("intermission");
            timeLabel.setValue(TimeFormatter.formatAsSeconds(remainingMilliseconds));
        }
        app.push();
        logger.trace("exit");
    }

    /*
     * Unregister listeners when window is closed.
     * 
     * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
     */
    @Override
    public void windowClose(CloseEvent e) {
        unregisterAsListener();
    }

    @Override
    public DownloadStream handleURI(URL context, String relativeUri) {
        logger.debug("registering handlers for {} {}", this, relativeUri);
        registerAsListener();
        return null;
    }

    /**
     * Process a decision regarding the current lifter. Make sure that the nameLabel of the lifter does not change until after the decision
     * has been shown.
     * 
     * @see org.concordiainternational.competition.decision.DecisionEventListener#updateEvent(org.concordiainternational.competition.decision.DecisionEvent)
     */
    @Override
    public void updateEvent(final DecisionEvent updateEvent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (app) {
                    switch (updateEvent.getType()) {
                    case DOWN:
                        waitingForDecisionLightsReset = true;
                        decisionLights.setVisible(false);
                        break;
                    case SHOW:
                        // if window is not up, show it.
                        waitingForDecisionLightsReset = true;
                        shown = true;
                        showDecisionLights(true);
                        decisionLights.setVisible(true);
                        break;
                    case RESET:
                        // we are done
                        waitingForDecisionLightsReset = false;
                        if (shown) {
                            decisionLights.setVisible(false);
                            showDecisionLights(false);
                            shown = false;
                        }
                        display(platformName, masterData);
                        break;
                    case WAITING:
                        waitingForDecisionLightsReset = true;
                        decisionLights.setVisible(false);
                        break;
                    case UPDATE:
                        // show change only if the lights are already on.
                        waitingForDecisionLightsReset = true;
                        if (shown) {
                            showDecisionLights(true);
                            decisionLights.setVisible(true);
                        }
                        break;
                    case BLOCK:
                        waitingForDecisionLightsReset = true;
                        if (shown) {
                            showDecisionLights(true);
                            decisionLights.setVisible(true);
                        }
                        break;
                    }
                }
            }
        }).start();
    }

    @Override
    public void registerAsListener() {
        logger.trace("entry");
        if (registered) {
            logger.trace("exit registered already");
            return;
        }

        // listen to changes in the competition data
        logger.debug("listening to session data updates.");
        updateListener = registerAsListener(platformName, masterData);

        // listen to intermission timer events
        masterData.addBlackBoardListener(this);
        logger.debug("listening to intermission timer events.");

        // listen to decisions
        IDecisionController decisionController = masterData.getRefereeDecisionController();
        if (decisionController != null) {
            if (decisionLights != null)
                decisionController.addListener(decisionLights);
            decisionController.addListener(this);
        }

        // listen to close events
        app.getMainWindow().addListener((CloseListener) this);

        // update whether timer is shown
        refreshShowTimer();

        registered = true;
        logger.trace("exit");
    }

    /**
     * Undo what registerAsListener did.
     */
    @Override
    public void unregisterAsListener() {
        logger.trace("entry");
        if (!registered)
            return;
        // stop listening to changes in the competition data
        if (updateListener != null) {
            masterData.removeListener(updateListener);
            logger.debug("stopped listening to UpdateEvents");
        }

        // stop listening to intermission timer events
        intermissionTimerShown = false;
        masterData.removeBlackBoardListener(this);
        logger.debug("stopped listening to intermission timer events");

        // stop listening to decisions
        IDecisionController decisionController = masterData.getRefereeDecisionController();
        if (decisionController != null) {
            decisionController.removeListener(decisionLights);
            decisionController.removeListener(this);
        }

        // stop listening to close events
        app.getMainWindow().removeListener((CloseListener) this);

        registered = false;
        logger.trace("exit");
    }

    @Override
    public void setStylesheetName(String stylesheetName) {
        logger.trace("entry {}", stylesheetName);
        this.stylesheetName = stylesheetName;
        logger.trace("exit");
    }

    @Override
    public String getStylesheetName() {
        return this.stylesheetName;
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
