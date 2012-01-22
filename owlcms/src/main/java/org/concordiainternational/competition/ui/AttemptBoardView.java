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
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.decision.IDecisionController;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent;
import org.concordiainternational.competition.publicAddress.PublicAddressTimerEvent;
import org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent.MessageDisplayListener;
import org.concordiainternational.competition.publicAddress.PublicAddressTimerEvent.MessageTimerListener;
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
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Show an WebPage underneath a banner.
 * @author jflamy
 *
 */

public class AttemptBoardView extends VerticalLayout implements 
		ApplicationView, 
		CountdownTimerListener,
		MessageDisplayListener,
		MessageTimerListener,
		Window.CloseListener, 
		URIHandler,
		DecisionEventListener
		{ 
	
	public final static Logger logger = LoggerFactory.getLogger(AttemptBoardView.class);
    private static final long serialVersionUID = 1437157542240297372L;

    public String urlString;
    private String platformName;
    private SessionData masterData;
    private GridLayout grid;
    final private transient CompetitionApplication app;
    
    private Label nameLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label firstNameLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label clubLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label attemptLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    private Label timeDisplayLabel = new Label();
    private Label weightLabel = new Label();
    private LoadImage plates;
    
	private UpdateEventListener updateListener;
	private DecisionLightsWindow decisionLights;
	protected boolean waitingForDecisionLightsReset;

    public AttemptBoardView(boolean initFromFragment, String viewName) {

        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        
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
			    logger.debug("browser panel: push disabled = {}",app.getPusherDisabled());
			}
			
			// URI handler must remain, so is not part of the register/unRegister paire
			app.getMainWindow().addURIHandler(this);
			registerHandlers(viewName);
		} finally {
			app.setPusherDisabled(prevDisabledPush);
		}
    }


	/**
	 * 
	 */
	protected void createDecisionLights() {
		decisionLights = new DecisionLightsWindow(false, true);
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
//							logger.debug("request to display {}",
//									AttemptBoardView.this);
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

        grid = new GridLayout(4,4);
        grid.setSizeFull();
        grid.setMargin(true);
        grid.addStyleName("newAttemptBoard");
        
        grid.setColumnExpandRatio(0, 50.0F);
        grid.setColumnExpandRatio(1, 50.0F);
        grid.setColumnExpandRatio(2, 0.0F);
        grid.setColumnExpandRatio(3, 0.0F);
        grid.setRowExpandRatio(0, 0.0F);
        grid.setRowExpandRatio(1, 0.0F);
        grid.setRowExpandRatio(2, 65.0F);
        grid.setRowExpandRatio(3, 35.0F);
        
        // we do not add the time display, plate display
        // and decision display -- they react to timekeeping
        grid.addComponent(nameLabel, 0, 0, 3, 0);
        nameLabel.setSizeUndefined();
        nameLabel.addStyleName("text");
        grid.setComponentAlignment(nameLabel, Alignment.MIDDLE_LEFT);
        
        grid.addComponent(firstNameLabel, 0, 1, 2, 1);
        firstNameLabel.setSizeUndefined();
        firstNameLabel.addStyleName("text");
        grid.setComponentAlignment(firstNameLabel, Alignment.MIDDLE_LEFT);
        
        grid.addComponent(clubLabel, 3, 1, 3, 1);
        clubLabel.setSizeUndefined();
        clubLabel.addStyleName("text");
        grid.setComponentAlignment(clubLabel, Alignment.MIDDLE_CENTER);
        
        grid.addComponent(weightLabel, 3, 2, 3, 2);
        weightLabel.setSizeUndefined();
        weightLabel.addStyleName("weightLabel");
        grid.setComponentAlignment(weightLabel, Alignment.MIDDLE_CENTER);
        
        grid.addComponent(attemptLabel, 3, 3, 3, 3);
        attemptLabel.setSizeUndefined();
        attemptLabel.addStyleName("text");
        grid.setComponentAlignment(attemptLabel, Alignment.MIDDLE_CENTER);
        
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
    	if (paShown) {
    		return;
    	}
        synchronized (app) {
            final Lifter currentLifter = masterData1.getCurrentLifter();
            if (currentLifter != null) {
            	logger.debug("masterData {}",masterData1.getCurrentSession().getName());
                boolean done = fillLifterInfo(currentLifter);
                updateTime(masterData1);
                showDecisionLights(false);
                timeDisplayLabel.setSizeUndefined();
                timeDisplayLabel.setVisible(!done);
            } else {
            	logger.debug("lifter null");
                hideAll();
            }

        }
        logger.debug("prior to display push disabled={}",app.getPusherDisabled());
        
        app.push();
    }


	/**
	 * 
	 */
	protected void hideAll() {
		nameLabel.setValue(getWaitingMessage()); //$NON-NLS-1$
		showDecisionLights(false);
		timeDisplayLabel.setSizeUndefined();
		timeDisplayLabel.setVisible(false);
		firstNameLabel.setValue("");
		clubLabel.setValue("");
		attemptLabel.setValue("");
		weightLabel.setValue("");
		plates.setVisible(false);
	}




	/**
     * @return message used when Announcer has not selected a group
     */
    private String getWaitingMessage() {
        String message = ""; //Messages.getString("ResultFrame.Waiting", CompetitionApplication.getCurrentLocale());
//        List<Competition> competitions = Competition.getAll();
//        if (competitions.size() > 0) {
//            message = competitions.get(0).getCompetitionName();
//        }
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
    		
    		nameLabel.setValue(lastName.toUpperCase());
    		firstNameLabel.setValue(firstName);
    		clubLabel.setValue(club);
    		
    		//nameLabel.setValue(lastName.toUpperCase() + " " + firstName + " &nbsp;&nbsp; " + club); //$NON-NLS-1$ //$NON-NLS-2$
    		//grid.addComponent(nameLabel, 0, 0, 3, 0);
    	} else {

    		nameLabel.setValue(MessageFormat.format(
    				Messages.getString("AttemptBoard.Done", locale), masterData.getCurrentSession().getName())); //$NON-NLS-1$
    		firstNameLabel.setValue("");
    		clubLabel.setValue("");
    		//grid.addComponent(nameLabel, 0, 0, 3, 0);
    	}

    }
    
    private void showDecisionLights(boolean decisionLightsVisible) {
//    	logger.debug("showDecisionLights {}",decisionLightsVisible);
    	// remove everything
		grid.removeComponent(timeDisplayLabel);
        grid.removeComponent(decisionLights);
        grid.removeComponent(plates);
        
    	if (decisionLightsVisible) {
    		grid.addComponent(decisionLights,0,2,2,3);
    		decisionLights.setSizeFull();
    		decisionLights.setMargin(true);
    		grid.setComponentAlignment(decisionLights, Alignment.TOP_LEFT);
    	} else {
            grid.addComponent(timeDisplayLabel, 0, 2, 1, 3);
            grid.addComponent(plates, 2, 2, 2, 3);
            plates.computeImageArea(masterData, masterData.getPlatform());
            
            grid.setComponentAlignment(timeDisplayLabel, Alignment.MIDDLE_CENTER);
            grid.setComponentAlignment(plates, Alignment.MIDDLE_CENTER);
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
			
            attemptLabel.setValue(tryInfo.replace(" ","<br>"));
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
        // we set the value to the time allowed for the current lifter as
        // computed by groupData
        int timeAllowed = groupData.getTimeAllowed();
        final CountdownTimer timer = groupData.getTimer();
        if (!paShown){
        	timeDisplayLabel.setValue(TimeFormatter.formatAsSeconds(timeAllowed));
        }
        timer.addListener(this);
    }

    @Override
    public void finalWarning(int timeRemaining) {
        normalTick(timeRemaining);
    }

    @Override
    public void forceTimeRemaining(int startTime, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
    	if (!paShown){
    		timeDisplayLabel.setValue(TimeFormatter.formatAsSeconds(startTime));
    	}
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
	private boolean paShown;

    @Override
    public void normalTick(int timeRemaining) {
        if (nameLabel == null) return;
        if (TimeFormatter.getSeconds(previousTimeRemaining) == TimeFormatter.getSeconds(timeRemaining)) {
            previousTimeRemaining = timeRemaining;
            return;
        } else {
            previousTimeRemaining = timeRemaining;
        }

        synchronized (app) {
        	if (!paShown){
        		timeDisplayLabel.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
        	}
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
        return viewName+"/"+platformName+(stylesheetName != null ? "/"+stylesheetName : "");
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
        
        if (params.length >= 3) {
            stylesheetName = params[2];
            logger.trace("setting stylesheetName to {}",stylesheetName);
        }
    }

	/* Listen to public address notifications.
	 * We only deal with creating and destroying the overlay that hides the normal display.
	 * @see org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent.MessageDisplayListener#messageUpdate(org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent)
	 */
	@Override
	public void messageUpdate(PublicAddressMessageEvent event) {
		synchronized(app) {
			if (event.setHide()) {
				removeMessage();
			} else {
				hideAll();
				displayMessage(event.getTitle(),event.getMessage(),event.getRemainingMilliseconds());
			}
		}
		app.push();

	}
	
	@Override
	public void timerUpdate(PublicAddressTimerEvent event) {
		if (!paShown) {
			return;
		}
		synchronized(app) {
			Integer remainingMilliseconds = event.getRemainingMilliseconds();
			if (remainingMilliseconds != null) {
				timeDisplayLabel.setVisible(true);
				timeDisplayLabel.setValue(TimeFormatter.formatAsSeconds(remainingMilliseconds));
			}
		}
		app.push();
	}

	/**
	 * Remove the currently displayed public message, if any.
	 */
	private void removeMessage() {
		paShown = false;
		refresh();
	}

	/**
	 * Display a new public address message
	 * Hide the current display with a popup.
	 * @param title
	 * @param message
	 * @param remainingMilliseconds
	 */
	private void displayMessage(String title, String message, Integer remainingMilliseconds) {
		// create content formatting
//		logger.debug("displayMessage {}",remainingMilliseconds);
		synchronized (app) {
			paShown = true;
			nameLabel.setValue(Messages.getString("AttemptBoard.Pause", CompetitionApplication.getCurrentLocale()));
			timeDisplayLabel.setVisible(true);
			timeDisplayLabel.setValue(TimeFormatter.formatAsSeconds(remainingMilliseconds));
		}
		app.push();
	}



	/**
	 * Register listeners for the various model events.
	 * @param viewName1
	 */
	private void registerHandlers(String viewName1) {
		// listen to changes in the competition data
		logger.debug("listening to session data updates.");
        updateListener = registerAsListener(platformName, masterData);
        
        // listen to public address events
        logger.debug("listening to public address events.");
        masterData.addBlackBoardListener(this);
        
        // listen to decisions
        IDecisionController decisionController = masterData.getRefereeDecisionController();
        if (decisionController != null) {
    		decisionController.addListener(decisionLights);
    		decisionController.addListener(this);
        }

        // listen to close events
        app.getMainWindow().addListener((CloseListener)this);
	}
	
	/**
	 * Undo what registerListeners did.
	 */
	private void unRegisterListeners() {
		// stop listening to changes in the competition data
		if (updateListener != null) {
			masterData.removeListener(updateListener);
			logger.debug("stopped listening to UpdateEvents");
		}
        
        // stop listening to public address events
		removeMessage();
        masterData.removeBlackBoardListener(this);
        logger.debug("stopped listening to PublicAddress TimerEvents");
        
        // stop listening to decisions
        IDecisionController decisionController = masterData.getRefereeDecisionController();
        if (decisionController != null) {
        	decisionController.removeListener(decisionLights);
        	decisionController.removeListener(this);
        }
        
        // stop listening to close events
        app.getMainWindow().removeListener((CloseListener)this);
	}
	

	/* Unregister listeners when window is closed.
	 * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
	 */
	@Override
	public void windowClose(CloseEvent e) {
        unRegisterListeners();
	}

	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		logger.debug("re-registering handlers for {} {}",this,relativeUri);
		registerHandlers(viewName);
		return null;
	}


	/**
	 * Process a decision regarding the current lifter.
	 * Make sure that the nameLabel of the lifter does not change until after the decision has been shown.
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
					}
				}
			}
		}).start();
	}


	@Override
	public void registerAsListener() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void unregisterAsListener() {
		// TODO Auto-generated method stub
		
	}
	

}
