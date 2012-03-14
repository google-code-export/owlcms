/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.spreadsheet;

import java.util.List;
import java.util.TreeSet;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter.Ranking;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;

import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellTypeMismatchException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * @author jflamy
 * 
 */
public class TeamSheet extends ResultSheet {

    public TeamSheet(HbnSessionManager hbnSessionManager) {
		super(hbnSessionManager);
	}

	@SuppressWarnings("unused")
	private int lifterRankWithinTeam;

    private String previousClub;

    private String previousGender;

    private static final int CLUB_OFFSET = 6; // first row for clubs (starting
                                              // at 0)

    private static final int LIFTER_OFFSET = 6; // first row for lifters
                                                // (starting at 0)

    /**
     * @param curLifter
     * @param workSheet
     * @param categoryLookup1
     * @param rankingType
     * @param i
     * @throws CellNotFoundException
     * @throws CellTypeMismatchException
     */
    private void writeTeamLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup1,
            int lifterIndex, Ranking rankingType) throws CellTypeMismatchException, CellNotFoundException {
        lifterIndex = lifterIndex + LIFTER_OFFSET;

        final String club = lifter.getClub();
        final String gender = lifter.getGender();

        if (!club.equals(previousClub) || !gender.equals(previousGender)) {
            lifterRankWithinTeam = 1;
        }

        workSheet.add(club, lifterIndex, 0).setVal(club);
        workSheet.add(lifter.getLastName(), lifterIndex, 1);
        workSheet.add(lifter.getFirstName(), lifterIndex, 2);
        workSheet.add(gender, lifterIndex, 3);
        workSheet.add(lifter.getBodyWeight(), lifterIndex, 4);

        logger.debug("lifter {}  lifterIndex {}", lifter.getLastName(),lifterIndex);
        
        workSheet.add(club + "_" + gender, lifterIndex, 5);
        switch (rankingType) {
        case SNATCH: {
            workSheet.add(lifter.getBestSnatch(), lifterIndex, 6);
            break;
        }
        case CLEANJERK: {
            workSheet.add(lifter.getBestCleanJerk(), lifterIndex, 6);
            break;
        }
        case TOTAL: {
            workSheet.add(lifter.getTotal(), lifterIndex, 6);
            break;
        }
        case CUSTOM: {
        	Double customScore = lifter.getCustomScore();
			if (customScore != null && customScore > 0.01D) {
				workSheet.add(customScore, lifterIndex, 6);
        	} else {
                workSheet.add(lifter.getTotal(), lifterIndex, 6);
        	}
            break;
        }
        }

        if (lifter.isInvited()) {
            final String message = Messages.getString(
                "ResultSheet.InvitedAbbreviation", CompetitionApplication.getCurrentLocale());
			workSheet.add(message, lifterIndex, 6); //$NON-NLS-1$
            //workSheet.add((Integer)0, lifterIndex, 9);
        } else {
            final Integer rank = LifterSorter.getRank(lifter, rankingType);
            if (rank <= 0) {
                workSheet.add(rank, lifterIndex, 7);
                // rank within team is computed by the Excel spreadsheet through
                // a formula.
                // this allows on-site changes to ranks.
                // workSheet.add(0,lifterIndex,9);
            } else {
                workSheet.add(rank, lifterIndex, 7);
                // rank within team is computed by the Excel spreadsheet through
                // a formula.
                // this allows on-site changes to ranks.
                // workSheet.add(lifterRankWithinTeam,lifterIndex,9);
                lifterRankWithinTeam++;
            }
        }

        previousClub = club;
        previousGender = gender;

    }


    /**
     * @param lifters
     * @param workSheet
     * @param rankingType
     * @param clubs
     * @param gender
     * @throws Exception
     */
    void writeTeamSheet(List<Lifter> lifters, WorkSheetHandle workSheet, Ranking rankingType, TreeSet<String> clubs,
            String gender) throws Exception {
        int i = 0;
        previousGender = null;
        previousClub = null;
        lifterRankWithinTeam = 0;

        setFooterLeft(workSheet);

        for (Lifter curLifter : lifters) {
            if (gender == null || gender.equals(curLifter.getGender())) {
                writeTeamLifter(curLifter, workSheet, categoryLookup, i++, rankingType);
            }
        }
        writeClubs(workSheet, clubs);
    }

    /**
     * @param clubs
     * @throws Exception
     */
    private void writeClubs(WorkSheetHandle workSheet, TreeSet<String> clubs) throws Exception {
        int rowNum = CLUB_OFFSET;

        int lastCol = 16;
        try {
            // check whether the template has two columns for the number of
            // athletes
            workSheet.getCell(rowNum - 1, lastCol);
        } catch (CellNotFoundException cnf) {
            lastCol = 15;
        }

        for (String club : clubs) {
            workSheet.add(club, rowNum, 12);
            rowNum++;
        }

        setPrintArea(workSheet, CLUB_OFFSET - 1, 12, rowNum - 1, lastCol);

    }


	public void writeTeamSheetAll(List<Lifter> lifters, WorkSheetHandle workSheet, Ranking rankingType, TreeSet<String> clubs,
            String gender) throws Exception {

        setFooterLeft(workSheet);

//        logger.debug("writing snatch");
        previousGender = null;
        previousClub = null;
        lifterRankWithinTeam = 0;
        int i = 0;
        for (Lifter curLifter : lifters) {
            if (gender == null || gender.equals(curLifter.getGender())) {
                writeTeamLifter(curLifter, workSheet, categoryLookup, i++, Ranking.SNATCH);
            }
        }
        
//        logger.debug("writing clean-and-jerk");
        previousGender = null;
        previousClub = null;
        lifterRankWithinTeam = 0;
        for (Lifter curLifter : lifters) {
            if (gender == null || gender.equals(curLifter.getGender())) {
                writeTeamLifter(curLifter, workSheet, categoryLookup, i++, Ranking.CLEANJERK);
            }
        }
        
//        logger.debug("writing total");
        previousGender = null;
        previousClub = null;
        lifterRankWithinTeam = 0;
        for (Lifter curLifter : lifters) {
            if (gender == null || gender.equals(curLifter.getGender())) {
                writeTeamLifter(curLifter, workSheet, categoryLookup, i++, Ranking.TOTAL);
            }
        }

        writeClubs(workSheet, clubs);
	}

}
