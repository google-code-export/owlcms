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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CategoryLookupByName;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.CompetitionSessionLookup;
import org.concordiainternational.competition.data.Lifter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.IsIncludedIn;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.extentech.ExtenXLS.WorkSheetHandle;
import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

public class InputCSVHelper implements InputSheet {
	final private static Logger logger = LoggerFactory.getLogger(InputCSVHelper.class);

	// constants
	final static int START_ROW = 7;
	static final int GENDER_COLUMN = 4;
	static final int BODY_WEIGHT_COLUMN = 6;

	private CompetitionSessionLookup competitionSessionLookup;
	private CategoryLookupByName categoryLookupByName;

	InputCSVHelper(HbnSessionManager hbnSessionManager, LifterReader reader) {
		new CategoryLookup(hbnSessionManager);
		categoryLookupByName = new CategoryLookupByName(hbnSessionManager);
		competitionSessionLookup = new CompetitionSessionLookup(hbnSessionManager);
	}

	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.spreadsheet.InputSheet#getAllLifters(java.io.InputStream, com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager)
	 */
	@Override
	public synchronized List<Lifter> getAllLifters(InputStream is, HbnSessionManager sessionMgr) throws IOException,
	CellNotFoundException, WorkSheetNotFoundException {
		LinkedList<Lifter> allLifters = new LinkedList<Lifter>() ;

		CsvBeanReader cbr = new CsvBeanReader(new InputStreamReader(is), CsvPreference.EXCEL_PREFERENCE);
		List<CompetitionSession> sessionList = CompetitionSession.getAll();
		Set<Object> sessionNameSet = new TreeSet<Object>();
		for (CompetitionSession s : sessionList) {
			sessionNameSet.add(s.getName());
		}
		List<Category> categoryList = CategoryLookup.getSharedInstance().getCategories();
		Set<Object> categoryNameSet = new TreeSet<Object>();
		for (Category c : categoryList) {
			categoryNameSet.add(c.getName());
		}

		final CellProcessor[] processors = new CellProcessor[] {
				null, // last name, as is.
				null, // first name, as is.
				new IsIncludedIn(new HashSet<Object>(Arrays.asList("M","F"))), // gender
				null, // club, as is.
				new ParseDate("yyyy"), // birth year
				new IsIncludedIn(categoryNameSet), // registrationCategory
				new IsIncludedIn(sessionNameSet), // sessionName
				new Optional(new ParseInt()), // registration total
		};

		try {
			final String[] header = cbr.getCSVHeader(true);
			Lifter lifter;
			while( (lifter = cbr.read(Lifter.class, header, processors)) != null) {
				logger.warn("lifter {}", lifter);
			} 
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				cbr.close();
			} catch (IOException e) {
				// ignored
			}
		}
		return allLifters;
	}



	/*
	 * (non-Javadoc)
	 */
	@Override
	public List<Lifter> getGroupLifters(InputStream is, String aGroup, HbnSessionManager session) throws IOException,
	CellNotFoundException, WorkSheetNotFoundException {
		List<Lifter> groupLifters = new ArrayList<Lifter>();
		for (Lifter curLifter : getAllLifters(is, session)) {
			if (aGroup.equals(curLifter.getCompetitionSession())) {
				groupLifters.add(curLifter);
			}
		}
		return groupLifters;
	}

	public static String toString(Lifter lifter, boolean includeTimeStamp) {
		return InputSheetHelper.toString(lifter,includeTimeStamp);
	}

	public static String toString(Lifter lifter) {
		return toString(lifter, true);
	}


	Category getCategory(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
		categoryLookupByName.lookup("m69");
		return null;
	}



	public CompetitionSession getCompetitionSession(WorkSheetHandle sheet, int row, int column) throws CellNotFoundException {
		competitionSessionLookup.lookup("F1");
		return null;
	}


}
