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
import java.util.List;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
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

public class StartList extends OutputSheet {
	
	private HbnSessionManager hbnSessionManager;
	
    /**
     * Create a sheet.
     * If this constructor is used, or newInstance is called, then 
     * {@link #init(CategoryLookup, CompetitionApplication, CompetitionSession)} must also be called.
     */
    public StartList() {
    	this.hbnSessionManager = CompetitionApplication.getCurrent();
    }
	
	public StartList(HbnSessionManager hbnSessionManager) {
		this.hbnSessionManager = hbnSessionManager;
	}

    protected static final String TEMPLATE_XLS = "/StartSheetTemplate.xls"; //$NON-NLS-1$

    final static Logger logger = LoggerFactory.getLogger(StartList.class);

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

            // get the data sheet
            workBookHandle = new WorkBookHandle(getTemplate());
            WorkSheetHandle workSheet;

            // Create the start list.
            try {
                workSheet = workBookHandle.getWorkSheet(0);
                new StartSheet(hbnSessionManager).writeStartSheet(lifters, workSheet);
            } catch (WorkSheetNotFoundException wnf) {
                LoggerUtils.logException(logger, wnf);
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
        final InputStream resourceAsStream = app.getResourceAsStream(TEMPLATE_XLS);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + TEMPLATE_XLS);} //$NON-NLS-1$
        return resourceAsStream;
    }

    @Override
    protected List<Lifter> getLifters(boolean excludeNotWeighed) {
        final List<Lifter> allLifters = new LifterContainer(app, excludeNotWeighed).getAllPojos();
        final List<Lifter> registrationOrderCopy = LifterSorter.registrationOrderCopy(allLifters);
        return registrationOrderCopy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.concordiainternational.competition.spreadsheet.OutputSheet#writeLifter
     * (org.concordiainternational.competition.data.Lifter,
     * com.extentech.ExtenXLS.WorkSheetHandle,
     * org.concordiainternational.competition.data.CategoryLookup, int)
     */
    @Override
    void writeLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup1, int rownum)
            throws CellTypeMismatchException, CellNotFoundException {
        // Intentionally empty

    }

}
