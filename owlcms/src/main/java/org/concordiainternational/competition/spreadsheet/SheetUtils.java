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
