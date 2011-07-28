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
	Logger logger = LoggerFactory.getLogger(JXLSResultSheet.class);
	

	private Competition competition;

	@Override
	protected void init() {
		super.init();
		competition = Competition.getAll().get(0);
		getReportingBeans().put("competition",competition);
		getReportingBeans().put("session",app.getCurrentCompetitionSession());
	}

	@Override
	public InputStream getTemplate() throws IOException {
    	String templateName = "/ResultSheetTemplate_"+CompetitionApplication.getCurrentLocale().getLanguage()+".xls";
        final InputStream resourceAsStream = app.getResourceAsStream(templateName);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + templateName);} //$NON-NLS-1$
        return resourceAsStream;
    }

	@Override
	protected void getSortedLifters()  {
		this.lifters = LifterSorter.resultsOrderCopy(new LifterContainer(CompetitionApplication.getCurrent()).getAllPojos(),
	            Ranking.TOTAL);
	    LifterSorter.assignMedals(lifters);
	}
	
	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.spreadsheet.JXLSWorkbookStreamSource#postProcess(org.apache.poi.ss.usermodel.Workbook)
	 */
	@Override
	protected void postProcess(Workbook workbook) {
		if (Competition.invitedIfBornBefore() <= 0) {
			zapCellPair(workbook,3,18);
		}
		final CompetitionSession currentCompetitionSession = app.getCurrentCompetitionSession();
		if (currentCompetitionSession == null) {
			zapCellPair(workbook,3,10);
		}
	}


}
