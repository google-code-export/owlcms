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

import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.ItemAdapter;

import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.ui.Select;

@SuppressWarnings("unchecked")
public class PlatformSelect extends Select implements Serializable {

    private static final long serialVersionUID = -5471881649385421098L;
    HbnContainer<Platform> dataSource;
    CompetitionApplication app;

    /**
     * @param competitionApplication
     * @param locale
     * @return
     */
    public PlatformSelect() {
        final Select PlatformSelect = this;
        app = CompetitionApplication.getCurrent();
        dataSource = new HbnContainer<Platform>(Platform.class, app);
        PlatformSelect.setContainerDataSource(dataSource);
        PlatformSelect.setItemCaptionPropertyId("name"); //$NON-NLS-1$
        PlatformSelect.setImmediate(true);
        PlatformSelect.setNullSelectionAllowed(true);
        PlatformSelect.setNullSelectionItemId(null);
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
