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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CategoryLookupByName;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.CompetitionSessionLookup;
import org.concordiainternational.competition.data.Gender;
import org.concordiainternational.competition.data.Lifter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.CellHandle;
import com.extentech.ExtenXLS.DateConverter;
import com.extentech.ExtenXLS.WorkBookHandle;
import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

public class ExtenXLSReader implements InputSheet, LifterReader {
    final private static Logger logger = LoggerFactory.getLogger(ExtenXLSReader.class);

    // constants
    final static int START_ROW = 7;
    static final int GENDER_COLUMN = 4;
    static final int BODY_WEIGHT_COLUMN = 6;

    private CategoryLookup categoryLookup;
    private CompetitionSessionLookup competitionSessionLookup;
    private CategoryLookupByName categoryLookupByName;

	private WorkBookHandle workBookHandle;

	private WorkSheetHandle workSheet;

    public ExtenXLSReader(HbnSessionManager hbnSessionManager) {
        categoryLookup = CategoryLookup.getSharedInstance(hbnSessionManager);
        categoryLookupByName = new CategoryLookupByName(hbnSessionManager);
        competitionSessionLookup = new CompetitionSessionLookup(hbnSessionManager);
    }

    /* (non-Javadoc)
     * @see org.concordiainternational.competition.spreadsheet.InputSheet#getAllLifters(java.io.InputStream, com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager)
     */
    @Override
	public synchronized List<Lifter> getAllLifters(InputStream is, Session session) throws IOException,
            CellNotFoundException, WorkSheetNotFoundException {

        LinkedList<Lifter> allLifters;
        try {
            getWorkSheet(is);

            // process data sheet
            allLifters = new LinkedList<Lifter>();
            for (int i = 0; true; i++) {
                final Lifter lifter = readLifter(i);
                if (lifter != null) {
                    allLifters.add(lifter);
                    System.err.println("added lifter " + ExtenXLSReader.toString(lifter));
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
        
        //System.err.println("allLifters "+allLifters.size());
        return allLifters;
    }

	/**
	 * @param is
	 * @throws WorkSheetNotFoundException
	 */
	protected void getWorkSheet(InputStream is)
			throws WorkSheetNotFoundException {
		// get the data sheet
		if (workSheet == null) {
		    workBookHandle = new WorkBookHandle(is);
		    workSheet = workBookHandle.getWorkSheet(0);
		}
	}
    
	@Override
    public void readHeader(InputStream is, Session session) 
	throws CellNotFoundException, WorkSheetNotFoundException, IOException {
		try {
			getWorkSheet(is);
            readHeader(session);
        } finally {
            // close workbook file and hide lock
            if (workBookHandle != null) workBookHandle.close();
            if (is != null) is.close();

        }
	}

    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.concordia_international.reader.ResultSheet#getGroup(java.lang.String)
     */
    @Override
	public List<Lifter> getGroupLifters(InputStream is, String aGroup, Session session) throws IOException,
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
                .append(" birthDate=" + lifter.getFullBirthDate()) //$NON-NLS-1$
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

    private Integer getInt(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
        CellHandle cell = sheet.getCell(row, column);
        Integer intVal = (cell != null ? cell.getIntVal() : null);
        return intVal;
    }

    private Double getDouble(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
        CellHandle cell = sheet.getCell(row, column);
        Double val = (cell != null ? cell.getDoubleVal() : null);
        return val;
    }

    private Date getDate(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
        CellHandle cell = sheet.getCell(row, column);
        Date val = null;
        if (cell != null) {
            Double doubleVal = getDouble(sheet, row, column);
            if (doubleVal > 9999) {
                val = DateConverter.getDateFromNumber(doubleVal);
            } else {
                val = DateConverter.getDateFromCell(cell);                
            }
            logger.info("date = {}",val);
        }
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
    @Override
    public Lifter readLifter(int lifterNumber) {
        int row = lifterNumber + START_ROW;
        Lifter lifter = new Lifter();

        // read in values; getInt returns null if the cell is empty as opposed
        // to a number or -

        try {
            lifter.setMembership(getString(workSheet, row, 0));
            lifter.setLotNumber(getInt(workSheet, row, 1));
            final String lastName = getString(workSheet, row, 2);
            final String firstName = getString(workSheet, row, 3);
            if (lastName.isEmpty() && firstName.isEmpty()) {
                return null; // no data on this row.
            }
            lifter.setLastName(lastName);
            lifter.setFirstName(firstName);
            lifter.setGender(getGender(workSheet, row, GENDER_COLUMN));
            lifter.setRegistrationCategory(getCategory(workSheet, row, 5));
            lifter.setBodyWeight(getDouble(workSheet, row, BODY_WEIGHT_COLUMN));
            lifter.setClub(getString(workSheet, row, 7));
            
            CellHandle birthDate = workSheet.getCell(row, 8);
            boolean date = birthDate.isDate();
            logger.info("{} {}",lastName, date);
            if (date){
                lifter.setFullBirthDate(getDate(workSheet, row, 8));
            } else {
                Integer int1 = getInt(workSheet, row, 8);
                if (int1 > 9999) {
                    lifter.setFullBirthDate(getDate(workSheet, row, 8));
                } else {
                    lifter.setYearOfBirth(int1);
                }
                
            };
            lifter.setSnatch1Declaration(getString(workSheet, row, 9)); 
            lifter.setSnatch1ActualLift(getString(workSheet, row, 10));
            lifter.setSnatch2ActualLift(getString(workSheet, row, 11));
            lifter.setSnatch3ActualLift(getString(workSheet, row, 12));
            lifter.setCleanJerk1Declaration(getString(workSheet, row, 14)); 
            lifter.setCleanJerk1ActualLift(getString(workSheet, row, 15));
            lifter.setCleanJerk2ActualLift(getString(workSheet, row, 16));
            lifter.setCleanJerk3ActualLift(getString(workSheet, row, 17));
            lifter.setCompetitionSession(getCompetitionSession(workSheet, row, 22));
            try {
                lifter.setQualifyingTotal(getInt(workSheet, row, 23));
            } catch (CellNotFoundException e) {
            }
            try {
                logger.debug("setQualifyingTotal");
                lifter.setQualifyingTotal(getInt(workSheet, row, 23));
            } catch (CellNotFoundException e) {
                logger.error(e.getLocalizedMessage());
            }           
            logger.debug(toString(lifter, false));
            return lifter;
        } catch (CellNotFoundException c) {
            logger.error(c.toString());
            return null;
        }
    }
    
    @SuppressWarnings( { "unchecked" })
    public void readHeader(Session hbnSession) throws CellNotFoundException {
        List<Competition> competitions = hbnSession.createCriteria(Competition.class).list();
        if (competitions.size() > 0) {
            final Competition competition = competitions.get(0);
            competition.setFederation(workSheet.getCell("A1").getStringVal()); //$NON-NLS-1$
            competition.setFederationAddress(workSheet.getCell("A2").getStringVal()); //$NON-NLS-1$
            competition.setFederationWebSite(workSheet.getCell("A3").getStringVal()); //$NON-NLS-1$
            competition.setFederationEMail(workSheet.getCell("B4").getStringVal()); //$NON-NLS-1$

            competition.setCompetitionName(workSheet.getCell("I1").getStringVal()); //$NON-NLS-1$
            competition.setCompetitionSite(workSheet.getCell("I2").getStringVal()); //$NON-NLS-1$
            
            final CellHandle dateCell = workSheet.getCell("X1");
            Date nDate = DateConverter.getDateFromCell(dateCell) ;
            competition.setCompetitionDate(nDate);

            competition.setCompetitionCity(workSheet.getCell("X2").getStringVal()); //$NON-NLS-1$
            competition.setCompetitionOrganizer(workSheet.getCell("I3").getStringVal()); //$NON-NLS-1$
            competition.setInvitedIfBornBefore(workSheet.getCell("I4").getIntVal()); //$NON-NLS-1$
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

    @Override
    public void init(ExtenXLSReader ish) {
        // do nothing.
    }

    @Override
    public List<Lifter> getLifters(boolean excludeNotWeighed) {
        // TODO Auto-generated method stub
        return null;
    }


}
