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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.List;

import net.sf.jxls.transformer.XLSTransformer;

import org.apache.poi.ss.usermodel.Workbook;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.LoggerUtils;
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
public class JXLSWorkbookStreamSource implements StreamResource.StreamSource {
    private final static Logger logger = LoggerFactory.getLogger(JXLSWorkbookStreamSource.class);
    
    protected static final String TEMPLATE_XLS = "/LifterCardTemplate_"+CompetitionApplication.getDefaultLocale().getLanguage()+".xls"; //$NON-NLS-1$
    
    protected CategoryLookup categoryLookup;
    protected CompetitionApplication app;
	private List<Lifter> lifters;

	private HashMap<String, Object> beans;

    public JXLSWorkbookStreamSource() {
    	this.app = CompetitionApplication.getCurrent();
    	init();
    }

    protected void init() {
        this.lifters = LifterSorter.registrationOrderCopy(new LifterContainer(app, false).getAllPojos());
        beans = new HashMap<String,Object>();
        beans.put("lifters",lifters);
        beans.put("masters",Competition.isMasters());
	}

    
    @Override
    public InputStream getStream() {
        try {
            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);
            
            new Thread(new Runnable() {
                @Override
				public void run() {
                    try {
                    	XLSTransformer transformer = new XLSTransformer();
                        Workbook workbook = transformer.transformXLS(getTemplate(),beans);
                        workbook.write(out);
                    } catch (Throwable e) {
                    	LoggerUtils.logException(logger, e);
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            return in;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    public InputStream getTemplate() throws IOException {
        final InputStream resourceAsStream = app.getResourceAsStream(TEMPLATE_XLS);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + TEMPLATE_XLS);} //$NON-NLS-1$
        return resourceAsStream;
    }

    public int size() {
        return lifters.size();
    }

	public List<Lifter> getLifters() {
		return lifters;
	}

}