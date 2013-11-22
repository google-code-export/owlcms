/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.components;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.decision.IDecisionController;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent;
import org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent.MessageDisplayListener;
import org.concordiainternational.competition.publicAddress.PublicAddressOverlay;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.timer.CountdownTimerListener;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.CompetitionApplicationComponents;
import org.concordiainternational.competition.ui.InteractionNotificationReason;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.SessionData.UpdateEvent;
import org.concordiainternational.competition.ui.SessionData.UpdateEventListener;
import org.concordiainternational.competition.ui.UserActions;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Show an WebPage underneath a banner.
 * 
 * @author jflamy
 * 
 */

public class ResultFrame extends VerticalLayout implements
        ApplicationView,
        CountdownTimerListener,
        MessageDisplayListener,
        Window.CloseListener,
        URIHandler,
        DecisionEventListener,
        Stylable
{

    private static final String ATTEMPT_WIDTH = "6em";
    public final static Logger logger = LoggerFactory.getLogger(ResultFrame.class);
    private static Logger listenerLogger = LoggerFactory.getLogger("listeners." + ResultFrame.class.getSimpleName()); //$NON-NLS-1$

    private static final long serialVersionUID = 1437157542240297372L;
    private Embedded iframe;
    public String urlString;
    private String platformName;
    private SessionData masterData;
    private CustomLayout top;
    private CompetitionApplication app;
    private Label name = new Label("<br>", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label attempt = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label timeDisplay = new Label();
    private Label weight = new Label();
    private String appUrlString;
    private UpdateEventListener updateListener;
    private DecisionLightsWindow decisionLights;
    protected boolean waitingForDecisionLightsReset;
    private boolean showDecisions = false;
    private boolean showTimer = false;

    public ResultFrame(boolean initFromFragment, String viewName, String urlString, String stylesheetName) throws MalformedURLException {
        this.viewName = viewName;
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());
 
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            setStylesheetName(stylesheetName);
        }

        this.app = CompetitionApplication.getCurrent();

        boolean prevDisabledPush = app.getPusherDisabled();
        try {
            app.setPusherDisabled(true);
            if (platformName == null) {
                // get the default platform name
                platformName = CompetitionApplicationComponents.initPlatformName();
            } else if (app.getPlatform() == null) {
                app.setPlatformByName(platformName);
            }

            this.urlString = urlString;
            getAppUrlString();

            create(app);
            masterData = app.getMasterData(platformName);
            Platform platform = masterData.getPlatform();
            LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.currentGroup, masterData.getCurrentSession().getName());
            
            showDecisions = Boolean.TRUE.equals(platform.getShowDecisionLights());
            showTimer = Boolean.TRUE.equals(platform.getShowTimer());

            // we cannot call push() at this point
            synchronized (app) {
                boolean prevDisabled = app.getPusherDisabled();
                try {
                    app.setPusherDisabled(true);
                    decisionLights = new DecisionLightsWindow(false, true);
                    display(platformName, masterData);
                } finally {
                    app.setPusherDisabled(prevDisabled);
                }
                // logger.debug("browser panel: push disabled = {}", app.getPusherDisabled());
            }

            registerAsListener();
        } finally {
            app.setPusherDisabled(prevDisabledPush);
        }
    }

    /**
     * Compute where we think the jsp file ought to be.
     */
    private void getAppUrlString() {
        appUrlString = app.getURL().toExternalForm();
        int lastSlash = appUrlString.lastIndexOf("/");
        if (lastSlash == appUrlString.length() - 1) {
            // go back one more slash, the string ended with /
            lastSlash = appUrlString.lastIndexOf("/", lastSlash - 1);
        }
        appUrlString = appUrlString.substring(0, lastSlash + 1);
        // System.err.println("appUrlString with slash="+appUrlString);
    }

    private UpdateEventListener createUpdateSessionUpdateListener(final String platformName1, final SessionData masterData1) {
        // locate the current group data for the platformName
        if (masterData1 != null) {
            SessionData.UpdateEventListener listener = new SessionData.UpdateEventListener() {

                @Override
                public void updateEvent(UpdateEvent updateEvent) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());
                            logger.debug("updateEvent {}", ResultFrame.this);
                            if (!showDecisions || !waitingForDecisionLightsReset) {
                                // the decision lights are not currently shown
                                display(platformName1, masterData1);
                            } else {
                                // do not update the display just yet, the RESET event for the decisions will
                                // trigger the update once the decision lights go off
                            }
                        }
                    }).start();
                }
            };
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
    private void create(UserActions app1) throws MalformedURLException {
        this.setSizeFull();

        top = new CustomLayout("projectorTop"); //$NON-NLS-1$
        top.setWidth("100%"); //$NON-NLS-1$

        this.addComponent(top);

        iframe = new Embedded(); //$NON-NLS-1$
        iframe.setType(Embedded.TYPE_BROWSER);
        iframe.setSizeFull();
        this.addComponent(iframe);

        this.setExpandRatio(top, 0);
        this.setExpandRatio(iframe, 100);
    }

    /**
     * @param platformName1
     * @param masterData1
     * @throws RuntimeException
     */
    private void display(final String platformName1, final SessionData masterData1) throws RuntimeException {
        synchronized (app) {
            URL url = computeUrl(platformName1);
            logger.debug("display {}", url, getStylesheetName());
            // LoggerUtils.traceBack(logger,"display()");
            iframe.setSource(new ExternalResource(url));
            final Lifter currentLifter = masterData1.getCurrentLifter();
            if (currentLifter != null) {
                boolean done = fillLifterInfo(currentLifter);
                if (showTimer) {
                    updateTime(masterData1);
                    top.addComponent(timeDisplay, "timeDisplay"); //$NON-NLS-1$
                    timeDisplay.setVisible(!done);
                }
            } else {
                logger.debug("lifter null");
                name.setValue(getWaitingMessage()); //$NON-NLS-1$
                top.addComponent(name, "name"); //$NON-NLS-1$
                displayDecision(true);
                attempt.setValue(""); //$NON-NLS-1$
                top.addComponent(attempt, "attempt"); //$NON-NLS-1$
                attempt.setWidth(ATTEMPT_WIDTH); //$NON-NLS-1$
                if (showTimer) {
                    timeDisplay.setValue(""); //$NON-NLS-1$
                    timeDisplay.setWidth("4em");
                    top.addComponent(timeDisplay, "timeDisplay"); //$NON-NLS-1$
                }
                weight.setValue(""); //$NON-NLS-1$
                weight.setWidth("4em"); //$NON-NLS-1$
                top.addComponent(weight, "weight"); //$NON-NLS-1$	
            }
        }
        // logger.debug("prior to display push disabled={}", app.getPusherDisabled());

        app.push();
    }

    /**
     * @param platformName1
     * @return
     * @throws RuntimeException
     */
    private URL computeUrl(final String platformName1) throws RuntimeException {
        URL url;
        String encodedPlatformName;
        try {
            encodedPlatformName = URLEncoder.encode(platformName1, "UTF-8");
            // System.err.println(encodedPlatformName);
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
        String styleSheet = getStylesheetName();
        if (styleSheet == null || styleSheet.isEmpty()) {
            styleSheet = "";
        } else {
            styleSheet = "&style=" + getStylesheetName() + ".css";
        }
        final String spec = appUrlString + urlString + encodedPlatformName + styleSheet + "&time=" + System.currentTimeMillis(); //$NON-NLS-1$
        try {
            url = new URL(spec);
            // logger.debug("url={} {}", url.toExternalForm(), this);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); // can't happen.
        }
        return url;
    }

    /**
     * @return message used when Announcer has not selected a group
     */
    private String getWaitingMessage() {
        String message = Messages.getString("ResultFrame.Waiting", CompetitionApplication.getCurrentLocale());
        List<Competition> competitions = Competition.getAll();
        if (competitions.size() > 0) {
            message = competitions.get(0).getCompetitionName();
        }
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
            displayDecision(done);
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
        // display lifter name and affiliation
        if (!done) {
            final String lastName = lifter.getLastName();
            final String firstName = lifter.getFirstName();
            final String club = lifter.getClub();
            name.setValue(lastName.toUpperCase() + " " + firstName + " &nbsp;&nbsp; " + club); //$NON-NLS-1$ //$NON-NLS-2$
            top.addComponent(name, "name"); //$NON-NLS-1$
        } else {

            name.setValue(MessageFormat.format(
                    Messages.getString("ResultFrame.Done", locale), masterData.getCurrentSession().getName())); //$NON-NLS-1$
            top.addComponent(name, "name"); //$NON-NLS-1$
        }

    }

    private void displayDecision(boolean done) {
        decisionLights.setSizeFull();
        decisionLights.setHeight("2ex");
        decisionLights.setMargin(false);
        top.addComponent(decisionLights, "decisionLights"); //$NON-NLS-1$
        decisionLights.setVisible(false);
    }

    /**
     * @param lifter
     * @param sb
     * @param locale
     * @param currentTry
     * @param done
     */
    private void displayAttemptNumber(Lifter lifter, final Locale locale, final int currentTry, boolean done) {
        // display current attempt number
        if (!done) {
            //appendDiv(sb, lifter.getNextAttemptRequestedWeight()+Messages.getString("Common.kg",locale)); //$NON-NLS-1$
            String tryInfo = MessageFormat.format(Messages.getString("ResultFrame.tryNumber", locale), //$NON-NLS-1$
                    currentTry, (lifter.getAttemptsDone() >= 3 ? Messages.getString("Common.shortCleanJerk", locale) //$NON-NLS-1$
                            : Messages.getString("Common.shortSnatch", locale))); //$NON-NLS-1$
            attempt.setValue(tryInfo);
        } else {
            attempt.setValue("");
        }
        attempt.setWidth(ATTEMPT_WIDTH);
        top.addComponent(attempt, "attempt"); //$NON-NLS-1$
    }

    /**
     * @param lifter
     * @param sb
     * @param locale
     * @param done
     * @return
     */
    private void displayRequestedWeight(Lifter lifter, final Locale locale, boolean done) {
        // display requested weight
        if (!done) {
            weight.setValue(lifter.getNextAttemptRequestedWeight() + Messages.getString("Common.kg", locale)); //$NON-NLS-1$
        } else {
            weight.setValue(""); //$NON-NLS-1$
        }
        top.addComponent(weight, "weight"); //$NON-NLS-1$
    }

    /**
     * @param groupData
     */
    private void updateTime(final SessionData groupData) {
        // we set the value to the time remaining for the current lifter as
        // computed by groupData
        int timeRemaining = groupData.getDisplayTime();
        timeDisplay.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
    }

    @Override
    public void finalWarning(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void forceTimeRemaining(int timeRemaining, CompetitionApplication originatingApp, InteractionNotificationReason reason) {
        timeDisplay.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
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
    private PublicAddressOverlay overlayContent;
    private Window overlay;
    protected boolean shown;
    private String stylesheetName;
    private CountdownTimer timer;

    @Override
    public void normalTick(int timeRemaining) {
        if (name == null)
            return;
        if (TimeFormatter.getSeconds(previousTimeRemaining) == TimeFormatter.getSeconds(timeRemaining)) {
            previousTimeRemaining = timeRemaining;
            return;
        } else {
            previousTimeRemaining = timeRemaining;
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
        String stn = getStylesheetName();
        return viewName + "/" + platformName + (stn != null ? "/" + stn : "");
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
            logger.debug("setting stylesheetName to {} {}", params[2], this);
            setStylesheetName(params[2]);
        }
    }

    /*
     * Listen to public address notifications. We only deal with creating and destroying the overlay that hides the normal display.
     * 
     * @see org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent.MessageDisplayListener#messageUpdate(org.
     * concordiainternational.competition.publicAddress.PublicAddressMessageEvent)
     */
    @Override
    public void messageUpdate(PublicAddressMessageEvent event) {
        synchronized (app) {
            if (viewName.contains("resultBoard")) {
                if (event.setHide()) {
                    removeMessage();
                } else if (overlay == null) {
                    displayMessage(event.getTitle(), event.getMessage(), event.getRemainingMilliseconds());
                } else {
                    // nothing to do: overlayContent listens to the events on its own
                }
            }
        }
        app.push();

    }

    /**
     * Remove the currently displayed message, if any.
     */
    private void removeMessage() {
        if (overlayContent != null)
            masterData.removeBlackBoardListener(overlayContent);
        overlayContent = null;
        if (overlay != null)
            app.getMainWindow().removeWindow(overlay);
        overlay = null;
    }

    /**
     * Display a new public address message Hide the current display with a popup.
     * 
     * @param title
     * @param message
     * @param remainingMilliseconds
     */
    private void displayMessage(String title, String message, Integer remainingMilliseconds) {
        // create content formatting
        logger.debug("displayMessage {} {}", title, message);
        if (overlayContent == null) {
            overlayContent = new PublicAddressOverlay(title, message, remainingMilliseconds);
            // overlayContent listens to message updates and timer updates
            masterData.addBlackBoardListener(overlayContent);
        }
        synchronized (app) {
            // create window
            if (overlay == null) {
                logger.debug("creating window");
                Window mainWindow = app.getMainWindow();
                ;
                overlay = new Window(platformName);
                overlay.addStyleName("decisionLightsWindow");
                overlay.setSizeFull();
                mainWindow.addWindow(overlay);
                overlay.center();
                overlay.setContent(overlayContent);
                overlay.setVisible(true);
            }
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
        listenerLogger.debug("re-registering handlers for {} {}", this, relativeUri);
        registerAsListener();
        return null;
    }

    /**
     * Process a decision regarding the current lifter. Make sure that the name of the lifter does not change until after the decision has
     * been shown.
     * 
     * @see org.concordiainternational.competition.decision.DecisionEventListener#updateEvent(org.concordiainternational.competition.decision.DecisionEvent)
     */
    @Override
    public void updateEvent(final DecisionEvent updateEvent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (showDecisions) {
                    LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());
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
                            decisionLights.setVisible(true);
                            break;
                        case RESET:
                            // we are done
                            waitingForDecisionLightsReset = false;
                            decisionLights.setVisible(false);
                            shown = false;
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
                                decisionLights.setVisible(true);
                            }
                            break;
                        case BLOCK:
                            waitingForDecisionLightsReset = true;
                            decisionLights.setVisible(true);
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.components.Stylable#setStylesheet(java.lang.String)
     */
    @Override
    public void setStylesheetName(String stylesheetName) {
        this.stylesheetName = stylesheetName;
        // String callerClassName = LoggerUtils.getCallerClassName();
        // logger.trace("stylesheetName={} ({}) {}", stylesheetName, callerClassName, this);
        // LoggerUtils.traceBack(logger);
    }

    @Override
    public String getStylesheetName() {
        // String callerClassName = LoggerUtils.getCallerClassName();
        // logger.trace("stylesheetName={} ({}) {}", stylesheetName, callerClassName, this);
        return stylesheetName;
    }

    @Override
    public void registerAsListener() {
        // listen to changes in the competition data
        if (updateListener == null) {
            updateListener = createUpdateSessionUpdateListener(platformName, masterData);
        }
        masterData.addListener(updateListener);
        listenerLogger.debug("{} listening to session data updates.", updateListener);

        // listen to public address events
        if (viewName.contains("resultBoard")) {
            masterData.addBlackBoardListener(this);
            listenerLogger.debug("{} listening to public address events.", this);
        }

        // listen to decisions
        IDecisionController decisionController = masterData.getRefereeDecisionController();
        if (decisionController != null) {
            decisionController.addListener(decisionLights);
            listenerLogger.debug("{} listening to decision events.", decisionLights);
            decisionController.addListener(this);
            listenerLogger.debug("{} listening to decision events.", this);
        }

        // refresh timer status
        timer = masterData.getTimer();
        timer.addListener(this);
        listenerLogger.debug("{} listening to clock events.", this);

        // listen to close events
        app.getMainWindow().addListener((CloseListener) this);
        listenerLogger.debug("{} listening to window close events.", this);

        // // listen to URI changes
        // app.getMainWindow().addURIHandler(this);
        // listenerLogger.debug("{} listening to URI events.", this);
    }

    @Override
    public void unregisterAsListener() {
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());

        // stop listening to changes in the competition data
        if (updateListener != null) {
            masterData.removeListener(updateListener);
            listenerLogger.debug("{} stopped listening session data updates", updateListener);
        }

        // stop listening to public address events
        removeMessage();
        if (viewName.contains("resultBoard")) {
            masterData.removeBlackBoardListener(this);
            listenerLogger.debug("{} stopped listening to PublicAddress TimerEvents", this);
        }

        // stop listening to decisions
        IDecisionController decisionController = masterData.getRefereeDecisionController();
        if (decisionController != null) {
            decisionController.removeListener(decisionLights);
            listenerLogger.debug("{} stopped listening to decision events.", decisionLights);
            decisionController.removeListener(this);
            listenerLogger.debug("{} listening to decision events.", this);
        }

        // stop listening to close events
        app.getMainWindow().removeListener((CloseListener) this);
        listenerLogger.debug("{} stopped listening to window close events..", this);

        // stop listening to URI changes
        // app.getMainWindow().removeURIHandler(this);
        // listenerLogger.debug("{} stopped listening to URI events.", this);
    }

    @Override
    public void showInteractionNotification(CompetitionApplication originatingApp, InteractionNotificationReason reason) {
        // ignored.
    }

    @Override
    public boolean needsBlack() {
        return false;
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
