/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, 
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.vaadin.addons.criteriacontainer.NestedBeanItem;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;

/**
 * @author jflamy
 * 
 */
public class LifterEditorTest {

    private static final String REFERENCE_STRING = "20"; //$NON-NLS-1$
    private static final Integer REFERENCE_INTEGER = 20;

    LifterContainer lifterContainer = null;
    BeanItemContainer<Lifter> lifters = null;
    final String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$

    @Before
    public void setupTest() {
        EntityManager entityManager = CompetitionApplication.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        
        // for this test, the initial data does not include body weights, so we use false
        // on the constructor to disable exclusion of incomplete data.
        lifterContainer = new LifterContainer(false);
        lifters = new BeanItemContainer<Lifter>(Lifter.class,lifterContainer.getAll());
    }

    @After
    public void tearDownTest() {
        EntityManager entityManager = CompetitionApplication.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        if (transaction.getRollbackOnly()) {
            transaction.rollback();
        } else {
            transaction.commit();
        }
        entityManager.close();
    }    
    

    @Test
    public void testInitialLoad() {
        int persistentSize = lifterContainer.size();
        int beanSize = lifters.size();
        assertTrue(persistentSize > 0);
        assertEquals(persistentSize, beanSize);
    }

    @Test
    public void testProperties() {
        @SuppressWarnings("unchecked")
        Collection<String> hbnProperties = (Collection<String>) lifterContainer.getContainerPropertyIds();
        Collection<String> lifterProperties = lifters.getContainerPropertyIds();
        lifterProperties.removeAll(hbnProperties);
        assertTrue("transient properties are visible in bean", lifterProperties.size() > 0); //$NON-NLS-1$
    }

    @Ignore
    // this test worked only because getTotal was broken
    public void testTransientGet() { // this test worked only because of a bug
                                     // in getTotal (c&j bomb-out was not being
                                     // considered)
        Object firstItemId = lifters.firstItemId();
        BeanItem<Lifter> lifterItem = lifters.getItem(firstItemId);
        Lifter lifter = (Lifter) lifterItem.getBean();
        Property s1 = lifterItem.getItemProperty("snatch1ActualLift"); //$NON-NLS-1$
        s1.setValue(REFERENCE_STRING);
        assertEquals(s1.getValue(), lifter.getSnatch1ActualLift());
        Property s2 = lifterItem.getItemProperty("total"); //$NON-NLS-1$
        assertEquals(lifter.getTotal(), REFERENCE_INTEGER);
        assertEquals(lifter.getTotal(), s2.getValue());
    }

    @Ignore
    // this test worked only because getTotal was broken
    public void testTableSort() {
        lifters.sort(new String[] { "lastName" }, new boolean[] { true }); //$NON-NLS-1$
        BeanItem<Lifter> lifterItem = lifters.getItem(lifters.firstItemId());
        Property s1 = lifterItem.getItemProperty("snatch1ActualLift"); //$NON-NLS-1$
        s1.setValue(REFERENCE_STRING);

        // sort on total, this makes the lifter that was first to become last
        lifters.sort(new String[] { "total" }, new boolean[] { true }); //$NON-NLS-1$

        lifterItem = lifters.getItem(lifters.lastItemId());
        s1 = lifterItem.getItemProperty("snatch1ActualLift"); //$NON-NLS-1$
        assertEquals(s1.getValue(), REFERENCE_STRING);
    }


	@Test
    public void checkSameness() {
        Item lifterItem = lifters.getItem(lifters.lastItemId());
        Lifter lifter = ((Lifter) ((BeanItem<?>) lifterItem).getBean());
        @SuppressWarnings("unchecked")
        final NestedBeanItem<Lifter> hbnLifterItem = (NestedBeanItem<Lifter>) lifterContainer.getItem(lifter.getId());
        Lifter hbnLifter = ((Lifter) hbnLifterItem.getBean());
        assertEquals(hbnLifter, lifter);
    }

}
