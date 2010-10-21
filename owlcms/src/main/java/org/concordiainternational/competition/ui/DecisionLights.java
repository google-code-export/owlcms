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

import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionController.Decision;
import org.concordiainternational.competition.decision.DecisionController.DecisionEventListener;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class DecisionLights extends SplitPanel implements DecisionEventListener, ApplicationView {

    private static final String CELL_WIDTH = "8em";
    private static final boolean PUSHING = true;

    private static final long serialVersionUID = 1L;

    HorizontalLayout top = new HorizontalLayout();
    Label[] decisionLights = new Label[3];
    GridLayout bottom;
    SessionData masterData;
    CompetitionApplication app = CompetitionApplication.getCurrent();

    private Logger logger = LoggerFactory.getLogger(DecisionLights.class);

    private boolean juryMode = false;
    private ICEPush pusher;
    private String platformName;
    private String viewName;

    DecisionLights(boolean initFromFragment, String viewName, boolean juryMode, boolean publicFacing) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        
        this.app = CompetitionApplication.getCurrent();
        
        if (platformName == null) {
        	// get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        } else if (app.getPlatform() == null) {
        	app.setPlatformByName(platformName);
        }
        this.juryMode = juryMode;
        
        createLights();

        top.setMargin(true);
        top.setSpacing(true);

        this.setFirstComponent(top);
        bottom = createDecisionButtons();
        this.setSecondComponent(bottom);
        setSplitPosition(650, UNITS_PIXELS);

        if (PUSHING) {
            pusher = app.ensurePusher();
        } else {
            setupPolling();
        }
        
        resetLights();
        resetBottom();

    }

	/**
	 * 
	 */
	private void createLights() {
		masterData = app.getMasterData(platformName);
        masterData.getDecisionController().addListener(this);
        top.setSizeFull();

        for (int i = 0; i < decisionLights.length; i++) {
            decisionLights[i] = new Label();
            decisionLights[i].setSizeFull();
            decisionLights[i].setStyleName("decisionLight");
            top.addComponent(decisionLights[i]);
            top.setExpandRatio(decisionLights[i], 100.0F / decisionLights.length);
        }
	}

    private GridLayout createDecisionButtons() {
        GridLayout bottom = new GridLayout(3, 3);
        bottom.setMargin(true);
        bottom.setSpacing(true);

        createLabel(bottom, refereeLabel(0));
        createLabel(bottom, refereeLabel(1));
        createLabel(bottom, refereeLabel(2));

        for (int i = 0; i < decisionLights.length; i++) {
            final int j = i;
            final NativeButton yesButton = new NativeButton("oui", new ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(ClickEvent event) {
                    masterData.getDecisionController().decisionMade(j, true);
                }

            });
            yesButton.addStyleName("referee"); //$NON-NLS-1$
            yesButton.addStyleName("yesButton"); //$NON-NLS-1$
            yesButton.setWidth(CELL_WIDTH);
            bottom.addComponent(yesButton);
        }
        for (int i = 0; i < decisionLights.length; i++) {
            final int j = i;
            final NativeButton noButton = new NativeButton("non", new ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(ClickEvent event) {
                    masterData.getDecisionController().decisionMade(j, false);
                }

            });
            noButton.addStyleName("referee"); //$NON-NLS-1$
            noButton.addStyleName("noButton"); //$NON-NLS-1$
            noButton.setWidth(CELL_WIDTH);
            bottom.addComponent(noButton);
        }
        return bottom;
    }

    /**
     * @param bottom
     * @param width
     */
    private void createLabel(GridLayout bottom, String caption) {
        final Label ref1Label = new Label("");
        ref1Label.setCaption(caption);
        ref1Label.setWidth(CELL_WIDTH);
        ref1Label.addStyleName("refereeButtonLabel");
        bottom.addComponent(ref1Label);
    }

    @Override
    public void updateEvent(DecisionEvent updateEvent) {
        synchronized (app) {
            Decision[] decisions = updateEvent.getDecisions();
            switch (updateEvent.getType()) {
            case DOWN:
                logger.debug("received DOWN event");
                if (juryMode) {
                    showLights(decisions);
                } else {
                    decisionLights[1].setStyleName("decisionLight");
                    decisionLights[1].addStyleName("undecided");    
                }
                decisionLights[1].addStyleName("down");

                for (int i = 0; i < decisions.length; i++) {
                    if (decisions[i].accepted == null) {
                        ((Label) bottom.getComponent(i, 0)).setValue("decision required");
                    }
                }
                updateBottom();
                break;
            case WAITING:
                logger.debug("received WAITING event");
                for (int i = 0; i < decisions.length; i++) {
                    if (decisions[i].accepted == null) {
                        ((Label) bottom.getComponent(i, 0)).setValue("decision required");
                    }
                }
                updateBottom();
                break;
            case UPDATE:
                logger.debug("received UPDATE event");
                if (juryMode) {
                    showLights(decisions);
                    updateTop();
                }
                break;
            case SHOW:
                logger.debug("received SHOW event");
                showLights(decisions);
                updateTop();

                break;
            case RESET:
                logger.debug("received RESET event");
                resetLights();
                resetBottom();
                break;
            }
        }
        if (pusher != null) {
            pusher.push();
        }
    }

    /**
     * @param decisions
     */
    private void showLights(Decision[] decisions) {
        for (int i = 0; i < decisionLights.length; i++) {
            decisionLights[i].setStyleName("decisionLight");
            Boolean accepted = decisions[i].accepted;
            if (decisions[i] != null && accepted != null) {
                decisionLights[i].addStyleName(accepted ? "lift" : "nolift");
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
		}
		updateTop();
    }

    private void resetBottom() {
        synchronized (app) {
			for (int i = 0; i < decisionLights.length; i++) {
				((Label) bottom.getComponent(i, 0)).setValue(" ");
			}
		}
		updateBottom();
    }

    @Override
	public void refresh() {
    }

    /**
     * 
     */
    private void updateTop() {

        if (pusher != null) {
            pusher.push();
        } else {
            synchronized (app) {
                top.requestRepaint();
            }
        }
    }

    /**
     * 
     */
    private void updateBottom() {

        if (pusher != null) {
            pusher.push();
        } else {
            synchronized (app) {
                bottom.requestRepaint();
            }
            ;
        }
    }

    /**
     * @param refereeIndex2
     * @return
     */
    private String refereeLabel(int refereeIndex2) {
        return Messages.getString("RefereeConsole.Referee", CompetitionApplication.getCurrentLocale()) + " "
            + (refereeIndex2 + 1);
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
    
    /**
     * 
     */
    private void setupPolling() {
        final ProgressIndicator refresher = new ProgressIndicator();
        refresher.setStyleName("invisible");
        top.addComponent(refresher);
        refresher.setPollingInterval(150);
    }
}
