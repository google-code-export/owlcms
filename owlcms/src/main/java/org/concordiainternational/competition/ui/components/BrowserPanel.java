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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.timer.CountdownTimerListener;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.CompetitionApplicationComponents;
import org.concordiainternational.competition.ui.GroupData;
import org.concordiainternational.competition.ui.UserActions;
import org.concordiainternational.competition.ui.GroupData.UpdateEvent;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

public class BrowserPanel extends VerticalLayout implements ApplicationView, CountdownTimerListener {
    public final static Logger logger = LoggerFactory.getLogger(BrowserPanel.class);
    private static final long serialVersionUID = 1437157542240297372L;
    private static final boolean PUSHING = true;

    private Embedded iframe;
    public String urlString;
    private ProgressIndicator refresher;
    private String platformName;
    private GroupData masterData;
    private CustomLayout top;
    private CompetitionApplication app;
    private Label name = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label attempt = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label timeDisplay = new Label();
    private Label weight = new Label();
    private ICEPush pusher = null;
    private String appUrlString;

    public BrowserPanel(boolean initFromFragment, String viewName, String urlString) throws MalformedURLException {

        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        
        this.app = CompetitionApplication.getCurrent();
        
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
        registerAsListener(platformName, masterData);
        display(platformName, masterData);

        if (app != masterData.getMasterApplication()) {
            // we are not the master application; hide the menu bar.
            app.components.menu.setVisible(false);
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

    private void registerAsListener(final String platformName, final GroupData masterData) {
        // locate the current group data for the platformName
        if (masterData != null) {
            logger.debug(urlString + "{} listening to: {}", platformName, masterData); //$NON-NLS-1$	
            //masterData.addListener(GroupData.UpdateEvent.class, this, "update"); //$NON-NLS-1$

            GroupData.UpdateEventListener listener = new GroupData.UpdateEventListener() {

                @Override
                public void updateEvent(UpdateEvent updateEvent) {
                    display(platformName, masterData);
                }

            };
            masterData.addListener(listener); //$NON-NLS-1$		

        } else {
            logger.debug(urlString + "{} NOT listening to:  = {}", platformName, masterData); //$NON-NLS-1$	
        }
    }

    /**
     * @param app
     * @param platformName
     * @throws MalformedURLException
     */
    private void create(UserActions app) throws MalformedURLException {
        this.setSizeFull();

        top = new CustomLayout("projectorTop"); //$NON-NLS-1$
        top.setWidth("100%"); //$NON-NLS-1$

        this.addComponent(top);

        iframe = new Embedded(); //$NON-NLS-1$
        iframe.setType(Embedded.TYPE_BROWSER);
        iframe.setSizeFull();
        this.addComponent(iframe);

        if (PUSHING) {
            pusher = this.app.ensurePusher();
        } else {
            setupPolling();
        }

        this.setExpandRatio(top, 0);
        this.setExpandRatio(iframe, 100);
    }

    /**
     * Force the client to poll.
     */
    private void setupPolling() {
        // we need this because the client side won't refresh unless it
        // initiates the poll.
        refresher = new ProgressIndicator();
        refresher.setIndeterminate(true);
        refresher.setPollingInterval(1000);
        refresher.addStyleName("hidden"); //$NON-NLS-1$
        refresher.setHeight("0"); //$NON-NLS-1$
        this.addComponent(refresher);
        this.setExpandRatio(refresher, 0);
    }

    /**
     * @param platformName
     * @param masterData
     * @throws RuntimeException
     */
    private void display(final String platformName, final GroupData masterData) throws RuntimeException {
        synchronized (app) {
            URL url = computeUrl(platformName);
            iframe.setSource(new ExternalResource(url));
            final Lifter currentLifter = masterData.getCurrentLifter();
            if (currentLifter != null) {
                boolean done = fillLifterInfo(currentLifter);
                updateTime(masterData);
                top.addComponent(timeDisplay, "timeDisplay"); //$NON-NLS-1$
                timeDisplay.setVisible(!done);
            } else {
                name.setValue(getWaitingMessage()); //$NON-NLS-1$
                top.addComponent(name, "name"); //$NON-NLS-1$
                attempt.setValue(""); //$NON-NLS-1$
                top.addComponent(attempt, "attempt"); //$NON-NLS-1$
                attempt.setWidth("12em"); //$NON-NLS-1$
                timeDisplay.setValue(""); //$NON-NLS-1$
                timeDisplay.setWidth("4em");
                top.addComponent(timeDisplay, "timeDisplay"); //$NON-NLS-1$
                weight.setValue(""); //$NON-NLS-1$
                weight.setWidth("4em"); //$NON-NLS-1$
                top.addComponent(weight, "weight"); //$NON-NLS-1$	
            }
        }
        if (pusher != null) {
            pusher.push();
        }
    }

    /**
     * @param platformName
     * @return
     * @throws RuntimeException
     */
    private URL computeUrl(final String platformName) throws RuntimeException {
        URL url;
        String encodedPlatformName;
        try {
            encodedPlatformName = URLEncoder.encode(platformName, "UTF-8");
            // System.err.println(encodedPlatformName);
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
        final String spec = appUrlString + urlString + encodedPlatformName + "&time=" + System.currentTimeMillis(); //$NON-NLS-1$
        try {
            url = new URL(spec);
            // System.err.println(url.toExternalForm());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); // can't happen.
        }
        return url;
    }

    /**
     * @return message used when Announcer has not selected a group
     */
    private String getWaitingMessage() {
        String message = Messages.getString("BrowserPanel.Waiting", CompetitionApplication.getCurrentLocale());
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

        displayName(lifter, done);
        displayAttemptNumber(lifter, locale, currentTry, done);
        displayRequestedWeight(lifter, locale, done);
        return done;
    }

    /**
     * @param lifter
     * @param alwaysShowName
     * @param sb
     * @param done
     */
    private void displayName(Lifter lifter, boolean done) {
        // display lifter name and affiliation
        if (!done) {
            final String lastName = lifter.getLastName();
            final String firstName = lifter.getFirstName();
            final String club = lifter.getClub();
            name.setValue(lastName.toUpperCase() + " " + firstName + " &nbsp;&nbsp; " + club); //$NON-NLS-1$ //$NON-NLS-2$

        } else {
            name.setValue(""); //$NON-NLS-1$

        }
        top.addComponent(name, "name"); //$NON-NLS-1$
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
        if (done) {
            // we're done, write a "finished" message and return.
            attempt.setValue(Messages.getString("LifterInfo.Done", locale)); //$NON-NLS-1$
        } else {
            //appendDiv(sb, lifter.getNextAttemptRequestedWeight()+Messages.getString("Common.kg",locale)); //$NON-NLS-1$
            String tryInfo = MessageFormat.format(Messages.getString("LifterInfo.tryNumber", locale), //$NON-NLS-1$
                currentTry, (lifter.getAttemptsDone() >= 3 ? Messages.getString("Common.shortCleanJerk", locale) //$NON-NLS-1$
                        : Messages.getString("Common.shortSnatch", locale))); //$NON-NLS-1$
            attempt.setValue(tryInfo);
        }
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
    private void updateTime(final GroupData groupData) {
        // we set the value to the time allowed for the current lifter as
        // computed by groupData
        int timeAllowed = groupData.getTimeAllowed();
        final CountdownTimer timer = groupData.getTimer();
        timeDisplay.setValue(TimeFormatter.formatAsSeconds(timeAllowed));
        timer.addListener(this);
    }

    @Override
    public void finalWarning(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void forceTimeRemaining(int startTime) {
        timeDisplay.setValue(TimeFormatter.formatAsSeconds(startTime));
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

    @Override
    public void normalTick(int timeRemaining) {
        if (name == null) return;
        if (TimeFormatter.getSeconds(previousTimeRemaining) == TimeFormatter.getSeconds(timeRemaining)) {
            previousTimeRemaining = timeRemaining;
            return;
        } else {
            previousTimeRemaining = timeRemaining;
        }

        synchronized (app) {
            timeDisplay.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
            if (pusher == null) timeDisplay.requestRepaint();
        }
        if (pusher != null) {
            pusher.push();
        }
    }

    @Override
    public void pause(int timeRemaining) {
    }

    @Override
    public void start(int timeRemaining) {
    }

    @Override
    public void stop(int timeRemaining) {
    }

    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.components.ApplicationView#needsMenu()
     */
    @Override
    public boolean needsMenu() {
        return false;
    }

    /**
     * @return
     */
    public String getFragment() {
        return viewName+"/"+platformName;
    }
    

    /* (non-Javadoc)
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
    }
}
