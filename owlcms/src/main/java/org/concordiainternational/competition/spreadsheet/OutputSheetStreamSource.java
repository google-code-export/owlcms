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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.WorkBookHandle;
import com.vaadin.terminal.StreamResource;

/**
 * Encapsulate a spreadsheet as a StreamSource so that it can be used as a
 * source of data when the user clicks on a link. This class converts the output
 * stream produced by the
 * {@link OutputSheet#writeWorkBook(WorkBookHandle, OutputStream)} method to an
 * input stream that the vaadin framework can consume.
 */
@SuppressWarnings("serial")
public class OutputSheetStreamSource<T extends OutputSheet> implements StreamResource.StreamSource {
    private final static Logger logger = LoggerFactory.getLogger(OutputSheetStreamSource.class);
    private final T outputSheet;
    private List<Lifter> lifters;

    public OutputSheetStreamSource(Class<T> parameterizedClass, CompetitionApplication app, boolean excludeNotWeighed) {
        CategoryLookup categoryLookup = new CategoryLookup(app);
        try {
            this.outputSheet = parameterizedClass.newInstance();
        } catch (Exception t) {
            throw new RuntimeException(t);
        }
        outputSheet.init(categoryLookup, app, app.getCurrentCompetitionSession());
        this.lifters = outputSheet.getLifters(excludeNotWeighed);
    }

    @Override
    public InputStream getStream() {
        try {
            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);
            logger.debug("starting getStream"); //$NON-NLS-1$
            new Thread(new Runnable() {
                public void run() {
                    try {
                        outputSheet.writeLifters(lifters, out);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
            logger.debug("returning inputStream"); //$NON-NLS-1$
            return in;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public int size() {
        return lifters.size();
    }

}