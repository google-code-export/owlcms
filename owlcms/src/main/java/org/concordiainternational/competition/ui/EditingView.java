/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;

import com.vaadin.data.Item;
import com.vaadin.ui.Component;

public interface EditingView extends Component, Bookmarkable {

    /**
     * @return true if editor in bottom pane is pinned (not to be updated)
     */
    public boolean isStickyEditor();

    /**
     * Indicate that the editor at bottom must not be updated
     * 
     * @param freezeLifterCardEditor
     */
    public void setStickyEditor(boolean freezeLifterCardEditor);

    /**
     * Indicate that the editor at bottom must not be updated
     * 
     * @param freezeLifterCardEditor
     */
    public void setStickyEditor(boolean freezeLifterCardEditor, boolean reloadLifterInfo);

    /**
     * Load the designated lifter in the bottom pane
     * 
     * @param lifter
     * @param lifterItem
     */
    public void editLifter(Lifter lifter, Item lifterItem);

    /**
     * Switch to editing a new group.
     * 
     * @param newSession
     */
    public void setCurrentSession(CompetitionSession newSession);

    public void setSessionData(SessionData newSessionData);

    public String getInstanceId();

    public String getViewName();

    public String getLoggingId();

}
