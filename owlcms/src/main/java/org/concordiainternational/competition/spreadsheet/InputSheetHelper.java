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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CategoryLookupByName;
import org.concordiainternational.competition.data.Gender;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.CompetitionSessionLookup;
import org.concordiainternational.competition.data.Lifter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.CellHandle;
import com.extentech.ExtenXLS.WorkBookHandle;
import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

public class InputSheetHelper implements InputSheet {
    final private static Logger logger = LoggerFactory.getLogger(InputSheetHelper.class);

    // constants
    final static int START_ROW = 7;
    static final int GENDER_COLUMN = 4;
    static final int BODY_WEIGHT_COLUMN = 6;

    private CategoryLookup categoryLookup;
    private CompetitionSessionLookup competitionSessionLookup;
    private CategoryLookupByName categoryLookupByName;

	private LifterReader reader;

    InputSheetHelper(HbnSessionManager hbnSessionManager, LifterReader reader) {
        categoryLookup = new CategoryLookup(hbnSessionManager);
        categoryLookupByName = new CategoryLookupByName(hbnSessionManager);
        competitionSessionLookup = new CompetitionSessionLookup(hbnSessionManager);
        this.reader = reader;
    }

    /* (non-Javadoc)
     * @see org.concordiainternational.competition.spreadsheet.InputSheet#getAllLifters(java.io.InputStream, com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager)
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
            LifterReader lifterReader = reader;
            for (int i = 0; true; i++) {
                final Lifter lifter = lifterReader.readLifter(workSheet, i);
                if (lifter != null) {
                    allLifters.add(lifter);
                    // System.err.println("added lifter " +
                    // InputSheetHelper.toString(lifter));
                } else {
                    break;
                }
            }

            // readHeader(workSheet,sessionMgr.getHbnSession());

        } finally {
            // close workbook file and hide lock
            if (workBookHandle != null) workBookHandle.close();
            if (is != null) is.close();

        }
        return allLifters;
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
    
    public static String toString(Lifter lifter, boolean includeTimeStamp) {
        final Category category = lifter.getCategory();
        final CompetitionSession competitionSession = lifter.getCompetitionSession();
        final Date lastLiftTime = lifter.getLastLiftTime();
        return (new StringBuilder())
                .append(" lastName=" + lifter.getLastName()) //$NON-NLS-1$
                .append(" firstName=" + lifter.getFirstName()) //$NON-NLS-1$
                .append(" membership=" + lifter.getMembership()) //$NON-NLS-1$
                .append(" lotNumber=" + lifter.getLotNumber()) //$NON-NLS-1$
                .append(" group=" + (competitionSession != null ? competitionSession.getName() : null)) //$NON-NLS-1$
                .append(" club=" + lifter.getClub()) //$NON-NLS-1$
                .append(" gender=" + lifter.getGender()) //$NON-NLS-1$
                .append(" bodyWeight=" + lifter.getBodyWeight()) //$NON-NLS-1$
                .append(" birthDate=" + lifter.getBirthDate()) //$NON-NLS-1$
                .append(" registrationCategory=" + lifter.getRegistrationCategory()) //$NON-NLS-1$
                .append(" category=" + (category != null ? category.getName() : null)) //$NON-NLS-1$
                .append(" snatch1ActualLift=" + lifter.getSnatch1ActualLift()) //$NON-NLS-1$
                .append(" snatch2=" + lifter.getSnatch2ActualLift()) //$NON-NLS-1$
                .append(" snatch3=" + lifter.getSnatch3ActualLift()) //$NON-NLS-1$
                .append(" bestSnatch=" + lifter.getBestSnatch()) //$NON-NLS-1$
                .append(" cleanJerk1ActualLift=" + lifter.getCleanJerk1ActualLift()) //$NON-NLS-1$
                .append(" cleanJerk2=" + lifter.getCleanJerk2ActualLift()) //$NON-NLS-1$
                .append(" cleanJerk3=" + lifter.getCleanJerk3ActualLift()) //$NON-NLS-1$
                .append(
                    includeTimeStamp ? (" lastLiftTime=" + (lastLiftTime != null ? lastLiftTime.getTime() : null)) : "") //$NON-NLS-1$ //$NON-NLS-2$
                .append(" total=" + lifter.getTotal()) //$NON-NLS-1$
                .append(" totalRank=" + lifter.getRank()) //$NON-NLS-1$
                .toString();
    }

    public static String toString(Lifter lifter) {
        return toString(lifter, true);
    }

    public Integer getInt(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
        CellHandle cell = sheet.getCell(row, column);
        Integer intVal = (cell != null ? cell.getIntVal() : null);
        return intVal;
    }

    public Double getDouble(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
        CellHandle cell = sheet.getCell(row, column);
        Double val = (cell != null ? cell.getDoubleVal() : null);
        return val;
    }

    public String getString(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
        CellHandle cell = sheet.getCell(row, column);
        return cell.getStringVal().trim();
    }

    /**
     * Fill in a lifter record from a row in the spreadsheet.
     * 
     * @param lifterNumber
     *            index of the lifter, starting at 0
     * @throws CellNotFoundException
     */
    Lifter readLifter(WorkSheetHandle sheet, int lifterNumber) {
        int row = lifterNumber + START_ROW;
        Lifter lifter = new Lifter();
        // read in values; getInt returns null if the cell is empty as opposed
        // to a number or -

        try {
            lifter.setMembership(getString(sheet, row, 0));
            lifter.setLotNumber(getInt(sheet, row, 1));
            final String lastName = getString(sheet, row, 2);
            final String firstName = getString(sheet, row, 3);
            if (lastName.isEmpty() && firstName.isEmpty()) {
                return null; // no data on this row.
            }
            lifter.setLastName(lastName);
            lifter.setFirstName(firstName);
            lifter.setGender(getGender(sheet, row, GENDER_COLUMN));
            lifter.setRegistrationCategory(getCategory(sheet, row, 5));
            lifter.setBodyWeight(getDouble(sheet, row, BODY_WEIGHT_COLUMN));
            lifter.setClub(getString(sheet, row, 7));
            lifter.setBirthDate(getInt(sheet, row, 8));
            lifter.setSnatch1Declaration(getString(sheet, row, 9)); 
            lifter.setSnatch1ActualLift(getString(sheet, row, 10));
            lifter.setSnatch2ActualLift(getString(sheet, row, 11));
            lifter.setSnatch3ActualLift(getString(sheet, row, 12));
            lifter.setCleanJerk1Declaration(getString(sheet, row, 14)); 
            lifter.setCleanJerk1ActualLift(getString(sheet, row, 15));
            lifter.setCleanJerk2ActualLift(getString(sheet, row, 16));
            lifter.setCleanJerk3ActualLift(getString(sheet, row, 17));
            lifter.setCompetitionSession(getCompetitionSession(sheet, row, 22));
            try {
                lifter.setQualifyingTotal(getInt(sheet, row, 24));
            } catch (CellNotFoundException e) {
            }
            logger.debug(toString(lifter, false));
            return lifter;
        } catch (CellNotFoundException c) {
            logger.debug(c.toString());
            return null;
        }
    }

