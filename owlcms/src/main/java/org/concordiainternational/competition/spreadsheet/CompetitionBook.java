/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.spreadsheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.TreeSet;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter.Ranking;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.WorkBookHandle;
import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.CellTypeMismatchException;
import com.extentech.formats.XLS.RowNotFoundException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * Result sheet, with team rankings
 * 
 * @author jflamy
 * 
 */
public class CompetitionBook extends ResultSheet {
    
	protected static String templateXls = "/TeamResultSheetTemplate_Standard.xls"; //$NON-NLS-1$
    private Logger logger = LoggerFactory.getLogger(CompetitionBook.class);
    Competition competition;
	private HbnSessionManager hbnSessionManager;
	
	
    /**
     * Create a sheet.
     * If this constructor is used, or newInstance is called, then 
     * {@link #init(CategoryLookup, CompetitionApplication, CompetitionSession)} must also be called.
     * OutputSheetStreamSource does call init() correctly.
     */
	public CompetitionBook() {
	}
	
	public CompetitionBook(HbnSessionManager hbnSessionManager) {
		super(hbnSessionManager);
		this.hbnSessionManager = hbnSessionManager;
	}

    @Override
	public void init(CategoryLookup categoryLookup1, CompetitionApplication app1,
			CompetitionSession competitionSession1) {
		super.init(categoryLookup1, app1, competitionSession1);
		this.hbnSessionManager = app1;
		createInputSheetHelper(app1);
	}


    @Override
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
            // extract club lists
            TreeSet<String> clubs = new TreeSet<String>();
            for (Lifter curLifter : lifters) {
                clubs.add(curLifter.getClub());
            }

            // produce point rankings.
            final LifterSorter lifterSorter = new LifterSorter();
            List<Lifter> teamRankingLifters;
            List<Lifter> sinclairLifters;
            teamRankingLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.SNATCH);
            lifterSorter.assignCategoryRanksAndPoints(teamRankingLifters, Ranking.SNATCH);

            teamRankingLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.CLEANJERK);
            lifterSorter.assignCategoryRanksAndPoints(teamRankingLifters, Ranking.CLEANJERK);

            teamRankingLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.TOTAL);
            lifterSorter.assignCategoryRanksAndPoints(teamRankingLifters, Ranking.TOTAL);

            sinclairLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.SINCLAIR);
            teamRankingLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.SINCLAIR);
            lifterSorter.assignSinclairRanksAndPoints(teamRankingLifters, Ranking.SINCLAIR);

            teamRankingLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.COMBINED);
            lifterSorter.assignCategoryRanksAndPoints(teamRankingLifters, Ranking.COMBINED);

            teamRankingLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.CUSTOM);
            lifterSorter.assignCategoryRanksAndPoints(teamRankingLifters, Ranking.CUSTOM);
            
            // this final sort is necessary to put all the lifters from the same
            // team together.
            LifterSorter.teamRankingOrder(teamRankingLifters, Ranking.TOTAL);

            // get the data sheet
            workBookHandle = new WorkBookHandle(getTemplate());
            WorkSheetHandle workSheet;

            // Result sheet, men
            try {
                workSheet = workBookHandle.getWorkSheet("Hommes 6 essais");
                if (Competition.isMasters()) {
                    new MastersIndividualSheet(hbnSessionManager).writeIndividualSheet(lifters, workSheet, "M");
                } else {
                    new IndividualSheet(hbnSessionManager).writeIndividualSheet(lifters, workSheet, "M");
                }
            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }

            // Result sheet, women
            try {
                workSheet = workBookHandle.getWorkSheet("Femmes 6 essais");
                if (Competition.isMasters()) {
                    new MastersIndividualSheet(hbnSessionManager).writeIndividualSheet(lifters, workSheet, "F");
                } else {
                    new IndividualSheet(hbnSessionManager).writeIndividualSheet(lifters, workSheet, "F");
                }

            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }

            // Sinclair sheet, men
            try {
                workSheet = workBookHandle.getWorkSheet("Hommes Sinclair");
                new SinclairSheet(hbnSessionManager).writeSinclairSheet(sinclairLifters, workSheet, "M");
            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }

            // Sinclair sheet, women
            try {
                workSheet = workBookHandle.getWorkSheet("Femmes Sinclair");
                new SinclairSheet(hbnSessionManager).writeSinclairSheet(sinclairLifters, workSheet, "F");
            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }

            // Men Total ranking
            try {
                workSheet = workBookHandle.getWorkSheet("Hommes équipes");
                new TeamSheet(hbnSessionManager).writeTeamSheet(teamRankingLifters, workSheet, Ranking.TOTAL, clubs, "M");
            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }

            // Women total ranking
            try {
                workSheet = workBookHandle.getWorkSheet("Femmes équipes");
                new TeamSheet(hbnSessionManager).writeTeamSheet(teamRankingLifters, workSheet, Ranking.TOTAL, clubs, "F");
            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }

            // Team Total ranking
            try {
                workSheet = workBookHandle.getWorkSheet("Mixte équipes");
                new TeamSheet(hbnSessionManager).writeTeamSheet(teamRankingLifters, workSheet, Ranking.TOTAL, clubs, null);
            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }

            // Team Sum ranking
            try {
                workSheet = workBookHandle.getWorkSheet("Somme équipes");
                new TeamSheet(hbnSessionManager).writeTeamSheetAll(teamRankingLifters, workSheet, Ranking.ALL, clubs, null);
            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }
            
            // Team Custom ranking
            try {
                workSheet = workBookHandle.getWorkSheet("Spécial équipes");
                new TeamSheet(hbnSessionManager).writeTeamSheet(teamRankingLifters, workSheet, Ranking.CUSTOM, clubs, null);
            } catch (WorkSheetNotFoundException wnf) {
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
            }
            
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

    @Override
    public InputStream getTemplate() throws IOException {
        // final InputStream resourceAsStream =
        // app.getResourceAsStream(templateXls);
        File templateFile = new File(SheetUtils.getCompetition().getResultTemplateFileName());
        FileInputStream resourceAsStream = new FileInputStream(templateFile);
        return resourceAsStream;
    }

    @Override
    protected List<Lifter> getLifters(boolean excludeNotWeighed) {
        final List<Lifter> resultsOrderCopy = LifterSorter.resultsOrderCopy(new LifterContainer(app).getAllPojos(),
            Ranking.TOTAL);
        return resultsOrderCopy;
    }
}
