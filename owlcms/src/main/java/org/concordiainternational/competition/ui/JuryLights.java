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
import org.concordiainternational.competition.decision.DecisionController.Decision;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.incubator.dashlayout.ui.HorDashLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class JuryLights extends Panel implements DecisionEventListener, ApplicationView {


    private static final long serialVersionUID = 1L;

    HorDashLayout top = new HorDashLayout();
    Label[] decisionLights = new Label[3];
    SessionData masterData;
    CompetitionApplication app = CompetitionApplication.getCurrent();

    private Logger logger = LoggerFactory.getLogger(JuryLights.class);

    private String platformName;
    private String viewName;

	@SuppressWarnings("unused")
	private boolean downShown;

    JuryLights(boolean initFromFragment, String viewName, boolean publicFacing) {
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
        
        createLights();

        top.setMargin(false);
        top.setSpacing(false);

        this.setContent(top);
        this.setSizeFull();
        
        resetLights();

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
            decisionLights[i].addStyleName("juryLight");
            top.addComponent(decisionLights[i]);
            top.setExpandRatio(decisionLights[i], 100.0F / decisionLights.length);
        }
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
						logger.debug("received DOWN event");
						downShown = true;
						showLights(decisions);
						//decisionLights[1].addStyleName("down");
						break;
					case WAITING:
						logger.debug("received WAITING event");
						break;
					case UPDATE:
						logger.debug("received UPDATE event");
						showLights(decisions);
						//if (downShown) decisionLights[1].addStyleName("down");
						break;
					case SHOW:
						logger.debug("received SHOW event");
						showLights(decisions);
						break;
					case RESET:
						logger.debug("received RESET event");
						resetLights();
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
			downShown = false;
		}
		app.push();
    }

    @Override
	public void refresh() {
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
            throw new RuleViolationException("Error.ViewNameIsMissing"); 
        }
        if (params.length >= 2) {
            platformName = params[1];
        } else {
        	platformName = CompetitionApplicationComponents.initPlatformName();
        }
    }
    
}
