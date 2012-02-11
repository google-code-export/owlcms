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

import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.ItemAdapter;

import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.ui.ListSelect;

@SuppressWarnings("unchecked")
public class PlatformSelect extends ListSelect implements Serializable {

    private static final long serialVersionUID = -5471881649385421098L;
    HbnContainer<Platform> dataSource;
    CompetitionApplication app;

    /**
     * @param competitionApplication
     * @param locale
     * @return
     */
    public PlatformSelect() {
        final PlatformSelect platformSelect = this;
        app = CompetitionApplication.getCurrent();
        dataSource = new HbnContainer<Platform>(Platform.class, app);
        platformSelect.setContainerDataSource(dataSource);
        platformSelect.setItemCaptionPropertyId("name"); //$NON-NLS-1$
        platformSelect.setImmediate(true);
        platformSelect.setNullSelectionAllowed(true);
        platformSelect.setNullSelectionItemId(null);
        platformSelect.setRows(1);
    }

	@Override
	public String toString() {
		
		Set<Long> PlatformIds = (Set<Long>) getValue();
        if (PlatformIds == null) return Messages.getString("Common.emptyList", app.getLocale()); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        final String separator = Messages.getString("CommonFieldFactory.listSeparator", app.getLocale()); //$NON-NLS-1$
        for (Long curPlatformId : PlatformIds) {
            // Item s = (categories.getItem(curPlatformId));
            Platform curPlatform = (Platform) ItemAdapter.getObject(dataSource.getItem(curPlatformId));
            sb.append(curPlatform.getName());
            sb.append(separator); //$NON-NLS-1$
        }
        if (sb.length() == 0) {
            return Messages.getString("Common.emptyList", app.getLocale()); //$NON-NLS-1$
        } else {
            return sb.substring(0, sb.length() - separator.length()); // hide last comma
        }
	}
    
    
}
