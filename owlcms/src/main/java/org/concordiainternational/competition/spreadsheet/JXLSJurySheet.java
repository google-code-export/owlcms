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

import net.sf.jxls.transformer.XLSTransformer;

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
public class JXLSJurySheet extends JXLSWorkbookStreamSource {
	

	private JXLSJurySheet(){
		super(true);
	}
	
	public JXLSJurySheet(boolean excludeNotWeighed) {
		super(excludeNotWeighed);
	}

	Logger logger = LoggerFactory.getLogger(JXLSJurySheet.class);
	

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
    	String templateName = "/JurySheetTemplate_"+CompetitionApplication.getCurrentSupportedLocale().getLanguage()+".xls";
        final InputStream resourceAsStream = app.getResourceAsStream(templateName);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + templateName);} //$NON-NLS-1$
        return resourceAsStream;
    }

	@Override
	protected void getSortedLifters()  {
		this.lifters = LifterSorter.displayOrderCopy(new LifterContainer(app, isExcludeNotWeighed()).getAllPojos());
	    LifterSorter.assignMedals(lifters);
	}

	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.spreadsheet.JXLSWorkbookStreamSource#configureTransformer(net.sf.jxls.transformer.XLSTransformer)
	 */
	@Override
	protected void configureTransformer(XLSTransformer transformer) {
		transformer.markAsFixedSizeCollection("lifters");
	}


}
