/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.mobile;

import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;

import com.vaadin.ui.Window.CloseEvent;

public interface IRefereeConsole extends DecisionEventListener {

    @Override
    public abstract void updateEvent(final DecisionEvent updateEvent);

    public abstract void refresh();

    /**
     * @param refereeIndex
     */
    public abstract void setIndex(int refereeIndex);

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.components.ApplicationView#needsMenu()
     */
    public abstract boolean needsMenu();

    /**
     * @return
     */
    public abstract String getFragment();

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.components.ApplicationView#setParametersFromFragment(java.lang.String)
     */
    public abstract void setParametersFromFragment();

    /*
     * Will be called when page is unloaded (including on refresh).
     * 
     * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
     */
    public abstract void windowClose(CloseEvent e);

}
