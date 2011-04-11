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

import java.net.URL;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.ui.AnnouncerView.Mode;
import org.concordiainternational.competition.ui.PlatesInfoEvent.PlatesInfoListener;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.components.DecisionLightsWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Display information about the current athlete and lift.
 * Shows lifter information, decision lights, and plates loading diagram.
 * 
 * @author jflamy
 *
 */

public class AttemptBoardView extends VerticalLayout implements
	ApplicationView,
	SessionData.UpdateEventListener,
	CloseListener,
	PlatesInfoListener,
	DecisionEventListener,
	URIHandler {

    Logger logger = LoggerFactory.getLogger(AttemptBoardView.class);

    private static final long serialVersionUID = 2443396161202824072L;
    private SessionData masterData;
    private LifterInfo announcerInfo;
    private Mode mode;
    private Platform platform;

    private VerticalLayout imageArea;

    private String platformName;

    private String viewName;

	private DecisionLightsWindow decisionArea;

	private LoadImage plates;

	private HorizontalLayout horLayout;

	private boolean ie;

	private CompetitionApplication app;

	private boolean decisionDisplayInProgress;

	protected boolean shown;

    public AttemptBoardView(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        
        app = CompetitionApplication.getCurrent();
        this.mode = AnnouncerView.Mode.DISPLAY;
        this.addStyleName("attemptBoardView");


		boolean prevPusherDisabled = app.getPusherDisabled();
        try {
        	app.setPusherDisabled(true);
        	
            if (platformName == null) {
            	// get the default platform name
                platformName = CompetitionApplicationComponents.initPlatformName();
            } else if (app.getPlatform() == null) {
            	app.setPlatformByName(platformName);
            }
            
            masterData = app.getMasterData(platformName);
            if (app != masterData.getMasterApplication()) {
                // we are not the master application; hide the menu bar.
                Component menuComponent = app.components.menu;
				if (menuComponent != null) menuComponent.setVisible(false);
				menuComponent = app.getMobileMenu();
				if (menuComponent != null) menuComponent.setVisible(false);
            }
            platform = Platform.getByName(platformName);
	        Lifter currentLifter = masterData.getCurrentLifter();

        	this.setSizeFull();
            horLayout = new HorizontalLayout();
        	
            WebApplicationContext context = (WebApplicationContext) app.getContext();
			ie = context.getBrowser().isIE();
        	
			announcerInfo = new LifterInfo("display", masterData, mode, horLayout); //$NON-NLS-1$
			announcerInfo.addStyleName("currentLifterSummary"); //$NON-NLS-1$
			announcerInfo.setSizeFull(); //$NON-NLS-1$
			announcerInfo.setHeight("30em");
			announcerInfo.setWidth("32em");
			announcerInfo.setMargin(false);
			announcerInfo.setSpacing(false);
			announcerInfo.addStyleName("zoomLarge");
			logger.debug("after announcerInfo"); //$NON-NLS-1$

			decisionArea = new DecisionLightsWindow(false, true);
			decisionArea.setSizeFull(); //$NON-NLS-1$
			decisionArea.setHeight("35em");
			decisionArea.setVisible(false);
			//decisionArea.addStyleName("zoomMedium");
			
			imageArea = new VerticalLayout();
			plates = new LoadImage(null);
			imageArea.addComponent(plates);
			imageArea.setHeight("30em");
			imageArea.setSpacing(false);
			imageArea.setMargin(true);
			imageArea.setComponentAlignment(plates, Alignment.MIDDLE_LEFT);
			imageArea.addStyleName("zoomMedium");
			imageArea.setCaption("");
			
			horLayout.addComponent(announcerInfo);
			horLayout.addComponent(decisionArea);
			horLayout.addComponent(imageArea);

	        horLayout.setMargin(true);
	        horLayout.setSpacing(true);

	        this.addComponent(horLayout);
	        this.setComponentAlignment(horLayout, Alignment.MIDDLE_CENTER);

	        
	        // we are now fully initialized
	        announcerInfo.loadLifter(currentLifter, masterData);
	        
			// URI handler must remain, so is not part of the register/unRegister paire
			app.getMainWindow().addURIHandler(this);
	        registerAsListener();
	        doDisplay();
		} finally {
			app.setPusherDisabled(prevPusherDisabled);
			logger.warn("init pusherDisabled = {}",app.getPusherDisabled());
		}
    }


    @Override
    public void refresh() {
    }

    @Override
    public void updateEvent(SessionData.UpdateEvent updateEvent) {
    	new Thread(new Runnable() {
			@Override
			public void run() {
				if (!decisionDisplayInProgress) {
					doDisplay();
				}
			}
		}).start();
    }


	/**
	 * @param updateEvent
	 */
	private void doDisplay() {
		synchronized (app) {
            platform = Platform.getByName(platformName);
	        Lifter currentLifter = masterData.getCurrentLifter();
			boolean done = currentLifter != null && currentLifter.getAttemptsDone() >= 6;
			boolean displayLights = platform.getShowDecisionLights() && !done;
            logger.debug("loading {} ", masterData.getCurrentLifter()); //$NON-NLS-1$
            announcerInfo.loadLifter(masterData.getCurrentLifter(), masterData);
            
			if (displayLights) {
            	horLayout.setSizeFull();
            } else {
            	// looks better centered
            	horLayout.setSizeUndefined();
            }
            
	        if (ie || !done) {
	        	horLayout.setComponentAlignment(announcerInfo, Alignment.MIDDLE_CENTER);
	        	horLayout.setExpandRatio(announcerInfo, 5);
	        } else {
	        	horLayout.setComponentAlignment(announcerInfo, Alignment.MIDDLE_LEFT);
	        	horLayout.setExpandRatio(announcerInfo, 5);
	        }
	        
	        decisionArea.setVisible(false);
	        horLayout.setComponentAlignment(decisionArea, Alignment.MIDDLE_CENTER);
	        horLayout.setExpandRatio(decisionArea, 60);
	        
			imageArea.setVisible(false);
			if (ie || !done) {
				//logger.warn("recomputing image area: pusherDisabled = {}",app.getPusherDisabled());
	            plates.computeImageArea(masterData, masterData.getPlatform());
				imageArea.setVisible(true);
				horLayout.setComponentAlignment(imageArea, Alignment.MIDDLE_CENTER);
				horLayout.setExpandRatio(imageArea, 40);
			}

        }
		app.push();
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
        return viewName+"/"+(platformName == null ? "" : platformName);
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
        	platformName = CompetitionApplicationComponents.initPlatformName(); 
        }
        if (params.length >= 2) {
            platformName = params[1];
        } else {
        	platformName = CompetitionApplicationComponents.initPlatformName();
        }
    }


	@Override
	public void plateLoadingUpdate(PlatesInfoEvent event) {
		//logger.warn("plateLoadingUpdate");
		doDisplay();
	}

	
	/* Unregister listeners when window is closed.
	 * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
	 */
	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();
	}

	private void registerAsListener() {
        masterData.addListener(this);
        masterData.getRefereeDecisionController().addListener(this);
        masterData.getRefereeDecisionController().addListener(decisionArea);
        masterData.addBlackBoardListener(this);
	}
	
	private void unregisterAsListener() {
		masterData.removeListener(this);
		masterData.getRefereeDecisionController().removeListener(this);
		masterData.getRefereeDecisionController().removeListener(decisionArea);
		masterData.removeBlackBoardListener(this);
	}


	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		//logger.warn("re-registering handlers for {} {}",this,relativeUri);
		registerAsListener();
		return null;
	}


