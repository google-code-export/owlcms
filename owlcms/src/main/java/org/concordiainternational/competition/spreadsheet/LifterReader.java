package org.concordiainternational.competition.spreadsheet;

import org.concordiainternational.competition.data.Lifter;

import com.extentech.ExtenXLS.WorkSheetHandle;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

public interface LifterReader {
	Lifter readLifter(WorkSheetHandle sheet, int lifterNumber);
	
	void createInputSheetHelper(HbnSessionManager hbnSessionManager);
}
