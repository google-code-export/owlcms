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

import org.concordiainternational.competition.data.CompetitionContainer;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.CompetitionSessionLookup;
import org.concordiainternational.competition.webapp.EntityManagerProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jflamy
 * 
 */
public class GroupLookupTest {

    EntityManagerProvider provider = new AllTests();
    CompetitionContainer competitionSessions = null;
    CompetitionSessionLookup competitionSessionLookup = null;

    @Before
    public void setupTest() {
        Assert.assertNotNull(provider);
        Assert.assertNotNull(provider.getEntityManager());
        provider.getEntityManager().getTransaction().begin();
        competitionSessions = new CompetitionContainer(provider.getEntityManager());
        competitionSessionLookup = new CompetitionSessionLookup();
    }

    @After
    public void tearDownTest() {
        provider.getEntityManager().close();
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.CompetitionSessionLookup#reload()}.
     */
    @Test
    public void testReload() {
        int initialSize = competitionSessionLookup.getGroups().size();
        assertEquals(initialSize, 3);
        // groups.removeItem(2L); // hide group B -- won't work because we
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
