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
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.WorkBookHandle;


/**
 * Encapsulate a spreadsheet as a StreamSource so that it can be used as a
 * source of data when the user clicks on a link. This class converts the output
 * stream produced by the
 * {@link OutputSheet#writeWorkBook(WorkBookHandle, OutputStream)} method to an
 * input stream that the Vaadin framework can consume.
 */
@SuppressWarnings("serial")
public class JXLSLifterCard extends JXLSWorkbookStreamSource {

	/**
	 * Number of rows in a card
	 */
	final static int CARD_SIZE = 10;
	/**
	 * Number of cards per page
	 */
	final static int CARDS_PER_PAGE = 2;

    /**
     * 
     */
    public JXLSLifterCard() {
		super(false);
	}
	
    public JXLSLifterCard(boolean excludeNotWeighed) {
		super(excludeNotWeighed);
	}


	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory.getLogger(JXLSLifterCard.class);

    
    @Override
	public InputStream getTemplate() throws IOException {
    	String templateName = "/LifterCardTemplate_"+CompetitionApplication.getCurrentSupportedLocale().getLanguage()+".xls";
        final InputStream resourceAsStream = app.getResourceAsStream(templateName);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + templateName);} //$NON-NLS-1$
        return resourceAsStream;
    }


	@Override
	protected void getSortedLifters()  {
		this.lifters = LifterSorter.registrationOrderCopy(new LifterContainer(isExcludeNotWeighed()).getAll());
	}

	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.spreadsheet.JXLSWorkbookStreamSource#postProcess(org.apache.poi.ss.usermodel.Workbook)
	 */
	@Override
	protected void postProcess(Workbook workbook) {
		setPageBreaks(workbook);
	}

	private void setPageBreaks(Workbook workbook) {
		Sheet sheet = workbook.getSheetAt(0);
		int lastRowNum = sheet.getLastRowNum();
		sheet.setAutobreaks(false);
		int increment = CARDS_PER_PAGE*CARD_SIZE + (CARDS_PER_PAGE-1);
		
		for (int curRowNum = increment; curRowNum < lastRowNum;) {
			sheet.setRowBreak(curRowNum-1);
			curRowNum += increment;
		}
	}
}
