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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflamy
 *
 */
@SuppressWarnings("serial")
public class JXLSStartingList extends JXLSWorkbookStreamSource {
	
	public JXLSStartingList(){
		super(true);
	}
	
	public JXLSStartingList(boolean excludeNotWeighed) {
		super(excludeNotWeighed);
	}

	Logger logger = LoggerFactory.getLogger(JXLSStartingList.class);
	

	private Competition competition;
	Set<CompetitionSession> cleanSessions;

	@SuppressWarnings("unchecked")
	@Override
	protected void init() {
		super.init();
		
		final Session hbnSession = CompetitionApplication.getCurrent().getHbnSession();
		List<Competition> competitionList = hbnSession.createCriteria(Competition.class).list();
		competition = competitionList.get(0);
		getReportingBeans().put("competition",competition);
		getReportingBeans().put("session",app.getCurrentCompetitionSession());
		List<CompetitionSession> rawSessions = hbnSession.createCriteria(CompetitionSession.class).list();
		cleanSessions = new TreeSet<CompetitionSession>();
		for (CompetitionSession session: rawSessions) {
			if (session.getLifters().size() > 0) {
				cleanSessions.add(session);
			}
		}
		for (CompetitionSession session: cleanSessions) {
			System.err.println(session.getName());
		}
		getReportingBeans().put("sessions",cleanSessions);
	}

	@Override
	public InputStream getTemplate() throws IOException {
    	String templateName = "/StartSheetTemplate_"+CompetitionApplication.getCurrentLocale().getLanguage()+".xls";
        final InputStream resourceAsStream = app.getResourceAsStream(templateName);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + templateName);} //$NON-NLS-1$
        return resourceAsStream;
    }

	@Override
	protected void getSortedLifters()  {
	}


	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.spreadsheet.JXLSWorkbookStreamSource#size()
	 */
	@Override
	public int size() {
		return cleanSessions.size();
	}

}
