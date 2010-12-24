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
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.PlatesInfoEvent.PlatesInfoListener;
import org.concordiainternational.competition.ui.SessionData.UpdateEvent;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
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

public class MPlatesInfoView extends VerticalLayout implements
	ApplicationView,
	CloseListener,
	PlatesInfoListener,
	SessionData.UpdateEventListener,
	URIHandler {

    Logger logger = LoggerFactory.getLogger(MPlatesInfoView.class);

    private static final long serialVersionUID = 2443396161202824072L;
    private SessionData masterData;

    private String platformName;

    private String viewName;

	private LoadImage plates;

	private boolean ie;

	private CompetitionApplication app;

    public MPlatesInfoView(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        
        app = CompetitionApplication.getCurrent();
        this.addStyleName("loadChart");


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
				menuComponent = app.mobileMenu;
				if (menuComponent != null) menuComponent.setVisible(false);
            }
            Platform.getByName(platformName);
            
        	this.setSizeFull();
        	this.setSpacing(true);
            //horLayout = new HorizontalLayout();
        	
            WebApplicationContext context = (WebApplicationContext) app.getContext();
			ie = context.getBrowser().isIE();
			
			plates = new LoadImage(null);
			plates.setMargin(true);
			plates.addStyleName("zoomMedium");
			//horLayout.addComponent(plates);
			//horLayout.setSizeFull();
	        this.addComponent(plates);
	        this.setComponentAlignment(plates, Alignment.MIDDLE_CENTER);
	        
			// URI handler must remain, so is not part of the register/unRegister pair
			app.getMainWindow().addURIHandler(this);
	        registerAsListener();
	        doDisplay();
		} finally {
			app.setPusherDisabled(prevPusherDisabled);
		}
    }


    @Override
    public void refresh() {
    }


	/**
	 * @param updateEvent
	 */
	private void doDisplay() {
		synchronized (app) {
            Platform.getByName(platformName);
	        Lifter currentLifter = masterData.getCurrentLifter();
			Integer nextAttemptRequestedWeight = 0;
			if (currentLifter != null) nextAttemptRequestedWeight = currentLifter.getNextAttemptRequestedWeight();
			boolean done = (currentLifter != null && currentLifter.getAttemptsDone() >= 6) || nextAttemptRequestedWeight == 0;
            
			plates.setVisible(false);
			if (!done || ie) {
				//logger.warn("recomputing image area: pusherDisabled = {}",app.getPusherDisabled());
	            plates.computeImageArea(masterData, masterData.getPlatform());
				plates.setVisible(true);
				//horLayout.setComponentAlignment(plates, Alignment.MIDDLE_CENTER);
				//horLayout.setExpandRatio(plates, 80);
			}
			if (currentLifter == null) {
				plates.setVisible(true);
				plates.removeAllComponents();
				plates.setCaption(Messages.getString("PlatesInfo.waiting", app.getLocale())); //$NON-NLS-1$
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
		masterData.addListener(this); // weight changes
        masterData.addBlackBoardListener(this); // changes in available plates
	}
	
	private void unregisterAsListener() {
		masterData.removeListener(this); // weight changes
		masterData.removeBlackBoardListener(this); // changes in available plates
	}


	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		//logger.warn("re-registering handlers for {} {}",this,relativeUri);
		registerAsListener();
		return null;
	}


	@Override
	public void updateEvent(UpdateEvent updateEvent) {
		doDisplay();
	}



}
