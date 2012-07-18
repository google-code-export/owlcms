/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, 
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.persistence.EntityManager;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryContainer;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CategoryLookupByName;
import org.concordiainternational.competition.data.Gender;
import org.concordiainternational.competition.webapp.EntityManagerProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jflamy
 * 
 */
public class CategoryLookupTest {

    EntityManagerProvider managerProvider = new AllTests();
    CategoryContainer categories = null;
    CategoryLookup categoryLookup = null;
    CategoryLookupByName categoryLookupByName;

    @Before
    public void setupTest() {
        Assert.assertNotNull(managerProvider);
        EntityManager entityManager = managerProvider.getEntityManager();
        Assert.assertNotNull(entityManager);
        entityManager.getTransaction().begin();
        categories = new CategoryContainer(entityManager,true);
        categoryLookup = CategoryLookup.getSharedInstance(entityManager);
        categoryLookup.reload();
        categoryLookupByName = new CategoryLookupByName();
    }

    @After
    public void tearDownTest() {
        managerProvider.getEntityManager().close();
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.CategoryLookup#reload()}
     * .
     */
    @Test
    public void testReloadAfterRemoval() {
        int initialSize = categoryLookup.getCategories().size();
        categories.removeItem(22L); // the first few entries are inactive, and
                                    // are filtered out.
        categoryLookup.reload();
        int finalSize = categoryLookup.getCategories().size();
        if (!(finalSize == (initialSize - 1)))
            fail("reload after removal failed (finalSize=" + finalSize + ", initialSize=" + initialSize + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Test
    public void testLookupByName() {
        Category found = categoryLookupByName.lookup("f69"); //$NON-NLS-1$
        if (found == null) fail("category not found"); //$NON-NLS-1$
        if (!(found.getName().equals("f69"))) fail("wrong category (" + found.getName() + " <> f69)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Test
    public void testLookupByName2() {
        Category found = categoryLookupByName.lookup("M94"); //$NON-NLS-1$
        if (found == null) fail("category not found"); //$NON-NLS-1$
        if (!(found.getName().equals("m94"))) fail("wrong category (" + found.getName() + " <> m94)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Test
    public void testLookupByName3() {
        Category found = categoryLookupByName.lookup("m105"); //$NON-NLS-1$
        if (found == null) fail("category not found"); //$NON-NLS-1$
        assertEquals("m105", found.getName()); //$NON-NLS-1$
    }

    @Test
    public void testLookupByName4() {
        Category found = categoryLookupByName.lookup("m94 "); //$NON-NLS-1$
        if (found == null) fail("category not found"); //$NON-NLS-1$
        assertEquals("m94", found.getName()); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.CategoryLookup#lookup(org.concordiainternational.competition.data.Gender, java.lang.Double)}
     * .
     */
    @Test
    public void testWeightInsideCategoryBoundaries() {
        Category found = categoryLookup.lookup(Gender.F.toString(), 68.0);
        if (found == null) fail("category not found"); //$NON-NLS-1$
        if (!(found.getName().equals("f69"))) fail("wrong category (" + found.getName() + " <> f69)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.CategoryLookup#lookup(org.concordiainternational.competition.data.Gender, java.lang.Double)}
     * .
     */
    @Test
    public void testWeightAtCategoryBoundary() {
        Category found = categoryLookup.lookup(Gender.M.toString(), 69.0);
        assertNotNull("category not found", found); //$NON-NLS-1$
        assertEquals("wrong category", "m69", found.getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.CategoryLookup#lookup(org.concordiainternational.competition.data.Gender, java.lang.Double)}
     * .
     */
    @Test
    public void testWeightAboveLargestCategoryBoundary() {
        Category found = categoryLookup.lookup(Gender.F.toString(), 80.0);
        assertNotNull("category not found", found); //$NON-NLS-1$
        assertEquals("wrong category", "f>75", found.getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.CategoryLookup#lookup(org.concordiainternational.competition.data.Gender, java.lang.Double)}
     * .
     */
    @Test
    public void testWeightAtLargestCategoryBoundary() {
        Category found = categoryLookup.lookup(Gender.F.toString(), 75.0);
        assertNotNull("category not found", found); //$NON-NLS-1$
        assertEquals("wrong category", "f75", found.getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.CategoryLookup#lookup(org.concordiainternational.competition.data.Gender, java.lang.Double)}
     * .
     */
    @Test
    public void testWeightBelowSmallestCategoryBoundary() {
        Category found = categoryLookup.lookup(Gender.M.toString(), 50.0);
        assertNotNull("category not found", found); //$NON-NLS-1$
        assertEquals("wrong category", "m56", found.getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.CategoryLookup#lookup(org.concordiainternational.competition.data.Gender, java.lang.Double)}
     * .
     */
    @Test
    public void testWeightAtSmallestCategoryBoundary() {
        Category found = categoryLookup.lookup(Gender.M.toString(), 56.0);
        assertNotNull("category not found", found); //$NON-NLS-1$
        assertEquals("wrong category", "m56", found.getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
