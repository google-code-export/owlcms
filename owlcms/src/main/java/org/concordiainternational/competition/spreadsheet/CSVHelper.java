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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CategoryLookupByName;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.CompetitionSessionLookup;
import org.concordiainternational.competition.data.Lifter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.IsIncludedIn;
import org.supercsv.cellprocessor.constraint.StrRegEx;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

import com.extentech.formats.XLS.CellNotFoundException;
import com.extentech.formats.XLS.WorkSheetNotFoundException;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * Read registration data in CSV format. The file is expected to contain a header line, as illustrated:
 * 
 * <pre>
 * lastName,firstName,gender,club,yearOfBirth,registrationCategory,competitionSession,qualifyingTotal
 * Lamy,Jean-François,M,C-I,1961,m69,H1,140
 * </pre>
 * 
 * registrationCategory and competitionSession must be valid entries in the database.
 * 
 * @author Jean-François Lamy
 * 
 */
public class CSVHelper implements InputSheet {
    final private Logger logger = LoggerFactory.getLogger(CSVHelper.class);
    private CompetitionSessionLookup competitionSessionLookup;
    private CategoryLookupByName categoryLookupByName;
    private CellProcessor[] processors;

    CSVHelper(HbnSessionManager hbnSessionManager) {
        initProcessors(hbnSessionManager);
    }

    /**
     * Configure the cell validators and value converters.
     * 
     * @param hbnSessionManager
     *            to access current database.
     */
    private void initProcessors(HbnSessionManager hbnSessionManager) {
        categoryLookupByName = new CategoryLookupByName(hbnSessionManager);
        competitionSessionLookup = new CompetitionSessionLookup(hbnSessionManager);

        List<CompetitionSession> sessionList = CompetitionSession.getAll();
        Set<Object> sessionNameSet = new HashSet<Object>();
        for (CompetitionSession s : sessionList) {
            sessionNameSet.add(s.getName());
        }
        List<Category> categoryList = CategoryLookup.getSharedInstance().getCategories();
        Set<Object> categoryNameSet = new HashSet<Object>();
        for (Category c : categoryList) {
            categoryNameSet.add(c.getName());
        }
        processors = new CellProcessor[] {
                null, // last name, as is.
                null, // first name, as is.
                new StrRegEx("[mfMF]"), // gender
                null, // club, as is.
                new StrRegEx("(19|20)[0-9][0-9]", new ParseInt()), // birth year
                new Optional(new IsIncludedIn(categoryNameSet, new AsCategory())), // registrationCategory
                new IsIncludedIn(sessionNameSet, new AsCompetitionSession()), // sessionName
                new Optional(new ParseInt()), // registration total
        };
    }

    @Override
    public synchronized List<Lifter> getAllLifters(InputStream is, Session session) throws IOException,
            CellNotFoundException, WorkSheetNotFoundException {
        LinkedList<Lifter> allLifters = new LinkedList<Lifter>();

        CsvBeanReader cbr = new CsvBeanReader(new InputStreamReader(is), CsvPreference.EXCEL_PREFERENCE);
        try {
            final String[] header = cbr.getCSVHeader(true);
            Lifter lifter;
            while ((lifter = cbr.read(Lifter.class, header, processors)) != null) {
                logger.debug("adding {}", toString(lifter));
                allLifters.add(lifter);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                cbr.close();
            } catch (Exception e) {
                // ignored
            }
        }
        return allLifters;
    }

    @SuppressWarnings("unused")
    private class AsCategory extends CellProcessorAdaptor {

        public AsCategory() {
            super();
        }

        public AsCategory(CellProcessor next) {
            super(next);
        }

        @Override
        public Object execute(Object value, CSVContext context) {
            final Category result = categoryLookupByName.lookup((String) value);
            return next.execute(result, context);
        }
    }

    @SuppressWarnings("unused")
    private class AsCompetitionSession extends CellProcessorAdaptor {

        public AsCompetitionSession() {
            super();
        }

        public AsCompetitionSession(CellProcessor next) {
            super(next);
        }

        @Override
        public Object execute(Object value, CSVContext context) {
            final CompetitionSession result = competitionSessionLookup.lookup((String) value);
            return next.execute(result, context);
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public List<Lifter> getGroupLifters(InputStream is, String aGroup, Session session) throws IOException,
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
        return ExtenXLSReader.toString(lifter, includeTimeStamp);
    }

    public static String toString(Lifter lifter) {
        return toString(lifter, true);
    }

    @Override
    public void init(ExtenXLSReader ish) {
        // do nothing
    }

    @Override
    public List<Lifter> getLifters(boolean excludeNotWeighed) {
        // do nothing
        return null;
    }

    @Override
    public void readHeader(InputStream is, Session session) throws CellNotFoundException, WorkSheetNotFoundException, IOException {
        // do nothing
    }

}
