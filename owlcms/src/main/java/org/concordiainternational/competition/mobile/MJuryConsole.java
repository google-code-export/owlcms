/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.mobile;

import java.net.URL;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.touchdiv.TouchDiv;
import org.vaadin.touchdiv.TouchDiv.TouchEvent;
import org.vaadin.touchdiv.TouchDiv.TouchListener;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.Alignment;
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
public class MJuryConsole extends VerticalLayout implements DecisionEventListener, ApplicationView, CloseListener, URIHandler, IRefereeConsole {

	private static final long serialVersionUID = 1L;

	private HorizontalLayout top = new HorizontalLayout();
	private HorizontalLayout bottom = new HorizontalLayout();

	private SessionData masterData;
	private CompetitionApplication app = CompetitionApplication.getCurrent();

	private Logger logger = LoggerFactory.getLogger(MJuryConsole.class);

	private Integer juryIndex = null;

	private Label juryReminder = new Label();

	private String platformName;

	private String viewName;

	private IDecisionController decisionController;

	private TouchDiv red;

	private TouchDiv white;


	public MJuryConsole(boolean initFromFragment, String viewName) {
		if (initFromFragment) {
			setParametersFromFragment();
		} else {
			this.viewName = viewName;
		}

		if (app == null) this.app = CompetitionApplication.getCurrent();

		if (platformName == null) {
			// get the default platform name
			platformName = CompetitionApplicationComponents.initPlatformName();
		} else if (app.getPlatform() == null) {
			app.setPlatformByName(platformName);
		}

		if (masterData == null) masterData = app.getMasterData(platformName);
		if (decisionController == null) decisionController = getDecisionController();

		app.getMainWindow().addURIHandler(this);
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
		return masterData.getJuryDecisionController();
	}



