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
import static org.concordiainternational.competition.tests.AllTests.assertEqualsToReferenceFile;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter.Ranking;
import org.concordiainternational.competition.data.lifterSort.WinningOrderComparator;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

public class LifterSorterTest {

    HbnSessionManager hbnSessionManager = AllTests.getSessionManager();
    HbnContainer<Lifter> hbnLifters = null;
    List<Lifter> lifters = null;

    @Before
    public void setupTest() {
        assertNotNull(hbnSessionManager);
        assertNotNull(hbnSessionManager.getHbnSession());
        hbnSessionManager.getHbnSession().beginTransaction();

        // mock the application
        final CompetitionApplication application = new CompetitionApplication();
        CompetitionApplication.setCurrent(application);

        // for this test, the initial data does not include body weights, so we
        // use false
        // on the constructor to disable exclusion of incomplete data.
        hbnLifters = new LifterContainer(new CompetitionApplication(), false);
        lifters = (hbnLifters.getAllPojos());
    }

    @After
    public void tearDownTest() {
        hbnSessionManager.getHbnSession().close();
    }

    @Test
    public void initialCheck() {
        final String resName = "/initialCheck.txt"; //$NON-NLS-1$
        LifterSorter.assignLotNumbers(lifters);

        Collections.shuffle(lifters);

        List<Lifter> sorted = LifterSorter.liftingOrderCopy(lifters);
        final String actual = AllTests.shortDump(sorted);
        assertEqualsToReferenceFile(resName, actual);
    }

