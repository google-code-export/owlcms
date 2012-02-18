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

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.timer.CountdownTimerListener;
import org.concordiainternational.competition.ui.SessionData.UpdateEvent;
import org.concordiainternational.competition.ui.SessionData.UpdateEventListener;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.components.DecisionLightsWindow;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
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
	CloseListener,
	URIHandler
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
       
        synchronized (app) {
        	boolean prevDisabled = app.getPusherDisabled();
        	try {
        		app.setPusherDisabled(true);
        		create(app, platformName);
        		masterData = app.getMasterData(platformName);
        		
        		app.getMainWindow().addURIHandler(this);
        		
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
        timeDisplay1.setHeight("600px");
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
            if (currentLifter != null) {
                boolean done = fillLifterInfo(currentLifter);
                updateTime(masterData1);
                timeDisplay.setVisible(!done);
            } else {
                timeDisplay.setValue(""); //$NON-NLS-1$
            }
        }

        app.push();
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
        pushTime(timeAllowed);
    }

    @Override
    public void finalWarning(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void forceTimeRemaining(int startTime, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
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
            timeDisplay.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
        }
        app.push();
    }

    @Override
    public void pause(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
    }

    @Override
    public void start(int timeRemaining) {
    }

    @Override
    public void stop(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
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
    @Override
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
						logger.info("received DOWN event");
						showLights(updateEvent);
						break;
						
					case SHOW:
						// if window is not up, show it.
						shown = true;
						logger.info("received SHOW event {}",shown);
						showLights(updateEvent);
						break;

					case RESET:
						// we are done
						logger.info("received RESET event");
						hideLights(updateEvent);
						shown = false;
						break;
						
					case WAITING:
						logger.info("ignoring WAITING event");
						break;
						
					case UPDATE:
						logger.debug("received UPDATE event {}",shown);
						// we need to show that referees have changed their mind.
						if (shown) {
							showLights(updateEvent);
						}
						break;
					
					case BLOCK:
						logger.debug("received BLOCK event {}",shown);
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
			popUp.setContent(decisionLights);
		}
		popUp.setVisible(true);
		
		// relay the event
		logger.debug("relaying");
		decisionLights.updateEvent(updateEvent);
		
	}
	
	/**
	 * Hide the decision lights.
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
		app.getMainWindow().addListener((CloseListener)this);
		registerAsGroupDataListener(platformName, masterData);
		masterData.getRefereeDecisionController().addListener(this);
        final CountdownTimer timer = masterData.getTimer();
        timer.setCountdownDisplay(this);
	}
	
	/**
	 * Undo what registerAsListener did.
	 */
	@Override
	public void unregisterAsListener() {
		app.getMainWindow().removeListener((CloseListener)this);
		masterData.removeListener(updateEventListener);
		masterData.getRefereeDecisionController().removeListener(this);
        final CountdownTimer timer = masterData.getTimer();
        timer.setCountdownDisplay(null);
	}
	
	
	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		registerAsListener();
		return null;
	}

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

}
