/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.spreadsheet;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import com.extentech.ExtenXLS.CellRange;
import com.extentech.ExtenXLS.WorkBookHandle;
import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellTypeMismatchException;
import com.extentech.formats.XLS.RowNotFoundException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;

/**
 * 
 * @author jflamy
 * 
 */
public abstract class OutputSheet {

    private Logger logger = LoggerFactory.getLogger(OutputSheet.class);

    protected CompetitionSession competitionSession;
    protected CategoryLookup categoryLookup;
    protected CompetitionApplication app;

    public OutputSheet() {
    }

    public OutputSheet(CategoryLookup categoryLookup, CompetitionApplication app, CompetitionSession competitionSession) {
        init(categoryLookup, app, competitionSession);
    }

    /**
     * @param categoryLookup1
     * @param app1
     * @param competitionSession1
     */
    public void init(CategoryLookup categoryLookup1, CompetitionApplication app1, CompetitionSession competitionSession1) {
        this.categoryLookup = categoryLookup1;
        this.app = app1;
        this.competitionSession = competitionSession1;
        if (competitionSession1 != null){
            logger.debug("resultSheet session = {} {}",System.identityHashCode(competitionSession1), competitionSession1.getReferee3());
        }
    }

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
            // get the data sheet
            workBookHandle = new WorkBookHandle(getTemplate());
            setupWorkbook(workBookHandle);
            final WorkSheetHandle workSheet = workBookHandle.getWorkSheet(0);

            // fill-in the header.
            writeHeader(workSheet);

            // process data sheet
            int i = 0;
            for (Lifter curLifter : lifters) {
                writeLifter(curLifter, workSheet, categoryLookup, i++);
            }
            removeLastRowIfInserting(workSheet, i + InputSheetHelper.START_ROW);
            cleanUpLifters(workSheet,i);

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

    protected void cleanUpLifters(WorkSheetHandle workSheet, int i) throws CellTypeMismatchException, CellNotFoundException {
	}

	protected void setupWorkbook(WorkBookHandle workBookHandle) {
	}

	/**
     * Override this method to do nothing if you are not inserting rows.
     * 
     * @param workSheet
     * @param i
     * @throws RowNotFoundException
     */
    protected void removeLastRowIfInserting(final WorkSheetHandle workSheet, int rownum) throws RowNotFoundException {
        workSheet.removeRow(rownum);
    }

    /**
     * Write top and bottom part of spreadsheet.
     * @param workSheet
     * @throws CellTypeMismatchException
     * @throws CellNotFoundException
     */
    @SuppressWarnings("unchecked")
    protected void writeHeader(WorkSheetHandle workSheet) throws CellTypeMismatchException, CellNotFoundException {
        List<Competition> competitions = CompetitionApplication.getCurrent().getHbnSession().createCriteria(
            Competition.class).list();
        if (competitions.size() > 0) {
            final Competition competition = competitions.get(0);
            workSheet.getCell("A1").setVal(competition.getFederation()); //$NON-NLS-1$
            workSheet.getCell("A2").setVal(competition.getFederationAddress()); //$NON-NLS-1$
            workSheet.getCell("A3").setVal(competition.getFederationWebSite()); //$NON-NLS-1$
            workSheet.getCell("B4").setVal(competition.getFederationEMail()); //$NON-NLS-1$

            if (competitionSession != null) {
                setCellValue(workSheet, "L4", competitionSession.getName());
            }

            workSheet.getCell("L1").setVal(competition.getCompetitionName()); //$NON-NLS-1$
            workSheet.getCell("L2").setVal(competition.getCompetitionSite()); //$NON-NLS-1$

            final Date competitionDate = competition.getCompetitionDate();
            if (competitionDate != null) workSheet.getCell("L3").setVal(SheetUtils.getShortDate(competitionDate)); //$NON-NLS-1$

            workSheet.getCell("T2").setVal(competition.getCompetitionCity()); //$NON-NLS-1$
            workSheet.getCell("T3").setVal(competition.getCompetitionOrganizer()); //$NON-NLS-1$
            final Integer invitedIfBornBefore = competition.getInvitedIfBornBefore();
            if (invitedIfBornBefore != null && invitedIfBornBefore > 0)
                workSheet.getCell("T4").setVal(invitedIfBornBefore); //$NON-NLS-1$

            writeGroup(workSheet);
        }
    }

    /**
     * Try to set the cell value; if it fails, try to create the cell.
     * 
     * @param workSheet
     * @param cellAddress
     * @param value
     */
    protected void setCellValue(WorkSheetHandle workSheet, String cellAddress, Object value) {
        try {
            workSheet.getCell(cellAddress).setVal(value); //$NON-NLS-1$
        } catch (Exception e) {
            workSheet.add(value, cellAddress);
        }
    }

    /**
     * @param workSheet
     * @throws CellTypeMismatchException
     * @throws CellNotFoundException
     */
    protected void writeGroup(WorkSheetHandle workSheet) throws CellTypeMismatchException, CellNotFoundException {
        if (competitionSession != null) {
            setCellValue(workSheet, "L4", competitionSession.getName());
        } else {
            setCellValue(workSheet, "K4", "");// clear the cell;
            setCellValue(workSheet, "L4", "");// clear the cell;
        }
    }

    abstract void writeLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup1, int rownum)
            throws CellTypeMismatchException, CellNotFoundException;

    /**
     * @param workBookHandle
     * @param out
     * @throws IOException
     */
    void writeWorkBook(WorkBookHandle workBookHandle, OutputStream out) throws IOException {
        BufferedOutputStream bout = new BufferedOutputStream(out);
        workBookHandle.writeBytes(bout);
        bout.flush();
        bout.close();
    }

    public void setCategoryLookup(CategoryLookup categoryLookup) {
        this.categoryLookup = categoryLookup;
    }

    public CategoryLookup getCategoryLookup() {
        return categoryLookup;
    }

    abstract public InputStream getTemplate() throws IOException;

    abstract protected List<Lifter> getLifters(boolean excludeNotWeighed);

    /**
     * @param workSheet
     * @param firstRow
     * @param lastCol
     * @param lastRow
     * @param firstCol
     */
    protected void setPrintArea(WorkSheetHandle workSheet, int firstRow, int firstCol, final int lastRow, int lastCol) {
        CellRange cellRange = null;
        int[] rangeCoords = null;
        try {
            logger.trace("setPrintArea for {}", workSheet.getSheetName());

            rangeCoords = new int[] { firstRow, firstCol, lastRow, lastCol };
            logger.trace("sheet {} : print area coords: {}, range: {}", new Object[] { workSheet.getSheetName(),
                    rangeCoords, cellRange });

            cellRange = new CellRange(workSheet, rangeCoords);
            workSheet.setPrintArea(cellRange);
        } catch (Exception e) {
            LoggerUtils.logException(logger, e);
            logger.error("sheet {} : print area coords: {}, range: {}", new Object[] { workSheet.getSheetName(),
                    rangeCoords, cellRange });
        }
    }

    /**
     * @param workSheet
     */
    protected void setFooterLeft(WorkSheetHandle workSheet) {
        final Competition competition = SheetUtils.getCompetition();
        final Date competitionDate = competition.getCompetitionDate();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String s = formatter.format(competitionDate);
        workSheet.setFooterText("&L" + competition.getCompetitionName() + " (" + s + ")" + "&R&P");
    }

}
