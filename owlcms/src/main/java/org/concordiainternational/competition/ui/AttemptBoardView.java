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

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Show information about the current attempt.
 * 
 * @author jflamy
 * 
 */

public class AttemptBoardView extends VerticalLayout implements
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
    private static final int TEAM_COLUMN = 4;
    private static final int NAME_COLUMN = 0;
    private static final int TIME_COLUMN = 0;
    private static final int PLATES_COLUMN = 2;
    private static final int WEIGHT_COLUMN = 3;
    private static final int PLATES_ROW = 3;
    private static final int DECISION_ROW = PLATES_ROW - 1;

    public String urlString;
    private String platformName;
    private SessionData masterData;
    private GridLayout grid;
    final private transient CompetitionApplication app;

    private Label nameLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label firstNameLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label clubLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label startLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label attemptLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label timeDisplayLabel = new Label();
    private Label weightLabel = new Label();
    private LoadImage plates;

    private UpdateEventListener updateListener;
    private DecisionLightsWindow decisionLights;
    protected boolean waitingForDecisionLightsReset;

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
    private boolean showTimer = true;

    public AttemptBoardView(boolean initFromFragment, String viewName, boolean publicFacing) {

        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
            this.publicFacing = publicFacing;
        }
        addStyleName("blendWithBackground");
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

            create(app);
            masterData = app.getMasterData(platformName);
            refreshShowTimer();

            // we cannot call push() at this point
            synchronized (app) {
                boolean prevDisabled = app.getPusherDisabled();
                try {
                    app.setPusherDisabled(true);
                    createDecisionLights();
                    plates = new LoadImage(null);
                    display(platformName, masterData);
                } finally {
                    app.setPusherDisabled(prevDisabled);
                }
                logger.debug("browser panel: push disabled = {}", app.getPusherDisabled());
            }

            // URI handler must remain, so is not part of the register/unRegister pair
            app.getMainWindow().addURIHandler(this);
            registerAsListener();
        } finally {
            app.setPusherDisabled(prevDisabledPush);
        }
    }

    public void refreshShowTimer() {
        // force use of current value.
        Platform curPlatform = masterData.getPlatform();
        if (curPlatform != null) {
            String platformName1 = curPlatform.getName();
            Platform refreshedPlatform = Platform.getByName(platformName1);
            showTimer = refreshedPlatform.getShowTimer();
        }
    }

    /**
     * 
     */
    protected void createDecisionLights() {
        decisionLights = new DecisionLightsWindow(false, publicFacing);
        decisionLights.setSizeFull();
        decisionLights.setMargin(false);
    }

    private UpdateEventListener registerAsListener(final String platformName1, final SessionData masterData1) {
        // locate the current group data for the platformName
        if (masterData1 != null) {
            logger.debug(urlString + "{} listening to: {}", platformName1, masterData1); //$NON-NLS-1$	
            //masterData.addListener(SessionData.UpdateEvent.class, this, "update"); //$NON-NLS-1$

            SessionData.UpdateEventListener listener = new SessionData.UpdateEventListener() {

                @Override
                public void updateEvent(UpdateEvent updateEvent) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // logger.debug("request to display {}",AttemptBoardView.this);
                            if (!waitingForDecisionLightsReset) {
                                display(platformName1, masterData1);
                            }
                        }
                    }).start();
                }

            };
            masterData1.addListener(listener); //$NON-NLS-1$		
            return listener;

        } else {
            logger.debug(urlString + "{} NOT listening to:  = {}", platformName1, masterData1); //$NON-NLS-1$	
            return null;
        }
    }

    /**
     * @param app1
     * @param platformName
     * @throws MalformedURLException
     */
    private void create(UserActions app1) {
        this.setSizeFull();
        this.setMargin(true);

        grid = new GridLayout(6, 5);
        grid.setSizeFull();

        grid.setMargin(true);
        grid.addStyleName("newAttemptBoard");
        grid.addStyleName(getStylesheetName());
        grid.setSpacing(false);

        // grid.setColumnExpandRatio(0, 0.0F);
        // grid.setColumnExpandRatio(1, 0.0F);
        // grid.setColumnExpandRatio(2, 0.0F);
        // grid.setColumnExpandRatio(3, 0.0F);
        // grid.setColumnExpandRatio(4, 0.0F);
        grid.setRowExpandRatio(0, 50.0F);
        grid.setRowExpandRatio(1, 0.0F);
        grid.setRowExpandRatio(2, 50.0F);
        grid.setRowExpandRatio(3, 0.0F);
        grid.setRowExpandRatio(4, 50.0F);

        Label filler = new Label("&nbsp;", Label.CONTENT_XHTML);
        filler.setWidth("10px");
        grid.addComponent(filler, TEAM_COLUMN + 1, 0, TEAM_COLUMN + 1, 0);
        // we do not add the time display, plate display
        // and decision display -- they react to timekeeping

        // last name
        grid.addComponent(nameLabel, NAME_COLUMN, 0, TEAM_COLUMN - 1, 1);
        nameLabel.setSizeUndefined();
        nameLabel.addStyleName("name");
        nameLabel.addStyleName("bolded");
        grid.setComponentAlignment(nameLabel, Alignment.MIDDLE_LEFT);

        // first name
        // grid.addComponent(firstNameLabel, NAME_COLUMN, 1, WEIGHT_COLUMN-1, 1);
        // firstNameLabel.setSizeUndefined();
        // firstNameLabel.addStyleName("name");
        // grid.setComponentAlignment(firstNameLabel, Alignment.MIDDLE_LEFT);

        // start number
        grid.addComponent(startLabel, TEAM_COLUMN, 0, TEAM_COLUMN, 0);
        startLabel.setSizeUndefined();
        startLabel.addStyleName("start");
        grid.setComponentAlignment(startLabel, Alignment.MIDDLE_RIGHT);

        // team
        grid.addComponent(clubLabel, TEAM_COLUMN, 1, TEAM_COLUMN, 1);
        clubLabel.setSizeUndefined();
        clubLabel.addStyleName("text");
        grid.setComponentAlignment(clubLabel, Alignment.MIDDLE_RIGHT);

        // requested weight
        grid.addComponent(weightLabel, WEIGHT_COLUMN, 4, TEAM_COLUMN, 4);
        weightLabel.setSizeUndefined();
        weightLabel.addStyleName("weightLabel");
        grid.setComponentAlignment(weightLabel, Alignment.MIDDLE_RIGHT);

        // attempt
        grid.addComponent(attemptLabel, WEIGHT_COLUMN, 3, TEAM_COLUMN, 3);
        attemptLabel.setSizeUndefined();
        attemptLabel.addStyleName("text");
        grid.setComponentAlignment(attemptLabel, Alignment.MIDDLE_RIGHT);

        timeDisplayLabel.setSizeUndefined();
        timeDisplayLabel.addStyleName("largeCountdown");

        this.addComponent(grid);
        this.setExpandRatio(grid, 100.0F);

    }

    /**
     * @param platformName1
     * @param masterData1
     * @throws RuntimeException
     */
    private void display(final String platformName1, final SessionData masterData1) throws RuntimeException {
        if (intermissionTimerShown) {
            return;
        }
        synchronized (app) {
            final Lifter currentLifter = masterData1.getCurrentLifter();
            if (currentLifter != null) {
                boolean done = fillLifterInfo(currentLifter);
                updateTime(masterData1);
                showDecisionLights(false);
                timeDisplayLabel.removeStyleName("intermission");
                timeDisplayLabel.setSizeUndefined();
                timeDisplayLabel.setVisible(!done);
                startLabel.setVisible(!done);
            } else {
                logger.debug("lifter null");
                hideAttemptBoard();
            }

        }
        logger.debug("prior to display push disabled={}", app.getPusherDisabled());

        app.push();
    }

    /**
     * 
     */
    protected void hideAttemptBoard() {
        nameLabel.setValue(getWaitingMessage()); //$NON-NLS-1$
        showDecisionLights(false);
        timeDisplayLabel.setSizeUndefined();
        timeDisplayLabel.setVisible(false);
        firstNameLabel.setValue("");
        clubLabel.setValue("");
        attemptLabel.setValue("");
        weightLabel.setValue("");
        startLabel.setValue("");
        startLabel.setVisible(false);
        plates.setVisible(false);
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
        display(platformName, masterData);
    }

    public boolean fillLifterInfo(Lifter lifter) {
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
        return done;
    }

    /**
     * @param lifter
     * @param alwaysShowName
     * @param sb
     * @param done
     */
    private void displayName(Lifter lifter, final Locale locale, boolean done) {
        // display lifter nameLabel and affiliation
        if (!done) {
            final String lastName = lifter.getLastName();
            final String firstName = lifter.getFirstName();
            final String club = lifter.getClub();
            final Integer startNumber = lifter.getStartNumber();

            nameLabel.setValue(formatName(lastName, firstName));
            firstNameLabel.setValue(firstName);
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
            firstNameLabel.setValue("");
            clubLabel.setValue("");
            startLabel.setStyleName("text");
            startLabel.setValue("");
        }

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

    private void showDecisionLights(boolean decisionLightsVisible) {
        // logger.debug("showDecisionLights {}",decisionLightsVisible);
        // remove everything
        grid.removeComponent(timeDisplayLabel);
        grid.removeComponent(decisionLights);
        grid.removeComponent(plates);

        if (decisionLightsVisible) {
            grid.addComponent(decisionLights, TIME_COLUMN, DECISION_ROW, TIME_COLUMN + 2, DECISION_ROW + 2);
            decisionLights.setSizeFull();
            decisionLights.setMargin(true);
            grid.setComponentAlignment(decisionLights, Alignment.TOP_LEFT);
        } else {
            grid.addComponent(timeDisplayLabel, TIME_COLUMN, PLATES_ROW + 1, TIME_COLUMN + 1, PLATES_ROW + 1);
            grid.setComponentAlignment(timeDisplayLabel, Alignment.MIDDLE_LEFT);
            grid.addComponent(plates, PLATES_COLUMN, PLATES_ROW, PLATES_COLUMN, PLATES_ROW + 1);
            plates.computeImageArea(masterData, masterData.getPlatform());

            grid.setComponentAlignment(timeDisplayLabel, Alignment.MIDDLE_LEFT);
            grid.setComponentAlignment(plates, Alignment.BOTTOM_CENTER);
            timeDisplayLabel.setVisible(true);
            plates.setVisible(true);
        }
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
            //appendDiv(sb, lifter.getNextAttemptRequestedWeight()+Messages.getString("Common.kg",locale)); //$NON-NLS-1$
            final String lift = lifter.getAttemptsDone() >= 3 ? Messages.getString("Common.shortCleanJerk", locale) //$NON-NLS-1$
                    : Messages.getString("Common.shortSnatch", locale);//$NON-NLS-1$
            String tryInfo = MessageFormat.format(Messages.getString("ResultFrame.tryNumber", locale), //$NON-NLS-1$
                    currentTry, lift);

            attemptLabel.setValue(tryInfo
                    // .replace(" ","<br>")
                    );
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
        //grid.addComponent(weightLabel, 3, 2, 3, 2); //$NON-NLS-1$
    }

    /**
     * @param groupData
     */
    private void updateTime(final SessionData groupData) {
        // we set the value to the time remaining for the current lifter as
        // computed by groupData
        int timeRemaining = groupData.getDisplayTime();
        final CountdownTimer timer = groupData.getTimer();
        if (!intermissionTimerShown) {
            showTimeRemaining(timeRemaining);
        }
        timer.addListener(this);
    }

    @Override
    public void finalWarning(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void forceTimeRemaining(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
        if (!intermissionTimerShown) {
            showTimeRemaining(timeRemaining);
        }
    }

    private void showTimeRemaining(int timeRemaining) {
        synchronized (app) {
            if (showTimer) {
                timeDisplayLabel.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
            } else {
                timeDisplayLabel.setValue("");
            }
        }
        app.push();
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
    private boolean intermissionTimerShown;
    private boolean publicFacing;

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
    public void pause(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
        showTimeRemaining(timeRemaining);
    }

    @Override
    public void start(int timeRemaining) {
        showTimeRemaining(timeRemaining);
    }

    @Override
    public void stop(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
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
        intermissionTimerShown = false;
        refresh();
    }

    /**
     * Display the break timer
     * 
     * @param remainingMilliseconds
     */
    private void displayIntermissionTimer(Integer remainingMilliseconds) {
        synchronized (app) {
            if (!intermissionTimerShown) {
                hideAttemptBoard();
            }
            intermissionTimerShown = true;
            nameLabel.setValue(Messages.getString("AttemptBoard.Pause", CompetitionApplication.getCurrentLocale()));
            timeDisplayLabel.setVisible(true);
            timeDisplayLabel.addStyleName("intermission");
            timeDisplayLabel.setValue(TimeFormatter.formatAsSeconds(remainingMilliseconds));
        }
        app.push();
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
        logger.debug("re-registering handlers for {} {}", this, relativeUri);
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
        // listen to changes in the competition data
        logger.debug("listening to session data updates.");
        updateListener = registerAsListener(platformName, masterData);

        // listen to intermission timer events
        masterData.addBlackBoardListener(this);
        logger.debug("listening to intermission timer events.");

        // listen to decisions
        IDecisionController decisionController = masterData.getRefereeDecisionController();
        if (decisionController != null) {
            decisionController.addListener(decisionLights);
            decisionController.addListener(this);
        }

        // listen to close events
        app.getMainWindow().addListener((CloseListener) this);

        // listen to keyboard
        addActions(app.getMainWindow());

        // update whether timer is shown
        refreshShowTimer();
    }

    /**
     * Undo what registerAsListener did.
     */
    @Override
    public void unregisterAsListener() {
        // stop listening to changes in the competition data
        if (updateListener != null) {
            masterData.removeListener(updateListener);
            logger.debug("stopped listening to UpdateEvents");
        }

        // stop listening to intermission timer events
        removeIntermissionTimer();
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

        // stop listening to keyboard
        removeActions(app.getMainWindow());
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
        startAction = new ShortcutActionListener("start", ShortcutAction.KeyCode.G) {
            @Override
            public void handleAction(Object sender, Object target) {
                masterData.startUpdateModel();
            }
        };
        stopAction = new ShortcutActionListener("stop", ShortcutAction.KeyCode.P) {
            @Override
            public void handleAction(Object sender, Object target) {
                masterData.stopUpdateModel();
            }
        };
        oneMinuteAction = new ShortcutActionListener("1 minute", ShortcutAction.KeyCode.O) {
            @Override
            public void handleAction(Object sender, Object target) {
                masterData.oneMinuteUpdateModel();
            }
        };
        twoMinutesAction = new ShortcutActionListener("2 minutes", ShortcutAction.KeyCode.T) {
            @Override
            public void handleAction(Object sender, Object target) {
                masterData.twoMinuteUpdateModel();
            }
        };

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

        actionNotifier.addAction(startAction);
        actionNotifier.addAction(stopAction);
        actionNotifier.addAction(oneMinuteAction);
        actionNotifier.addAction(twoMinutesAction);
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
    public void setStylesheetName(String stylesheetName) {
        this.stylesheetName = stylesheetName;
    }

    @Override
    public String getStylesheetName() {
        return this.stylesheetName;
    }

}
