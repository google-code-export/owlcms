/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.spreadsheet;

import java.util.Date;
import java.util.List;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.lifterSort.WinningOrderComparator;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.CellHandle;
import com.extentech.ExtenXLS.CellRange;
import com.extentech.ExtenXLS.FormatHandle;
import com.extentech.ExtenXLS.RowHandle;
import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellTypeMismatchException;
import com.extentech.formats.XLS.RowNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * @author jflamy
 * 
 */
public class MastersIndividualSheet extends IndividualSheet {


	public MastersIndividualSheet(HbnSessionManager hbnSessionManager) {
		super(hbnSessionManager);
	}

	private static final int AGEGROUP_BACKGROUND = FormatHandle.COLOR_BLACK;
    private static final int CATEGORY_BACKGROUND = FormatHandle.COLOR_GRAY50;

    private static final int INDIVIDUAL_COLS = 23;

    private Category prevCategory;
    private String prevAgeGroup;

    private int rownum;

    private FormatHandle spacerFormat;
    private FormatHandle categoryFormatCenter;
    private FormatHandle categoryFormatLeft;
    private FormatHandle categoryFormatRight;
    @SuppressWarnings("unused")
    private FormatHandle ageGroupFormat;

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(MastersIndividualSheet.class);

    private void writeIndividualLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup1)
            throws CellTypeMismatchException, CellNotFoundException, RowNotFoundException {

        Category category = null;
        String ageGroup = "";

        // create an age group boundary if needed
        ageGroup = lifter.getMastersAgeGroup();
        // logger.debug("lifter {}  agegroup {}",lifter,ageGroup);
        boolean newHeadingRequired = false;
        if (!ageGroup.equals(prevAgeGroup)) {
            createAgeGroupHeading(workSheet, INDIVIDUAL_COLS - 1, lifter.getMastersGenderAgeGroupInterval());
            // logger.debug("different");
            newHeadingRequired = true;
        }
        prevAgeGroup = ageGroup;

        // create a category header if needed
        if (WinningOrderComparator.useRegistrationCategory) {
            category = lifter.getRegistrationCategory();
        } else {
            category = lifter.getCategory();
        }
        if (!category.equals(prevCategory) || newHeadingRequired) {
            createCategoryHeading(workSheet, INDIVIDUAL_COLS - 1, category, lifter);
        }
        prevCategory = category;

        // add the lifter
        workSheet.insertRow(rownum, true); // insérer une nouvelle ligne.

        workSheet.getCell(rownum, 0).setVal(lifter.getMembership());
        workSheet.getCell(rownum, 1).setVal(lifter.getLotNumber());
        workSheet.getCell(rownum, 2).setVal(lifter.getLastName());
        workSheet.getCell(rownum, 3).setVal(lifter.getFirstName());
        final String gender = lifter.getGender();
        workSheet.getCell(rownum, 4).setVal((gender != null ? gender.toString() : null));
        workSheet.getCell(rownum, 5).setVal((category != null ? category.getName() : null));

        workSheet.getCell(rownum, 6).setVal(lifter.getBodyWeight());
        workSheet.getCell(rownum, 7).setVal(lifter.getClub());
        workSheet.getCell(rownum, 8).setVal(lifter.getBirthDate());

        workSheet.getCell(rownum, 9).setVal(SheetUtils.fixValue(lifter.getSnatch1ActualLift()));
        workSheet.getCell(rownum, 10).setVal(SheetUtils.fixValue(lifter.getSnatch2ActualLift()));
        workSheet.getCell(rownum, 11).setVal(SheetUtils.fixValue(lifter.getSnatch3ActualLift()));
        workSheet.getCell(rownum, 13).setVal(SheetUtils.fixRank(lifter.getSnatchRank()));

        workSheet.getCell(rownum, 14).setVal(SheetUtils.fixValue(lifter.getCleanJerk1ActualLift()));
        workSheet.getCell(rownum, 15).setVal(SheetUtils.fixValue(lifter.getCleanJerk2ActualLift()));
        workSheet.getCell(rownum, 16).setVal(SheetUtils.fixValue(lifter.getCleanJerk3ActualLift()));
        workSheet.getCell(rownum, 18).setVal(SheetUtils.fixRank(lifter.getCleanJerkRank()));

        workSheet.getCell(rownum, 19).setVal(SheetUtils.fixValue(lifter.getTotal()));
        workSheet.getCell(rownum, 20).setVal(SheetUtils.fixRank(lifter.getRank()));
        workSheet.getCell(rownum, 21).setVal(lifter.getSinclair());
        workSheet.getCell(rownum, INDIVIDUAL_COLS - 1).setVal(lifter.getSMM());

        rownum++;
    }

    /**
     * @param lifters
     * @param workSheet
     * @param gender
     * @throws CellTypeMismatchException
     * @throws CellNotFoundException
     * @throws RowNotFoundException
     */
    @Override
    void writeIndividualSheet(List<Lifter> lifters, WorkSheetHandle workSheet, String gender)
            throws CellTypeMismatchException, CellNotFoundException, RowNotFoundException {

        spacerFormat = defineSpacerFormat(workSheet);
        categoryFormatCenter = defineCategoryFormatCenter(workSheet);
        categoryFormatLeft = defineCategoryFormatLeft(workSheet);
        categoryFormatRight = defineCategoryFormatRight(workSheet);
        ageGroupFormat = defineAgeGroupFormat(workSheet);

        // fill-in the header.
        writeHeader(workSheet);
        setFooterLeft(workSheet);

        // table heading
        // setRepeatedLinesFormat(workSheet);

        // process data sheet
        rownum = InputSheetHelper.START_ROW;
        int i = 0;
        for (Lifter curLifter : lifters) {
            if (gender == null || gender.equals(curLifter.getGender())) {
                writeIndividualLifter(curLifter, workSheet, categoryLookup);
            }
            i++;
        }
        removeLastRowIfInserting(workSheet, rownum);

        setPrintArea(workSheet, InputSheetHelper.START_ROW - 2, 0, rownum - 1, INDIVIDUAL_COLS - 1);
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

    /**
     * @param workSheet
     * @throws CellNotFoundException
     * @throws RowNotFoundException
     */
    private int createAgeGroupHeading(WorkSheetHandle workSheet, int nbCols, String ageGroup)
            throws CellNotFoundException, RowNotFoundException {

        return rownum;

        /*
         * addSpacer(workSheet, nbCols);
         * 
         * workSheet.insertRow(rownum,true); CellHandle cell =
         * workSheet.getCell(rownum, 0);
         * 
         * cell.setVal(ageGroup); // // create a bilingual age group label. //
         * String ageGroupCode = "AgeGroup."+ageGroup; // // fix category code
         * to be a legal property name. // if (ageGroupCode.contains("+")) { //
         * ageGroupCode = ageGroupCode.replace("+", "plus"); // } // String name
         * = // Messages.getString(ageGroupCode,Locale.FRENCH) + " - " + //
         * Messages.getString(ageGroupCode,Locale.ENGLISH); //
         * cell.setVal(name);
         * 
         * int[] rangeCoords = new int[]{rownum,0,rownum,nbCols}; try {
         * CellRange cellRange = new CellRange(workSheet,rangeCoords);
         * cellRange.mergeCells(true); ageGroupFormat.addCellRange(cellRange); }
         * catch (Exception e) {}
         * 
         * 
         * rownum++; return rownum;
         */
    }

    /**
     * @param workSheet
     * @param nbCols
     * @throws RowNotFoundException
     */
    private void addSpacer(WorkSheetHandle workSheet, int nbCols) throws RowNotFoundException {
        try {
            workSheet.insertRow(rownum, true);
        } catch (Exception e1) {
        }
        RowHandle row = workSheet.getRow(rownum);
        row.setHeight(200);
        for (int i = 0; i <= nbCols; i++) {
            final CellHandle cell = workSheet.add("", rownum, i);
            cell.setFormatHandle(spacerFormat);
        }
        rownum++;
    }


    /**
     * @param workSheet
     * @param nbCols
     * @param category
     * @param lifter
     * @return
     * @throws CellNotFoundException
     * @throws RowNotFoundException
     */
    private int createCategoryHeading(WorkSheetHandle workSheet, int nbCols, Category category, Lifter lifter)
            throws CellNotFoundException, RowNotFoundException {

        addSpacer(workSheet, nbCols);

        workSheet.insertRow(rownum, true);
        CellHandle cell = workSheet.getCell(rownum, 0);

        // // create a bilingual category label.
        // String catCode = "CategoryLong."+category.getName();
        // // fix category code to be a legal property name.
        // if (catCode.contains(">")) {
        // catCode = catCode.replace(">", "gt");
        // }
        // String name =
        // Messages.getString(catCode,Locale.FRENCH) + " - " +
        // Messages.getString(catCode,Locale.ENGLISH);
        // cell.setVal(name);
        cell.setVal(lifter.getMastersGenderAgeGroupInterval() + "  " + lifter.getShortCategory());

        int[] rangeCoords = new int[] { rownum, 0, rownum, nbCols };
        try {
            CellRange cellRange = new CellRange(workSheet, rangeCoords);
            cellRange.mergeCells(true);
            categoryFormatCenter.addCellRange(cellRange);
        } catch (Exception e) {
        }
        cell = workSheet.getCell(rownum, 0);
        cell.setFormatHandle(categoryFormatLeft);
        cell = workSheet.getCell(rownum, nbCols);
        cell.setFormatHandle(categoryFormatRight);

        rownum++;
        return rownum;
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
    private FormatHandle defineAgeGroupFormat(WorkSheetHandle workSheet) {
        FormatHandle fh = new FormatHandle(workSheet.getWorkBook());
        fh.setPattern(FormatHandle.PATTERN_FILLED);
        fh.setBorderColor(FormatHandle.COLOR_BLACK);
        fh.setBackgroundColor(AGEGROUP_BACKGROUND);
        fh.setFontColor(FormatHandle.COLOR_WHITE);

        fh.setBorderLeftColor(FormatHandle.COLOR_BLACK);
        fh.setBorderRightColor(FormatHandle.COLOR_BLACK);
        fh.setBorderLineStyle(FormatHandle.BORDER_THIN);
        fh.setHorizontalAlignment(FormatHandle.ALIGN_CENTER);
        fh.setVerticalAlignment(FormatHandle.ALIGN_VERTICAL_CENTER);
        return fh;
    }

    /**
     * @param workSheet
     * @param i
     * @throws CellNotFoundException
     */
    private FormatHandle defineCategoryFormatCenter(WorkSheetHandle workSheet) {
        FormatHandle fh = new FormatHandle(workSheet.getWorkBook());
        defineCategoryBackground(fh);

        fh.setBorderLeftColor(FormatHandle.COLOR_WHITE);
        fh.setBorderRightColor(FormatHandle.COLOR_WHITE);
        fh.setHorizontalAlignment(FormatHandle.ALIGN_CENTER);
        fh.setVerticalAlignment(FormatHandle.ALIGN_VERTICAL_CENTER);
        return fh;
    }

    /**
     * @param workSheet
     * @param i
     * @throws CellNotFoundException
     */
    private FormatHandle defineCategoryFormatRight(WorkSheetHandle workSheet) {
        FormatHandle fh = new FormatHandle(workSheet.getWorkBook());
        defineCategoryBackground(fh);

        fh.setBorderLeftColor(CATEGORY_BACKGROUND);
        fh.setBorderRightColor(FormatHandle.COLOR_BLACK);
        fh.setHorizontalAlignment(FormatHandle.ALIGN_CENTER);
        fh.setVerticalAlignment(FormatHandle.ALIGN_VERTICAL_CENTER);
        return fh;
    }

    /**
     * @param workSheet
     * @param i
     * @throws CellNotFoundException
     */
    private FormatHandle defineCategoryFormatLeft(WorkSheetHandle workSheet) {
        FormatHandle fh = new FormatHandle(workSheet.getWorkBook());
        defineCategoryBackground(fh);
        fh.setFontWeight(FormatHandle.BOLD);
        fh.setFontColor(FormatHandle.COLOR_WHITE);
        fh.setBorderLeftColor(FormatHandle.COLOR_BLACK);
        fh.setBorderRightColor(CATEGORY_BACKGROUND);
        fh.setHorizontalAlignment(FormatHandle.ALIGN_CENTER);
        fh.setVerticalAlignment(FormatHandle.ALIGN_VERTICAL_CENTER);
        return fh;
    }

    /**
     * @param fh
     */
    private void defineCategoryBackground(FormatHandle fh) {
        fh.setPattern(FormatHandle.PATTERN_FILLED);
        fh.setBorderTopColor(FormatHandle.COLOR_BLACK);
        fh.setBorderBottomColor(FormatHandle.COLOR_BLACK);
        fh.setBackgroundColor(CATEGORY_BACKGROUND);
        fh.setFontColor(FormatHandle.COLOR_WHITE);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void writeHeader(WorkSheetHandle workSheet) throws CellTypeMismatchException, CellNotFoundException {
        List<Competition> competitions = CompetitionApplication.getCurrent().getHbnSession().createCriteria(
            Competition.class).list();
        if (competitions.size() > 0) {
            final Competition competition = competitions.get(0);

            setCellValue(workSheet, "K1", competition.getCompetitionName()); //$NON-NLS-1$
            setCellValue(workSheet, "K2", competition.getCompetitionSite()); //$NON-NLS-1$

            final Date competitionDate = competition.getCompetitionDate();
            if (competitionDate != null) setCellValue(workSheet, "K3", SheetUtils.getShortDate(competitionDate)); //$NON-NLS-1$

            setCellValue(workSheet, "T2", competition.getCompetitionCity()); //$NON-NLS-1$
            setCellValue(workSheet, "T3", competition.getCompetitionOrganizer()); //$NON-NLS-1$

            writeGroup(workSheet);
        }
    }

    /**
     * @param workSheet
     * @throws CellTypeMismatchException
     * @throws CellNotFoundException
     */
    @Override
    protected void writeGroup(WorkSheetHandle workSheet) throws CellTypeMismatchException, CellNotFoundException {
        if (competitionSession != null) {
            setCellValue(workSheet, "K4", competitionSession.getName());
        } else {
            setCellValue(workSheet, "J4", "");// clear the cell;
            setCellValue(workSheet, "K4", "");// clear the cell;
        }
    }
}
