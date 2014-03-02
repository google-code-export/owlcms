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
import org.hibernate.Session;

import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;

public interface InputSheet {

    public abstract List<Lifter> getAllLifters(InputStream is, Session session) throws CellNotFoundException,
            IOException, WorkSheetNotFoundException, InterruptedException, Throwable;

    public abstract List<Lifter> getGroupLifters(InputStream is, String aGroup, Session session)
            throws CellNotFoundException, IOException, WorkSheetNotFoundException, InterruptedException, Throwable;

    void init(ExtenXLSReader ish);

    List<Lifter> getLifters(boolean excludeNotWeighed);

    void readHeader(InputStream is, Session session) throws CellNotFoundException, WorkSheetNotFoundException, IOException;

}
