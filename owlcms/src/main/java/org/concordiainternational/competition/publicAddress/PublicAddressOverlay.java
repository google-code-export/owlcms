package org.concordiainternational.competition.publicAddress;

import org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent.MessageDisplayListener;
import org.concordiainternational.competition.publicAddress.PublicAddressTimerEvent.MessageTimerListener;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class PublicAddressOverlay extends CustomLayout implements MessageTimerListener, MessageDisplayListener {

	private ICEPush pusher;
	private Label remainingTime;
	private Label title;
	private Label message;
	private CompetitionApplication app;

	public PublicAddressOverlay(String title2, String message2, Integer remainingMilliseconds) {
		app = CompetitionApplication.getCurrent();
		synchronized(app) {
			setTemplateName("publicAddressOverlay");
			setSizeFull();
			pusher = CompetitionApplication.getCurrent().ensurePusher();
			title = new Label(title2);
			addComponent(title,"title");
			message = new Label(message2,Label.CONTENT_PREFORMATTED);
			addComponent(message,"message");
			String formatAsSeconds = TimeFormatter.formatAsSeconds(remainingMilliseconds);
			remainingTime = new Label(formatAsSeconds != null ? formatAsSeconds : "");
			addComponent(remainingTime,"remainingTime");
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
		pusher.push();
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
			pusher.push();
		}
	}

}
