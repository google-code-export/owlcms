/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data;

import javax.persistence.EntityManager;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.vaadin.addons.criteriacontainer.CriteriaContainer;

@SuppressWarnings("serial")
public class PlatformContainer extends CriteriaContainer<Platform> {

    /**
     * Default constructor, shows all items.
     * 
     * @param application
     */
    private PlatformContainer(EntityManager em) {
        super(em,false,true,Platform.class,50);
    }

    public PlatformContainer() {
        this(CompetitionApplication.getNewGlobalEntityManager());
    }
    

}
