package org.concordiainternational.competition.mobile;


import java.util.List;

import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
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

	public static final String BUTTON_WIDTH = "6em"; //$NON-NLS-1$
	public static final String BUTTON_NARROW_WIDTH = "4em"; //$NON-NLS-1$
	public static final String BUTTON_HEIGHT = "3em"; //$NON-NLS-1$

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
		
		final MJurySelect jurySelection = new MJurySelect();
		this.addComponent(jurySelection);
		final MJuryDecisions juryDecisions = new MJuryDecisions();
		this.addComponent(juryDecisions);
		final MPlatesInfo platesInfo = new MPlatesInfo();
		this.addComponent(platesInfo);
		this.setStyleName("mobileMenu"); //$NON-NLS-1$
		this.setSpacing(true);
		this.setMargin(true);
		app.getMainWindow().executeJavaScript("scrollTo(0,1)"); //$NON-NLS-1$
	}

	public class MRefereeDecisions extends HorizontalLayout {
		public MRefereeDecisions() {
			this.setSpacing(true);
			final Label label = new Label(Messages.getString("MobileMenu.RefDecisions",app.getLocale())); //$NON-NLS-1$
			this.addComponent(label);
			this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
			final NativeButton button = new NativeButton(Messages.getString("MobileMenu.Display",app.getLocale()), new Button.ClickListener() {			 //$NON-NLS-1$
				@Override
				public void buttonClick(ClickEvent event) {
					RefereeDecisions refereeDecisions = createRefereeDecisions();
					app.setMainLayoutContent(refereeDecisions);
				}
			});
			button.setWidth(BUTTON_WIDTH);
			button.setHeight(BUTTON_HEIGHT);
			this.addComponent(button);
		}
	}
	
	public class MJuryDecisions extends HorizontalLayout {
		public MJuryDecisions() {
			this.setSpacing(true);
			final Label label = new Label(Messages.getString("MobileMenu.JuryDecisions",app.getLocale())); //$NON-NLS-1$
			this.addComponent(label);
			this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
			final NativeButton button = new NativeButton(Messages.getString("MobileMenu.Display",app.getLocale()), new Button.ClickListener() {			 //$NON-NLS-1$
				@Override
				public void buttonClick(ClickEvent event) {
					RefereeDecisions refereeDecisions = createJuryDecisions();
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
			final Label label = new Label(Messages.getString("MobileMenu.Plates",app.getLocale())); //$NON-NLS-1$
			this.addComponent(label);
			this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
			final NativeButton button = new NativeButton(Messages.getString("MobileMenu.Display",app.getLocale()), new Button.ClickListener() {			 //$NON-NLS-1$
				@Override
				public void buttonClick(ClickEvent event) {
					MPlatesInfoView plates = new MPlatesInfoView(false, Messages.getString("MobileMenu.PlatesTitle",app.getLocale())); //$NON-NLS-1$
					app.setMainLayoutContent(plates);
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
			final Label label = new Label(Messages.getString("MobileMenu.Platforms",app.getLocale())); //$NON-NLS-1$
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
			final Label label = new Label(Messages.getString("MobileMenu.Referee",app.getLocale())); //$NON-NLS-1$
			this.addComponent(label);
			this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
			final NativeButton button1 = new NativeButton("1", new Button.ClickListener() {			 //$NON-NLS-1$
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayMRefereeConsole(0);
				}
			});
			button1.setWidth(BUTTON_NARROW_WIDTH);
			button1.setHeight(BUTTON_HEIGHT);
			this.addComponent(button1);

			final NativeButton button2 = new NativeButton("2", new Button.ClickListener() {			 //$NON-NLS-1$
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayMRefereeConsole(1);
				}
			});
			button2.setWidth(BUTTON_NARROW_WIDTH);
			button2.setHeight(BUTTON_HEIGHT);
			this.addComponent(button2);

			final NativeButton button3 = new NativeButton("3", new Button.ClickListener() {			 //$NON-NLS-1$
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayMRefereeConsole(2);
				}
			});
			button3.setWidth(BUTTON_NARROW_WIDTH);
			button3.setHeight(BUTTON_HEIGHT);
			this.addComponent(button3);
		}
	}
	
	public class MJurySelect extends HorizontalLayout {

		MJurySelect() {
			this.setSpacing(true);
			final Label label = new Label(Messages.getString("MobileMenu.Jury",app.getLocale())); //$NON-NLS-1$
			this.addComponent(label);
			this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
			final NativeButton button1 = new NativeButton("1", new Button.ClickListener() {			 //$NON-NLS-1$
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayMJuryConsole(0);
				}
			});
			button1.setWidth(BUTTON_NARROW_WIDTH);
			button1.setHeight(BUTTON_HEIGHT);
			this.addComponent(button1);

			final NativeButton button2 = new NativeButton("2", new Button.ClickListener() {			 //$NON-NLS-1$
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayMJuryConsole(1);
				}
			});
			button2.setWidth(BUTTON_NARROW_WIDTH);
			button2.setHeight(BUTTON_HEIGHT);
			this.addComponent(button2);

			final NativeButton button3 = new NativeButton("3", new Button.ClickListener() {			 //$NON-NLS-1$
				@Override
				public void buttonClick(ClickEvent event) {
					CompetitionApplication.getCurrent().displayMJuryConsole(2);
				}
			});
			button3.setWidth(BUTTON_NARROW_WIDTH);
			button3.setHeight(BUTTON_HEIGHT);
			this.addComponent(button3);
		}
	}


	//	/**
	//	 * @return
	//	 */
	//	private ORefereeConsole createRefConsole() {
	//		ORefereeConsole refConsole = new ORefereeConsole(false, "Refereeing");
	//		return refConsole;
	//	}

	/**
	 * @param refIndex
	 * @return
	 */
	private RefereeDecisions createRefereeDecisions() {
		RefereeDecisions decisionLights = new RefereeDecisions(false, "DecisionLights", false, false); //$NON-NLS-1$
		return decisionLights;
	}

	/**
	 * @param refIndex
	 * @return
	 */
	private RefereeDecisions createJuryDecisions() {
		RefereeDecisions decisionLights = new RefereeDecisions(false, "DecisionLights", false, true); //$NON-NLS-1$
		return decisionLights;
	}


}
