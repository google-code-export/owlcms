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

package org.concordiainternational.competition.ui.components;

import java.io.Serializable;
import java.util.Set;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.ItemAdapter;

import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.ui.Select;

@SuppressWarnings("unchecked")
public class CategorySelect extends Select implements Serializable {

    private static final long serialVersionUID = -5471881649385421098L;
    HbnContainer<Category> dataSource;
    CompetitionApplication app;

    /**
     * @param competitionApplication
     * @param locale
     * @return
     */
    public CategorySelect() {
        final Select categorySelect = this;
        app = CompetitionApplication.getCurrent();
        dataSource = new HbnContainer<Category>(Category.class, app);
        categorySelect.setContainerDataSource(dataSource);
        categorySelect.setItemCaptionPropertyId("name"); //$NON-NLS-1$
        categorySelect.setImmediate(true);
        categorySelect.setNullSelectionAllowed(true);
        categorySelect.setNullSelectionItemId(null);
    }

	@Override
	public String toString() {
		
		Set<Long> categoryIds = (Set<Long>) getValue();
        if (categoryIds == null) return Messages.getString("Common.emptyList", app.getLocale()); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        final String separator = Messages.getString("CommonFieldFactory.listSeparator", app.getLocale()); //$NON-NLS-1$
        for (Long curCategoryId : categoryIds) {
            // Item s = (categories.getItem(curCategoryId));
            Category curCategory = (Category) ItemAdapter.getObject(dataSource.getItem(curCategoryId));
            sb.append(curCategory.getName());
            sb.append(separator); //$NON-NLS-1$
        }
        if (sb.length() == 0) {
            return Messages.getString("Common.emptyList", app.getLocale()); //$NON-NLS-1$
        } else {
            return sb.substring(0, sb.length() - separator.length()); // hide last comma
        }
	}
    
    
}
