/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.components;

import org.concordiainternational.competition.decision.Decision;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class DecisionLightsWindow extends HorizontalLayout implements DecisionEventListener {

    private static final long serialVersionUID = 1L;

    Label[] decisionLights = new Label[3];
    CompetitionApplication app = CompetitionApplication.getCurrent();

    private Logger logger = LoggerFactory.getLogger(DecisionLightsWindow.class);

    private boolean immediateMode = false;

	private boolean publicFacing;

	private boolean shown = false;

    public DecisionLightsWindow(boolean immediateMode, boolean publicFacing) {
        
        this.app = CompetitionApplication.getCurrent();
        this.publicFacing = publicFacing;
        this.immediateMode = immediateMode;
        
        createLights();

        this.setMargin(true);
        this.setSpacing(true);
        
        resetLights();
    }

	/**
	 * Create the red/white display rectangles for decisions.
	 */
	private void createLights() {
		logger.debug("createLights");
        this.setSizeFull();

        for (int i = 0; i < decisionLights.length; i++) {
            decisionLights[i] = new Label();
            decisionLights[i].setSizeFull();
            decisionLights[i].setStyleName("decisionLight");
            this.addComponent(decisionLights[i]);
            this.setComponentAlignment(decisionLights[i], Alignment.MIDDLE_CENTER);
            this.setExpandRatio(decisionLights[i], 100.0F / decisionLights.length);
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
						doDown();
						if (immediateMode) {
							showLights(decisions);
//							decisionLights[1].addStyleName("down");
						} else {
//							decisionLights[1].setStyleName("decisionLight");
//							decisionLights[1].addStyleName("undecided");
						}


						for (int i = 0; i < decisions.length; i++) {
							if (decisions[i].accepted == null) {
								// do nothing; maybe show in yellow in Jury Mode ?
							}
						}
						break;
					case WAITING:
						logger.debug("received WAITING event");
						for (int i = 0; i < decisions.length; i++) {
							if (decisions[i].accepted == null) {
								// do nothing; maybe show in yellow in Jury Mode ?
							}
						}
						break;
					case UPDATE:
						logger.debug("received UPDATE event");
						DecisionLightsWindow.this.removeStyleName("down");
						if (immediateMode || shown) {
							logger.debug("showing immediateMode={} shown={}",immediateMode, shown);
							showLights(decisions);
						} else {
							logger.debug("not showing {} {}",immediateMode, shown);
						}
						break;
					case SHOW:
						logger.debug("received SHOW event, removing down");
						DecisionLightsWindow.this.removeStyleName("down");
						showLights(decisions);
						break;
					case BLOCK:
						logger.debug("received BLOCK event, removing down");
						DecisionLightsWindow.this.removeStyleName("down");
						showLights(decisions);
						break;
					case RESET:
						logger.debug("received RESET event");
						DecisionLightsWindow.this.removeStyleName("down");
						resetLights();
						break;
					default:
						logger.debug("received default");
						break;
					}
				}
				app.push();
			}


		}).start();
    }

	/**
	 * show down signal in window.
	 */
	public void doDown() {
		this.addStyleName("down");
		//decisionLights[1].addStyleName("down");
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
        shown = true;
    }

    private void resetLights() {
    	synchronized(app) {
    		for (int i = 0; i < decisionLights.length; i++) {
    			decisionLights[i].setStyleName("decisionLight");
    			decisionLights[i].addStyleName("undecided");
    			decisionLights[i].setContentMode(Label.CONTENT_XHTML);
    			decisionLights[i].setValue("&nbsp;");
    		}
    	}
    	shown = false;
        app.push();
    }

    public void refresh() {
    }


    /**
     * @param refereeIndex2
     * @return
     */
    @SuppressWarnings("unused")
	private String refereeLabel(int refereeIndex2) {
        return Messages.getString("ORefereeConsole.Referee", CompetitionApplication.getCurrentLocale()) + " "
            + (refereeIndex2 + 1);
    }


}
