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
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.hbnutil.HbnContainer.EntityItem;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;

/**
 * @author jflamy
 * 
 */
public class LifterEditorTest {

    private static final String REFERENCE_STRING = "20"; //$NON-NLS-1$
    private static final Integer REFERENCE_INTEGER = 20;
    HbnSessionManager hbnSessionManager = AllTests.getSessionManager();
    LifterContainer hbnLifters = null;
    BeanItemContainer<Lifter> lifters = null;
    final String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$

    @Before
    public void setupTest() {
        assertNotNull(hbnSessionManager);
        assertNotNull(hbnSessionManager.getHbnSession());
        hbnSessionManager.getHbnSession().beginTransaction();
        // for this test, the initial data does not include body weights, so we
        // use false
        // on the constructor to disable exclusion of incomplete data.
        hbnLifters = new LifterContainer(new CompetitionApplication(), false);
        lifters = new BeanItemContainer<Lifter>(Lifter.class,hbnLifters.getAllPojos());
    }

    @After
    public void tearDownTest() {
        hbnSessionManager.getHbnSession().close();
    }

    @Test
    public void testInitialLoad() {
        int hbnSize = hbnLifters.size();
        int beanSize = lifters.size();
        assertTrue(hbnSize > 0);
        assertEquals(hbnSize, beanSize);
    }

    @Test
    public void testProperties() {
        Collection<String> hbnProperties = hbnLifters.getContainerPropertyIds();
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

    @SuppressWarnings("rawtypes")
	@Test
    public void checkSameness() {
        Item lifterItem = lifters.getItem(lifters.lastItemId());
        Lifter lifter = ((Lifter) ((BeanItem<?>) lifterItem).getBean());
        final EntityItem hbnLifterItem = (EntityItem) hbnLifters.getItem(lifter.getId());
        Lifter hbnLifter = ((Lifter) hbnLifterItem.getPojo());
        assertEquals(hbnLifter, lifter);
    }

}
