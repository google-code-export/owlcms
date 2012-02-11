/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.spreadsheet;

import org.concordiainternational.competition.data.Lifter;

import com.extentech.ExtenXLS.WorkSheetHandle;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

public interface LifterReader {
	Lifter readLifter(WorkSheetHandle sheet, int lifterNumber);
	
	void createInputSheetHelper(HbnSessionManager hbnSessionManager);
}