    @Test
    public void liftSequence1() {
        LifterSorter.assignLotNumbers(lifters);

        final Lifter schneiderF = lifters.get(0);
        final Lifter simpsonR = lifters.get(1);
        final Lifter allisonA = lifters.get(2);
        final Lifter verneU = lifters.get(3);

        // simulate initial declaration at weigh-in
        schneiderF.setSnatch1Declaration(Integer.toString(60));
        simpsonR.setSnatch1Declaration(Integer.toString(60));
        allisonA.setSnatch1Declaration(Integer.toString(55));
        verneU.setSnatch1Declaration(Integer.toString(55));
        schneiderF.setCleanJerk1Declaration(Integer.toString(80));
        simpsonR.setCleanJerk1Declaration(Integer.toString(82));
        allisonA.setCleanJerk1Declaration(Integer.toString(61));
        verneU.setCleanJerk1Declaration(Integer.toString(68));

        // check initial lift order -- this checks the "lot number" rule
        LifterSorter.liftingOrder(lifters);
        assertEqualsToReferenceFile("/seq1_lift0.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$
        // hide non-lifters
        final int size = lifters.size();
        for (int i = 4; i < size; i++)
            lifters.remove(4);

        // competition start
        successfulLift(lifters);
        assertEqualsToReferenceFile("/seq1_lift1.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$
        successfulLift(lifters);
        assertEqualsToReferenceFile("/seq1_lift2.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$

        // change weights to have all lifters are the same at 60
        declaration(verneU, lifters, "58"); //$NON-NLS-1$
        declaration(allisonA, lifters, "60"); //$NON-NLS-1$
        change1(verneU, lifters, "59"); //$NON-NLS-1$
        change2(verneU, lifters, "60"); //$NON-NLS-1$
        assertEqualsToReferenceFile("/seq1_lift3.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$

        // failure so we can test "earlier lifter"
        failedLift(lifters);
        assertTrue(
            "earlier lifter has precedence", lifters.get(2).getLastLiftTime().before(lifters.get(3).getLastLiftTime())); //$NON-NLS-1$
        assertTrue("lift order not considered", (lifters.get(2).getLotNumber()) > (lifters.get(3).getLotNumber())); //$NON-NLS-1$
        assertEqualsToReferenceFile("/seq1_lift4.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$

        // one more failure -- we now have 3 lifters at second try, 60kg.
        failedLift(lifters);
        assertTrue(
            "time stamp precedence failed 0 vs 1 " + lifters.get(0).getLastLiftTime() + ">=" + lifters.get(1).getLastLiftTime(), //$NON-NLS-1$ //$NON-NLS-2$
            lifters.get(0).getLastLiftTime().before(lifters.get(1).getLastLiftTime()));
        assertTrue(
            "time stamp precedence failed 1 vs 2 " + lifters.get(1).getLastLiftTime() + ">=" + lifters.get(2).getLastLiftTime(), //$NON-NLS-1$ //$NON-NLS-2$
            lifters.get(1).getLastLiftTime().before(lifters.get(2).getLastLiftTime()));
        assertEqualsToReferenceFile("/seq1_lift5.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$

        // get second try done
        failedLift(lifters);
        successfulLift(lifters);
        successfulLift(lifters);
        successfulLift(lifters);
        assertEqualsToReferenceFile("/seq1_lift6.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$

        // get third try done
        successfulLift(lifters);
        assertEqualsToReferenceFile("/seq1_lift7.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$
        successfulLift(lifters);
        successfulLift(lifters);
        successfulLift(lifters);
        assertEqualsToReferenceFile("/seq1_lift8.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$

        // end of snatch

        // mixed-up sequence of pass/fail/go-up
        Random rnd = new Random(0); // so the sequence is repeatable from test
                                    // to test.
        for (int i = 0; i < 16; i++) { // 16 is purely empirical, observing the
                                       // sequence of events generated
            switch (rnd.nextInt(3)) {
            case 0:
                // System.err.println("success "+lifters.get(0).getLastName()+" at "+lifters.get(0).getNextAttemptRequestedWeight());
                successfulLift(lifters);
                break;
            case 1:
                // System.err.println("failure "+lifters.get(0).getLastName()+" at "+lifters.get(0).getNextAttemptRequestedWeight());
                failedLift(lifters);
                break;
            case 2:
                final String change = Integer.toString(2 + lifters.get(0).getNextAttemptRequestedWeight());
                // System.err.println("change "+lifters.get(0).getLastName()+" to "+change);
                // in practice, declarations can't be redone, but for this test
                // all we care about
                // is that nextAttemptRequestedWeight has changed.
                declaration(lifters.get(0), lifters, change);
                break;
            }
        }
        // in this sequence, one lifter is already done, check that others are
        // listed below
        assertEqualsToReferenceFile("/seq1_lift9.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$

        // proceed with competition
        successfulLift(lifters);
        successfulLift(lifters);
        successfulLift(lifters);
        failedLift(lifters);
        // two lifters are now done
        assertEqualsToReferenceFile("/seq1_lift10.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$
        successfulLift(lifters);
        successfulLift(lifters);

        // all lifters are done, check medals
        // ==========================================
        // all lifters have body weight = 0
        // we have two lifters at same total and same bodyweight.
        // The one who reached total *first* should win.
        // in this test sequence, the winner has bigger lot number, but still
        // wins because of earlier lift.
        Collections.sort(lifters, new WinningOrderComparator(Ranking.TOTAL));
        LifterSorter.assignMedals(lifters);
        assertEqualsToReferenceFile("/seq1_medals_timeStamp.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$

        // now we give the first two lifters different body weights (second is
        // lighter)
        lifters.get(0).setBodyWeight(68.0);
        lifters.get(1).setBodyWeight(67.9);
        lifters.get(2).setBodyWeight(68.5);
        lifters.get(2).setBodyWeight(68.4);
        // and we sort again for medals.
        Collections.sort(lifters, new WinningOrderComparator(Ranking.TOTAL));
        LifterSorter.assignMedals(lifters);
        assertEqualsToReferenceFile("/seq1_medals_bodyWeight.txt", AllTests.shortDump(lifters)); //$NON-NLS-1$
    }

    @Test
    public void liftSequence2() {
        LifterSorter.assignLotNumbers(lifters);

        final Lifter schneiderF = lifters.get(0);
        final Lifter simpsonR = lifters.get(1);

        // hide non-lifters
        final int size = lifters.size();
        for (int i = 2; i < size; i++)
            lifters.remove(2);

        // simulate weigh-in
        schneiderF.setBodyWeight(68.0);
        simpsonR.setBodyWeight(67.9);
        schneiderF.setSnatch1Declaration(Integer.toString(70));
        simpsonR.setSnatch1Declaration(Integer.toString(60));
        schneiderF.setCleanJerk1Declaration(Integer.toString(80));
        simpsonR.setCleanJerk1Declaration(Integer.toString(80));
        LifterSorter.liftingOrder(lifters);

        // simpson will do all his lifts first and finish first
        successfulLift(lifters);
        successfulLift(lifters);
        successfulLift(lifters);
        successfulLift(lifters);
        successfulLift(lifters);
        successfulLift(lifters);
        // but schneider should still start first CJ (does not matter who lifted
        // first)
        assertEquals(schneiderF, lifters.get(0));
    }

    /*************************************************************************************
     * Utility routines
     */

    /**
     * Current lifter has successul lift
     * 
     * @param lifter
     */
    private void successfulLift(List<Lifter> lifters1) {
        final Lifter lifter = lifters1.get(0);
        final String weight = Integer.toString(lifter.getNextAttemptRequestedWeight());
        doLift(lifter, lifters1, weight);
    }

    /**
     * Current lifter fails.
     * 
     * @param lifter
     * @param lifters1
     */
    private void failedLift(List<Lifter> lifters1) {
        final Lifter lifter = lifters1.get(0);
        final Integer nextAttemptRequestedWeight = lifter.getNextAttemptRequestedWeight();
        final String weight = Integer.toString(-nextAttemptRequestedWeight);
        doLift(lifter, lifters1, weight);
        if (lifter.getAttemptsDone() < 5)
            assertEquals(
                "next requested weight should be equal after failed lift", nextAttemptRequestedWeight, lifter.getNextAttemptRequestedWeight()); //$NON-NLS-1$
    }

    /**
     * @param lifter
     * @param lifters1
     * @param weight
     */
    private void declaration(final Lifter lifter, List<Lifter> lifters1, final String weight) {
        // sleep for a while to ensure that we get different time stamps on the
        // lifts.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        ;
        switch (lifter.getAttemptsDone() + 1) {
        case 1:
            lifter.setSnatch1Declaration(weight);
            break;
        case 2:
            lifter.setSnatch2Declaration(weight);
            break;
        case 3:
            lifter.setSnatch3Declaration(weight);
            break;
        case 4:
            lifter.setCleanJerk1Declaration(weight);
            break;
        case 5:
            lifter.setCleanJerk2Declaration(weight);
            break;
        case 6:
            lifter.setCleanJerk3Declaration(weight);
            break;
        }
        LifterSorter.liftingOrder(lifters1);
    }

    /**
     * @param lifter
     * @param lifters1
     * @param weight
     */
    private void change1(final Lifter lifter, List<Lifter> lifters1, final String weight) {
        // sleep for a while to ensure that we get different time stamps on the
        // lifts.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        ;
        switch (lifter.getAttemptsDone() + 1) {
        case 1:
            lifter.setSnatch1Change1(weight);
            break;
        case 2:
            lifter.setSnatch2Change1(weight);
            break;
        case 3:
            lifter.setSnatch3Change1(weight);
            break;
        case 4:
            lifter.setCleanJerk1Change1(weight);
            break;
        case 5:
            lifter.setCleanJerk2Change1(weight);
            break;
        case 6:
            lifter.setCleanJerk3Change1(weight);
            break;
        }
        LifterSorter.liftingOrder(lifters1);
    }

    /**
     * @param lifter
     * @param lifters1
     * @param weight
     */
    private void change2(final Lifter lifter, List<Lifter> lifters1, final String weight) {
        // sleep for a while to ensure that we get different time stamps on the
        // lifts.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        ;
        switch (lifter.getAttemptsDone() + 1) {
        case 1:
            lifter.setSnatch1Change2(weight);
            break;
        case 2:
            lifter.setSnatch2Change2(weight);
            break;
        case 3:
            lifter.setSnatch3Change2(weight);
            break;
        case 4:
            lifter.setCleanJerk1Change2(weight);
            break;
        case 5:
            lifter.setCleanJerk2Change2(weight);
            break;
        case 6:
            lifter.setCleanJerk3Change2(weight);
            break;
        }
        LifterSorter.liftingOrder(lifters1);
    }

    /**
     * @param lifter
     * @param lifters1
     * @param weight
     */
    private void doLift(final Lifter lifter, List<Lifter> lifters1, final String weight) {
        // sleep for a while to ensure that we get different time stamps on the
        // lifts.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        ;
        switch (lifter.getAttemptsDone() + 1) {
        case 1:
            lifter.setSnatch1ActualLift(weight);
            break;
        case 2:
            lifter.setSnatch2ActualLift(weight);
            break;
        case 3:
            lifter.setSnatch3ActualLift(weight);
            break;
        case 4:
            lifter.setCleanJerk1ActualLift(weight);
            break;
        case 5:
            lifter.setCleanJerk2ActualLift(weight);
            break;
        case 6:
            lifter.setCleanJerk3ActualLift(weight);
            break;
        }
        LifterSorter.liftingOrder(lifters1);
    }

}
