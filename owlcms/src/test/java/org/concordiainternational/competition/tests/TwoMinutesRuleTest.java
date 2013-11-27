/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, 
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.tests;

import static org.concordiainternational.competition.tests.AllTests.assertEqualsToReferenceFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.CompetitionApplicationComponents;
import org.concordiainternational.competition.ui.SessionData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

public class TwoMinutesRuleTest {
    final static Logger logger = LoggerFactory.getLogger(TwoMinutesRuleTest.class);
    HbnSessionManager hbnSessionManager = AllTests.getSessionManager();
    HbnContainer<Lifter> hbnLifters = null;
    List<Lifter> lifters = null;
    SessionData groupData;

    @Before
    public void setupTest() {
        assertNotNull(hbnSessionManager);
        assertNotNull(hbnSessionManager.getHbnSession());
        hbnSessionManager.getHbnSession().beginTransaction();

        // mock the application
        final CompetitionApplication application = new CompetitionApplication();
        CompetitionApplication.setCurrent(application);
        application.components = new CompetitionApplicationComponents(null, null);
        String platformName = CompetitionApplicationComponents.firstPlatformName();
        application.setPlatformByName(platformName);

        // for this test, the initial data does not include body weights, so we
        // use false
        // on the constructor to keep the incomplete data.
        hbnLifters = new LifterContainer(application, false);

        // initialize the competition data with the lifters
        lifters = (hbnLifters.getAllPojos());

        // set up the port
        String portName = null;
        try {
        	Properties props = new Properties();
        	props.load(this.getClass().getResourceAsStream("/tests.properties"));
        	portName = (String) props.get("portName");
        	if (portName != null && portName.trim().isEmpty()) portName = null;
        } catch (IOException ioe) {        	
        }

    }

    @After
    public void tearDownTest() {
        hbnSessionManager.getHbnSession().close();
    }

    @Test
    public void initialCheck() {
        final String resName = "/initialCheck.txt"; //$NON-NLS-1$
        LifterSorter.assignLotNumbers(lifters);
        LifterSorter.assignStartNumbers(lifters);

        Collections.shuffle(lifters);

        List<Lifter> sorted = LifterSorter.liftingOrderCopy(lifters);
        final String actual = AllTests.shortDump(sorted);
        assertEqualsToReferenceFile(resName, actual);
    }

