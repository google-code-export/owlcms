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

import com.vaadin.terminal.StreamResource;

/**
 * Encapsulate a spreadsheet as a StreamSource so that it can be used as a
 * source of data when the user clicks on a link. This class converts the output
 * stream to an
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
        setReportingBeans(new HashMap<String,Object>());
        getSortedLifters();
        if (lifters != null) {
            getReportingBeans().put("lifters",lifters);
        }
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
                        HashMap<String, Object> reportingBeans2 = getReportingBeans();
						Workbook workbook = null;
                        try {
                            workbook = transformer.transformXLS(getTemplate(),reportingBeans2);
                        } catch (Exception e) {
                            LoggerUtils.logException(logger, e);
                        }
                        if (workbook != null) {
                            postProcess(workbook);
                            workbook.write(out);
                        }
                    } catch (IOException e) {
                    	// ignore
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
		Cell cellRight = row.getCell(cellnum+1);
        cellRight.setCellValue("");
		CellStyle blank = workbook.createCellStyle();
		blank.setBorderBottom(CellStyle.BORDER_NONE);
		cellLeft.setCellStyle(blank);
		cellRight.setCellStyle(blank);
	}

	public InputStream getTemplate() throws IOException {
		String templateName = "/competitionBook/CompetitionBook_Total_"+CompetitionApplication.getCurrentSupportedLocale().getLanguage()+".xls";
		final InputStream resourceAsStream = app.getResourceAsStream(templateName);
		if (resourceAsStream == null) {
			throw new IOException("Resource not found: " + templateName);} //$NON-NLS-1$
		return resourceAsStream;
	}

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
