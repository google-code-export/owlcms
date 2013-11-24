/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.mobile;

import java.net.URL;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.CompetitionApplicationComponents;
import org.concordiainternational.competition.ui.LoadImage;
import org.concordiainternational.competition.ui.PlatesInfoEvent;
import org.concordiainternational.competition.ui.PlatesInfoEvent.PlatesInfoListener;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.SessionData.UpdateEvent;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.utils.LoggerUtils;
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
 * Display information about the current athlete and lift. Shows lifter information, decision lights, and plates loading diagram.
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
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());

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
                if (menuComponent != null)
                    menuComponent.setVisible(false);
                menuComponent = app.getMobileMenu();
                if (menuComponent != null)
                    menuComponent.setVisible(false);
            }
            Platform.getByName(platformName);

            this.setSizeFull();
            this.setSpacing(true);
            // horLayout = new HorizontalLayout();

            WebApplicationContext context = (WebApplicationContext) app.getContext();
            ie = context.getBrowser().isIE();

            plates = new LoadImage(null);
            plates.setMargin(true);
            plates.addStyleName("zoomMedium");
            // horLayout.addComponent(plates);
            // horLayout.setSizeFull();
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
            if (currentLifter != null)
                nextAttemptRequestedWeight = currentLifter.getNextAttemptRequestedWeight();
            boolean done = (currentLifter != null && currentLifter.getAttemptsDone() >= 6) || nextAttemptRequestedWeight == 0;

            plates.setVisible(false);
            if (!done || ie) {
                // logger.debug("recomputing image area: pusherDisabled = {}",app.getPusherDisabled());
                plates.computeImageArea(masterData, masterData.getPlatform(), true);
                plates.setVisible(true);
                // horLayout.setComponentAlignment(plates, Alignment.MIDDLE_CENTER);
                // horLayout.setExpandRatio(plates, 80);
            }
            if (currentLifter == null) {
                plates.setVisible(true);
                plates.removeAllComponents();
                plates.setCaption(Messages.getString("PlatesInfo.waiting", app.getLocale())); //$NON-NLS-1$
            }
        }
        app.push();
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
        return viewName + "/" + (platformName == null ? "" : platformName);
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
        // logger.debug("plateLoadingUpdate");
        doDisplay();
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
    public void registerAsListener() {
        masterData.addListener(this); // weight changes
        masterData.addBlackBoardListener(this); // changes in available plates
    }

    @Override
    public void unregisterAsListener() {
        masterData.removeListener(this); // weight changes
        masterData.removeBlackBoardListener(this); // changes in available plates
    }

    @Override
    public DownloadStream handleURI(URL context, String relativeUri) {
        // logger.debug("re-registering handlers for {} {}",this,relativeUri);
        registerAsListener();
        return null;
    }

    @Override
    public void updateEvent(UpdateEvent updateEvent) {
        doDisplay();
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
