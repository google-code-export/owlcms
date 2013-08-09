/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.spreadsheet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.generators.WeightFormatter;
import org.hibernate.HibernateException;

/**
 * @author jflamy
 * 
 */
public class SheetUtils {

    private static Competition competition;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(Messages.getString(
        "OutputSheet.DateFormat", CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$

    static public String fixRank(Integer rank) {
        if (rank == null || rank == 0) {
            return ""; //$NON-NLS-1$
        } else if (rank > 0) {
            return Integer.toString(rank);
        } else return Messages.getString("ResultSheet.InvitedAbbreviation", CompetitionApplication.getCurrentLocale()); //$NON-NLS-1$
    }

    static public Object fixValue(String value) {
        try {
            return WeightFormatter.parseInt(value);
        } catch (NumberFormatException e) {
            return "-"; //$NON-NLS-1$
        }
    }

    static public Integer fixValue(Integer val) {
        return val; // do nothing
    }

    /**
     * @param competitionDate
     * @return
     */
    static Object getShortDate(Date competitionDate) {
        return dateFormat.format(competitionDate);
    }

    /**
     * @return
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    static Competition getCompetition() throws HibernateException {
        if (competition == null) {
            List<Competition> competitions = CompetitionApplication.getCurrent().getHbnSession().createCriteria(
                Competition.class).list();
            if (competitions.size() > 0) competition = competitions.get(0);
        }
        return competition;
    }
}
