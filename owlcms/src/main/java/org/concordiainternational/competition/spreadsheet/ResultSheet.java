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

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter.Ranking;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellTypeMismatchException;

/**
 * 
 * @author jflamy
 * 
 */
public class ResultSheet extends OutputSheet {

    protected static final String TEMPLATE_XLS = "/ResultSheetTemplate.xls"; //$NON-NLS-1$
    Logger logger = LoggerFactory.getLogger(ResultSheet.class);

    public ResultSheet() {
    }

    public ResultSheet(CategoryLookup categoryLookup, CompetitionApplication app, CompetitionSession competitionSession) {
        super(categoryLookup, app, competitionSession);
    }

    @Override
    public void writeLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup, int rownum)
            throws CellTypeMismatchException, CellNotFoundException {
        rownum = rownum + LifterReader.START_ROW;
        workSheet.insertRow(rownum, true); // insérer une nouvelle ligne.

        workSheet.getCell(rownum, 0).setVal(lifter.getMembership());
        workSheet.getCell(rownum, 1).setVal(lifter.getLotNumber());
        workSheet.getCell(rownum, 2).setVal(lifter.getLastName());
        workSheet.getCell(rownum, 3).setVal(lifter.getFirstName());
        final String gender = lifter.getGender();
        workSheet.getCell(rownum, 4).setVal((gender != null ? gender.toString() : null));
        workSheet.getCell(rownum, 5).setVal(lifter.getDisplayCategory());
        workSheet.getCell(rownum, 6).setVal(lifter.getBodyWeight());
        workSheet.getCell(rownum, 7).setVal(lifter.getClub());
        workSheet.getCell(rownum, 8).setVal(lifter.getBirthDate());

        workSheet.getCell(rownum, 9).setVal(SheetUtils.fixValue(lifter.getSnatch1ActualLift()));
        workSheet.getCell(rownum, 10).setVal(SheetUtils.fixValue(lifter.getSnatch2ActualLift()));
        workSheet.getCell(rownum, 11).setVal(SheetUtils.fixValue(lifter.getSnatch3ActualLift()));

        workSheet.getCell(rownum, 13).setVal(SheetUtils.fixValue(lifter.getCleanJerk1ActualLift()));
        workSheet.getCell(rownum, 14).setVal(SheetUtils.fixValue(lifter.getCleanJerk2ActualLift()));
        workSheet.getCell(rownum, 15).setVal(SheetUtils.fixValue(lifter.getCleanJerk3ActualLift()));

        workSheet.getCell(rownum, 17).setVal(SheetUtils.fixValue(lifter.getTotal()));
        workSheet.getCell(rownum, 18).setVal(SheetUtils.fixRank(lifter.getRank()));
        workSheet.getCell(rownum, 20).setVal(lifter.getSinclair());
        workSheet.getCell(rownum, 21).setVal(lifter.getCategorySinclair());
        // final Group group = lifter.getGroup();
        // workSheet.getCell(rownum,22).setVal((group != null ? group.getName()
        // : null));
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
        final List<Lifter> resultsOrderCopy = LifterSorter.resultsOrderCopy(new LifterContainer(app).getAllPojos(),
            Ranking.TOTAL);
        LifterSorter.assignMedals(resultsOrderCopy);
        return resultsOrderCopy;
    }
}
