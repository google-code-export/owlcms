/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.list;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import org.concordiainternational.competition.ui.CompetitionApplication;

import com.vaadin.data.util.BeanItemContainer;

/**
 * Normally we would use an HbnContainer directly, but we need to sort on the
 * lifting order, which we do not want written to the database. HbnContainer
 * uses the database to sort, so we have to create our own container as a
 * wrapper. The resulting BeanItemContainer is used to feed the table.
 * 
 * 
 * @author jflamy
 * @param <T>
 */
public abstract class GenericBeanList<T extends Serializable> extends GenericList<T> {

    private static final long serialVersionUID = -5396475029309979597L;
    protected List<T> allPojos;

    public GenericBeanList(CompetitionApplication app, Class<T> parameterizedClass, String caption) {
        super(app, parameterizedClass, caption);
    }

    /**
     * Additional initializations, once super.populateAndConfigureTable() (and
     * hence loadData()) has been done.
     */
    @Override
    protected void init() {
        super.init();
    }

    /**
     * Load container content to Table. We create a BeanItemContainer to gain
     * sorting flexibility. Note: this routine is invoked as part of the super()
     * chain in the constructor, and before our own init is called.
     */
    @Override
    protected void loadData() {
        EntityManager em = null;
        try {
            em = CompetitionApplication.getNewGlobalEntityManager();
            CriteriaQuery<T> cq = em.getCriteriaBuilder().createQuery(parameterizedClass);
            cq.from(parameterizedClass);
            final BeanItemContainer<T> cont = new BeanItemContainer<T>(parameterizedClass,em.createQuery(cq).getResultList());
            table.setContainerDataSource(cont);
        } finally {
            if(em != null) em.close();
        }  
    }


}
