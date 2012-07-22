/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.list;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.ui.CompetitionApplication;

/**
 * This class specializes the List component to use the LifterContainer class.
 * LifterContainer takes into account the currently active filters set in the
 * application.
 * 
 * @author jflamy
 * 
 */
public abstract class LifterPersistedList extends GenericPersistedList<Lifter> {
    private static final long serialVersionUID = 1L;

    public LifterPersistedList(CompetitionApplication app, String caption) {
        super(app, Lifter.class, caption);
    }

    /**
     * Load container content to Table
     */
    @Override
    protected void loadData() {
        final LifterContainer cont = new LifterContainer();
        table.setContainerDataSource(cont);
    }

}
