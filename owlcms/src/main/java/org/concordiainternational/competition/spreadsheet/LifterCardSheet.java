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
import java.util.List;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.CellHandle;
import com.extentech.ExtenXLS.FormatHandle;
import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellPositionConflictException;
import com.extentech.formats.XLS.CellTypeMismatchException;
import com.extentech.formats.XLS.RowNotFoundException;

/**
 * 
 * @author jflamy
 * 
 */
public class LifterCardSheet extends OutputSheet {

    protected static final String TEMPLATE_XLS = "/LifterCardTemplate.xls"; //$NON-NLS-1$
    Logger logger = LoggerFactory.getLogger(LifterCardSheet.class);

    final static int TEMPLATE_ROWS = 11;
    final static int TEMPLATE_COLUMNS = 8;

    int nbCardsPrinted = 0; // how many cards we have printed so far

    public LifterCardSheet() {

    }

    public LifterCardSheet(CategoryLookup categoryLookup, CompetitionApplication app, CompetitionSession competitionSession) {
        super(categoryLookup, app, competitionSession);
    }

    @Override
    public void writeLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup, int ignored)
            throws CellTypeMismatchException, CellNotFoundException {
        int rowNum = (nbCardsPrinted * TEMPLATE_ROWS) - (nbCardsPrinted / 2);

        if (nbCardsPrinted > 0) {
            createNewCard(rowNum, workSheet); // copy rows to the bottom of the
                                              // spreadsheet.
        }

        workSheet.getCell(rowNum, 1).setVal(lifter.getLastName().toUpperCase() + ", " + lifter.getFirstName());
        workSheet.getCell(rowNum, 5).setVal(lifter.getClub());

        workSheet.getCell(rowNum + 2, 1).setVal(lifter.getLotNumber());
        workSheet.getCell(rowNum + 2, 3).setVal(lifter.getCompetitionSession().getName());
        if (Competition.isMasters()) {
            workSheet.getCell(rowNum + 2, 5).setVal(lifter.getMastersAgeGroupInterval());
        } else {
            workSheet.getCell(rowNum + 2, 4).setVal("");
            workSheet.getCell(rowNum + 2, 5).setVal("");
            workSheet.getCell(rowNum + 2, 5).setBorderLineStyle(FormatHandle.BORDER_NONE);
        }

        final Category category = lifter.getRegistrationCategory();
        workSheet.getCell(rowNum + 2, 7).setVal((category != null ? category.getName() : null));

        nbCardsPrinted++;

    }

    /**
     * @param rownum
     * @param worksheet
     */
    private void createNewCard(int rownum, WorkSheetHandle worksheet) {
        int row = 0;
        int column = 0;

        int templateRows = TEMPLATE_ROWS;

        // we do two cards per page. The last row is the spacer between the top
        // and
        // bottom card; on bottom cards we don't copy the spacer.

        if ((nbCardsPrinted % 2) == 1) {
            // we we are printing the bottom one.
            templateRows--;
        }
        // System.err.println("nbcards = "+nbCardsPrinted+" rownum = "+rownum+" templateRows = "+templateRows);

        for (row = 0; row < templateRows;) {
            for (column = 0; column < TEMPLATE_COLUMNS;) {
                try {
                    CellHandle sourceCell = worksheet.getCell(row, column);
                    final int destination = row + rownum;
                    CellHandle.copyCellToWorkSheet(sourceCell, worksheet, destination, column);
                } catch (CellNotFoundException e) {
                    // ignore (do not copy empty cells).
                } catch (CellPositionConflictException e) {
                    LoggerUtils.logException(logger, e);
                }
                column++;
            }
            row++;
        }

    }

    @Override
    public InputStream getTemplate() throws IOException {
        final InputStream resourceAsStream = app.getResourceAsStream(TEMPLATE_XLS);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + TEMPLATE_XLS);} //$NON-NLS-1$
        return resourceAsStream;
    }

    @Override
    protected List<Lifter> getLifters(boolean excludeNotWeighed) {
        return LifterSorter.registrationOrderCopy(Lifter.getAll());
    }

    @Override
    protected void removeLastRowIfInserting(WorkSheetHandle workSheet, int rownum) throws RowNotFoundException {
        // do nothing
    }

    @Override
    protected void writeHeader(WorkSheetHandle workSheet) throws CellTypeMismatchException, CellNotFoundException {
        // do nothing
    }
}
