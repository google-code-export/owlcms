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

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.CompetitionSessionLookup;
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
public class GroupLookupTest {

    HbnSessionManager hbnSessionManager = AllTests.getSessionManager();
    HbnContainer<CompetitionSession> competitionSessions = null;
    CompetitionSessionLookup competitionSessionLookup = null;

    @Before
    public void setupTest() {
        Assert.assertNotNull(hbnSessionManager);
        Assert.assertNotNull(hbnSessionManager.getHbnSession());
        hbnSessionManager.getHbnSession().beginTransaction();
        competitionSessions = new HbnContainer<CompetitionSession>(CompetitionSession.class, hbnSessionManager);
        competitionSessionLookup = new CompetitionSessionLookup(hbnSessionManager);
    }

    @After
    public void tearDownTest() {
        hbnSessionManager.getHbnSession().close();
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.CompetitionSessionLookup#reload()}.
     */
    @Test
    public void testReload() {
        int initialSize = competitionSessionLookup.getGroups().size();
        assertEquals(initialSize, 3);
        // groups.removeItem(2L); // remove group B -- won't work because we
        // have lifters connected.
        competitionSessionLookup.reload();
        int finalSize = competitionSessionLookup.getGroups().size();
        if (!(finalSize == (initialSize)))
            fail("reload failed (finalSize=" + finalSize + ", initialSize=" + initialSize + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Test
    public void testSmallestLookup() {
        CompetitionSession found = competitionSessionLookup.lookup("A"); //$NON-NLS-1$
        assertNotNull("group not found", found); //$NON-NLS-1$
        assertEquals("A", found.getName()); //$NON-NLS-1$
    }

    @Test
    public void testMiddleLookup() {
        CompetitionSession found = competitionSessionLookup.lookup("B"); //$NON-NLS-1$
        assertNotNull("group not found", found); //$NON-NLS-1$
        assertEquals("B", found.getName()); //$NON-NLS-1$
    }

    @Test
    public void testLargestLookup() {
        CompetitionSession found = competitionSessionLookup.lookup("C"); //$NON-NLS-1$
        assertNotNull("group not found", found); //$NON-NLS-1$
        assertEquals("C", found.getName()); //$NON-NLS-1$
    }

    @Test
    public void testFailedLookup() {
        CompetitionSession found = competitionSessionLookup.lookup("Z"); //$NON-NLS-1$
        assertEquals(null, found);
    }

}