    @Test
    public void liftTimeOrder() {
        LifterSorter.assignLotNumbers(lifters);
        groupData = new SessionData(lifters);

        final Lifter schneiderF = lifters.get(0);
        final Lifter simpsonR = lifters.get(1);
        final Lifter allisonR = lifters.get(2);
        final Lifter verneU = lifters.get(3);

        // hide non-lifters
        LifterSorter.liftingOrder(lifters);
        final int size = lifters.size();
        for (int i = 4; i < size; i++)
            lifters.remove(4);

        // simulate initial declaration at weigh-in
        schneiderF.setSnatch1Declaration(Integer.toString(60));
        simpsonR.setSnatch1Declaration(Integer.toString(65));
        allisonR.setSnatch1Declaration(Integer.toString(65));
        verneU.setSnatch1Declaration(Integer.toString(70));
        schneiderF.setCleanJerk1Declaration(Integer.toString(70));
        simpsonR.setCleanJerk1Declaration(Integer.toString(81));
        allisonR.setCleanJerk1Declaration(Integer.toString(90));
        verneU.setCleanJerk1Declaration(Integer.toString(92));

        Lifter curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        System.out.println(AllTests.shortDump(groupData.getAttemptOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());

        curLifter = groupData.getLifters().get(0);
        groupData.callLifter(curLifter);
        successfulLift(lifters);
        System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(curLifter, groupData.getPreviousLifter());
    }

    @Test
    public void liftSequence3() throws InterruptedException {
        LifterSorter.assignLotNumbers(lifters);

        final Lifter schneiderF = lifters.get(0);
        final Lifter simpsonR = lifters.get(1);

        // simulate initial declaration at weigh-in
        schneiderF.setSnatch1Declaration(Integer.toString(60));
        simpsonR.setSnatch1Declaration(Integer.toString(60));
        schneiderF.setCleanJerk1Declaration(Integer.toString(80));
        simpsonR.setCleanJerk1Declaration(Integer.toString(82));

        // hide non-lifters
        LifterSorter.liftingOrder(lifters);
        final int size = lifters.size();
        for (int i = 2; i < size; i++)
            lifters.remove(2);
        groupData = new SessionData(lifters);

        // competition start
        assertEquals(60000, groupData.timeAllowed(schneiderF));
        assertEquals(60000, groupData.timeAllowed(simpsonR));

        // schneiderF is called with initial weight
        Lifter curLifter = groupData.getLifters().get(0);
        assertEquals(schneiderF, curLifter);
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$

        // first is now simpsonR ; he has declared 60kg
        curLifter = groupData.getLifters().get(0);
        assertEquals(simpsonR, curLifter);
        assertEquals(60000, groupData.timeAllowed(curLifter));
        // ... but changes to 62 before being called by announcer (time not
        // restarted)
        declaration(curLifter, groupData.getLifters(), "62"); //$NON-NLS-1$
        logger.info("declaration by {}: {}", curLifter, "62"); //$NON-NLS-1$ //$NON-NLS-2$

        // now schneider should be back on top at 61, with two minutes because
        // there was no announce.
        curLifter = groupData.getLifters().get(0);
        assertEquals(schneiderF, curLifter);
        assertEquals(120000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$

        // schneider has lifted 62, is now simpson's turn, should NOT have 2
        // minutes
        curLifter = groupData.getLifters().get(0);
        assertEquals(simpsonR, curLifter);
        assertEquals(60000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        failedLift(groupData.getLifters());
        logger.info("failed lift for {}", curLifter); //$NON-NLS-1$

        // still simpson because 2nd try and schneider is at 3rd.
        curLifter = groupData.getLifters().get(0);
        assertEquals(simpsonR, curLifter);
        assertEquals(120000, groupData.timeAllowed(curLifter));
        // simpson is called again with two minutes
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        // but asks for more
        Thread.sleep(500); // wait for a while
        declaration(curLifter, groupData.getLifters(), "67"); //$NON-NLS-1$
        logger.info("declaration by {}: {}", curLifter, "67"); //$NON-NLS-1$ //$NON-NLS-2$

        // schneider does not get 2 minutes.
        curLifter = groupData.getLifters().get(0);
        assertEquals(schneiderF, curLifter);
        assertEquals(60000, groupData.timeAllowed(curLifter));
        // schneider is called
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        // but asks for more
        Thread.sleep(400); // wait for a while
        // the following stops time.
        declaration(curLifter, lifters, "65"); //$NON-NLS-1$
        int remainingTime = groupData.getTimer().getTimeRemaining();

        // at this point, if schneider is called, he should get the remaining.
        curLifter = groupData.getLifters().get(0);
        assertEquals(schneiderF, curLifter);
        assertEquals(remainingTime, groupData.timeAllowed(curLifter));

        // and if instead if declaring 65 schneider had declared 69, simpson
        // would be lifting
        // but with one minute.
        assertEquals(60000, groupData.timeAllowed(simpsonR));
    }

    @Test
    public void liftSequence4() throws InterruptedException {
        LifterSorter.assignLotNumbers(lifters);

        final Lifter schneiderF = lifters.get(0);
        final Lifter simpsonR = lifters.get(1);

        // simulate initial declaration at weigh-in
        schneiderF.setSnatch1Declaration(Integer.toString(60));
        simpsonR.setSnatch1Declaration(Integer.toString(65));
        schneiderF.setCleanJerk1Declaration(Integer.toString(80));
        simpsonR.setCleanJerk1Declaration(Integer.toString(85));

        // hide non-lifters
        LifterSorter.liftingOrder(lifters);
        final int size = lifters.size();
        for (int i = 2; i < size; i++)
            lifters.remove(2);
        groupData = new SessionData(lifters);

        // competition start
        assertEquals(60000, groupData.timeAllowed(schneiderF));
        assertEquals(60000, groupData.timeAllowed(simpsonR));

        // schneiderF snatch1
        Lifter curLifter = groupData.getLifters().get(0);
        assertEquals(schneiderF, curLifter);
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$

        // schneiderF snatch2
        curLifter = groupData.getLifters().get(0);
        assertEquals(schneiderF, curLifter);
        assertEquals(120000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$

        // schneiderF snatch3
        curLifter = groupData.getLifters().get(0);
        assertEquals(schneiderF, curLifter);
        assertEquals(120000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$

        // simpsonR snatch1
        curLifter = groupData.getLifters().get(0);
        assertEquals(simpsonR, curLifter);
        assertEquals(60000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$
        // simpsonR snatch2
        curLifter = groupData.getLifters().get(0);
        assertEquals(simpsonR, curLifter);
        assertEquals(120000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$
        // simpsonR snatch3
        curLifter = groupData.getLifters().get(0);
        assertEquals(simpsonR, curLifter);
        assertEquals(120000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$

        // schneiderF cj1
        curLifter = groupData.getLifters().get(0);
        assertEquals(schneiderF, curLifter);
        assertEquals(60000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$

        // schneiderF cj2
        curLifter = groupData.getLifters().get(0);
        assertEquals(schneiderF, curLifter);
        // System.err.println(AllTests.longDump(groupData.getLiftTimeOrder()));
        assertEquals(schneiderF, groupData.getPreviousLifter());
        assertEquals(120000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$

        // schneiderF cj3
        curLifter = groupData.getLifters().get(0);
        assertEquals(schneiderF, curLifter);
        assertEquals(120000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$

        // simpsonR cj1
        curLifter = groupData.getLifters().get(0);
        assertEquals(simpsonR, curLifter);
        assertEquals(60000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$
        // simpsonR cj2
        curLifter = groupData.getLifters().get(0);
        assertEquals(simpsonR, curLifter);
        assertEquals(120000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$
        // simpsonR cj3
        curLifter = groupData.getLifters().get(0);
        assertEquals(simpsonR, curLifter);
        assertEquals(120000, groupData.timeAllowed(curLifter));
        logger.info("calling lifter: {}", curLifter); //$NON-NLS-1$
        groupData.callLifter(curLifter);
        successfulLift(groupData.getLifters());
        logger.info("successful lift for {}", curLifter); //$NON-NLS-1$
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
     * @throws InterruptedException
     */
    private void declaration(final Lifter lifter, List<Lifter> lifters1, final String weight)
            throws InterruptedException {
        // sleep for a while to ensure that we get different time stamps on the
        // lifts.
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        ;
        groupData.getTimer().pause();
        logger.info("time paused : time remaining {}", groupData.getTimer().getTimeRemaining()); //$NON-NLS-1$
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
        groupData.updateListsForLiftingOrderChange(lifter);
    }

    /**
     * @param lifter
     * @param lifters1
     * @param weight
     */
    @SuppressWarnings("unused")
    private void change1(final Lifter lifter, List<Lifter> lifters1, final String weight) {
        // sleep for a while to ensure that we get different time stamps on the
        // lifts.
        try {
            Thread.sleep(100);
            groupData.getTimer().pause();
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
        groupData.updateListsForLiftingOrderChange(lifter);
    }

    /**
     * @param lifter
     * @param lifters1
     * @param weight
     */
    @SuppressWarnings("unused")
    private void change2(final Lifter lifter, List<Lifter> lifters1, final String weight) {
        // sleep for a while to ensure that we get different time stamps on the
        // lifts.
        try {
            Thread.sleep(100);
            groupData.getTimer().pause();
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
        groupData.updateListsForLiftingOrderChange(lifter);
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
            Thread.sleep(1000);
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
        groupData.liftDone(lifter, !weight.startsWith("-")); //$NON-NLS-1$
        groupData.updateListsForLiftingOrderChange(lifter);
    }

}
