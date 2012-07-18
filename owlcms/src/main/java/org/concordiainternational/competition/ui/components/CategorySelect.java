/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.components;

import java.io.Serializable;
import java.util.Set;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryContainer;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.ItemAdapter;

import com.vaadin.ui.ListSelect;

@SuppressWarnings("unchecked")
public class CategorySelect extends ListSelect implements Serializable {

    private static final long serialVersionUID = -5471881649385421098L;
    CategoryContainer dataSource;
    CompetitionApplication app;

    /**
     * @param competitionApplication
     * @param locale
     * @return
     */
    public CategorySelect() {
        final ListSelect categorySelect = this;
        app = CompetitionApplication.getCurrent();
        dataSource = new CategoryContainer(app.getEntityManager());
        categorySelect.setContainerDataSource(dataSource);
        categorySelect.setItemCaptionPropertyId("name"); //$NON-NLS-1$
        categorySelect.setImmediate(true);
        categorySelect.setNullSelectionAllowed(true);
        categorySelect.setNullSelectionItemId(null);
        categorySelect.setRows(6);
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