//	/**
//	 * Process a decision regarding the current lifter.
//	 * Make sure that the name of the lifter does not change until after the decision has been shown.
//	 * @see org.concordiainternational.competition.decision.DecisionEventListener#updateEvent(org.concordiainternational.competition.decision.DecisionEvent)
//	 */
//	@Override
//	public void updateEvent(final DecisionEvent updateEvent) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				synchronized (app) {
//					switch (updateEvent.getType()) {
//					case DOWN:
//						decisionDisplayInProgress = true;
//						decisionArea.setVisible(false);
//						break;
//					case SHOW:
//						// if window is not up, show it.
//						decisionDisplayInProgress = true;
//						decisionArea.setVisible(true);
//						break;
//					case RESET:
//						// we are done
//						decisionDisplayInProgress = false;
//						decisionArea.setVisible(false);
//						doDisplay();
//						break;
//					case WAITING:
//						decisionDisplayInProgress = true;
//						decisionArea.setVisible(false);
//						break;
//					case UPDATE:
//						// change is made during 3 seconds where refs
//						// can change their mind privately.
//						decisionDisplayInProgress = true;
//						decisionArea.setVisible(false);
//						break;
//					}
//				}
//				app.push();
//			}
//		}).start();
//	}
	
	/**
	 * Process a decision regarding the current lifter.
	 * Make sure that the name of the lifter does not change until after the decision has been shown.
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
						decisionDisplayInProgress = true;
						decisionArea.setVisible(false);
						break;
					case SHOW:
						// if window is not up, show it.
						decisionDisplayInProgress = true;
						shown = true;
						decisionArea.setVisible(true);
						break;
					case RESET:
						// we are done
						decisionDisplayInProgress = false;
						decisionArea.setVisible(false);
						shown = false;
						doDisplay();
						break;
					case WAITING:
						decisionDisplayInProgress = true;
						decisionArea.setVisible(false);
						break;
					case UPDATE:
						// show change only if the lights are already on.
						decisionDisplayInProgress = true;
						if (shown) {
							decisionArea.setVisible(true);
						}
						break;
					case BLOCK:
						decisionDisplayInProgress = true;
						decisionArea.setVisible(true);
					}
				}
			}
		}).start();
	}

}
