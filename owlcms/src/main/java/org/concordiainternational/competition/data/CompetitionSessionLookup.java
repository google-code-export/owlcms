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

import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * Utility class to compute a lifter's group. Group definitions are retrieved from the database. Only groups marked as active are considered
 * (this allows the same list to be used in different types of championships).
 * 
 * @author jflamy
 * 
 */
public class CompetitionSessionLookup {

    private List<CompetitionSession> competitionSessions;
    private HbnSessionManager hbnSessionManager;

    /**
     * @param hbnSessionManager
     *            required because we are using Hibernate to filter groups.
     */
    public CompetitionSessionLookup(HbnSessionManager hbnSessionManager) {
        this.hbnSessionManager = hbnSessionManager;
        reload();
    }

    /**
     * Compare the current group (presumed to be in a list) with a the group being searched.
     * 
     * @return -1 if the current group is too light, 0 if it is a match, 1 if the current group is too heavy
     */
    final Comparator<CompetitionSession> nameComparator = new Comparator<CompetitionSession>() {
        @Override
        public int compare(CompetitionSession comparisonGroup, CompetitionSession lifterData) {
            String name1 = comparisonGroup.getName();
            String name2 = lifterData.getName();
            if (name1 == null && name2 == null)
                return 0;
            if (name1 == null)
                return 1;
            if (name2 == null)
                return -1;
            return name1.compareTo(name2);

        }
    };

    /**
     * Reload cache from database. Only groups marked as active are loaded.
     */
    public void reload() {
        final HbnContainer<CompetitionSession> groupsFromDb = new HbnContainer<CompetitionSession>(CompetitionSession.class,
                hbnSessionManager);
        competitionSessions = groupsFromDb.getAllPojos();
        Collections.sort(competitionSessions, nameComparator);
    }

    public CompetitionSession lookup(String catString) {
        // inside a fake Group, and search for it.
        int index = Collections.binarySearch(competitionSessions, new CompetitionSession(catString), nameComparator);
        if (index >= 0)
            return competitionSessions.get(index);
        return null;
    }

    public List<CompetitionSession> getGroups() {
        return competitionSessions;
    }

    // public void setGroups(List<Group> groups) {
    // this.groups = groups;
    // }

}
