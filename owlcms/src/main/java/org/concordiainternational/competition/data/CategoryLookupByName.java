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

package org.concordiainternational.competition.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * Utility class to compute a lifter's category. Category definitions are
 * retrieved from the database. Only categories marked as active are considered
 * (this allows the same list to be used in different types of championships).
 * 
 * @author jflamy
 * 
 */
public class CategoryLookupByName {

    private List<Category> categories;
    private HbnSessionManager hbnSessionManager;

    /**
     * @param hbnSessionManager
     *            required because we are using Hibernate to filter categories.
     */
    public CategoryLookupByName(HbnSessionManager hbnSessionManager) {
        this.hbnSessionManager = hbnSessionManager;
        reload();
    }

    /**
     * Compare the current category (presumed to be in a list) with a the
     * category being searched.
     * 
     * @return -1 if the current category is too light, 0 if it is a match, 1 if
     *         the current category is too heavy
     */
    final Comparator<Category> nameComparator = new Comparator<Category>() {
        @Override
        public int compare(Category comparisonCategory, Category lifterData) {
            String name1 = comparisonCategory.getName();
            String name2 = lifterData.getName();
            if (name1 == null && name2 == null) return 0;
            if (name1 == null) return 1;
            if (name2 == null) return -1;
            return name1.compareTo(name2);

        }
    };

    /**
     * Compare the current category (presumed to be in a list) with a the
     * category being searched.
     * 
     * @return -1 if the current category is too light, 0 if it is a match, 1 if
     *         the current category is too heavy
     */
    final Comparator<Category> searchComparator = nameComparator;

    /**
     * compare categories (assumed to be properly defined and non-overlapping).
     * Female categories come first, categories are sorted according to minimum
     * weight.
     * 
     * @return -1 if first category should come first, 0 if same, 1 if first
     *         category should come second
     */
    final Comparator<Category> sortComparator = nameComparator;

    /**
     * Reload cache from database. Only categories marked as active are loaded.
     */
    public void reload() {
        final HbnContainer<Category> categoriesFromDb = new HbnContainer<Category>(Category.class, hbnSessionManager);
        categoriesFromDb.addContainerFilter("active", "true", false, false); //$NON-NLS-1$ //$NON-NLS-2$
        categories = categoriesFromDb.getAllPojos();
        Collections.sort(categories, sortComparator);
    }

    public Category lookup(String catString) {
        // in order to use the predefined Java sorting routine, we place the
        // data from our lifter
        // inside a fake Category, and search for it.
        int index = Collections.binarySearch(categories, new Category(catString.toLowerCase().trim(), 0.0, 0.0, null,
                false), nameComparator);
        if (index >= 0) return categories.get(index);
        return null;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

}
