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
import java.util.Locale;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.lifterSort.WinningOrderComparator;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;

import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellTypeMismatchException;
import com.extentech.formats.XLS.RowNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * @author jflamy
 * 
 */
public class SinclairSheet extends ResultSheet {

    public SinclairSheet(HbnSessionManager hbnSessionManager) {
		super(hbnSessionManager);
	}

	private int rownum = 0;

    private void writeSinclairLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup1)
            throws CellTypeMismatchException, CellNotFoundException {
        workSheet.insertRow(rownum, true); // insérer une nouvelle ligne.

        int firstRow = 0;
        workSheet.getCell(rownum, firstRow).setVal(lifter.getMembership());
        workSheet.getCell(rownum, 1).setVal(lifter.getLotNumber());
        workSheet.getCell(rownum, 2).setVal(lifter.getLastName());
        workSheet.getCell(rownum, 3).setVal(lifter.getFirstName());
        final String gender = lifter.getGender();

        workSheet.getCell(rownum, 4).setVal((gender != null ? gender.toString() : null));
        if (WinningOrderComparator.useRegistrationCategory) {
            final Category category = lifter.getRegistrationCategory();
            workSheet.getCell(rownum, 5).setVal((category != null ? category.getName() : null));
        } else {
            final Category category = lifter.getCategory();
            workSheet.getCell(rownum, 5).setVal((category != null ? category.getName() : null));
        }

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

        workSheet.getCell(rownum, 18).setVal(lifter.getSinclair());
        workSheet.getCell(rownum, 19).setVal(getSinclairValue(lifter));
        final Integer sinclairRank = lifter.getSinclairRank();
        if (lifter.isInvited()) {
            final Locale currentLocale = CompetitionApplication.getCurrentLocale();
            workSheet.getCell(rownum, 20).setVal(Messages.getString("Lifter.InvitedAbbreviated", currentLocale));
        } else if (sinclairRank > firstRow) {
            workSheet.getCell(rownum, 20).setVal(sinclairRank);
        } else {
            workSheet.getCell(rownum, 20).setVal("-");
        }
        // final Group group = lifter.getGroup();
        // workSheet.getCell(rownum,22).setVal((group != null ? group.getName()
        // : null));

        rownum++;
    }

    /**
     * @param lifter
     * @return
     */
    private Double getSinclairValue(Lifter lifter) {
        if (Competition.isMasters()) {
            return lifter.getSMM();
        } else {
            return lifter.getCategorySinclair();
        }
    }

    /**
     * @param lifters
     * @param workSheet
     * @param gender
     * @throws CellTypeMismatchException
     * @throws CellNotFoundException
     * @throws RowNotFoundException
     */
    void writeSinclairSheet(List<Lifter> lifters, WorkSheetHandle workSheet, String gender)
            throws CellTypeMismatchException, CellNotFoundException, RowNotFoundException {
        // fill-in the header.
        writeHeader(workSheet);
        setFooterLeft(workSheet);

        // process data sheet
        rownum = InputSheetHelper.START_ROW;
        for (Lifter curLifter : lifters) {
            if (gender == null || gender.equals(curLifter.getGender())) {
                writeSinclairLifter(curLifter, workSheet, categoryLookup);
            }
        }
        removeLastRowIfInserting(workSheet, rownum);

        setPrintArea(workSheet, InputSheetHelper.START_ROW - 2, 0, rownum - 1, 20);
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
