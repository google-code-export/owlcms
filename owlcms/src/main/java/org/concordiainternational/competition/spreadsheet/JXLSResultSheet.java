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

import org.apache.poi.ss.usermodel.Workbook;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter.Ranking;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflamy
 * 
 */
@SuppressWarnings("serial")
public class JXLSResultSheet extends JXLSWorkbookStreamSource {

    public JXLSResultSheet() {
        super(true);
    }

    public JXLSResultSheet(boolean excludeNotWeighed) {
        super(excludeNotWeighed);
    }

    Logger logger = LoggerFactory.getLogger(JXLSResultSheet.class);

    private Competition competition;

    @Override
    protected void init() {
        super.init();
        competition = Competition.getAll().get(0);
        getReportingBeans().put("competition", competition);
        getReportingBeans().put("session", app.getCurrentCompetitionSession());
    }

    @Override
    public InputStream getTemplate() throws IOException {
        String protocolTemplateFileName = competition.getProtocolFileName();
        // logger.info("protocol sheet: {}",protocolTemplateFileName);
        if (protocolTemplateFileName != null) {
            File templateFile = new File(protocolTemplateFileName);
            if (templateFile.exists()) {
                FileInputStream resourceAsStream = new FileInputStream(templateFile);
                return resourceAsStream;
            }
            // can't happen unless system is misconfigured.
            throw new IOException("resource not found: " + protocolTemplateFileName); //$NON-NLS-1$
        } else {
            throw new RuntimeException("Protocol sheet template not defined.");
        }
    }

    @Override
    protected void getSortedLifters() {
        this.lifters = LifterSorter.resultsOrderCopy(
                new LifterContainer(CompetitionApplication.getCurrent(), isExcludeNotWeighed()).getAllPojos(),
                Ranking.TOTAL);
        LifterSorter.assignCategoryRanks(lifters, Ranking.TOTAL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.spreadsheet.JXLSWorkbookStreamSource#postProcess(org.apache.poi.ss.usermodel.Workbook)
     */
    @Override
    protected void postProcess(Workbook workbook) {
        if (Competition.invitedIfBornBefore() <= 0) {
            zapCellPair(workbook, 3, 17);
        }
        final CompetitionSession currentCompetitionSession = app.getCurrentCompetitionSession();
        if (currentCompetitionSession == null) {
            zapCellPair(workbook, 3, 9);
        }
    }

}
