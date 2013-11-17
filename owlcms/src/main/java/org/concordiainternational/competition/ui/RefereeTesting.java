/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.net.URL;

import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.components.DecisionLightsWindow;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

public class RefereeTesting extends VerticalSplitPanel implements ApplicationView, CloseListener, URIHandler {

    private static final String CELL_WIDTH = "8em";

    private static final long serialVersionUID = 1L;

    GridLayout bottom;
    SessionData masterData;
    CompetitionApplication app = CompetitionApplication.getCurrent();

    @SuppressWarnings("unused")
    private Logger logger = LoggerFactory.getLogger(RefereeTesting.class);

    private String platformName;
    private String viewName;

    private DecisionLightsWindow decisionArea;

    RefereeTesting(boolean initFromFragment, String viewName, boolean juryMode, boolean publicFacing) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view,getLoggingId());

        this.app = CompetitionApplication.getCurrent();

        if (platformName == null) {
            // get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        } else if (app.getPlatform() == null) {
            app.setPlatformByName(platformName);
        }

        createLights();

        HorizontalLayout top = new HorizontalLayout();
        top.setSizeFull();
        top.setStyleName("decisionLightsWindow");
        top.addComponent(decisionArea);
        this.setFirstComponent(top);
        bottom = createDecisionButtons();
        this.setSecondComponent(bottom);
        setSplitPosition(75);

        resetBottom();
        // URI handler must remain, so is not part of the register/unRegister paire
        app.getMainWindow().addURIHandler(this);
        registerAsListener();
    }

    /**
	 * 
	 */
    private void createLights() {
        masterData = app.getMasterData(platformName);
        decisionArea = new DecisionLightsWindow(false, true);
        masterData.getRefereeDecisionController().addListener(decisionArea);

        decisionArea.setSizeFull(); //$NON-NLS-1$
        // decisionArea.setHeight("35em");

    }

    private GridLayout createDecisionButtons() {
        GridLayout bottom1 = new GridLayout(3, 3);
        bottom1.setMargin(true);
        bottom1.setSpacing(true);

        createLabel(bottom1, refereeLabel(0));
        createLabel(bottom1, refereeLabel(1));
        createLabel(bottom1, refereeLabel(2));

        for (int i = 0; i < 3; i++) {
            final int j = i;
            final NativeButton yesButton = new NativeButton("oui", new ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(ClickEvent event) {
                    masterData.getRefereeDecisionController().decisionMade(j, true);
                }

            });
            yesButton.addStyleName("referee"); //$NON-NLS-1$
            yesButton.addStyleName("yesButton"); //$NON-NLS-1$
            yesButton.setWidth(CELL_WIDTH);
            bottom1.addComponent(yesButton);
        }
        for (int i = 0; i < 3; i++) {
            final int j = i;
            final NativeButton noButton = new NativeButton("non", new ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(ClickEvent event) {
                    masterData.getRefereeDecisionController().decisionMade(j, false);
                }

            });
            noButton.addStyleName("referee"); //$NON-NLS-1$
            noButton.addStyleName("noButton"); //$NON-NLS-1$
            noButton.setWidth(CELL_WIDTH);
            bottom1.addComponent(noButton);
        }
        return bottom1;
    }

    /**
     * @param bottom1
     * @param width
     */
    private void createLabel(GridLayout bottom1, String caption) {
        final Label ref1Label = new Label("");
        ref1Label.setCaption(caption);
        ref1Label.setWidth(CELL_WIDTH);
        ref1Label.addStyleName("refereeButtonLabel");
        bottom1.addComponent(ref1Label);
    }

    private void resetBottom() {
        synchronized (app) {
            for (int i = 0; i < 3; i++) {
                ((Label) bottom.getComponent(i, 0)).setValue(" ");
            }
        }
        app.push();
    }

    @Override
    public void refresh() {
    }

    /**
     * @param refereeIndex2
     * @return
     */
    private String refereeLabel(int refereeIndex2) {
        return Messages.getString("RefereeConsole.Referee", CompetitionApplication.getCurrentLocale()) + " "
                + (refereeIndex2 + 1);
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
        masterData.getRefereeDecisionController().addListener(decisionArea);
    }

    @Override
    public void unregisterAsListener() {
        masterData.getRefereeDecisionController().removeListener(decisionArea);
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
    public DownloadStream handleURI(URL context, String relativeUri) {
        // logger.debug("re-registering handlers for {} {}",this,relativeUri);
        registerAsListener();
        return null;
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
