/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data;

import javax.persistence.EntityManager;

import org.vaadin.addons.criteriacontainer.CriteriaContainer;

@SuppressWarnings("serial")
public class CompetitionSessionContainer extends CriteriaContainer<CompetitionSession> {

	/**
     * Default constructor, shows all items.
     * 
     * @param application
     */
    public CompetitionSessionContainer(EntityManager em) {
        super(em,false,true,CompetitionSession.class,50);
    }
    

}
