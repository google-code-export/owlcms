/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.criteriacontainer.CriteriaContainer;

import com.vaadin.data.Container;
import com.vaadin.data.util.filter.Compare;

/**
 * Standard container for Lifters that respects the currently applicable group
 * for the application.
 * 
 * @author jflamy
 * 
 */
@SuppressWarnings("serial")
public class LifterContainer extends CriteriaContainer<Lifter> implements Container {
    Logger logger = LoggerFactory.getLogger(LifterContainer.class);
    private Boolean excludeNotWeighed = null;
    private CompetitionSession currentSession;

    //    private CompetitionApplication app;
    //
    //    private boolean excludeNotWeighed = true;

    /**
     * Default constructor, shows all items.
     * 
     * @param application
     */
    private LifterContainer(EntityManager em) {
        super(em,false,true,Lifter.class,50);
    }

    /**
     * Default constructor, shows all athletes.
     */
    public LifterContainer() {
        this(CompetitionApplication.getNewGlobalEntityManager());
    }

    /**
     * Alternate constructor that shows only athletes that have weighed-in.
     * @param excludeNotWeighed
     */
    public LifterContainer(boolean excludeNotWeighed) {
        this();
        setExcludeNonWeighed(excludeNotWeighed);
    }

    /**
     * Alternate constructor that shows only athletes that have weighed-in,
     * in the current session.
     * @param excludeNotWeighed
     */
    public LifterContainer(boolean excludeNotWeighed, CompetitionSession currentSession) {
        this();
        setExcludeNonWeighed(excludeNotWeighed);
        setCurrentSession(currentSession);
    }


    public void setExcludeNonWeighed(Boolean b) {
        this.excludeNotWeighed  = b;

    }

    public void setCurrentSession(CompetitionSession currentSession) {
        this.currentSession = currentSession;
    }

    /**
     * Return all lifters, according to current filters.
     * Note that calling the class methods on Lifter is more efficient. This method ensures that the 
     * list is synchronised with what is in the container.
     * @return the list of all lifters currently in the container, taking filters into account.
     */
    public List<Lifter> getAll() {
        removeAllContainerFilters();
        if (excludeNotWeighed != null || currentSession != null) {            
            if (excludeNotWeighed != null && excludeNotWeighed) {
                addContainerFilter(new Compare.Greater(Lifter_.bodyWeight.getName(), 0));
            } if (currentSession != null) {
                addContainerFilter(new Compare.Equal(Lifter_.competitionSession.getName(), currentSession.getName()));
            }

        }
        refresh();
        List<Lifter> lifters = new ArrayList<Lifter>(size());
        for (Object i : getItemIds()) {
            lifters.add(getEntity(i));
        }
        return lifters;
    }

    //    @Override
    //    protected void doFiltering() {
    //        // get filtering string from the user interface
    //        final String nameFilterValue = (String) nameFilterField.getValue();
    //        // this makes code portable between CriteriaContainer and BeanItemContainer
    //        String propertyId = qd.getPropertyId(Task_.class, Task_.name);
    //        
    //        // if value define add the filtering conditions, else remove them.
    //        if (nameFilterValue != null && nameFilterValue.length() != 0) {
    //            // conditions are added to the container filter mechanism.
    //            criteriaContainer.removeAllContainerFilters();
    //            SimpleStringFilter filter = new SimpleStringFilter(propertyId, nameFilterValue, true, true);
    //            criteriaContainer.addContainerFilter(filter);
    //            criteriaContainer.refresh();
    //        } else {
    //            criteriaContainer.removeAllContainerFilters();
    //            criteriaContainer.refresh();
    //        }
    //    }

    //    /*
    //     * This class adds filtering criteria other than simple textual filtering as
    //     * implemented by the Filterable interface. (non-Javadoc)
    //     * 
    //     * @see
    //     * com.vaadin.data.hbnutil.HbnContainer#addSearchCriteria(org.hibernate.
    //     * Criteria)
    //     */
    //    public Criteria addSearchCriteria(Criteria criteria) {
    //        final CompetitionSession currentGroup = ((CompetitionApplication) app).getCurrentCompetitionSession();
    //        logger.debug("Application " + app + " getCurrentCompetitionSession()=" + currentGroup);
    //        final String name = (currentGroup != null ? (String) currentGroup.getName() : null);
    //        if (currentGroup != null) {
    //            String simpleName = CompetitionSession.class.getSimpleName();
    //            char firstChar = simpleName.charAt(0);
    //            char nFirstChar = Character.toLowerCase(firstChar);
    //            criteria.createCriteria(simpleName.replace(firstChar,nFirstChar)).add(
    //                Restrictions.eq("name", name)); //$NON-NLS-1$
    //        }
    //        if (excludeNotWeighed) {
    //            criteria.add(Restrictions.gt("bodyWeight", 0.0D)); //$NON-NLS-1$
    //        }
    //        return criteria;
    //    }

}
