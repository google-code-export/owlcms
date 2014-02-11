/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.publicAddress;

import org.concordiainternational.competition.publicAddress.IntermissionTimerEvent.IntermissionTimerListener;
import org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent.MessageDisplayListener;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.generators.TimeFormatter;

import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class PublicAddressOverlay extends CustomLayout implements IntermissionTimerListener, MessageDisplayListener {

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
                addComponent(title, "title");
                message = new Label(message2, Label.CONTENT_PREFORMATTED);
                addComponent(message, "message");
                String formatAsSeconds = TimeFormatter.formatAsSeconds(remainingMilliseconds);
                remainingTime = new Label(formatAsSeconds != null ? formatAsSeconds : "");
                addComponent(remainingTime, "remainingTime");
            } finally {
                app.setPusherDisabled(prevDisabled);
            }
        }
    }

    @Override
    public void intermissionTimerUpdate(IntermissionTimerEvent event) {
        synchronized (app) {
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
            synchronized (app) {
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
