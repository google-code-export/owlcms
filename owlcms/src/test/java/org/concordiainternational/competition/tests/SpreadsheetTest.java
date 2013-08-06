/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, 
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.tests;

import java.io.InputStream;
import java.util.List;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.spreadsheet.ExtenXLSReader;
import org.concordiainternational.competition.spreadsheet.InputSheet;
import org.concordiainternational.competition.spreadsheet.WeighInSheet;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * @author jflamy
 * 
 */
public class SpreadsheetTest {

    HbnSessionManager hbnSessionManager = AllTests.getSessionManager();
    Logger logger = LoggerFactory.getLogger(SpreadsheetTest.class);
    static private List<Lifter> lifters;

    @Before
    public void setupTest() {
        Assert.assertNotNull(hbnSessionManager);
        Assert.assertNotNull(hbnSessionManager.getHbnSession());
        hbnSessionManager.getHbnSession().beginTransaction();
        CategoryLookup categoryLookup = CategoryLookup.getSharedInstance(hbnSessionManager);
        categoryLookup.reload();
    }

    @After
    public void tearDownTest() {
        hbnSessionManager.getHbnSession().close();
    }

    /**
     * @param args
     * @throws Throwable
     */
    @Test
    public void getAllLifters() throws Throwable {

        HbnSessionManager sessionManager = AllTests.getSessionManager();
        Session hbnSession = sessionManager.getHbnSession();
 
        InputStream is = AllTests.class.getResourceAsStream("/testData/roundTripInputSheet.xls"); //$NON-NLS-1$
        InputSheet results = new WeighInSheet();
        results.init(new ExtenXLSReader(sessionManager));

        lifters = results.getAllLifters(is, hbnSession);
//        lifters = LifterSorter.displayOrderCopy(Lifter.getAll(hbnSession,false));  
        List<Lifter> sortedLifters = LifterSorter.displayOrderCopy(lifters);
        System.out.println(AllTests.longDump(sortedLifters));
    }




}
