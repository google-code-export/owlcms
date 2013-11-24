/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.decision.Decision;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.decision.IDecisionController;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.mobile.IRefereeConsole;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Yes/No buttons for each referee.
 * 
 * @author jflamy
 */
/**
 * @author jflamy
 * 
 */
@SuppressWarnings("serial")
public class OJuryConsole extends VerticalLayout implements DecisionEventListener, ApplicationView, CloseListener,
        IRefereeConsole {

    private static final long serialVersionUID = 1L;

    private HorizontalLayout top = new HorizontalLayout();
    private HorizontalLayout bottom = new HorizontalLayout();

    private SessionData masterData;
    private CompetitionApplication app = CompetitionApplication.getCurrent();

    private Logger logger = LoggerFactory.getLogger(OJuryConsole.class);

    private Integer refereeIndex = null;

    private Label refereeReminder = new Label();

    private String platformName;

    private String viewName;

    private IDecisionController decisionController;

    private Label red;

    private Label white;

    public OJuryConsole(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());

        if (app == null)
            this.app = CompetitionApplication.getCurrent();

        if (platformName == null) {
            // get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        } else if (app.getPlatform() == null) {
            app.setPlatformByName(platformName);
        }

        if (masterData == null)
            masterData = app.getMasterData(platformName);
        if (decisionController == null)
            decisionController = getDecisionController();

        registerAsListener();

        this.setSizeFull();
        this.addStyleName("refereePad");
        setupTop(decisionController);
        setupBottom();

        this.addComponent(top);
        this.addComponent(bottom);
        this.setExpandRatio(top, 80.0F);
        this.setExpandRatio(bottom, 20.0F);
    }

    /**
     * @return
     */
    private IDecisionController getDecisionController() {
        if (masterData == null) {
            app = CompetitionApplication.getCurrent();
            masterData = app.getMasterData(platformName);
        }
        return masterData.getRefereeDecisionController();
    }

    /**
     * @param decisionController
     */
    private void setupTop(final IDecisionController decisionController) {
        top.setSizeFull();
        top.setMargin(true);
        top.setSpacing(true);

        red = new Label();
        red.setSizeFull();
        red.addStyleName("red");
        white = new Label();
        white.addStyleName("white");
        white.setSizeFull();
        top.addComponent(red);
        top.addComponent(white);
        top.setExpandRatio(red, 50.0F);
        top.setExpandRatio(white, 50.0F);
        top.addListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                Component child = event.getChildComponent();
                if (child == red) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            decisionController
                                    .decisionMade(refereeIndex, false);
                        }
                    }).start();
                    redSelected();
                } else if (child == white) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            decisionController.decisionMade(refereeIndex, true);
                        }
                    }).start();
                    whiteSelected();
                }
            }

            /**
			 * 
			 */
            private void whiteSelected() {
                white.removeStyleName("decisionUnselected");
                red.removeStyleName("decisionSelected");
                white.addStyleName("decisionSelected");
                red.addStyleName("decisionUnselected");
                resetBottom();
            }

            /**
			 * 
			 */
            private void redSelected() {
                white.removeStyleName("decisionSelected");
                red.removeStyleName("decisionUnselected");
                white.addStyleName("decisionUnselected");
                red.addStyleName("decisionSelected");
                resetBottom();
            }
        });

    }

    /**
	 * 
	 */
    private void setupBottom() {
        bottom.setSizeFull();
        bottom.setMargin(false);
        refereeReminder.setValue(refereeLabel(refereeIndex));
        refereeReminder.setSizeFull();
        bottom.addComponent(refereeReminder);
        refereeReminder.setStyleName("refereeOk");
    }

    /**
     * @param refereeIndex2
     * @return
     */
    private String refereeLabel(Integer refereeIndex2) {
        if (refereeIndex2 == null)
            refereeIndex2 = 0;
        return Messages.getString("RefereeConsole.Referee", CompetitionApplication.getCurrentLocale()) + " "
                + (refereeIndex2 + 1);
    }

    @Override
    public void updateEvent(final DecisionEvent updateEvent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (app) {
                    Decision[] decisions = updateEvent.getDecisions();

                    final Boolean accepted = decisions[refereeIndex].accepted;
                    switch (updateEvent.getType()) {
                    case DOWN:
                        if (accepted == null) {
                            logger.info(
                                    "referee #{} decision required after DOWN",
                                    refereeIndex + 1);
                            requireDecision();
                        }
                        break;
                    case WAITING:
                        if (accepted == null) {
                            logger.info(
                                    "referee #{} decision required WAITING",
                                    refereeIndex + 1);
                            requireDecision();
                        }
                        break;
                    case UPDATE:
                        break;
                    case BLOCK:
                        // decisions are shown to the public; prevent refs from changing.
                        white.setEnabled(false);
                        red.setEnabled(false);
                        // top.setEnabled(false);
                        break;
                    case RESET:
                        logger.info("referee #{} RESET", refereeIndex + 1);
                        white.setStyleName("white");
                        red.setStyleName("red");
                        resetTop();
                        resetBottom();
                        break;
                    }
                }
                app.push();
            }
        }).start();
    }

    /**
     * @param decisions
     */
    @SuppressWarnings("unused")
    private void allDecisionsIn(Decision[] decisions) {
        synchronized (app) {
            boolean allDecisionsIn = true;
            for (int i = 0; i < 3; i++) {
                allDecisionsIn = allDecisionsIn
                        && (decisions[i].accepted != null);
            }
            white.setEnabled(!allDecisionsIn);
            red.setEnabled(!allDecisionsIn);
        }
        app.push();
    }

    /**
	 * 
	 */
    private void requireDecision() {
        refereeReminder.setValue(Messages.getString("RefereeConsole.decisionRequired", CompetitionApplication.getCurrentLocale()));
        refereeReminder.setStyleName("refereeReminder");
        // CompetitionApplication.getCurrent().getMainWindow().executeJavaScript("alert('wakeup')");
    }

    /**
     * reset styles for top part
     */
    private void resetTop() {
        white.setEnabled(true);
        red.setEnabled(true);
    }

    @Override
    public void refresh() {
    }

    private void resetBottom() {
        synchronized (app) {
            refereeReminder.setValue(refereeLabel(refereeIndex));
            refereeReminder.setStyleName("refereeOk");
        }
        app.push();
    }

    /**
     * @param refereeIndex
     */
    @Override
    public void setIndex(int refereeIndex) {
        synchronized (app) {
            this.refereeIndex = refereeIndex;
            refereeReminder.setValue(refereeLabel(refereeIndex));
            UriFragmentUtility uriFragmentUtility = CompetitionApplication.getCurrent().getUriFragmentUtility();
            uriFragmentUtility.setFragment(getFragment(), false);
        }
        getDecisionController().addListener(this, refereeIndex);
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
        return viewName + "/" + (platformName == null ? "" : platformName) + "/" + ((int) this.refereeIndex + 1);
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
            setIndex(Integer.parseInt(params[2]) - 1);
        } else {
            throw new RuleViolationException("Error.RefereeNumberIsMissing");
        }
    }

    /**
     * Register all listeners for this app.
     */
    @Override
    public void registerAsListener() {
        logger.debug("registering as listener");
        app.getMainWindow().addListener((CloseListener) this);
        if (refereeIndex != null) {
            setIndex(refereeIndex);
        }
    }

    /**
     * Undo all registrations in {@link #registerAsListener()}.
     */
    @Override
    public void unregisterAsListener() {
        logger.debug("unregistering as listener");
        app.getMainWindow().removeListener((CloseListener) this);
        decisionController.removeListener(this);
    }

    /*
     * Will be called when page is unloaded (including on refresh).
     * 
     * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
     */
    @Override
    public void windowClose(CloseEvent e) {
        unregisterAsListener();
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
