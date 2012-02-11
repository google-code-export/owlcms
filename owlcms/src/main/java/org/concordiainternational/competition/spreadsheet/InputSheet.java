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

import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

public interface InputSheet {

    public abstract List<Lifter> getAllLifters(InputStream is, HbnSessionManager session) throws CellNotFoundException,
            IOException, WorkSheetNotFoundException, InterruptedException, Throwable;

    public abstract List<Lifter> getGroupLifters(InputStream is, String aGroup, HbnSessionManager session)
            throws CellNotFoundException, IOException, WorkSheetNotFoundException, InterruptedException, Throwable;

}