	/**
	 * @param decisionController
	 */
	private void setupTop(final IDecisionController decisionController) {
		top.setSizeFull();
		top.setMargin(true);
		top.setSpacing(true);

		red = new TouchDiv("");
		red.setHeight("90%");
		red.setWidth("90%");
		red.addStyleName("red");
		red.addListener(new TouchListener(){

			@Override
			public void onTouch(TouchEvent event) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						decisionController.decisionMade(juryIndex, false);
					}
				}).start();
				redSelected();
			}});
		
		white = new TouchDiv("");
		white.addStyleName("white");
		white.setHeight("90%");
		white.setWidth("90%");
		white.addListener(new TouchListener(){

			@Override
			public void onTouch(TouchEvent event) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						decisionController
								.decisionMade(juryIndex, true);
					}
				}).start();
				whiteSelected();
			}});
		
		top.addComponent(red);
		top.setComponentAlignment(red,Alignment.MIDDLE_CENTER);
		
		top.addComponent(white);
		top.setComponentAlignment(white,Alignment.MIDDLE_CENTER);
		
		top.setExpandRatio(red,50.0F);
		top.setExpandRatio(white,50.0F);
	}
	
	/**
	 * 
	 */
	private void whiteSelected() {
		white.removeStyleName("decisionUnselected");
		red.removeStyleName("decisionSelected");

		white.addStyleName("decisionSelected");
		red.addStyleName("decisionUnselected");
		
		white.setValue("\u2714"); // heavy checkmark
		red.setValue("");
		
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
		
		white.setValue("");
		red.setValue("\u2714"); // heavy checkmark
		
		resetBottom();
	}
	
	/**
	 * 
	 */
	private void setupBottom() {
		bottom.setSizeFull();
		bottom.setMargin(false);
		juryReminder.setValue(juryLabel(juryIndex));
		juryReminder.setSizeFull();
		bottom.addComponent(juryReminder);
		juryReminder.setStyleName("juryOk");
	}


	/**
	 * @param refereeIndex2
	 * @return
	 */
	private String juryLabel(Integer refereeIndex2) {
		if (refereeIndex2 == null) refereeIndex2 = 0;
		return Messages.getString("RefereeConsole.Jury", CompetitionApplication.getCurrentLocale()) + " "
		+ (refereeIndex2 + 1);
	}


	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.IRefereeConsole#updateEvent(org.concordiainternational.competition.decision.DecisionEvent)
	 */
	@Override
	public void updateEvent(final DecisionEvent updateEvent) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (app) {
					Decision[] decisions = updateEvent.getDecisions();

					final Boolean accepted = decisions[juryIndex].accepted;
					switch (updateEvent.getType()) {
					case DOWN:
						if (accepted == null) {
							logger.info(
									"jury #{} decision required after DOWN",
									juryIndex + 1);
							requireDecision();
						}
						break;
					case WAITING:
						if (accepted == null) {
							logger.info(
									"jury #{} decision required WAITING",
									juryIndex + 1);
							requireDecision();
						}
						break;
					case UPDATE:
						break;
					case BLOCK:
						// decisions are shown to the public; prevent refs from changing.
						white.setEnabled(false);
						red.setEnabled(false);
						juryReminder.setStyleName("blocked");
						//top.setEnabled(false);
						break;
					case RESET:
						logger.info("jury #{} RESET", juryIndex + 1);
						white.setStyleName("white");
						red.setStyleName("red");
						resetTop();
						resetBottom();
						requestRepaintAll();
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
		juryReminder.setValue(Messages.getString("RefereeConsole.decisionRequired", CompetitionApplication.getCurrentLocale()));
		juryReminder.setStyleName("refereeReminder");
		//CompetitionApplication.getCurrent().getMainWindow().executeJavaScript("alert('wakeup')");
	}

	/**
	 * reset styles for top part
	 */
	private void resetTop() { 
		white.setEnabled(true);
		red.setEnabled(true);
		white.setValue("");
		red.setValue("");
	}

	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.IRefereeConsole#refresh()
	 */
	@Override
	public void refresh() {
	}


	private void resetBottom() {
		synchronized (app) {
			juryReminder.setEnabled(true);
			juryReminder.setValue(juryLabel(juryIndex));
			juryReminder.setStyleName("juryOk");
		}
		app.push();
	}


	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.IRefereeConsole#setRefereeIndex(int)
	 */
	@Override
	public void setIndex(int refereeIndex) {
		synchronized (app) {
			this.juryIndex = refereeIndex;
			juryReminder.setValue(juryLabel(refereeIndex));
			UriFragmentUtility uriFragmentUtility = CompetitionApplication.getCurrent().getUriFragmentUtility();
			uriFragmentUtility.setFragment(getFragment(), false);
		}
		getDecisionController().addListener(this,refereeIndex);
		app.push();
	}


	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.components.ApplicationView#needsMenu()
	 */
	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.IRefereeConsole#needsMenu()
	 */
	@Override
	public boolean needsMenu() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.IRefereeConsole#getFragment()
	 */
	@Override
	public String getFragment() {
		return viewName+"/"+(platformName == null ? "" : platformName)+"/"+((int)this.juryIndex+1);
	}


	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.IRefereeConsole#setParametersFromFragment()
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
			setIndex(Integer.parseInt(params[2])-1);
		} else {
			throw new RuleViolationException("Error.RefereeNumberIsMissing");
		}
	}
	

	/**
	 * Register all listeners for this app.
	 * Exception: do not register the URIHandler here.
	 */
	@Override
	public void registerAsListener() {
		logger.debug("registering as listener");
		app.getMainWindow().addListener((CloseListener)this);
		if (juryIndex != null) {
			setIndex(juryIndex);
		}
	}


	/**
	 * Undo all registrations in {@link #registerAsListener()}.
	 */
	@Override
	public void unregisterAsListener() {
		logger.debug("unregistering as listener");
		app.getMainWindow().removeListener((CloseListener)this);
		decisionController.removeListener(this);
	}


	/* Will be called when page is loaded.
	 * @see com.vaadin.terminal.URIHandler#handleURI(java.net.URL, java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.IRefereeConsole#handleURI(java.net.URL, java.lang.String)
	 */
	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		registerAsListener();
		app.getMainWindow().executeJavaScript("scrollTo(0,1)");
		return null;
	}


	/* Will be called when page is unloaded (including on refresh).
	 * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
	 */
	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.IRefereeConsole#windowClose(com.vaadin.ui.Window.CloseEvent)
	 */
	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();
	}



}
