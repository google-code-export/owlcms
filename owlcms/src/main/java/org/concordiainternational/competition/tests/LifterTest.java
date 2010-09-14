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

import java.util.Calendar;

import org.concordiainternational.competition.data.Lifter;
import org.junit.BeforeClass;
import org.junit.Test;

public class LifterTest {

    private static Lifter lifter;

    @BeforeClass
    public static void setupTest() {
        lifter = new Lifter();
        lifter.setLastName("Strong"); //$NON-NLS-1$
        lifter.setFirstName("Paul"); //$NON-NLS-1$
        lifter.setGender("M"); //$NON-NLS-1$
        lifter.setBodyWeight(68.5);
        lifter.setSnatch1Declaration("60"); //$NON-NLS-1$
        lifter.setCleanJerk1Declaration("80"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.Lifter#getTotal()}.
     */
    @Test
    public void testGetTotalNoData() {
        assertEquals("total without any results", 0, (long) lifter.getTotal()); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.Lifter#getTotal()}.
     */
    @Test
    public void testGetTotalNoSnatchData() {
        lifter.setSnatch1ActualLift(null);
        lifter.setSnatch2ActualLift(null);
        lifter.setSnatch3ActualLift(null);
        lifter.setCleanJerk1ActualLift("80"); //$NON-NLS-1$
        lifter.setCleanJerk2ActualLift("81"); //$NON-NLS-1$
        lifter.setCleanJerk3ActualLift("82"); //$NON-NLS-1$
        assertEquals("total with no snatch results", 0L, (long) lifter.getTotal()); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.Lifter#getTotal()}.
     */
    @Test
    public void testGetTotalNoCleanJerkData() {
        lifter.setSnatch1ActualLift("60"); //$NON-NLS-1$
        lifter.setSnatch2ActualLift("61"); //$NON-NLS-1$
        lifter.setSnatch3ActualLift("62"); //$NON-NLS-1$
        lifter.setCleanJerk1ActualLift(null);
        lifter.setCleanJerk2ActualLift(null);
        lifter.setCleanJerk3ActualLift(null);
        assertEquals("total with no clean and jerk results", 0, (long) lifter.getTotal()); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.Lifter#getTotal()}.
     */
    @Test
    public void testGetTotalPartialData() {
        lifter.setSnatch1ActualLift("60"); //$NON-NLS-1$
        lifter.setSnatch2ActualLift(""); //$NON-NLS-1$
        lifter.setSnatch3ActualLift(null);
        lifter.setCleanJerk1ActualLift("-80"); //$NON-NLS-1$
        lifter.setCleanJerk2ActualLift("-"); //$NON-NLS-1$
        lifter.setCleanJerk3ActualLift(null);
        assertEquals("total with failed clean and jerk results", 0, (long) lifter.getTotal()); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.Lifter#getTotal()}.
     */
    @Test
    public void testGetTotalHappyPath() {
        lifter.setSnatch1ActualLift("60"); //$NON-NLS-1$
        lifter.setSnatch2ActualLift("61"); //$NON-NLS-1$
        lifter.setSnatch3ActualLift("62"); //$NON-NLS-1$
        lifter.setCleanJerk1ActualLift("80"); //$NON-NLS-1$
        lifter.setCleanJerk2ActualLift("81"); //$NON-NLS-1$
        lifter.setCleanJerk3ActualLift("82"); //$NON-NLS-1$
        assertEquals("total with all values", 144, (long) lifter.getTotal()); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.Lifter#getTotal()}.
     */
    @Test
    public void testGetTotalSnatchBombOut() {
        lifter.setSnatch1ActualLift("-60"); //$NON-NLS-1$
        lifter.setSnatch2ActualLift("-60"); //$NON-NLS-1$
        lifter.setSnatch3ActualLift("-60"); //$NON-NLS-1$
        lifter.setCleanJerk1ActualLift("80"); //$NON-NLS-1$
        lifter.setCleanJerk2ActualLift("81"); //$NON-NLS-1$
        lifter.setCleanJerk3ActualLift("-"); //$NON-NLS-1$
        assertEquals("total with snatch bomb out", 0, (long) lifter.getTotal()); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.concordiainternational.competition.data.Lifter#getTotal()}.
     */
    @Test
    public void testGetTotalBombOut() {
        lifter.setSnatch1ActualLift("-60"); //$NON-NLS-1$
        lifter.setSnatch2ActualLift("-60"); //$NON-NLS-1$
        lifter.setSnatch3ActualLift("-60"); //$NON-NLS-1$
        lifter.setCleanJerk1ActualLift("-80"); //$NON-NLS-1$
        lifter.setCleanJerk2ActualLift("-80"); //$NON-NLS-1$
        lifter.setCleanJerk3ActualLift("-80"); //$NON-NLS-1$
        assertEquals("total with full bomb out", 0, (long) lifter.getTotal()); //$NON-NLS-1$
    }

    @Test
    public void ageGroup() {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        assertEquals(80, (long) lifter.getAgeGroup());
        lifter.setBirthDate(thisYear - 40);
        assertEquals(40, (long) lifter.getAgeGroup());
        lifter.setBirthDate(thisYear - 39);
        assertEquals(35, (long) lifter.getAgeGroup());
        lifter.setBirthDate(thisYear - 41);
        assertEquals(40, (long) lifter.getAgeGroup());
        lifter.setBirthDate(thisYear - 86);
        assertEquals(80, (long) lifter.getAgeGroup());
        lifter.setGender("F"); //$NON-NLS-1$
        assertEquals(70, (long) lifter.getAgeGroup());
        lifter.setBirthDate(null);
        assertEquals(null, lifter.getAgeGroup());
        lifter.setGender(""); //$NON-NLS-1$
        lifter.setBirthDate(1900);
        assertEquals(null, lifter.getAgeGroup());
    }

}
