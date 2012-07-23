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

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.spreadsheet.InputSheet;
import org.concordiainternational.competition.spreadsheet.WeighInSheet;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflamy
 * 
 */
public class SpreadsheetTest extends SharedTestSetup {

    Logger logger = LoggerFactory.getLogger(SpreadsheetTest.class);
    static private List<Lifter> lifters;

    @Override
    @Before
    public void setUpTest() {
        super.setUpTest();
        
    }

    /**
     * @param args
     * @throws Throwable
     */
    @Test
    public void getAllLifters() throws Throwable {
        InputStream is = AllTests.class.getResourceAsStream("/testData/roundTripInputSheet.xls"); //$NON-NLS-1$
        InputSheet results = new WeighInSheet();
        lifters = results.getAllLifters(is, CompetitionApplication.getEntityManager());
        AllTests.longDump(lifters, false);
    }



}
