/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.mobile;

import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.decision.Decision;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.decision.IDecisionController;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.CompetitionApplicationComponents;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.incubator.dashlayout.ui.HorDashLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

public class RefereeDecisions extends VerticalLayout implements DecisionEventListener, ApplicationView {

    private static final long serialVersionUID = 1L;

    HorDashLayout top = new HorDashLayout();
    Label[] decisionLights = new Label[3];
    HorizontalLayout bottom = new HorizontalLayout();

    SessionData masterData;
    CompetitionApplication app = CompetitionApplication.getCurrent();

    private Logger logger = LoggerFactory.getLogger(RefereeDecisions.class);

    private String platformName;
    private String viewName;

    private boolean juryMode;

    private boolean shown;
    
    private ShortcutActionListener action1ok;
    private ShortcutActionListener action1fail;
    private ShortcutActionListener action2ok;
    private ShortcutActionListener action2fail;
    private ShortcutActionListener action3ok;
    private ShortcutActionListener action3fail;

    private boolean listenToKeys;

    public RefereeDecisions(boolean initFromFragment, String viewName, boolean publicFacing, boolean juryMode, boolean listenToKeys) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());

        this.juryMode = juryMode;
        this.setStyleName("decisionPad");
        this.listenToKeys = listenToKeys;

        this.app = CompetitionApplication.getCurrent();

        if (platformName == null) {
            // get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        } else if (app.getPlatform() == null) {
            app.setPlatformByName(platformName);
        }

        
        masterData = app.getMasterData(platformName);
        createLights();

        top.setMargin(false);
        top.setSpacing(false);
        setupBottom();

        this.setSizeFull();
        this.addComponent(top);
        this.addComponent(bottom);
        this.setExpandRatio(top, 100.0F);
        this.setMargin(false);

        resetLights();
    }
    
    public RefereeDecisions(boolean initFromFragment, String viewName, boolean publicFacing) {
        this(initFromFragment,viewName, publicFacing, false, false);
        registerAsListener();
    }

    /**
	 * 
	 */
    private void createLights() {

        top.setSizeFull();

        for (int i = 0; i < decisionLights.length; i++) {
            decisionLights[i] = new Label();
            decisionLights[i].setSizeFull();
            decisionLights[i].setStyleName("decisionLight");
            decisionLights[i].addStyleName("juryLight");
            top.addComponent(decisionLights[i]);
            top.setExpandRatio(decisionLights[i], 100.0F / decisionLights.length);
        }
    }

    private void setupBottom() {
        bottom.setWidth("100%");
        bottom.setHeight("20pt");
        Label bottomLabel = new Label(
                juryMode
                        ? Messages.getString("MobileMenu.JuryDecisions", CompetitionApplication.getCurrentLocale())
                        : Messages.getString("MobileMenu.RefDecisions", CompetitionApplication.getCurrentLocale()));
        bottom.setStyleName(juryMode ? "juryDecisionsLabel" : "refereeDecisionsLabel");
        bottomLabel.setSizeUndefined();
        bottomLabel.setStyleName("refereeOk");
        bottom.addComponent(bottomLabel);
        bottom.setComponentAlignment(bottomLabel, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void updateEvent(final DecisionEvent updateEvent) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (app) {
                    Decision[] decisions = updateEvent.getDecisions();
                    switch (updateEvent.getType()) {
                    case DOWN:
                        logger.warn("received DOWN event dontShow={}", juryMode);
                        showLights(decisions, false, juryMode);
                        break;
                    case WAITING:
                        logger.warn("received WAITING event dontShow={}", juryMode);
                        showLights(decisions, false, juryMode);
                        break;
                    case UPDATE:
                        logger.warn("received UPDATE event dontShow=({] && {})", juryMode, !shown);
                        showLights(decisions, false, (juryMode && !shown)); 
                        break;
                    case SHOW:
                        logger.warn("received SHOW event dontShow={}", false);
                        showLights(decisions, false, false);
                        shown = true;
                        break;
                    case RESET:
                        logger.debug("received RESET event");
                        resetLights();
                        break;
                    case BLOCK:
                        break;
                    }
                }
                app.push();
            }
        }).start();
    }

    /**
     * @param decisions
     * @param showWaiting
     *            show lights while waiting for last referee
     * @param doNotShowDecisions
     *            do not show the decisions as they are made
     * @param juryMode 
     */
    private void showLights(Decision[] decisions, boolean showWaiting, boolean doNotShowDecisions) {
        for (int i = 0; i < decisionLights.length; i++) {
            decisionLights[i].setStyleName("decisionLight");
            decisionLights[i].setValue("&nbsp;"); // empty cell
            Boolean accepted = decisions[i].accepted;
            if (accepted == null && showWaiting) {
                decisionLights[i].addStyleName("waiting");
            } else if (accepted != null && (!doNotShowDecisions)) {
                decisionLights[i].addStyleName(accepted ? "lift" : "nolift");
            } else if (accepted != null) {
                decisionLights[i].addStyleName("refereeHasChosen");
            } else {
                decisionLights[i].addStyleName("undecided");
            }
        }
    }

    private void resetLights() {
        synchronized (app) {
            for (int i = 0; i < decisionLights.length; i++) {
                decisionLights[i].setStyleName("decisionLight");
                decisionLights[i].addStyleName("undecided");
                decisionLights[i].setContentMode(Label.CONTENT_XHTML);
                decisionLights[i].setValue("&nbsp;");
            }
            shown = false;
        }
        app.push();
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
        String fragment = viewName + (platformName == null ? "/" : "/" + platformName) + (juryMode == true ? "/jury" : "/referee");
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
        if (params.length >= 3) {
            juryMode = "jury".equalsIgnoreCase(params[2]);
        } else {
            juryMode = false;
        }
    }

    @Override
    public void registerAsListener() {
        Window mainWindow = app.getMainWindow();
        mainWindow.addListener((CloseListener) this);
        addActions(mainWindow);

        if (juryMode) {
            masterData.getJuryDecisionController().addListener(this);
        } else {
            masterData.getRefereeDecisionController().addListener(this);
        }
    }

    @Override
    public void unregisterAsListener() {
        Window mainWindow = app.getMainWindow();
        mainWindow.removeListener((CloseListener) this);
        removeActions(mainWindow);
        
        if (juryMode) {
            masterData.getJuryDecisionController().removeListener(this);
        } else {
            masterData.getRefereeDecisionController().removeListener(this);
        }
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
    

    @SuppressWarnings("serial")
    private abstract class ShortcutActionListener extends ShortcutAction implements Action.Listener {

        public ShortcutActionListener(String caption, int kc, int[] m) {
            super(caption, kc, m);
        }

        public ShortcutActionListener(String caption, int kc) {
            super(caption, kc, null);
        }

    }
    
    @SuppressWarnings("serial")
    private void addActions(Action.Notifier actionNotifier) {
        if (!listenToKeys) return;
        
        logger.info("{} listening to keys",(juryMode ? "jury" : "referee"));
        
        IDecisionController aDecisionController = null;
        if (juryMode) {
            aDecisionController = masterData.getJuryDecisionController();
        } else {
            aDecisionController = masterData.getRefereeDecisionController();  
        }
        
        final IDecisionController decisionController = aDecisionController;
        masterData.getJuryDecisionController();
        action1ok = new ShortcutActionListener("1+", ShortcutAction.KeyCode.NUM1) {
            @Override
            public void handleAction(Object sender, Object target) {
                decisionController.decisionMade(0, true);
            }
        };
        action1fail = new ShortcutActionListener("1-", ShortcutAction.KeyCode.NUM2) {
            @Override
            public void handleAction(Object sender, Object target) {
                decisionController.decisionMade(0, false);
            }
        };
        action2ok = new ShortcutActionListener("2+", ShortcutAction.KeyCode.NUM3) {
            @Override
            public void handleAction(Object sender, Object target) {
                decisionController.decisionMade(1, true);
            }
        };
        action2fail = new ShortcutActionListener("2-", ShortcutAction.KeyCode.NUM4) {
            @Override
            public void handleAction(Object sender, Object target) {
                decisionController.decisionMade(1, false);
            }
        };
        action3ok = new ShortcutActionListener("3+", ShortcutAction.KeyCode.NUM5) {
            @Override
            public void handleAction(Object sender, Object target) {
                decisionController.decisionMade(2, true);
            }
        };
        action3fail = new ShortcutActionListener("3-", ShortcutAction.KeyCode.NUM6) {
            @Override
            public void handleAction(Object sender, Object target) {
                decisionController.decisionMade(2, false);
            }
        };
        
        actionNotifier.addAction(action1ok);
        actionNotifier.addAction(action1fail);
        actionNotifier.addAction(action2ok);
        actionNotifier.addAction(action2fail);
        actionNotifier.addAction(action3ok);
        actionNotifier.addAction(action3fail);
    }

    private void removeActions(Action.Notifier actionNotifier) {
        if (!listenToKeys) return;
        
        logger.info("{} stopped listening to keys",(juryMode ? "jury" : "referee"));
        
        actionNotifier.removeAction(action1ok);
        actionNotifier.removeAction(action1fail);
        actionNotifier.removeAction(action2ok);
        actionNotifier.removeAction(action2fail);
        actionNotifier.removeAction(action3ok);
        actionNotifier.removeAction(action3fail);
    }

}
