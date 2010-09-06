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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.hibernate.Session;

import com.extentech.ExtenXLS.WorkBookHandle;
import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellTypeMismatchException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * The start sheet format is able to produce a round-trip. It implements both
 * the input sheet and output sheet interfaces.
 * 
 * @author jflamy
 * 
 */
public class WeighInSheet extends OutputSheet implements InputSheet {

    protected static final String TEMPLATE_XLS = "/WeighInSheetTemplate.xls"; //$NON-NLS-1$

    public WeighInSheet() {
    }

    public WeighInSheet(CategoryLookup categoryLookup, CompetitionApplication app, CompetitionSession competitionSession) {
        super(categoryLookup, app, competitionSession);
    }

    /**
     * @see org.concordiainternational.competition.spreadsheet.OutputSheet#writeLifter(org.concordiainternational.competition.data.Lifter,
     *      com.extentech.ExtenXLS.WorkSheetHandle,
     *      org.concordiainternational.competition.data.CategoryLookup, int)
     * 
     *      IMPORTANT: the columns in this routine must match those in
     *      {@link LifterReader#readLifter(WorkSheetHandle, int)}
     */
    @Override
    public void writeLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup, int rownum)
            throws CellTypeMismatchException, CellNotFoundException {
        rownum = rownum + LifterReader.START_ROW;
        workSheet.insertRow(rownum, true); // insérer une nouvelle ligne

        workSheet.getCell(rownum, 0).setVal(lifter.getMembership());
        workSheet.getCell(rownum, 1).setVal(lifter.getLotNumber());
        workSheet.getCell(rownum, 2).setVal(lifter.getLastName());
        workSheet.getCell(rownum, 3).setVal(lifter.getFirstName());
        if (Competition.isMasters()) {
            workSheet.getCell(rownum, 5).setVal(lifter.getMastersLongCategory());
        } else {
            workSheet.getCell(rownum, 5).setVal(lifter.getDisplayCategory());
        }

        workSheet.getCell(rownum, 6).setVal(lifter.getBodyWeight());
        workSheet.getCell(rownum, 7).setVal(lifter.getClub());
        workSheet.getCell(rownum, 8).setVal(lifter.getBirthDate());

        workSheet.getCell(rownum, 9).setVal(SheetUtils.fixValue(lifter.getSnatch1Declaration()));
        workSheet.getCell(rownum, 10).setVal(SheetUtils.fixValue(lifter.getSnatch1ActualLift()));
        workSheet.getCell(rownum, 11).setVal(SheetUtils.fixValue(lifter.getSnatch2ActualLift()));
        workSheet.getCell(rownum, 12).setVal(SheetUtils.fixValue(lifter.getSnatch3ActualLift()));

        workSheet.getCell(rownum, 14).setVal(SheetUtils.fixValue(lifter.getCleanJerk1Declaration()));
        workSheet.getCell(rownum, 15).setVal(SheetUtils.fixValue(lifter.getCleanJerk1ActualLift()));
        workSheet.getCell(rownum, 16).setVal(SheetUtils.fixValue(lifter.getCleanJerk2ActualLift()));
        workSheet.getCell(rownum, 17).setVal(SheetUtils.fixValue(lifter.getCleanJerk3ActualLift()));

        final CompetitionSession competitionSession = lifter.getCompetitionSession();
        workSheet.getCell(rownum, 22).setVal((competitionSession != null ? competitionSession.getName() : null));
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

    /*
     * (non-Javadoc)
     * 
     * @see org.concordia_international.reader.ResultSheet#getAllLifters()
     */
    public synchronized List<Lifter> getAllLifters(InputStream is, HbnSessionManager sessionMgr) throws IOException,
            CellNotFoundException, WorkSheetNotFoundException {
        WorkBookHandle workBookHandle = null;

        LinkedList<Lifter> allLifters;
        try {
            // get the data sheet
            workBookHandle = new WorkBookHandle(is);
            final WorkSheetHandle workSheet = workBookHandle.getWorkSheet(0);

            // process data sheet
            allLifters = new LinkedList<Lifter>();
            LifterReader lifterReader = new LifterReader(sessionMgr);
            for (int i = 0; true; i++) {
                final Lifter lifter = lifterReader.readLifter(workSheet, i);
                if (lifter != null) {
                    allLifters.add(lifter);
                    // System.err.println("added lifter " +
                    // LifterReader.toString(lifter));
                } else {
                    break;
                }
            }

            // readHeader(workSheet,sessionMgr.getHbnSession());

        } finally {
            // close workbook file and remove lock
            if (workBookHandle != null) workBookHandle.close();
            if (is != null) is.close();

        }
        return allLifters;
    }

    @SuppressWarnings( { "unchecked", "unused" })
    private void readHeader(WorkSheetHandle workSheet, Session hbnSession) throws CellNotFoundException {
        List<Competition> competitions = app.getHbnSession().createCriteria(Competition.class).list();
        if (competitions.size() > 0) {
            final Competition competition = competitions.get(0);
            competition.setFederation(workSheet.getCell("A1").getStringVal()); //$NON-NLS-1$
            competition.setFederationAddress(workSheet.getCell("A2").getStringVal()); //$NON-NLS-1$
            competition.setFederationWebSite(workSheet.getCell("A3").getStringVal()); //$NON-NLS-1$
            competition.setFederationEMail(workSheet.getCell("B4").getStringVal()); //$NON-NLS-1$

            competition.setCompetitionName(workSheet.getCell("L1").getStringVal()); //$NON-NLS-1$
            competition.setCompetitionSite(workSheet.getCell("L2").getStringVal()); //$NON-NLS-1$
            // competition.setCompetitionDate(workSheet.getCell("L3").getDateVal());

            competition.setCompetitionCity(workSheet.getCell("T2").getStringVal()); //$NON-NLS-1$
            competition.setCompetitionOrganizer(workSheet.getCell("T3").getStringVal()); //$NON-NLS-1$
            competition.setInvitedIfBornBefore(workSheet.getCell("T4").getIntVal()); //$NON-NLS-1$

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.concordia_international.reader.ResultSheet#getGroup(java.lang.String)
     */
    public List<Lifter> getGroupLifters(InputStream is, String aGroup, HbnSessionManager session) throws IOException,
            CellNotFoundException, WorkSheetNotFoundException {
        List<Lifter> groupLifters = new ArrayList<Lifter>();
        for (Lifter curLifter : getAllLifters(is, session)) {
            if (aGroup.equals(curLifter.getCompetitionSession())) {
                groupLifters.add(curLifter);
            }
        }
        return groupLifters;
    }

}
