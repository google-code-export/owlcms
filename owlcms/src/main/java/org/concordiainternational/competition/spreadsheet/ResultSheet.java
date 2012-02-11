/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
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
import com.extentech.formats.XLS.WorkSheetNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * 
 * @author jflamy
 * 
 */
public class ResultSheet extends OutputSheet implements InputSheet, LifterReader {

    protected static final String TEMPLATE_XLS = "/ResultSheetTemplate.xls"; //$NON-NLS-1$
    Logger logger = LoggerFactory.getLogger(ResultSheet.class);
    private InputSheetHelper lifterReaderHelper;

    /**
     * Create a sheet.
     * If this constructor is used, or newInstance is called, then 
     * {@link #init(CategoryLookup, CompetitionApplication, CompetitionSession)} must also be called.
     */
    public ResultSheet() {
    }
    
    public ResultSheet(HbnSessionManager hbnSessionManager) {
    	createInputSheetHelper(hbnSessionManager);
    }

    public ResultSheet(CategoryLookup categoryLookup, CompetitionApplication app, CompetitionSession competitionSession) {
        super(categoryLookup, app, competitionSession);
        createInputSheetHelper(app);
    }
      

    @Override
	public void init(CategoryLookup categoryLookup1, CompetitionApplication app1,
			CompetitionSession competitionSession1) {
		super.init(categoryLookup1, app1, competitionSession1);
		createInputSheetHelper(app1);
	}

	@Override
    public void writeLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup1, int rownum)
            throws CellTypeMismatchException, CellNotFoundException {
        rownum = rownum + InputSheetHelper.START_ROW;
        workSheet.insertRow(rownum, true); // insérer une nouvelle ligne.

        workSheet.getCell(rownum, 0).setVal(lifter.getMembership());
        workSheet.getCell(rownum, 1).setVal(lifter.getLotNumber());
        workSheet.getCell(rownum, 2).setVal(lifter.getLastName());
        workSheet.getCell(rownum, 3).setVal(lifter.getFirstName());
        final String gender = lifter.getGender();
        //logger.debug("lifter {} gender <{}>",lifter,gender);
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

        final CompetitionSession group = lifter.getCompetitionSession();
        workSheet.getCell(rownum,22).setVal((group != null ? group.getName(): null));
    }


    /**
     * Fill in a lifter record from a row in the spreadsheet.
     * 
     * @param lifterNumber
     *            index of the lifter, starting at 0
     * @throws CellNotFoundException
     */
    @Override
	public Lifter readLifter(WorkSheetHandle sheet, int lifterNumber) {
        int row = lifterNumber + InputSheetHelper.START_ROW;
        Lifter lifter = new Lifter();
        // read in values; getInt returns null if the cell is empty as opposed
        // to a number or -

        try {
            lifter.setMembership(lifterReaderHelper.getString(sheet, row, 0));
            lifter.setLotNumber(lifterReaderHelper.getInt(sheet, row, 1));
            final String lastName = lifterReaderHelper.getString(sheet, row, 2);
            final String firstName = lifterReaderHelper.getString(sheet, row, 3);
            if (lastName.isEmpty() && firstName.isEmpty()) {
                return null; // no data on this row.
            }
            lifter.setLastName(lastName);
            lifter.setFirstName(firstName);
            lifter.setGender(lifterReaderHelper.getGender(sheet, row, InputSheetHelper.GENDER_COLUMN));
            lifter.setRegistrationCategory(lifterReaderHelper.getCategory(sheet, row, 5));
            lifter.setBodyWeight(lifterReaderHelper.getDouble(sheet, row, InputSheetHelper.BODY_WEIGHT_COLUMN));
            lifter.setClub(lifterReaderHelper.getString(sheet, row, 7));
            lifter.setBirthDate(lifterReaderHelper.getInt(sheet, row, 8));

            lifter.setSnatch1ActualLift(lifterReaderHelper.getString(sheet, row, 9));
            lifter.setSnatch2ActualLift(lifterReaderHelper.getString(sheet, row, 10));
            lifter.setSnatch3ActualLift(lifterReaderHelper.getString(sheet, row, 11));

            lifter.setCleanJerk1ActualLift(lifterReaderHelper.getString(sheet, row, 13));
            lifter.setCleanJerk2ActualLift(lifterReaderHelper.getString(sheet, row, 14));
            lifter.setCleanJerk3ActualLift(lifterReaderHelper.getString(sheet, row, 15	));
            try {
            	lifter.setCompetitionSession(lifterReaderHelper.getCompetitionSession(sheet, row, 22));
            } catch (CellNotFoundException e) {
            }
            try {
            	lifter.setQualifyingTotal(lifterReaderHelper.getInt(sheet, row, 24));
            } catch (CellNotFoundException e) {
            }
            logger.debug(InputSheetHelper.toString(lifter, false));
            return lifter;
        } catch (CellNotFoundException c) {
            logger.debug(c.toString());
            return null;
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
        final List<Lifter> resultsOrderCopy = LifterSorter.resultsOrderCopy(new LifterContainer(app).getAllPojos(),
            Ranking.TOTAL);
        LifterSorter.assignMedals(resultsOrderCopy);
        return resultsOrderCopy;
    }

	@Override
	protected void writeHeader(WorkSheetHandle workSheet)
			throws CellTypeMismatchException, CellNotFoundException {
		
		super.writeHeader(workSheet);
		
		if (competitionSession != null) {
			//logger.debug("writeHeader {} {}",System.identityHashCode(competitionSession),competitionSession.getReferee3());
			String announcer = competitionSession.getAnnouncer();
			workSheet.getCell("C11").setVal(announcer != null ? announcer : ""); //$NON-NLS-1$
			String marshall = competitionSession.getMarshall();
			workSheet.getCell("G11").setVal(marshall != null ? marshall : ""); //$NON-NLS-1$
			String timeKeeper = competitionSession.getTimeKeeper();
			workSheet.getCell("M11").setVal(timeKeeper != null ? timeKeeper : ""); //$NON-NLS-1$
			String technicalController = competitionSession.getTechnicalController();
			workSheet.getCell("S11").setVal(technicalController != null ? technicalController : ""); //$NON-NLS-1$
			String referee1 = competitionSession.getReferee1();
			workSheet.getCell("C14").setVal(referee1 != null ? referee1 : ""); //$NON-NLS-1$
			String referee2 = competitionSession.getReferee2();
			workSheet.getCell("G14").setVal(referee2 != null ? referee2 : ""); //$NON-NLS-1$
			String referee3 = competitionSession.getReferee3();
			workSheet.getCell("M14").setVal(referee3 != null ? referee3 : ""); //$NON-NLS-1$
			writeGroup(workSheet);
		}
	}

	@Override
	public List<Lifter> getAllLifters(InputStream is, HbnSessionManager session)
			throws CellNotFoundException, IOException,
			WorkSheetNotFoundException, InterruptedException, Throwable {
		return lifterReaderHelper.getAllLifters(is, session);
	}

	@Override
	public List<Lifter> getGroupLifters(InputStream is, String aGroup,
			HbnSessionManager session) throws CellNotFoundException,
			IOException, WorkSheetNotFoundException, InterruptedException,
			Throwable {
		return lifterReaderHelper.getGroupLifters(is, aGroup, session);
	}

	@Override
	public void createInputSheetHelper(HbnSessionManager hbnSessionManager) {
		lifterReaderHelper = new InputSheetHelper(hbnSessionManager,this);
	}
    
    
}
