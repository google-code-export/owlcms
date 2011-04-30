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

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.UserActions;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.hbnutil.HbnContainer;

/**
 * Standard container for Lifters that respects the currently applicable group
 * for the application.
 * 
 * @author jflamy
 * 
 */
public class LifterContainer extends HbnContainer<Lifter> {
    private static final long serialVersionUID = -1470325985621499261L;
    Logger logger = LoggerFactory.getLogger(LifterContainer.class);

    private UserActions app;

    private boolean excludeNotWeighed = true;

    /**
     * Default constructor, shows all athletes.
     * 
     * @param application
     */
    public LifterContainer(CompetitionApplication application) {
        super(Lifter.class, application);
        this.app = application;
    }

    /**
     * Alternate constructor that shows only athletes that have weighed-in.
     * 
     * @param application
     * @param excludeNotWeighed
     */
    public LifterContainer(CompetitionApplication application, boolean excludeNotWeighed) {
        this(application);
        this.excludeNotWeighed = excludeNotWeighed;
    }

    /*
     * This class adds filtering criteria other than simple textual filtering as
     * implemented by the Filterable interface. (non-Javadoc)
     * 
     * @see
     * com.vaadin.data.hbnutil.HbnContainer#addSearchCriteria(org.hibernate.
     * Criteria)
     */
    @Override
    public Criteria addSearchCriteria(Criteria criteria) {
        final CompetitionSession currentGroup = ((CompetitionApplication) app).getCurrentCompetitionSession();
        logger.debug("Application " + app + " getCurrentCompetitionSession()=" + currentGroup);
        final String name = (currentGroup != null ? (String) currentGroup.getName() : null);
        if (currentGroup != null) {
            String simpleName = CompetitionSession.class.getSimpleName();
            char firstChar = simpleName.charAt(0);
            char nFirstChar = Character.toLowerCase(firstChar);
            criteria.createCriteria(simpleName.replace(firstChar,nFirstChar)).add(
                Restrictions.eq("name", name)); //$NON-NLS-1$
        }
        if (excludeNotWeighed) {
            criteria.add(Restrictions.gt("bodyWeight", 0.0D)); //$NON-NLS-1$
        }
        return criteria;
    }

}
