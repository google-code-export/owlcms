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

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.ui.CompetitionApplication;
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

	@Override
	protected void init() {
		super.init();

		List<Competition> competitionList = Competition.getAll();
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
		this.lifters = LifterSorter.registrationOrderCopy(new LifterContainer(app, isExcludeNotWeighed()).getAll());
	}

}
