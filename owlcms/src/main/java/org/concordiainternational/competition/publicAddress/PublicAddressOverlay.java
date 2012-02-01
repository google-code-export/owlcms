package org.concordiainternational.competition.publicAddress;

import org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent.MessageDisplayListener;
import org.concordiainternational.competition.publicAddress.PublicAddressTimerEvent.MessageTimerListener;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.generators.TimeFormatter;

import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class PublicAddressOverlay extends CustomLayout implements MessageTimerListener, MessageDisplayListener {

	private Label remainingTime;
	private Label title;
	private Label message;
	private CompetitionApplication app;

	public PublicAddressOverlay(String title2, String message2, Integer remainingMilliseconds) {
		app = CompetitionApplication.getCurrent();

        synchronized (app) {
            boolean prevDisabled = app.getPusherDisabled();
            try {
                app.setPusherDisabled(true);
    			setTemplateName("publicAddressOverlay");
    			setSizeFull();
    			title = new Label(title2);
    			addComponent(title,"title");
    			message = new Label(message2,Label.CONTENT_PREFORMATTED);
    			addComponent(message,"message");
    			String formatAsSeconds = TimeFormatter.formatAsSeconds(remainingMilliseconds);
    			remainingTime = new Label(formatAsSeconds != null ? formatAsSeconds : "");
    			addComponent(remainingTime,"remainingTime");
            } finally {
                app.setPusherDisabled(prevDisabled);
            }
        }
	}
	
	@Override
	public void timerUpdate(PublicAddressTimerEvent event) {
		synchronized(app) {
			Integer remainingMilliseconds = event.getRemainingMilliseconds();
			if (remainingMilliseconds != null) {
				remainingTime.setValue(TimeFormatter.formatAsSeconds(remainingMilliseconds));
			}
		}
		app.push();
	}

	@Override
	public void messageUpdate(PublicAddressMessageEvent event) {
		if (!event.setHide()) {
			synchronized(app) {
				title.setValue(event.getTitle());
				message.setValue(event.getMessage());
				Integer remainingMilliseconds = event.getRemainingMilliseconds();
				if (remainingMilliseconds != null) {
					remainingTime.setValue(TimeFormatter.formatAsSeconds(remainingMilliseconds));
				}
			}
			app.push();
		}
	}

}
