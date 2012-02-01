/* 
 * Copyright ©2009 Jean-François Lamy
 * 
 * Licensed under the Open Software Licence, Version 3.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.opensource.org/licenses/osl-3.0.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.concordiainternational.competition.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryContainer;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CategoryLookupByName;
import org.concordiainternational.competition.data.Gender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * @author jflamy
 * 
 */
public class CategoryLookupTest {

    HbnSessionManager hbnSessionManager = AllTests.getSessionManager();
    HbnContainer<Category> categories = null;
    CategoryLookup categoryLookup = null;
    CategoryLookupByName categoryLookupByName;

    @Before
    public void setupTest() {
        Assert.assertNotNull(hbnSessionManager);
        Assert.assertNotNull(hbnSessionManager.getHbnSession());
        hbnSessionManager.getHbnSession().beginTransaction();
        categories = new CategoryContainer(hbnSessionManager,true);
        categoryLookup = new CategoryLookup(hbnSessionManager);
        categoryLookupByName = new CategoryLookupByName(hbnSessionManager);
    }

    @After
    public void tearDownTest() {
        hbnSessionManager.getHbnSession().close();
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
