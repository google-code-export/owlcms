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

import java.net.URL;

import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.decision.Decision;
import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;
import org.concordiainternational.competition.decision.IDecisionController;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.mobile.IRefereeConsole;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
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
public class ORefereeConsole extends VerticalLayout implements DecisionEventListener, ApplicationView, CloseListener, URIHandler, IRefereeConsole {

	private static final long serialVersionUID = 1L;

	private HorizontalLayout top = new HorizontalLayout();
	private HorizontalLayout bottom = new HorizontalLayout();

	private SessionData masterData;
	private CompetitionApplication app = CompetitionApplication.getCurrent();

	private Logger logger = LoggerFactory.getLogger(ORefereeConsole.class);

	private Integer refereeIndex = null;

	private Label refereeReminder = new Label();

	private String platformName;

	private String viewName;

	private IDecisionController decisionController;

	private Label red;

	private Label white;


	public ORefereeConsole(boolean initFromFragment, String viewName) {
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
		top.setExpandRatio(red,50.0F);
		top.setExpandRatio(white,50.0F);        
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
		if (refereeIndex2 == null) refereeIndex2 = 0;
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
						//top.setEnabled(false);
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
		//CompetitionApplication.getCurrent().getMainWindow().executeJavaScript("alert('wakeup')");
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
		getDecisionController().addListener(this,refereeIndex);
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
		app.getMainWindow().removeListener((CloseListener)this);
		decisionController.removeListener(this);
	}


	/* Will be called when page is loaded.
	 * @see com.vaadin.terminal.URIHandler#handleURI(java.net.URL, java.lang.String)
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
	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();
	}


}
