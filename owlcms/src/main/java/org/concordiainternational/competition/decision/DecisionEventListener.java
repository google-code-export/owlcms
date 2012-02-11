/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.decision;

/**
 * Listener interface for receiving <code>SessionData.DecisionEvent</code>s.
 */
public interface DecisionEventListener extends java.util.EventListener {

    /**
     * This method will be invoked when a SessionData.DecisionEvent is fired.
     * 
     * @param updateEvent
     *            the event that has occured.
     */
    public void updateEvent(DecisionEvent updateEvent);
}
