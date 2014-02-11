/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * Utility class to compute a lifter's category. Category definitions are retrieved from the database. Only categories marked as active are
 * considered (this allows the same list to be used in different types of championships).
 * 
 * @author jflamy
 * 
 */
public class CategoryLookup {
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(CategoryLookup.class);
    private static CategoryLookup sharedCategoryLookup;
    private List<Category> categories;
    private HbnSessionManager hbnSessionManager;

    /**
     * @param hbnSessionManager
     *            required because we are using Hibernate to filter categories.
     */
    private CategoryLookup(HbnSessionManager hbnSessionManager) {
        this.hbnSessionManager = hbnSessionManager;
        reload();
    }

    public static synchronized CategoryLookup getSharedInstance() {
        if (sharedCategoryLookup == null) {
            sharedCategoryLookup = new CategoryLookup(null);
        }
        return sharedCategoryLookup;
    }

    public static synchronized CategoryLookup getSharedInstance(HbnSessionManager hbnSessionManager) {
        if (sharedCategoryLookup == null) {
            sharedCategoryLookup = new CategoryLookup(hbnSessionManager);
        }
        return sharedCategoryLookup;
    }

    /**
     * compare categories (assumed to be properly defined and non-overlapping). Female categories come first, categories are sorted
     * according to minimum weight.
     * 
     * @return -1 if first category should come first, 0 if same, 1 if first category should come second
     */
    final Comparator<Category> sortComparator = new Comparator<Category>() {
        @Override
        public int compare(Category o1, Category o2) {
            int compareTo = o1.getGender().compareTo(o2.getGender());
            if (compareTo != 0)
                return compareTo; // F < M
            return (o1.minimumWeight.compareTo(o2.minimumWeight));
        }
    };

    /**
     * Compare the current category (presumed to be in a list) with a the category being searched.
     * 
     * @return -1 if the current category is too light, 0 if it is a match, 1 if the current category is too heavy
     */
    final Comparator<Category> searchComparator = new Comparator<Category>() {
        @Override
        public int compare(Category comparisonCategory, Category lifterData) {

            // the bodyweight is passed in as a category minimum weight.
            final Double bodyWeight = lifterData.minimumWeight;

            // determine if we have the same gender.
            final String gender1 = comparisonCategory.getGender();
            final String gender2 = lifterData.getGender();
            if (gender1 == null || gender2 == null)
                return -1; // there won't be
                           // a match
            int compareTo = gender1.compareTo(gender2);
            if (compareTo != 0) {
                return compareTo; // F < M
            }

            // the genders match, now check that we are in the right category
            int compareToMin = (comparisonCategory.minimumWeight).compareTo(bodyWeight);
            int compareToMax = (comparisonCategory.maximumWeight).compareTo(bodyWeight);
            if (compareToMin >= 0) {
                // category's minimumWeight is greater or equal than lifter's
                // weight
                // (e.g. lifter is 68.5 or 69.0kg; if current category is 77kg,
                // return 1 to signal
                // that the category is too heavy.)
                return 1;
            } else if (compareToMax < 0) {
                // category maximumWeight is smaller than weight of lifter.
                // (e.g. lifter is 68.5kg; if current category is 62kg, return
                // -1 to signal that
                // the category is too light.). If the lifter is 62kg, the
                // category is ok, so we
                // fall to the next branch in the if.
                return -1;
            } else {
                // found
                return 0;
            }

        }
    };

    /**
     * Reload cache from database. Only categories marked as active are loaded.
     */
    public void reload() {
        // LoggerUtils.logException(logger, new
        // Exception("reloading categories"));
        HbnContainer<Category> activeCategoriesFromDb;

        HbnSessionManager sessMgr = hbnSessionManager;
        if (sessMgr == null) {
            sessMgr = CompetitionApplication.getCurrent();
            // patch for junit.
            // if (sessMgr == null) {
            // sessMgr = AllTests.getSessionManager();
            // }
        }
        activeCategoriesFromDb = new CategoryContainer(sessMgr, true); // only active categories
        categories = activeCategoriesFromDb.getAllPojos();
        Collections.sort(categories, sortComparator);
        // logger.debug("categories={}",categories);
    }

    public Category lookup(String gender, Double bodyWeight) {
        // in order to use the predefined Java sorting routine, we place the
        // data from our lifter
        // inside a fake Category, and search for it.
        if (bodyWeight == null || gender == null || bodyWeight < 0.1 || gender.trim().isEmpty())
            return null;
        int index = Collections.binarySearch(categories,
                new Category("lifter", bodyWeight, bodyWeight, gender, false), searchComparator); //$NON-NLS-1$
        if (index >= 0)
            return categories.get(index);
        return null;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

}
