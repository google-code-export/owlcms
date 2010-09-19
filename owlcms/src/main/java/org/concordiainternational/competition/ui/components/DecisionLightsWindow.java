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

package org.concordiainternational.competition.ui.components;

import org.concordiainternational.competition.decision.DecisionController.Decision;
import org.concordiainternational.competition.decision.DecisionController.DecisionEventListener;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;

public class DecisionLightsWindow extends HorizontalLayout implements DecisionEventListener {

    private static final boolean PUSHING = true;

    private static final long serialVersionUID = 1L;

    Label[] decisionLights = new Label[3];
    CompetitionApplication app = CompetitionApplication.getCurrent();

    private Logger logger = LoggerFactory.getLogger(DecisionLightsWindow.class);

    private boolean juryMode = false;
    private ICEPush pusher;

	private boolean publicFacing;

    public DecisionLightsWindow(boolean juryMode, boolean publicFacing) {
        
        this.app = CompetitionApplication.getCurrent();
        this.publicFacing = publicFacing;
        this.juryMode = juryMode;
        
        createLights();

        this.setMargin(true);
        this.setSpacing(true);

        if (PUSHING) {
            pusher = app.ensurePusher();
        } else {
            setupPolling();
        }
        
        resetLights();
    }

	/**
	 * Create the red/white display rectangles for decisions.
	 */
	private void createLights() {
        this.setSizeFull();

        for (int i = 0; i < decisionLights.length; i++) {
            decisionLights[i] = new Label();
            decisionLights[i].setSizeFull();
            decisionLights[i].setStyleName("decisionLight");
            this.addComponent(decisionLights[i]);
            this.setExpandRatio(decisionLights[i], 100.0F / decisionLights.length);
        }
	}

    @Override
    public void updateEvent(DecisionEvent updateEvent) {
        synchronized (app) {
            Decision[] decisions = updateEvent.getDecisions();
            switch (updateEvent.getType()) {
            case DOWN:
                logger.warn("received DOWN event");
                if (juryMode) {
                    showLights(decisions);
                } else {
                    decisionLights[1].setStyleName("decisionLight");
                    decisionLights[1].addStyleName("undecided");    
                }
                decisionLights[1].addStyleName("down");

                for (int i = 0; i < decisions.length; i++) {
                    if (decisions[i].accepted == null) {
                    	// do nothing; maybe show in yellow in Jury Mode ?
                    }
                }
                break;
            case WAITING:
                logger.warn("received WAITING event");
                for (int i = 0; i < decisions.length; i++) {
                    if (decisions[i].accepted == null) {
                        // do nothing; maybe show in yellow in Jury Mode ?
                    }
                }
                break;
            case UPDATE:
                logger.warn("received UPDATE event");
                if (juryMode) {
                    showLights(decisions);
                    updateLights();
                }
                break;
            case SHOW:
                logger.warn("received SHOW event");
                showLights(decisions);
                updateLights();

                break;
            case RESET:
                logger.warn("received RESET event");
                resetLights();
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
            Boolean accepted = null;
            if (publicFacing) {
            	accepted = decisions[i].accepted;
            } else {
            	// display in reverse order relative to what public sees
            	accepted = decisions[decisionLights.length-1-i].accepted;
            }
            
            if (decisions[i] != null && accepted != null) {
                decisionLights[i].addStyleName(accepted ? "lift" : "nolift");
            } else {
                decisionLights[i].addStyleName("undecided");
            }
        }
    }

    private void resetLights() {
        for (int i = 0; i < decisionLights.length; i++) {
            decisionLights[i].setStyleName("decisionLight");
            decisionLights[i].addStyleName("undecided");
            decisionLights[i].setContentMode(Label.CONTENT_XHTML);
            decisionLights[i].setValue("&nbsp;");
        }
        updateLights();
    }

    public void refresh() {
    }

    /**
     * Push information to browser
     */
    private void updateLights() {

        if (pusher != null) {
            pusher.push();
        } else {
            synchronized (app) {
                this.requestRepaint();
            }
        }
    }


    /**
     * @param refereeIndex2
     * @return
     */
    @SuppressWarnings("unused")
	private String refereeLabel(int refereeIndex2) {
        return Messages.getString("RefereeConsole.Referee", CompetitionApplication.getCurrentLocale()) + " "
            + (refereeIndex2 + 1);
    }

    
    
    /**
     * Obsolete now that we use pushing.
     */
    private void setupPolling() {
        final ProgressIndicator refresher = new ProgressIndicator();
        refresher.setStyleName("invisible");
        this.addComponent(refresher);
        refresher.setPollingInterval(150);
    }
}