    Category getCategory(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
        // first try category as written
        String catString = getString(sheet, row, column);
        Category lookup = categoryLookupByName.lookup(catString);
        if (lookup != null) return lookup;

        // else try category made up from sex and category.
        String genderString = getString(sheet, row, GENDER_COLUMN);
        lookup = categoryLookupByName.lookup(genderString + catString);
        if (lookup != null) return lookup;

        // else try bodyWeight and sex
        final String gender = getGender(sheet, row, GENDER_COLUMN);
        final Double bodyweight = getDouble(sheet, row, BODY_WEIGHT_COLUMN);
        lookup = categoryLookup.lookup(gender, bodyweight);
        if (lookup != null) return lookup;

        return null;
    }

    /**
     * @param sheet
     * @param row
     * @param column
     * @return
     * @throws CellNotFoundException
     */
    public String getGender(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
        final String genderString = getString(sheet, row, column);
        if (genderString != null && genderString.trim().length() > 0) {
            return Gender.valueOf(genderString.toUpperCase()).toString();
        } else {
            return null;
        }
    }

    public CompetitionSession getCompetitionSession(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
        // try group as written
        String catString = getString(sheet, row, column);
        CompetitionSession lookup = competitionSessionLookup.lookup(catString);
        if (lookup != null) return lookup;
        return null;
    }

}
