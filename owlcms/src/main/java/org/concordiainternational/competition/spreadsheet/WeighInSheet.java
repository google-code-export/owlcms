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

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;

/**
 * The start sheet format is able to produce a round-trip. It implements both the input sheet and output sheet interfaces.
 * 
 * @author jflamy
 * 
 */
public class WeighInSheet implements InputSheet, LifterReader {

    protected static final String TEMPLATE_XLS = "/WeighInSheetTemplate.xls"; //$NON-NLS-1$
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(WeighInSheet.class);
    private ExtenXLSReader inputSheetHelper;

    /**
     * Create a sheet. If this constructor is used, or newInstance is called, then {@link #init(CompetitionApplication)} must also be
     * called.
     */
    public WeighInSheet() {
    }

    @Override
    public void init(ExtenXLSReader inputSheetHelper1) {
        this.inputSheetHelper = inputSheetHelper1;
    }

    /**
     * Fill in a lifter record from a row in the spreadsheet.
     * 
     * @param lifterNumber
     *            index of the lifter, starting at 0
     * 
     * @throws CellNotFoundException
     */
    @Override
    public Lifter readLifter(int lifterNumber) {
        return inputSheetHelper.readLifter(lifterNumber);
    }

    @Override
    public List<Lifter> getLifters(boolean excludeNotWeighed) {
        return LifterSorter.displayOrderCopy(new LifterContainer(CompetitionApplication.getCurrent(), excludeNotWeighed).getAllPojos());
    }

    @Override
    public void readHeader(InputStream is, Session session)
            throws CellNotFoundException, WorkSheetNotFoundException, IOException {
        inputSheetHelper.readHeader(is, session);
        return;
    }

    @Override
    public List<Lifter> getAllLifters(InputStream is, Session session)
            throws Throwable {
        return inputSheetHelper.getAllLifters(is, session);
    }

    @Override
    public List<Lifter> getGroupLifters(InputStream is, String aGroup,
            Session session) throws Throwable {
        return inputSheetHelper.getGroupLifters(is, aGroup, session);
    }

}
