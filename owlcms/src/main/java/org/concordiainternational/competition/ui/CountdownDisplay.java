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

package org.concordiainternational.competition.ui;

import java.net.MalformedURLException;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.decision.DecisionController.DecisionEventListener;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.timer.CountdownTimerListener;
import org.concordiainternational.competition.ui.SessionData.UpdateEvent;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.components.DecisionLightsWindow;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class CountdownDisplay extends VerticalLayout implements ApplicationView, CountdownTimerListener, DecisionEventListener {
    public final static Logger logger = LoggerFactory.getLogger(CountdownDisplay.class);
    private static final long serialVersionUID = 1437157542240297372L;
    private static final boolean PUSHING = true;

    private ProgressIndicator refresher;
    private String platformName;
    private SessionData masterData;
    private CompetitionApplication app;
    private Label timeDisplay = new Label();
    private ICEPush pusher = null;
    private int lastTimeRemaining;
    private String viewName;
	private Window popUp = null;
	private DecisionLightsWindow content;

    public CountdownDisplay(boolean initFromFragment, String viewName) {
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

        create(app, platformName);
        masterData = app.getMasterData(platformName);

        registerAsGroupDataListener(platformName, masterData);
        masterData.getDecisionController().addListener(this);

        display(platformName, masterData);
    }

    private void registerAsGroupDataListener(final String platformName, final SessionData masterData) {
        // locate the current group data for the platformName
        if (masterData != null) {
            logger.debug("{} listening to: {}", platformName, masterData); //$NON-NLS-1$	
            //masterData.addListener(SessionData.UpdateEvent.class, this, "update"); //$NON-NLS-1$

            SessionData.UpdateEventListener listener = new SessionData.UpdateEventListener() {

                @Override
                public void updateEvent(UpdateEvent updateEvent) {
                    display(platformName, masterData);
                }

            };
            masterData.addListener(listener); //$NON-NLS-1$		

        } else {
            logger.debug("{} NOT listening to:  = {}", platformName, masterData); //$NON-NLS-1$	
        }
    }

    /**
     * @param app
     * @param platformName
     * @throws MalformedURLException
     */
    private void create(UserActions app, String platformName) {
        this.setSizeFull();
        this.addStyleName("largeCountdownBackground");
        timeDisplay = createTimeDisplay();
        this.addComponent(timeDisplay);
        this.setComponentAlignment(timeDisplay, "middle center");

        if (PUSHING) {
            pusher = this.app.ensurePusher();
        } else {
            setupPolling();
        }

        this.setExpandRatio(timeDisplay, 100);

    }

    /**
     * 
     */
    private Label createTimeDisplay() {
        Label timeDisplay = new Label();
        timeDisplay.setSizeUndefined();
        timeDisplay.addStyleName("largeCountdown");
        return timeDisplay;
    }



    /**
     * @param platformName
     * @param masterData
     * @throws RuntimeException
     */
    private void display(final String platformName, final SessionData masterData) throws RuntimeException {
        synchronized (app) {
            final Lifter currentLifter = masterData.getCurrentLifter();
            if (currentLifter != null) {
                boolean done = fillLifterInfo(currentLifter);
                updateTime(masterData);
                timeDisplay.setVisible(!done);
            } else {
                timeDisplay.setValue(""); //$NON-NLS-1$
            }
        }

        if (pusher != null) pusher.push();
    }

    @Override
    public void refresh() {
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
        // we set the value to the time allowed for the current lifter as
        // computed by groupData
        int timeAllowed = groupData.getTimeAllowed();
        final CountdownTimer timer = groupData.getTimer();
        pushTime(timeAllowed);

        // timer.addListener(this);
        timer.setCountdownDisplay(this);
    }

    @Override
    public void finalWarning(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void forceTimeRemaining(int startTime) {
        pushTime(startTime);
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
        if (timeDisplay == null) return;

        // do not update if no visible change
        if (TimeFormatter.getSeconds(timeRemaining) == TimeFormatter.getSeconds(lastTimeRemaining)) {
            lastTimeRemaining = timeRemaining;
            return;
        } else {
            lastTimeRemaining = timeRemaining;
        }

        synchronized (app) {
            if (pusher == null) {
                timeDisplay.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
                timeDisplay.requestRepaint();
            } else {
                timeDisplay.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
                pusher.push();
            }
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
    
    /**
     * Set up polling by browser.
     * Obsolete now that we use pushing.
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

    @Override
    public void updateEvent(DecisionEvent updateEvent) {
        synchronized (app) {
            switch (updateEvent.getType()) {
            case DOWN:
            	logger.info("received DOWN event");
            	showLights(updateEvent);
            	break;
            case SHOW:
                // if window is not up, show it.
            	logger.info("received SHOW event");
                showLights(updateEvent);
                break;
                
            case RESET:
            	// we are done
            	logger.info("received RESET event");
                hideLights(updateEvent);
                break;
                
                
            case WAITING:
            	logger.info("ignoring WAITING event");
            	break;
            case UPDATE:
            	// do nothing, lifter should not see that referees are changing their minds.
            	logger.info("ignoring UPDATE event");
            	break;
            }
        }
        if (pusher != null) {
            pusher.push();
        }
    }

    
	/**
	 * Make sure decision lights are shown, and relay the event to the display component.
	 * @param updateEvent
	 */
	private void showLights(DecisionEvent updateEvent) {
		// create window
		if (popUp == null) {
			logger.warn("creating window");
			Window mainWindow = app.getMainWindow();
			content = new DecisionLightsWindow(false, false);
			popUp = new Window(platformName);
			popUp.addStyleName("decisionLightsWindow");
			popUp.setSizeFull();
			mainWindow.addWindow(popUp);
			popUp.setContent(content);
		}
		popUp.setVisible(true);
		
		// relay the event
		content.updateEvent(updateEvent);
		
	}
	
	/**
	 * Hide the decision lights.
	 * @param updateEvent
	 */
	private void hideLights(DecisionEvent updateEvent) {
		// relay the event (just in case)
		if (content != null) {
			content.updateEvent(updateEvent);
		}
		
		
		// close window
		if (popUp != null) {
			popUp.setVisible(false);
		}
	}


}
