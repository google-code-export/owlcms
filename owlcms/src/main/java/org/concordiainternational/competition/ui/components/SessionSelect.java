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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;

public class SessionSelect extends HorizontalLayout implements Serializable {

    private static final long serialVersionUID = -5471881649385421098L;
    private static final Logger logger = LoggerFactory.getLogger(SessionSelect.class);
    
    CompetitionSession value = null;
    Item selectedItem = null;
    Serializable selectedId = null;
	private Select sessionSelect;
	private ValueChangeListener listener;


	/**
     * @param competitionApplication
     * @param locale
     * @return
     */
    public SessionSelect(final CompetitionApplication competitionApplication, final Locale locale) {
        final Label groupLabel = new Label(Messages.getString("CompetitionApplication.CurrentGroup", locale)); //$NON-NLS-1$
        groupLabel.setSizeUndefined();

        sessionSelect = new Select();
        loadData(competitionApplication, sessionSelect);
        sessionSelect.setImmediate(true);
        sessionSelect.setNullSelectionAllowed(true);
        sessionSelect.setNullSelectionItemId(null);
        final CompetitionSession currentGroup = competitionApplication.getCurrentCompetitionSession();
        sessionSelect.select((currentGroup != null ? currentGroup.getId() : null));
        listener = new ValueChangeListener() {
            private static final long serialVersionUID = -4650521592205383913L;


            @Override
            public void valueChange(ValueChangeEvent event) {
                final Serializable selectedValue = (Serializable) event.getProperty().getValue();

                if (selectedValue != null) {
                	selectedId = selectedValue;
                    selectedItem = sessionSelect.getContainerDataSource().getItem(selectedValue);
                    value = (CompetitionSession) ItemAdapter.getObject(selectedItem);
                } else {
                	selectedId = null;
                	selectedItem = null;
                	value = null;
                }
                logger.warn("valueChange {}",value);
                competitionApplication.setCurrentCompetitionSession(value);
            }

        };
        sessionSelect.addListener(listener);

        this.addComponent(groupLabel);
        this.setComponentAlignment(groupLabel, Alignment.MIDDLE_LEFT);
        this.addComponent(sessionSelect);
        this.setComponentAlignment(groupLabel, Alignment.MIDDLE_LEFT);
        this.setSpacing(true);
    }

	/**
	 * Force a reload of the names in the dropdown.
	 * @param competitionApplication
	 * @param sessionSelect1
	 * @return
	 */
	private HbnContainer<CompetitionSession> loadData(
			final CompetitionApplication competitionApplication,
			final Select sessionSelect1) {
		final HbnContainer<CompetitionSession> sessionDataSource = new HbnContainer<CompetitionSession>(CompetitionSession.class, competitionApplication);
        sessionSelect1.setContainerDataSource(sessionDataSource);
        sessionSelect1.setItemCaptionPropertyId("name"); //$NON-NLS-1$
		return sessionDataSource;
	}
	
	public void refresh() {
		sessionSelect.removeListener(listener);
		loadData(CompetitionApplication.getCurrent(), sessionSelect);
		sessionSelect.setValue(selectedId);
		logger.warn("selected {}",selectedId);
		sessionSelect.addListener(listener);
		CompetitionApplication.getCurrent().push();
	}
    
    public Item getSelectedItem() {
		return selectedItem;
	}

	public CompetitionSession getValue() {
		return value;
	}

	public Serializable getSelectedId() {
		return selectedId;
	}
}
