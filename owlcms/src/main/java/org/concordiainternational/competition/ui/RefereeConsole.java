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
import org.concordiainternational.competition.decision.DecisionController;
import org.concordiainternational.competition.decision.DecisionController.Decision;
import org.concordiainternational.competition.decision.DecisionController.DecisionEventListener;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.VerticalLayout;

/**
 * Yes/No buttons for each referee.
 * 
 * @author jflamy
 */
@SuppressWarnings("serial")
public class RefereeConsole extends VerticalLayout implements DecisionEventListener, ApplicationView {

    private static final long serialVersionUID = 1L;

    private HorizontalLayout top = new HorizontalLayout();
    private HorizontalLayout bottom = new HorizontalLayout();
    
    private SessionData masterData;
    private CompetitionApplication app = CompetitionApplication.getCurrent();

    private Logger logger = LoggerFactory.getLogger(RefereeConsole.class);

    private int refereeIndex;

    private Label refereeReminder = new Label();

    private String platformName;

    private String viewName;


    RefereeConsole(boolean initFromFragment, String viewName) {
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
        
        masterData = app.getMasterData(platformName);
        final DecisionController decisionController = masterData.getDecisionController();
        decisionController.addListener(this);
        
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
     * @param decisionController
     */
    private void setupTop(final DecisionController decisionController) {
        top.setSizeFull();
        top.setMargin(true);
        top.setSpacing(true);
        
        final Label red = new Label();
        red.setSizeFull();
        red.addStyleName("red");
        final Label white = new Label();
        white.addStyleName("white");
        white.setSizeFull();
        top.addComponent(red);
        top.addComponent(white);
        top.setExpandRatio(red,50.0F);
        top.setExpandRatio(white,50.0F);        
        top.addListener(new LayoutClickListener() {           
            @Override
            public void layoutClick(LayoutClickEvent event) {
                Component child = event.getChildComponent();
                if (child == red) {
                    decisionController.decisionMade(refereeIndex, false);
                    resetBottom();
                } else if (child == white) {
                    decisionController.decisionMade(refereeIndex, true);
                    resetBottom();
                }
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
    private String refereeLabel(int refereeIndex2) {
        return Messages.getString("RefereeConsole.Referee", CompetitionApplication.getCurrentLocale()) + " "
            + (refereeIndex2 + 1);
    }


    @Override
    public void updateEvent(DecisionEvent updateEvent) {
        synchronized (app) {
            Decision[] decisions = updateEvent.getDecisions();
            switch (updateEvent.getType()) {
            case DOWN:
                if (decisions[refereeIndex].accepted == null) {
                    logger.warn("referee #{} decision required after DOWN", refereeIndex+1);
                    requireDecision();
                }
                break;
            case WAITING:
                if (decisions[refereeIndex].accepted == null) {
                    logger.warn("referee #{} decision required WAITING", refereeIndex+1);
                    requireDecision();
                }
                break;
            case UPDATE:
                break;
            case SHOW:
                // decisions are shown to the public; prevent refs from changing.
                top.setEnabled(false);
                break;
            case RESET:
                logger.warn("referee #{} RESET", refereeIndex+1);
                resetTop();
                resetBottom();
                break;
            }
        }
        app.push();
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
			top.setEnabled(!allDecisionsIn);
		}
		app.push();
    }

    /**
     * 
     */
    private void requireDecision() {
        refereeReminder.setValue(Messages.getString("RefereeConsole.decisionRequired", CompetitionApplication.getCurrentLocale()));
        refereeReminder.setStyleName("refereeReminder");
        //CompetitionApplication.getCurrent().getMainWindow().executeJavaScript("alert('wakeup')");
    }

    /**
     * reset styles for top part
     */
    private void resetTop() { 
        top.setEnabled(true);
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
    public void setRefereeIndex(int refereeIndex) {
        synchronized (app) {
			this.refereeIndex = refereeIndex;
			refereeReminder.setValue(refereeLabel(refereeIndex));
			UriFragmentUtility uriFragmentUtility = CompetitionApplication
					.getCurrent().getUriFragmentUtility();
			uriFragmentUtility.setFragment(getFragment(), false);
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
        return viewName+"/"+(platformName == null ? "" : platformName)+"/"+((int)this.refereeIndex+1);
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
        
        if (params.length >= 3) {
            setRefereeIndex(Integer.parseInt(params[2])-1);
        } else {
            throw new RuleViolationException("Error.RefereeNumberIsMissing");
        }
    }


    


}
