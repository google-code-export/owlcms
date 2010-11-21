package org.concordiainternational.competition.mobile;


import java.util.List;

import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.ui.AttemptBoardView;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.JuryLights;
import org.concordiainternational.competition.ui.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class MobileMenu extends VerticalLayout {

	public static final String BUTTON_WIDTH = "6em";

	public static final String BUTTON_HEIGHT = "3em";

	private CompetitionApplication app;

	private List<Platform> platforms;

	private static Logger logger = LoggerFactory.getLogger(MobileMenu.class);

	public MobileMenu() {
		app = CompetitionApplication.getCurrent();
		platforms = Platform.getAll();
		if (platforms.size() > 1) {
			final MPlatformSelect platformSelection = new MPlatformSelect();
			this.addComponent(platformSelection);
		}
		final MRefereeSelect refereeSelection = new MRefereeSelect();
		this.addComponent(refereeSelection);
		final MRefereeDecisions refereeDecisions = new MRefereeDecisions();
		this.addComponent(refereeDecisions);
		final MPlatesInfo platesInfo = new MPlatesInfo();
		this.addComponent(platesInfo);
		this.setStyleName("mobileMenu");
		this.setSpacing(true);
		this.setMargin(true);
	}

	public class MRefereeDecisions extends HorizontalLayout {
		public MRefereeDecisions() {
			this.setSpacing(true);
			final Label label = new Label("Décisions des officiels");
			this.addComponent(label);
			this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
			final NativeButton button = new NativeButton("Afficher", new Button.ClickListener() {			
				@Override
				public void buttonClick(ClickEvent event) {
					JuryLights refereeDecisions = createRefereeDecisions();
					app.setMainLayoutContent(refereeDecisions);
				}
			});
			button.setWidth(BUTTON_WIDTH);
			button.setHeight(BUTTON_HEIGHT);
			this.addComponent(button);
		}
	}

	public class MPlatesInfo extends HorizontalLayout {
		public MPlatesInfo() {
			this.setSpacing(true);
			final Label label = new Label("Plaques");
			this.addComponent(label);
			this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
			final NativeButton button = new NativeButton("Afficher", new Button.ClickListener() {			
				@Override
				public void buttonClick(ClickEvent event) {
					AttemptBoardView attempt = new AttemptBoardView(false, "AttemptBoard");
					app.setMainLayoutContent(attempt);
				}
			});
			button.setWidth(BUTTON_WIDTH);
			button.setHeight(BUTTON_HEIGHT);
			this.addComponent(button);
		}
	}

	public class MJuryMemberSelect extends HorizontalLayout {

	}

	public class MPlatformSelect extends HorizontalLayout {
		public MPlatformSelect() {
			final Label label = new Label("Plateaux");
			this.addComponent(label);
			this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);

			for (Platform platform: platforms) {
				final String platformName = platform.getName();
				final NativeButton button = new NativeButton(platformName, new Button.ClickListener() {			
					@Override
					public void buttonClick(ClickEvent event) {
						app.setPlatformByName(platformName);
                        SessionData masterData = app.getMasterData(platformName);
                        logger.debug("new platform={}, new group = {}", platformName, masterData.getCurrentSession()); //$NON-NLS-1$
                        app.setCurrentCompetitionSession(masterData.getCurrentSession());
					}
				});
				button.setWidth(BUTTON_WIDTH);
				button.setHeight(BUTTON_HEIGHT);
				this.addComponent(button);
			}
		}
	}

	public class MRefereeSelect extends HorizontalLayout {

		MRefereeSelect() {
			this.setSpacing(true);
			final Label label = new Label("Arbitres");
			this.addComponent(label);
			this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
			final NativeButton button1 = new NativeButton("1", new Button.ClickListener() {			
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayRefereeConsole(0);
				}
			});
			button1.setWidth(BUTTON_WIDTH);
			button1.setHeight(BUTTON_HEIGHT);
			this.addComponent(button1);

			final NativeButton button2 = new NativeButton("2", new Button.ClickListener() {			
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayRefereeConsole(1);
				}
			});
			button2.setWidth(BUTTON_WIDTH);
			button2.setHeight(BUTTON_HEIGHT);
			this.addComponent(button2);

			final NativeButton button3 = new NativeButton("3", new Button.ClickListener() {			
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayRefereeConsole(2);
				}
			});
			button3.setWidth(BUTTON_WIDTH);
			button3.setHeight(BUTTON_HEIGHT);
			this.addComponent(button3);
		}
	}





	//	/**
	//	 * @return
	//	 */
	//	private RefereeConsole createRefConsole() {
	//		RefereeConsole refConsole = new RefereeConsole(false, "Refereeing");
	//		return refConsole;
	//	}

	/**
	 * @param refIndex
	 * @return
	 */
	private JuryLights createRefereeDecisions() {
		JuryLights decisionLights = new JuryLights(false, "DecisionLights", false);
		return decisionLights;
	}


}
