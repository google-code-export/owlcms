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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.spreadsheet.InputSheet;
import org.concordiainternational.competition.spreadsheet.ResultSheet;
import org.concordiainternational.competition.spreadsheet.WeighInSheet;
import org.concordiainternational.competition.ui.CompetitionApplication;
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
    static private String loadResults;
    static private File tempFile;
    static private List<Lifter> lifters;

    @Before
    public void setupTest() {
        Assert.assertNotNull(hbnSessionManager);
        Assert.assertNotNull(hbnSessionManager.getHbnSession());
        hbnSessionManager.getHbnSession().beginTransaction();
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
        InputStream is = AllTests.class.getResourceAsStream("/testData/roundTripInputSheet.xls"); //$NON-NLS-1$
        InputSheet results = new WeighInSheet(AllTests.getSessionManager());
        lifters = results.getAllLifters(is, hbnSessionManager);
        loadResults = AllTests.longDump(lifters, false);
    }

    @Test
    public void writeAllLifters() throws Throwable {
        tempFile = File.createTempFile("myApp", ".xls"); //$NON-NLS-1$ //$NON-NLS-2$
        //tempFile.deleteOnExit();
        logger.warn("temporary file is set to "+tempFile.getAbsolutePath());
        FileOutputStream os = new FileOutputStream(tempFile);
        new ResultSheet(new CategoryLookup(hbnSessionManager), new CompetitionApplication(), (CompetitionSession) null)
                .writeLifters(lifters, os);
    }

    @Test
    public void reloadFromOutput() throws Throwable {
        InputStream is = new FileInputStream(tempFile);
        ResultSheet readAgain = new ResultSheet(AllTests.getSessionManager());
        lifters = readAgain.getAllLifters(is, hbnSessionManager);
        String loadAgainResults = AllTests.longDump(lifters, false);
        assertEquals("roundtrip failed", loadResults, loadAgainResults); //$NON-NLS-1$
    }


}
