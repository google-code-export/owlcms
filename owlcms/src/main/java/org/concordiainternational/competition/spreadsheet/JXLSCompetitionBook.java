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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.jxls.transformer.XLSTransformer;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter.Ranking;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Result sheet, with team rankings
 * 
 * @author jflamy
 * 
 */
public class JXLSCompetitionBook extends JXLSWorkbookStreamSource {

    private static final long serialVersionUID = 1L;
    final private static int TEAMSHEET_FIRST_ROW = 5;

    @SuppressWarnings("unused")
    private Logger logger = LoggerFactory.getLogger(JXLSCompetitionBook.class);


    public JXLSCompetitionBook(){
        // by default, we exclude athletes who did not weigh in.
        super(true);
    }

    public JXLSCompetitionBook(boolean excludeNotWeighed) {
        super(excludeNotWeighed);
    }

    @Override
    public InputStream getTemplate() throws IOException {
        String resultTemplateFileName = SheetUtils.getCompetition().getResultTemplateFileName();
        File templateFile = new File(resultTemplateFileName);
        if (!templateFile.exists()) {
            // can't happen unless system is misconfigured.
            throw new IOException("resource not found: " + resultTemplateFileName); //$NON-NLS-1$
        }
        FileInputStream resourceAsStream = new FileInputStream(templateFile);
        return resourceAsStream;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init() {
        super.init();

        final Session hbnSession = CompetitionApplication.getCurrent().getHbnSession();
        List<Competition> competitionList = hbnSession.createCriteria(Competition.class).list();
        Competition competition = competitionList.get(0);
        getReportingBeans().put("competition",competition);
    }


    @Override
    protected void getSortedLifters() {
        HashMap<String, Object> reportingBeans = getReportingBeans();

        this.lifters = new LifterContainer(CompetitionApplication.getCurrent(),isExcludeNotWeighed()).getAllPojos();
        if (lifters.isEmpty()) {
            // prevent outputting silliness.
            throw new RuntimeException(Messages.getString(
                    "OutputSheet.EmptySpreadsheet", CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$
        }
        // extract club lists
        TreeSet<String> clubs = new TreeSet<String>();
        for (Lifter curLifter : lifters) {
            clubs.add(curLifter.getClub());
        }
        reportingBeans.put("clubs",clubs);

        final LifterSorter lifterSorter = new LifterSorter();
        List<Lifter> sortedLifters;
        List<Lifter> sortedMen = null;
        List<Lifter> sortedWomen = null;


        sortedLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.SNATCH);
        lifterSorter.assignCategoryRanks(sortedLifters, Ranking.SNATCH);
        sortedMen = new ArrayList<Lifter>(sortedLifters.size());
        sortedWomen = new ArrayList<Lifter>(sortedLifters.size());
        splitByGender(sortedLifters, sortedMen, sortedWomen);
        reportingBeans.put("mSn",sortedMen);
        reportingBeans.put("wSn",sortedWomen);

        sortedLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.CLEANJERK);
        lifterSorter.assignCategoryRanks(sortedLifters, Ranking.CLEANJERK);
        sortedMen = new ArrayList<Lifter>(sortedLifters.size());
        sortedWomen = new ArrayList<Lifter>(sortedLifters.size());
        splitByGender(sortedLifters, sortedMen, sortedWomen);
        reportingBeans.put("mCJ",sortedMen);
        reportingBeans.put("wCJ",sortedWomen);

        sortedLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.TOTAL);
        lifterSorter.assignCategoryRanks(sortedLifters, Ranking.TOTAL);
        sortedMen = new ArrayList<Lifter>(sortedLifters.size());
        sortedWomen = new ArrayList<Lifter>(sortedLifters.size());
        splitByGender(sortedLifters, sortedMen, sortedWomen);
        reportingBeans.put("mTot",sortedMen);
        reportingBeans.put("wTot",sortedWomen);

        sortedLifters = LifterSorter.resultsOrderCopy(lifters, Ranking.SINCLAIR);
        lifterSorter.assignSinclairRanksAndPoints(sortedLifters, Ranking.SINCLAIR);
        sortedMen = new ArrayList<Lifter>(sortedLifters.size());
        sortedWomen = new ArrayList<Lifter>(sortedLifters.size());
        splitByGender(sortedLifters, sortedMen, sortedWomen);
        reportingBeans.put("mSinclair",sortedMen);
        reportingBeans.put("wSinclair",sortedWomen);

        // team-oriented rankings. These put all the lifters from the same team together,
        // sorted from best to worst, so that the top "n" can be given points
        sortedLifters = LifterSorter.teamRankingOrderCopy(lifters, Ranking.CUSTOM);
        lifterSorter.assignCategoryRanks(sortedLifters, Ranking.CUSTOM);
        sortedMen = new ArrayList<Lifter>(sortedLifters.size());
        sortedWomen = new ArrayList<Lifter>(sortedLifters.size());
        splitByGender(sortedLifters, sortedMen, sortedWomen);
        reportingBeans.put("mCustom",sortedMen);
        reportingBeans.put("wCustom",sortedWomen);

        sortedLifters = LifterSorter.teamRankingOrderCopy(lifters, Ranking.COMBINED);
        lifterSorter.assignCategoryRanks(sortedLifters, Ranking.COMBINED);
        sortedMen = new ArrayList<Lifter>(sortedLifters.size());
        sortedWomen = new ArrayList<Lifter>(sortedLifters.size());
        splitByGender(sortedLifters, sortedMen, sortedWomen);
        reportingBeans.put("mCombined",sortedMen);
        reportingBeans.put("wCombined",sortedWomen);

        LifterSorter.teamRankingOrder(sortedLifters, Ranking.TOTAL);
        sortedMen = new ArrayList<Lifter>(sortedLifters.size());
        sortedWomen = new ArrayList<Lifter>(sortedLifters.size());
        splitByGender(sortedLifters, sortedMen, sortedWomen);
        reportingBeans.put("mTeam",sortedMen);
        reportingBeans.put("wTeam",sortedWomen);
        reportingBeans.put("mwTeam",sortedLifters);
    }



