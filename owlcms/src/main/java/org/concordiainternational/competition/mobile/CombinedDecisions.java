/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.mobile;

import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.CompetitionApplicationComponents;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.incubator.dashlayout.ui.HorDashLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

public class CombinedDecisions extends VerticalLayout implements ApplicationView {

    private static final long serialVersionUID = 1L;

    HorDashLayout top = new HorDashLayout();
    Label[] decisionLights = new Label[3];
    HorizontalLayout bottom = new HorizontalLayout();

    SessionData masterData;
    CompetitionApplication app = CompetitionApplication.getCurrent();

    private Logger logger = LoggerFactory.getLogger(CombinedDecisions.class);

    private String platformName;
    private String viewName;


    public CombinedDecisions(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());

        this.setStyleName("decisionPad");

        this.app = CompetitionApplication.getCurrent();

        if (platformName == null) {
            // get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        } else if (app.getPlatform() == null) {
            app.setPlatformByName(platformName);
        }

        
        RefereeDecisions juryDecisions = new RefereeDecisions(initFromFragment, viewName, false, true, true);
        RefereeDecisions refDecisions = new RefereeDecisions(initFromFragment, viewName, false, false, false);
        
        this.setSizeFull();
        this.addComponent(juryDecisions);
        juryDecisions.setSizeFull();
        this.setExpandRatio(juryDecisions, 0.666F);
        this.addComponent(refDecisions);
        refDecisions.setSizeFull();
        this.setExpandRatio(refDecisions, 0.333F);
    }


    @Override
    public void refresh() {
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
        String fragment = viewName + (platformName == null ? "/" : "/" + platformName);
        logger.debug("getFragment = {}", fragment);
        return fragment;
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
        } else {
            platformName = CompetitionApplicationComponents.initPlatformName();
        }
    }

    @Override
    public void registerAsListener() {
        Window mainWindow = app.getMainWindow();
        mainWindow.addListener((CloseListener) this);
    }

    @Override
    public void unregisterAsListener() {
        Window mainWindow = app.getMainWindow();
        mainWindow.removeListener((CloseListener) this);
    }

    @Override
    public void windowClose(CloseEvent e) {
        unregisterAsListener();
    }

    @Override
    public boolean needsBlack() {
        return true;
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
