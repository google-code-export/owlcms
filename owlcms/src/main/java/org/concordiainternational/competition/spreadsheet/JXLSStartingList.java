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

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
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
		super(false);
	}
	
	public JXLSStartingList(boolean excludeNotWeighed) {
		super(excludeNotWeighed);
	}

	Logger logger = LoggerFactory.getLogger(JXLSStartingList.class);

	@SuppressWarnings("unchecked")
	@Override
	protected void init() {
		super.init();
		
		final Session hbnSession = CompetitionApplication.getCurrent().getHbnSession();
		List<Competition> competitionList = hbnSession.createCriteria(Competition.class).list();
		Competition competition = competitionList.get(0);
		getReportingBeans().put("competition",competition);
	}
	
	@Override
	public InputStream getTemplate() throws IOException {
    	String templateName = "/StartSheetTemplate_"+CompetitionApplication.getCurrentSupportedLocale().getLanguage()+".xls";
        final InputStream resourceAsStream = app.getResourceAsStream(templateName);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + templateName);} //$NON-NLS-1$
        return resourceAsStream;
    }

	@Override
	protected void getSortedLifters()  {
		this.lifters = LifterSorter.registrationOrderCopy(new LifterContainer(app, isExcludeNotWeighed()).getAllPojos());
	}

}
