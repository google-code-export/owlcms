/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.components;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Window.CloseListener;

/**
 * In this application the views need to refresh themselves when the user switches groups, or when they receive events.
 * 
 */
public interface ApplicationView extends ComponentContainer, CloseListener {

    public void refresh();

    /**
     * @return true if the menu bar is needed
     */
    public boolean needsMenu();

    /**
     * @return true if the window uses a black background
     */
    public boolean needsBlack();

    /**
     */
    public void setParametersFromFragment();

    /**
     */
    public String getFragment();

    public void registerAsListener();

    public void unregisterAsListener();

    public String getInstanceId();

    public String getLoggingId();

}
