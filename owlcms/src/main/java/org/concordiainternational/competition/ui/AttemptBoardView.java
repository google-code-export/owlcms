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

import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.ui.AnnouncerView.Mode;
import org.concordiainternational.competition.ui.PlatesInfoEvent.PlatesInfoListener;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

public class AttemptBoardView extends Panel implements
	ApplicationView, SessionData.UpdateEventListener, CloseListener, PlatesInfoListener {

    Logger logger = LoggerFactory.getLogger(AttemptBoardView.class);

    private static final long serialVersionUID = 2443396161202824072L;
    private SessionData masterData;
    private LifterInfo announcerInfo;
    private Mode mode;
    private Platform platform;

    private LoadImage imageArea;

    private String platformName;

    private String viewName;

    public AttemptBoardView(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        
        this.mode = AnnouncerView.Mode.DISPLAY;
        final String height = "8cm";
        this.setHeight(height);
        this.addStyleName("attemptBoardView");

        HorizontalLayout horLayout = new HorizontalLayout();
        horLayout.setSizeFull();

        CompetitionApplication app = CompetitionApplication.getCurrent();
        if (platformName == null) {
        	// get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        } else if (app.getPlatform() == null) {
        	app.setPlatformByName(platformName);
        }
        
        masterData = app.getMasterData(platformName);
        if (app != masterData.getMasterApplication()) {
            // we are not the master application; hide the menu bar.
            app.components.menu.setVisible(false);
        }
        platform = Platform.getByName(platformName);

        announcerInfo = new LifterInfo("display", masterData, mode, horLayout); //$NON-NLS-1$
        announcerInfo.addStyleName("currentLifterSummary"); //$NON-NLS-1$
        announcerInfo.setWidth(15.0F, Sizeable.UNITS_CM); //$NON-NLS-1$
        announcerInfo.setHeight(height);
        announcerInfo.setMargin(true);
        logger.debug("after announcerInfo"); //$NON-NLS-1$

        imageArea = new LoadImage(null);
        imageArea.computeImageArea(masterData, platform);
        imageArea.setCaption("");

        // we are now fully initialized
        announcerInfo.loadLifter(masterData.getCurrentLifter(), masterData);
        registerAsListener();

        horLayout.addComponent(announcerInfo);
        horLayout.addComponent(imageArea);
        horLayout.setComponentAlignment(imageArea, Alignment.MIDDLE_CENTER);
        horLayout.setExpandRatio(imageArea, 100);
        horLayout.setMargin(true);

        this.addComponent(horLayout);
    }


    @Override
    public void refresh() {
    }

    @Override
    public void updateEvent(SessionData.UpdateEvent updateEvent) {
        doPlatesInfoUpdate();
    }


	/**
	 * @param updateEvent
	 */
	private void doPlatesInfoUpdate() {
		CompetitionApplication app = CompetitionApplication.getCurrent();
		synchronized (app) {
            logger.debug("loading {} ", masterData.getCurrentLifter()); //$NON-NLS-1$
            announcerInfo.loadLifter(masterData.getCurrentLifter(), masterData);
            imageArea.computeImageArea(masterData, platform);
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
		doPlatesInfoUpdate();
	}


	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();
	}

	private void registerAsListener() {
        masterData.addListener(this);
        masterData.addBlackBoardListener(this);
	}
	
	private void unregisterAsListener() {
		// TODO Auto-generated method stub	
	}

}
