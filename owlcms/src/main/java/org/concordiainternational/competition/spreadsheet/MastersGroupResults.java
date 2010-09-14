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

package org.concordiainternational.competition.spreadsheet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter.Ranking;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.WorkBookHandle;
import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellTypeMismatchException;
import com.extentech.formats.XLS.RowNotFoundException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;

/**
 * Result sheet, with team rankings
 * 
 * @author jflamy
 * 
 */
public class MastersGroupResults extends ResultSheet {

    protected static String templateXls = "/ResultSheetTemplate_Masters.xls"; //$NON-NLS-1$
    private Logger logger = LoggerFactory.getLogger(MastersGroupResults.class);
    Competition competition;

    public MastersGroupResults() {
    }

    public MastersGroupResults(CategoryLookup categoryLookup, CompetitionApplication app, CompetitionSession competitionSession) {
        super(categoryLookup, app, competitionSession);
    }

    @Override
    public void writeLifters(List<Lifter> lifters, OutputStream out) throws CellTypeMismatchException,
            CellNotFoundException, RowNotFoundException, IOException, WorkSheetNotFoundException {
        WorkBookHandle workBookHandle = null;

        try {
            if (lifters.isEmpty()) {
                // should have been dealt with earlier
                // this prevents a loop in the spreadsheet processing if it has
                // not.
                throw new RuntimeException(Messages.getString(
                    "OutputSheet.EmptySpreadsheet", CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$
            }
            // extract club lists
            TreeSet<String> clubs = new TreeSet<String>();
            for (Lifter curLifter : lifters) {
                clubs.add(curLifter.getClub());
            }

            // produce point rankings.
            List<Lifter> teamRankingLifters;

            teamRankingLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.TOTAL);
            LifterSorter.assignMedals(teamRankingLifters);

            // extract Canada lifters
            List<Lifter> canadaLifters = new ArrayList<Lifter>();
            for (Lifter curLifter : lifters) {
                if ("CAN".equals(curLifter.getClub())) {
                    canadaLifters.add(curLifter);
                }
            }

            // get the data sheet
            workBookHandle = new WorkBookHandle(getTemplate());
            WorkSheetHandle workSheet;

            // Result sheet, panam
            try {
                workSheet = workBookHandle.getWorkSheet("Results");
                new MastersIndividualSheet().writeIndividualSheet(lifters, workSheet, null);
            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }

            // Result sheet, canada
            try {
                workSheet = workBookHandle.getWorkSheet("Canada");
                new MastersIndividualSheet().writeIndividualSheet(canadaLifters, workSheet, null);
            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }

            // write out
            writeWorkBook(workBookHandle, out);
        } finally {
            // close files
            if (out != null) out.close();
            if (workBookHandle != null) workBookHandle.close();
            if (getTemplate() != null) getTemplate().close();
            logger.debug("done writing, closed files and handles."); //$NON-NLS-1$
        }
    }

    @Override
    public InputStream getTemplate() throws IOException {
        final InputStream resourceAsStream = app.getResourceAsStream(templateXls);
        // File templateFile = new
        // File(SheetUtils.getCompetition().getResultTemplateFileName());
        // FileInputStream resourceAsStream = new FileInputStream(templateFile);
        //if (resourceAsStream == null) {throw new IOException("file not found: "+templateFile.getAbsolutePath());} //$NON-NLS-1$
        return resourceAsStream;
    }

    @Override
    protected List<Lifter> getLifters(boolean excludeNotWeighed) {
        final List<Lifter> resultsOrderCopy = LifterSorter.resultsOrderCopy(new LifterContainer(app).getAllPojos(),
            Ranking.TOTAL);
        return resultsOrderCopy;
    }
}