    @Override
    protected void configureTransformer(XLSTransformer transformer) {
        super.configureTransformer(transformer);
        transformer.markAsFixedSizeCollection("clubs");
        transformer.markAsFixedSizeCollection("mTeam");
        transformer.markAsFixedSizeCollection("wTeam");
        transformer.markAsFixedSizeCollection("mwTeam");
        transformer.markAsFixedSizeCollection("mCombined");
        transformer.markAsFixedSizeCollection("wCombined");
        transformer.markAsFixedSizeCollection("mCustom");
        transformer.markAsFixedSizeCollection("wCustom");
    }



    /* team result sheets need columns hidden, print area fixed
     * @see org.concordiainternational.competition.spreadsheet.JXLSWorkbookStreamSource#postProcess(org.apache.poi.ss.usermodel.Workbook)
     */
    @Override
    protected void postProcess(Workbook workbook) {
        super.postProcess(workbook);
        @SuppressWarnings("unchecked")
        int nbClubs = ((Set<String>) getReportingBeans().get("clubs")).size();

        setTeamSheetPrintArea(workbook, "MT", nbClubs);
        setTeamSheetPrintArea(workbook, "WT", nbClubs);
        setTeamSheetPrintArea(workbook, "MWT", nbClubs);

        setTeamSheetPrintArea(workbook, "MXT", nbClubs);
        setTeamSheetPrintArea(workbook, "WXT", nbClubs);

        setTeamSheetPrintArea(workbook, "MCT", nbClubs);
        setTeamSheetPrintArea(workbook, "WCT", nbClubs);

        translateSheets(workbook);
        workbook.setForceFormulaRecalculation(true);

    }

    private void setTeamSheetPrintArea(Workbook workbook, String sheetName, int nbClubs) {
        int sheetIndex = workbook.getSheetIndex(sheetName);
        if (sheetIndex >= 0) {
            workbook.setPrintArea(sheetIndex, 0, 4, TEAMSHEET_FIRST_ROW, TEAMSHEET_FIRST_ROW+nbClubs);
        }

    }

    private void translateSheets(Workbook workbook) {
        //TODO: also set headers and footers
        int nbSheets = workbook.getNumberOfSheets();
        for (int sheetIndex = 0; sheetIndex < nbSheets; sheetIndex++) {
            Sheet curSheet = workbook.getSheetAt(sheetIndex);
            String sheetName = curSheet.getSheetName();
            workbook.setSheetName(sheetIndex,Messages.getString("CompetitionBook."+sheetName, CompetitionApplication.getCurrentLocale()));

            String leftHeader = Messages.getStringNullIfMissing("CompetitionBook."+sheetName+"_LeftHeader", CompetitionApplication.getCurrentLocale());
            if (leftHeader != null) curSheet.getHeader().setLeft(leftHeader);
            String centerHeader = Messages.getStringNullIfMissing("CompetitionBook."+sheetName+"_CenterHeader", CompetitionApplication.getCurrentLocale());
            if (centerHeader != null) curSheet.getHeader().setCenter(centerHeader);
            String rightHeader = Messages.getStringNullIfMissing("CompetitionBook."+sheetName+"_RightHeader", CompetitionApplication.getCurrentLocale());
            if (rightHeader != null) curSheet.getHeader().setRight(rightHeader);

            String leftFooter = Messages.getStringNullIfMissing("CompetitionBook."+sheetName+"_LeftFooter", CompetitionApplication.getCurrentLocale());
            if (leftFooter != null) curSheet.getFooter().setLeft(leftFooter);
            String centerFooter = Messages.getStringNullIfMissing("CompetitionBook."+sheetName+"_CenterFooter", CompetitionApplication.getCurrentLocale());
            if (centerFooter != null) curSheet.getFooter().setCenter(centerFooter);	
            String rightFooter = Messages.getStringNullIfMissing("CompetitionBook."+sheetName+"_RightFooter", CompetitionApplication.getCurrentLocale());
            if (rightFooter != null) curSheet.getFooter().setRight(rightFooter);
        }
    }

    private void splitByGender(List<Lifter> sortedLifters,
            List<Lifter> sortedMen, List<Lifter> sortedWomen) {
        for (Lifter l: sortedLifters) {
            if ("m".equalsIgnoreCase(l.getGender())) {
                sortedMen.add(l);
            } else {
                sortedWomen.add(l);
            }
        }
    }
}
