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
import java.util.Locale;

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.ItemAdapter;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;

public class GroupSelect extends HorizontalLayout implements Serializable {

    private static final long serialVersionUID = -5471881649385421098L;

    /**
     * @param competitionApplication
     * @param locale
     * @return
     */
    public GroupSelect(final CompetitionApplication competitionApplication, final Locale locale) {
        final Label groupLabel = new Label(Messages.getString("CompetitionApplication.CurrentGroup", locale)); //$NON-NLS-1$
        groupLabel.setSizeUndefined();

        final Select groupSelect = new Select();
        final HbnContainer<CompetitionSession> dbGroupDataSource = new HbnContainer<CompetitionSession>(CompetitionSession.class, competitionApplication);
        groupSelect.setContainerDataSource(dbGroupDataSource);
        groupSelect.setItemCaptionPropertyId("name"); //$NON-NLS-1$
        groupSelect.setImmediate(true);
        groupSelect.setNullSelectionAllowed(true);
        groupSelect.setNullSelectionItemId(null);
        final CompetitionSession currentGroup = competitionApplication.getCurrentCompetitionSession();
        groupSelect.select((currentGroup != null ? currentGroup.getId() : null));
        final ValueChangeListener listener = new ValueChangeListener() {
            private static final long serialVersionUID = -4650521592205383913L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                final Serializable selectedValue = (Serializable) event.getProperty().getValue();
                CompetitionSession value = null;
                if (selectedValue != null) {
                    Item selectedItem = dbGroupDataSource.getItem(selectedValue);
                    value = (CompetitionSession) ItemAdapter.getObject(selectedItem);
                }
                competitionApplication.setCurrentCompetitionSession(value);
            }

        };
        groupSelect.addListener(listener);

        this.addComponent(groupLabel);
        this.setComponentAlignment(groupLabel, Alignment.MIDDLE_LEFT);
        this.addComponent(groupSelect);
        this.setComponentAlignment(groupLabel, Alignment.MIDDLE_LEFT);
        this.setSpacing(true);
    }
}
