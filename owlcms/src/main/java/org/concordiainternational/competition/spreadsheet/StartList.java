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
import java.io.OutputStream;
import java.util.List;

import org.concordiainternational.competition.data.CategoryLookup;
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

public class StartList extends OutputSheet {

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
                new StartSheet().writeStartSheet(lifters, workSheet);
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
    void writeLifter(Lifter lifter, WorkSheetHandle workSheet, CategoryLookup categoryLookup, int rownum)
            throws CellTypeMismatchException, CellNotFoundException {
        // Intentionally empty

    }

}
