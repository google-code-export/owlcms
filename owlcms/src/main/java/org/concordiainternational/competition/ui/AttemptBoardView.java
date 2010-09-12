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
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.ui.AnnouncerView.Mode;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;

public class AttemptBoardView extends Panel implements ApplicationView, GroupData.UpdateEventListener {

    Logger logger = LoggerFactory.getLogger(AttemptBoardView.class);

    private static final long serialVersionUID = 2443396161202824072L;
    // private CompetitionApplication app;
    private GroupData masterData;
    private LifterInfo announcerInfo;
    private Mode mode;
    private Platform platform;

    private LoadImage imageArea;

    private ICEPush pusher;

    private String platformName;

    private String viewName;

    private static final boolean PUSHING = true;

    public AttemptBoardView(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        
        this.mode = AnnouncerView.Mode.DISPLAY;
        final String height = "8cm";
        this.setHeight(height);

        HorizontalLayout horLayout = new HorizontalLayout();
        horLayout.setSizeFull();

        CompetitionApplication app = CompetitionApplication.getCurrent();
        if (platformName == null) {
            platformName = CompetitionApplicationComponents.initPlatformName();
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

        if (PUSHING) {
            pusher = app.ensurePusher();
        } else {
            setupPolling(horLayout);
        }

        imageArea = new LoadImage(null);
        imageArea.computeImageArea(masterData, platform);
        imageArea.setCaption("");

        // we are now fully initialized
        announcerInfo.loadLifter(masterData.getCurrentLifter(), masterData);
        masterData.addListener(this);

        horLayout.addComponent(announcerInfo);
        horLayout.addComponent(imageArea);
        horLayout.setComponentAlignment(imageArea, "middle center");
        horLayout.setExpandRatio(imageArea, 100);
        horLayout.setMargin(true);

        this.addComponent(horLayout);
    }

    /**
     * @param horLayout
     */
    private void setupPolling(HorizontalLayout horLayout) {
        ProgressIndicator refresher = new ProgressIndicator();
        refresher.setWidth("0pt"); //$NON-NLS-1$
        refresher.setPollingInterval(1000);
        refresher.addStyleName("invisible"); //$NON-NLS-1$
        horLayout.addComponent(refresher);
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateEvent(GroupData.UpdateEvent updateEvent) {
        synchronized (CompetitionApplication.getCurrent()) {
            logger.debug("loading {} ", updateEvent.getCurrentLifter()); //$NON-NLS-1$
            announcerInfo.loadLifter(masterData.getCurrentLifter(), masterData);
            imageArea.computeImageArea(masterData, platform);
        }
        if (pusher != null) {
            pusher.push();
        }
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
