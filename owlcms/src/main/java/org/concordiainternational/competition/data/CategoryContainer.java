/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.vaadin.addons.criteriacontainer.CriteriaContainer;

@SuppressWarnings("serial")
public class CategoryContainer extends CriteriaContainerWrapper<Category> {

	private boolean activeOnly = false;

	/**
     * Default constructor, shows all athletes.
     * 
     * @param application
     */
    public CategoryContainer(EntityManager sessMgr) {
        super(Category.class, sessMgr);
    }
    
    /**
     * Alternate constructor that shows only athletes that have weighed-in.
     * 
     * @param application
     * @param excludeNotWeighed
     */
    public CategoryContainer(EntityManager sessMgr, boolean activeOnly) {
    	this(sessMgr);
        this.activeOnly  = activeOnly;
    }

    /*
     * This class adds filtering criteria other than simple textual filtering as
     * implemented by the Filterable interface. (non-Javadoc)
     * 
     * @see
     * com.vaadin.data.hbnutil.HbnContainer#addSearchCriteria(org.hibernate.
     * Criteria)
     */
    public Criteria addSearchCriteria(Criteria criteria) {
    	if (activeOnly) {
    		criteria.add(Restrictions.eq("active", Boolean.TRUE));
    	}
		return criteria;
    }
}
