package org.concordiainternational.competition.mobile;

import org.concordiainternational.competition.ui.CompetitionApplication;
/**
 * Attempt at using the TouchKit extension.
 * Commented out: could not figure out how to reliably switch to full screen functions.
 * @author jflamy
 *
 */
public class TouchKitApplication extends CompetitionApplication {
    private static final long serialVersionUID = 5474522369804563317L;

//	@SuppressWarnings("unused")
//	private static final Logger logger = LoggerFactory.getLogger(TouchKitApplication.class);
//	public Window mainWindow;
//	public TouchPanel panel;
//
//    public TouchKitApplication() {
//		super("/m/");
//	}
//    
//    @Override
//    public void init() {
//    	sharedInit();    	
//    	mainWindow = new Window("Refereeing");
//        mainWindow.setSizeFull();
//        setMainWindow(mainWindow);
//        
//        VerticalLayout vLayout = (VerticalLayout) mainWindow.getContent();
//        vLayout.setMargin(false,false,false,false);
//        vLayout.setSizeFull();
//
//        panel = new TouchPanel();
//        panel.setSizeFull();
//        mainWindow.addComponent(panel);
//        panel.navigateTo(new TouchKitHome());
//
//        mainWindow.setApplication(this);
//        setTheme("m");
//    }
//    
//    @Override
//	synchronized public void push() {
//    	pusher = this.ensurePusher();
//    	if (!pusherDisabled) {
//    		logger.warn("pushing with {} on window {}",pusher,mainWindow);
//    		pusher.push();
//    	}
//    }
//
//    /**
//     * @return
//     */
//    @Override
//	protected ICEPush ensurePusher() {
//
//        if (pusher == null) {
//        	logger.warn("ensuring pusher");
//        	LoggerUtils.logException(logger, new Exception("ensurePusher wherefrom"));
//            pusher = new ICEPush();
//            mainWindow.addComponent(pusher);
//        }
//        return pusher;
//    }
    
}
