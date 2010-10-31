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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.CellHandle;
import com.extentech.ExtenXLS.CellRange;
import com.extentech.ExtenXLS.FormatHandle;
import com.extentech.ExtenXLS.RowHandle;
import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellPositionConflictException;
import com.extentech.formats.XLS.CellTypeMismatchException;
import com.extentech.formats.XLS.ColumnNotFoundException;
import com.extentech.formats.XLS.RowNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * @author jflamy
 * 
 */
public class StartSheet extends ResultSheet {

    public StartSheet(HbnSessionManager hbnSessionManager) {
		super(hbnSessionManager);
	}

	/**
	 * 
	 */
    private static final int CATEGORY_BACKGROUND = FormatHandle.COLOR_GRAY50;

    private static final int INDIVIDUAL_COLS = 10;

    private CompetitionSession prevGroup;

    private int rownum;
    private int groupIx;

    private FormatHandle spacerFormat;
    // private FormatHandle groupFormatCenter;
    private FormatHandle groupFormatLeft;
    private FormatHandle groupFormatRight;

    private static Logger logger = LoggerFactory.getLogger(StartSheet.class);

    private void writeLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup1)
            throws CellTypeMismatchException, CellNotFoundException, RowNotFoundException {

        CompetitionSession competitionSession1 = lifter.getCompetitionSession();
        if (!competitionSession1.equals(prevGroup)) {
            createGroupHeading(workSheet, INDIVIDUAL_COLS - 1, competitionSession1);
        }
        prevGroup = competitionSession1;

        workSheet.insertRow(rownum, true); // insérer une nouvelle ligne.

        workSheet.getCell(rownum, 0).setVal(groupIx);
        workSheet.getCell(rownum, 1).setVal(lifter.getLotNumber());
        workSheet.getCell(rownum, 2).setVal(lifter.getLastName());
        workSheet.getCell(rownum, 3).setVal(lifter.getFirstName());
        final String gender = lifter.getGender();
        workSheet.getCell(rownum, 4).setVal((gender != null ? gender.toString() : null));
        workSheet.getCell(rownum, 5).setVal(lifter.getClub());
        workSheet.getCell(rownum, 6).setVal(lifter.getBirthDate());

        final Integer ageGroup = lifter.getAgeGroup();
        final String endOfAgeGroup = endOfGroup(ageGroup, lifter.getGender());
        workSheet.getCell(rownum, 7).setVal(ageGroup + endOfAgeGroup);
        workSheet.getCell(rownum, 8).setVal(lifter.getRegistrationCategory().getName());
        workSheet.getCell(rownum, 9).setVal(SheetUtils.fixValue(lifter.getQualifyingTotal()));

        rownum++;
        groupIx++;
    }

    /**
     * Handle men over 80 and women over 65.
     * 
     * @param ageGroup
     * @param gender
     * @return
     */
    private String endOfGroup(final Integer ageGroup, String gender) {
    	if (ageGroup == null) return "";
        if ("m".equalsIgnoreCase(gender)) {
            if (ageGroup == 80) {
                return "+";
            } else {
                return "-" + (ageGroup + 4);
            }

        } else {
            if (ageGroup == 65) {
                return "+";
            } else {
                return "-" + (ageGroup + 4);
            }
        }

    }

    /**
     * @param lifters
     * @param workSheet
     * @throws CellTypeMismatchException
     * @throws CellNotFoundException
     * @throws RowNotFoundException
     */
    void writeStartSheet(List<Lifter> lifters, WorkSheetHandle workSheet) throws CellTypeMismatchException,
            CellNotFoundException, RowNotFoundException {

        spacerFormat = defineSpacerFormat(workSheet);
        // groupFormatCenter = defineGroupFormatCenter(workSheet);
        groupFormatLeft = defineGroupFormatLeft(workSheet);
        groupFormatRight = defineGroupFormatRight(workSheet);

        // fill-in the header.
        writeHeader(workSheet);
        setFooterLeft(workSheet);

        // table heading
        // setRepeatedLinesFormat(workSheet);

        // process data sheet
        rownum = InputSheetHelper.START_ROW;
        int i = 0;
        for (Lifter curLifter : lifters) {
            writeLifter(curLifter, workSheet, categoryLookup);
            i++;
        }
        removeLastRowIfInserting(workSheet, rownum);

        setPrintArea(workSheet, 0, // InputSheetHelper.START_ROW-2,
            0, rownum - 1, INDIVIDUAL_COLS - 1);

        if (!Competition.isMasters()) {
            // hide the age group information
            try {
                CellHandle sourceCell;
                workSheet.getCell("G3").remove(true);
                sourceCell = workSheet.getCell("H3");
                sourceCell.moveTo("G3");

                workSheet.getCell("G4").remove(true);
                sourceCell = workSheet.getCell("H4");
                sourceCell.moveTo("G4");

                workSheet.getCol("H").setHidden(true);
            } catch (ColumnNotFoundException e) {
                LoggerUtils.logException(logger, e);
            } catch (CellPositionConflictException e) {
                LoggerUtils.logException(logger, e);
            }
        }
    }

    /**
     * @param workSheet
     */
    @SuppressWarnings("unused")
    private void setRepeatedLinesFormat(WorkSheetHandle workSheet) {
        int[] rangeCoords = new int[] { 5, 0, 6, INDIVIDUAL_COLS - 1 };
        try {
            CellRange cellRange = new CellRange(workSheet, rangeCoords);
            FormatHandle fh = new FormatHandle(workSheet.getWorkBook());
            fh.setBackgroundColor(FormatHandle.COLOR_GRAY25);
            fh.addCellRange(cellRange);
        } catch (Exception e) {
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void writeHeader(WorkSheetHandle workSheet) throws CellTypeMismatchException, CellNotFoundException {
        List<Competition> competitions = CompetitionApplication.getCurrent().getHbnSession().createCriteria(
            Competition.class).list();
        if (competitions.size() > 0) {
            final Competition competition = competitions.get(0);

            workSheet.getCell("D2").setVal(competition.getCompetitionName()); //$NON-NLS-1$
            workSheet.getCell("D3").setVal(competition.getCompetitionSite()); //$NON-NLS-1$

            final Date competitionDate = competition.getCompetitionDate();
            if (competitionDate != null) workSheet.getCell("D4").setVal(SheetUtils.getShortDate(competitionDate)); //$NON-NLS-1$

            workSheet.getCell("I3").setVal(competition.getCompetitionCity()); //$NON-NLS-1$
            workSheet.getCell("I4").setVal(competition.getCompetitionOrganizer()); //$NON-NLS-1$
        }
    }

    /**
     * @param workSheet
     * @throws CellNotFoundException
     * @throws RowNotFoundException
     */
    private int createGroupHeading(WorkSheetHandle workSheet, int nbCols, CompetitionSession competitionSession1) throws CellNotFoundException,
            RowNotFoundException {

        groupIx = 1;

        workSheet.insertRow(rownum, true);
        RowHandle row = workSheet.getRow(rownum);
        row.setHeight(200);
        for (int i = 0; i <= nbCols; i++) {
            final CellHandle cell = workSheet.getCell(rownum, i);
            cell.setFormatHandle(spacerFormat);
        }
        rownum++;

        workSheet.insertRow(rownum, true);
        CellHandle cell = workSheet.getCell(rownum, 0);

        // create a group label
        String groupCode = competitionSession1.getName();
        cell.setVal(groupCode);

        int[] rangeCoords = new int[] { rownum, 0, rownum, 2 };
        try {
            CellRange cellRange = new CellRange(workSheet, rangeCoords);
            cellRange.mergeCells(true);
            groupFormatLeft.addCellRange(cellRange);
        } catch (Exception e) {
        }
        // cell = workSheet.getCell(rownum, 0);
        // cell.setFormatHandle(groupFormatLeft);
        // cell = workSheet.getCell(rownum, nbCols);
        // cell.setFormatHandle(groupFormatRight);

        
        cell = workSheet.getCell(rownum, 3);
        Date weighInTime = competitionSession1.getWeighInTime();
		Date competitionTime = competitionSession1.getCompetitionTime();
		
		String weighIn = weighInTime != null ? formatDate(weighInTime) : Messages.getString("StartSheet.TBA",CompetitionApplication.getCurrentLocale());
		String start = competitionTime != null ? formatDate(competitionTime) : Messages.getString("StartSheet.TBA",CompetitionApplication.getCurrentLocale());
		cell.setVal("Weigh-in/Pesée: " + weighIn + "   " + "Start/Début: "+ start);
		
        rangeCoords = new int[] { rownum, 3, rownum, nbCols };
        try {
            CellRange cellRange = new CellRange(workSheet, rangeCoords);
            cellRange.mergeCells(true);
            groupFormatRight.addCellRange(cellRange);
        } catch (Exception e) {
        }
        // cell = workSheet.getCell(rownum, 0);
        // cell.setFormatHandle(groupFormatLeft);
        // cell = workSheet.getCell(rownum, nbCols);
        // cell.setFormatHandle(groupFormatRight);

        rownum++;

        return rownum;
    }

    final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * @param weighInTime
     * @return
     */
    private String formatDate(Date weighInTime) {
        return simpleDateFormat.format(weighInTime);
    }

    /**
     * @param workSheet
     * @param i
     * @throws CellNotFoundException
     */
    private FormatHandle defineSpacerFormat(WorkSheetHandle workSheet) {
        FormatHandle fh = new FormatHandle(workSheet.getWorkBook());
        fh.setPattern(FormatHandle.PATTERN_FILLED);
        fh.setBorderTopColor(FormatHandle.COLOR_BLACK);
        fh.setBorderBottomColor(FormatHandle.COLOR_BLACK);
        fh.setBorderLeftColor(FormatHandle.COLOR_WHITE);
        fh.setBorderRightColor(FormatHandle.COLOR_WHITE);
        fh.setBackgroundColor(FormatHandle.COLOR_WHITE);
        fh.setFontColor(FormatHandle.COLOR_WHITE);
        return fh;
    }

    /**
     * @param workSheet
     * @param i
     * @throws CellNotFoundException
     */
    private FormatHandle defineGroupFormatRight(WorkSheetHandle workSheet) {
        FormatHandle fh = new FormatHandle(workSheet.getWorkBook());
        defineGroupBackground(fh);
        fh.setFontWeight(FormatHandle.DEFAULT_FONT_WEIGHT);
        fh.setFontColor(FormatHandle.COLOR_WHITE);

        fh.setLeftBorderLineStyle(FormatHandle.BORDER_NONE);
        fh.setHorizontalAlignment(FormatHandle.ALIGN_CENTER);
        fh.setVerticalAlignment(FormatHandle.ALIGN_VERTICAL_CENTER);
        return fh;
    }

    /**
     * @param workSheet
     * @param i
     * @throws CellNotFoundException
     */
    private FormatHandle defineGroupFormatLeft(WorkSheetHandle workSheet) {
        FormatHandle fh = new FormatHandle(workSheet.getWorkBook());
        defineGroupBackground(fh);
        fh.setFontWeight(FormatHandle.BOLD);
        fh.setFontColor(FormatHandle.COLOR_WHITE);
        fh.setBorderLeftColor(FormatHandle.COLOR_BLACK);
        fh.setRightBorderLineStyle(FormatHandle.BORDER_NONE);
        fh.setHorizontalAlignment(FormatHandle.ALIGN_CENTER);
        fh.setVerticalAlignment(FormatHandle.ALIGN_VERTICAL_CENTER);
        return fh;
    }

    /**
     * @param fh
     */
    private void defineGroupBackground(FormatHandle fh) {
        fh.setPattern(FormatHandle.PATTERN_FILLED);
        fh.setBorderLineStyle(FormatHandle.BORDER_THIN);
        fh.setBorderColor(FormatHandle.COLOR_BLACK);
        fh.setBackgroundColor(CATEGORY_BACKGROUND);
        fh.setFontColor(FormatHandle.COLOR_WHITE);
    }

}
