/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import org.concordiainternational.competition.data.CompetitionSession;

import com.vaadin.terminal.StreamResource;

/**
 * This interface defines the actions that are managed by the application controller. In principle, all actions that affect more than a
 * single application pane should be registered here
 * 
 * @author jflamy
 * 
 */
public interface UserActions {

    public abstract void setPlatformByName(String plaftorm);

    public abstract void setCurrentCompetitionSession(CompetitionSession value);

    /**
     * @param streamSource
     * @param filename
     */
    public abstract void openSpreadsheet(StreamResource.StreamSource streamSource,
            final String filename);

}
