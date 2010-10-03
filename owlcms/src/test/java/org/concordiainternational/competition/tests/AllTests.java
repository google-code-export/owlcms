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
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.nec.NECDisplay;
import org.concordiainternational.competition.spreadsheet.InputSheetHelper;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.hibernate.Session;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

@RunWith(Suite.class)
@Suite.SuiteClasses( { CategoryLookupTest.class, GroupLookupTest.class, LifterEditorTest.class, LifterSorterTest.class,
        LifterTest.class, SpreadsheetTest.class, TwoMinutesRuleTest.class, NECDisplay.class, })
public class AllTests implements HbnSessionManager {

    final static String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$

    /**
     * Always return a session in test mode, where the database is run in memory
     * and flushed at the end of the test.
     * 
     * @return
     */
    @Override
    public Session getHbnSession() {
        final boolean testMode = true;
        return WebApplicationConfiguration.getSessionFactory(testMode, "tests").getCurrentSession(); //$NON-NLS-1$
    }

    // private String iterateContainer(Container.Ordered container) {
    // StringBuffer sb = new StringBuffer();
    // Object curId = container.firstItemId();
    // while (curId != null) {
    // Item item = container.getItem(curId);
    // Lifter lifter = (Lifter) ItemAdapter.getObject(item);
    // sb.append(lifter.getLastName()+" "+lifter.getFirstName()+" "+lifter.getNextAttemptRequestedWeight()+" "+(lifter.getAttemptsDone()+1)+" "+lifter.getLastLiftTime()+" "+lifter.getLotNumber());
    // sb.append(lineSeparator);
    // curId = container.nextItemId(curId);
    // }
    // return sb.toString();
    // }

    /**
     * @param lifterList
     * @return ordered printout of lifters, one per line.
     */
    public static String shortDump(List<Lifter> lifterList) {
        StringBuffer sb = new StringBuffer();
        for (Lifter lifter : lifterList) {
            sb.append(lifter.getLastName() + " " + lifter.getFirstName() //$NON-NLS-1$
                + " " + lifter.getNextAttemptRequestedWeight() //$NON-NLS-1$
                + " " + (lifter.getAttemptsDone() + 1) //$NON-NLS-1$
                + " " + lifter.getLotNumber()); //$NON-NLS-1$
            sb.append(AllTests.lineSeparator);
        }
        return sb.toString();
    }

    /**
     * @param lifterList
     * @return ordered printout of lifters, one per line.
     */
    public static String longDump(List<Lifter> lifterList) {
        StringBuffer sb = new StringBuffer();
        for (Lifter lifter : lifterList) {
            sb.append(InputSheetHelper.toString(lifter));
            sb.append(AllTests.lineSeparator);
        }
        return sb.toString();
    }

    /**
     * @param lifterList
     * @return ordered printout of lifters, one per line.
     */
    static String longDump(List<Lifter> lifterList, boolean includeTimeStamp) {
        StringBuffer sb = new StringBuffer();
        for (Lifter lifter : lifterList) {
            sb.append(InputSheetHelper.toString(lifter, includeTimeStamp));
            sb.append(AllTests.lineSeparator);
        }
        return sb.toString();
    }

    /**
     * Compare actual with expected that is read from a file (a resource found
     * on the class path)
     * 
     * @param referenceFilePath
     *            a path of the form /filename where filename is located in a
     *            directory that is found on the class path.
     * @param actual
     */
    static public void assertEqualsToReferenceFile(final String referenceFilePath, String actual) {
        InputStream is = AllTests.class.getResourceAsStream("/testData" + referenceFilePath); //$NON-NLS-1$
        if (is != null) {
            String expected = getContents(is);
            assertEquals(referenceFilePath, expected, actual);
        } else {
            System.err.println("------ if ok, copy following to " + referenceFilePath); //$NON-NLS-1$
            System.err.println(actual);
            System.err.println("------"); //$NON-NLS-1$
            fail(referenceFilePath + " not found"); //$NON-NLS-1$
        }
    }

    static public String getContents(InputStream is) {
        StringBuilder contents = new StringBuilder();

        try {
            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            try {
                String line = null; // not declared within while loop
                /*
                 * readLine is a bit quirky : it returns the content of a line
                 * MINUS the newline. it returns null only for the END of the
                 * stream. it returns an empty String if two newlines appear in
                 * a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator")); //$NON-NLS-1$
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return contents.toString();
    }

    public static HbnSessionManager getSessionManager() {
        return new AllTests();
    }
}
