package org.concordiainternational.competition.mobile;

/**
 * Attempt at using the TouchKit extension.
 * Commented out: could not figure out how to reliably switch to full screen functions.
 * @author jflamy
 *
 */
public class TouchKitHome //extends TouchLayout
{
	
//	@SuppressWarnings("unused")
//	private static Logger logger = LoggerFactory.getLogger(TouchLayout.class);
//
//	public class MJuryDisplay extends TouchLayout {
//		public MJuryDisplay() {
//			
//		}
//	}
//
//	public class MJuryMemberSelect extends TouchLayout {
//
//	}
//
//	public class MPlatformSelect extends TouchLayout {
//
//	}
//
//	public class MRefereeSelect extends TouchLayout {
//
//		MRefereeSelect() {
//			TouchMenu menu = new TouchMenu();
//			menu.addItem("1", new TouchCommand() {
//				@Override
//				public void itemTouched(TouchMenuItem selectedItem) {
//					ORefereeConsole refConsole = createRefConsole();
//					TouchKitHome.this.fixLayout(refConsole);
//					refConsole.setRefereeIndex(0);
//				}});
//			menu.addItem("2", new TouchCommand() {
//				@Override
//				public void itemTouched(TouchMenuItem selectedItem) {
//					ORefereeConsole refConsole = createRefConsole();
//					TouchKitHome.this.fixLayout(refConsole);
//					refConsole.setRefereeIndex(1);
//				}});
//			menu.addItem("3", new TouchCommand() {
//				@Override
//				public void itemTouched(TouchMenuItem selectedItem) {
//					ORefereeConsole refConsole = createRefConsole();
//					TouchKitHome.this.fixLayout(refConsole);
//					refConsole.setRefereeIndex(2);
//				}});
//			addComponent(menu);
//		}
//
//	}
//
//	private TouchKitApplication app;
//
//	public TouchKitHome() {
//		app = (TouchKitApplication)CompetitionApplication.getCurrent();
//		
//		setCaption("Competition Pad");
//
//		addComponent(new Label("<p>Refereeing functions for the "
//				+ "competition management application.</p>"
//				, Label.CONTENT_XHTML));
//
//		TouchMenu menu = new TouchMenu();
//
//		menu.addItem("Platform", new TouchCommand() {
//
//			@Override
//			public void itemTouched(TouchMenuItem selectedItem) {
//				getParent().navigateTo(new MPlatformSelect());
//			}
//		});
//
//		menu.addItem("Referee", new TouchCommand() {
//
//			@Override
//			public void itemTouched(TouchMenuItem selectedItem) {
//				getParent().navigateTo(new MRefereeSelect());
//			}
//		});
//
//		menu.addItem("Jury Member", new TouchCommand() {
//
//			@Override
//			public void itemTouched(TouchMenuItem selectedItem) {
//				getParent().navigateTo(new MJuryMemberSelect());
//			}
//		});
//
//		menu.addItem("Decision Display", new TouchCommand() {
//
//			@Override
//			public void itemTouched(TouchMenuItem selectedItem) {
//				final JuryLights refDecisions = createRefereeDecisions();
//				addComponent(refDecisions);
//				TouchKitHome.this.fixLayout(refDecisions);
//			}
//		});
//
//		addComponent(menu);
//	}
//
//	/**
//	 * @return
//	 */
//	private ORefereeConsole createRefConsole() {
//		ORefereeConsole refConsole = new ORefereeConsole(false, "Refereeing");
//		return refConsole;
//	}
//	
//	/**
//	 * @param refIndex
//	 * @return
//	 */
//	private JuryLights createRefereeDecisions() {
//		JuryLights decisionLights = new JuryLights(false, "DecisionLights", false);
//		return decisionLights;
//	}
//
//	/**
//	 * @param component
//	 */
//	private void fixLayout(Component component) {
//		app.panel.setVisible(false);
//		component.setSizeFull();
//		final VerticalLayout content = (VerticalLayout)app.mainWindow.getContent();
//		content.setSizeFull();
//		content.addComponent(component);
//		content.setExpandRatio(app.panel, 0);
//		content.setExpandRatio(component, 100);
//	}
}
