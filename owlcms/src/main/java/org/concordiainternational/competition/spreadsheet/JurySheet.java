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
import java.util.Date;
import java.util.List;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellTypeMismatchException;
import com.extentech.formats.XLS.RowNotFoundException;

/**
 * The Jury sheet is used when the Jury rates officials for promotion.
 * 
 * @author jflamy
 * 
 */
public class JurySheet extends OutputSheet {

    protected static final String TEMPLATE_XLS = "/JurySheetTemplate.xls"; //$NON-NLS-1$
    private static final int START_ROW = 7;
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(JurySheet.class);

    public JurySheet() {
    }

    public JurySheet(CategoryLookup categoryLookup, CompetitionApplication app, CompetitionSession competitionSession) {
        super(categoryLookup, app, competitionSession);
    }

    /**
     * @see org.concordiainternational.competition.spreadsheet.OutputSheet#writeLifter(org.concordiainternational.competition.data.Lifter,
     *      com.extentech.ExtenXLS.WorkSheetHandle,
     *      org.concordiainternational.competition.data.CategoryLookup, int)
     */
    @Override
    public void writeLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup1, int rownum)
            throws CellTypeMismatchException, CellNotFoundException {
        rownum = rownum + START_ROW;
        final String lastName = lifter.getLastName();
        workSheet.getCell(rownum, 1).setVal(lastName != null ? lastName : ""); //$NON-NLS-1$
        final String firstName = lifter.getFirstName();
        workSheet.getCell(rownum, 2).setVal(firstName != null ? firstName : ""); //$NON-NLS-1$
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
        return LifterSorter.displayOrderCopy(new LifterContainer(app, excludeNotWeighed).getAllPojos());
    }

    private String group;

    /*
     * (non-Javadoc)
     * 
     * @see org.concordia_international.reader.ResultSheet#getGroup()
     */
    public String getGroup() {
        return this.group;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.concordia_international.reader.ResultSheet#setGroup(java.lang.String)
     */
    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void writeHeader(WorkSheetHandle workSheet) throws CellTypeMismatchException, CellNotFoundException {
        List<Competition> competitions = app.getHbnSession().createCriteria(Competition.class).list();
        if (competitions.size() > 0) {
            final Competition competition = competitions.get(0);

            if (group != null) workSheet.getCell("D6").setVal(group); //$NON-NLS-1$
            workSheet.getCell("C2").setVal(competition.getCompetitionName()); //$NON-NLS-1$
            workSheet.getCell("C3").setVal(competition.getCompetitionSite()); //$NON-NLS-1$

            final Date competitionDate = competition.getCompetitionDate();
            if (competitionDate != null) workSheet.getCell("I3").setVal(SheetUtils.getShortDate(competitionDate)); //$NON-NLS-1$
        }
    }

    @Override
    protected void removeLastRowIfInserting(WorkSheetHandle workSheet, int i) throws RowNotFoundException {
        // Do nothing; we do not insert rows, so we don't have to hide an
        // extra one inserted prior to writing data
    }

}
