package org.concordiainternational.competition.ui;

import org.concordiainternational.competition.data.Lifter;

import com.vaadin.ui.Window.Notification;

public interface Notifyable {

    public void showNotificationForLifter(Lifter lifter, Notification notification, boolean unlessCurrent);

}
