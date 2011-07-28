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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.Lifter;
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
public abstract class JXLSWorkbookStreamSource implements StreamResource.StreamSource {
    private final static Logger logger = LoggerFactory.getLogger(JXLSWorkbookStreamSource.class);
    
    protected CategoryLookup categoryLookup;
    protected CompetitionApplication app;
	protected List<Lifter> lifters;

	private HashMap<String, Object> reportingBeans;

	private boolean excludeNotWeighed;

    public JXLSWorkbookStreamSource(boolean excludeNotWeighed) {
    	this.excludeNotWeighed = excludeNotWeighed;
    	this.app = CompetitionApplication.getCurrent();
    	init();
    }

    protected void init() {
        getSortedLifters();
        setReportingBeans(new HashMap<String,Object>());
        getReportingBeans().put("lifters",lifters);
        getReportingBeans().put("masters",Competition.isMasters());
	}
    
	/**
	 * Return lifters as they should be sorted.
	 */
	abstract protected void getSortedLifters();

    
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
                    	configureTransformer(transformer);
                        Workbook workbook = transformer.transformXLS(getTemplate(),getReportingBeans());
                        postProcess(workbook);
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
    
    protected void configureTransformer(XLSTransformer transformer) {
    	// do nothing, to be overridden as needed,
	}

	protected void postProcess(Workbook workbook) {
    	// do nothing, to be overridden as needed,
	}
    

	/**
	 * Erase a pair of adjoining cells.
	 * @param workbook
	 * @param rownum
	 * @param cellnum
	 */
	public void zapCellPair(Workbook workbook, int rownum, int cellnum) {
		Row row = workbook.getSheetAt(0).getRow(rownum);
		final Cell cellLeft = row.getCell(cellnum);
		cellLeft.setCellValue("");
		row.getCell(19).setCellValue("");
		CellStyle blank = workbook.createCellStyle();
		blank.setBorderBottom(CellStyle.BORDER_NONE);
		row.getCell(cellnum+1).setCellStyle(blank);
	}

	public abstract InputStream getTemplate() throws IOException ;

    public int size() {
        return lifters.size();
    }

	public List<Lifter> getLifters() {
		return lifters;
	}

	public void setReportingBeans(HashMap<String, Object> jXLSBeans) {
		this.reportingBeans = jXLSBeans;
	}

	public HashMap<String, Object> getReportingBeans() {
		return reportingBeans;
	}

	public void setExcludeNotWeighed(boolean excludeNotWeighed) {
		this.excludeNotWeighed = excludeNotWeighed;
	}

	public boolean isExcludeNotWeighed() {
		return excludeNotWeighed;
	}

}