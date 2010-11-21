package org.concordiainternational.competition.mobile;


import org.concordiainternational.competition.ui.AttemptBoardView;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.JuryLights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class MobileMenu extends VerticalLayout {

	private CompetitionApplication app;

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(MobileMenu.class);
	
	public MobileMenu() {
		app = CompetitionApplication.getCurrent();
		final MRefereeSelect refereeSelection = new MRefereeSelect();
		this.addComponent(refereeSelection);
		final MRefereeDecisions refereeDecisions = new MRefereeDecisions();
		this.addComponent(refereeDecisions);
		final MPlatesInfo platesInfo = new MPlatesInfo();
		this.addComponent(platesInfo);
		this.setStyleName("mobileMenu");
	}

	public class MRefereeDecisions extends HorizontalLayout {
		public MRefereeDecisions() {
			this.setMargin(true);
			this.setSpacing(true);
			this.addComponent(new Label("Décisions des officiels"));
			final NativeButton button = new NativeButton("Afficher", new Button.ClickListener() {			
				@Override
				public void buttonClick(ClickEvent event) {
					JuryLights refereeDecisions = createRefereeDecisions();
					app.setMainLayoutContent(refereeDecisions);
				}
			});
			button.setWidth("10em");
			button.setHeight("2em");
			this.addComponent(button);
		}
	}
	
	public class MPlatesInfo extends HorizontalLayout {
		public MPlatesInfo() {
			this.setMargin(true);
			this.setSpacing(true);
			this.addComponent(new Label("Plaques"));
			final NativeButton button = new NativeButton("Afficher", new Button.ClickListener() {			
				@Override
				public void buttonClick(ClickEvent event) {
					AttemptBoardView attempt = new AttemptBoardView(false, "AttemptBoard");
					app.setMainLayoutContent(attempt);
				}
			});
			button.setWidth("10em");
			button.setHeight("2em");
			this.addComponent(button);
		}
	}

	public class MJuryMemberSelect extends HorizontalLayout {

	}

	public class MPlatformSelect extends HorizontalLayout {

	}

	public class MRefereeSelect extends HorizontalLayout {

		MRefereeSelect() {
			this.setMargin(true);
			this.setSpacing(true);
			this.addComponent(new Label("Arbitres"));
			final NativeButton button1 = new NativeButton("1", new Button.ClickListener() {			
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayRefereeConsole(0);
				}
			});
			button1.setWidth("10em");
			button1.setHeight("2em");
			this.addComponent(button1);
			
			final NativeButton button2 = new NativeButton("2", new Button.ClickListener() {			
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayRefereeConsole(1);
				}
			});
			button2.setWidth("10em");
			button2.setHeight("2em");
			this.addComponent(button2);
			
			final NativeButton button3 = new NativeButton("3", new Button.ClickListener() {			
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayRefereeConsole(2);
				}
			});
			button3.setWidth("10em");
			button3.setHeight("2em");
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
