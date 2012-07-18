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

import com.vaadin.data.util.filter.Compare;

@SuppressWarnings("serial")
public class CategoryContainer extends CriteriaContainer<Category> {

	private boolean activeOnly = false;

	/**
     * Default constructor, shows all items.
     * 
     * @param application
     */
    public CategoryContainer(EntityManager em) {
        super(em,false,true,Category.class,50);
    }
    
    /**
     * Alternate constructor that shows only active items
     * 
     * @param application
     * @param excludeNotWeighed
     */
    public CategoryContainer(EntityManager entityManager, boolean activeOnly) {
    	this(entityManager);
        this.activeOnly  = activeOnly;
        setFilters();
        
    }

    private void setFilters() {
        removeAllContainerFilters();
        if (activeOnly) {
           addContainerFilter(new Compare.Equal(Category_.active.getName(), activeOnly));
        }
    }

}
