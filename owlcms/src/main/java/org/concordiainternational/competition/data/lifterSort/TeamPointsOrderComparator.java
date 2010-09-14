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

package org.concordiainternational.competition.data.lifterSort;

import java.util.Comparator;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter.Ranking;

/**
 * This comparator sorts lifters within their team
 * 
 * @author jflamy
 * 
 */
public class TeamPointsOrderComparator extends AbstractLifterComparator implements Comparator<Lifter> {

    private Ranking rankingType;

    TeamPointsOrderComparator(Ranking rankingType) {
        this.rankingType = rankingType;
    }

    @Override
    public int compare(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        compare = compareClub(lifter1, lifter2);
        if (compare != 0) return compare;

        compare = compareGender(lifter1, lifter2);
        if (compare != 0) return compare;

        compare = compareRanking(lifter1, lifter2);
        if (compare != 0) return compare;

        return compare;
    }

    /**
     * @param lifter1
     * @param lifter2
     * @return
     */
    private int compareRanking(Lifter lifter1, Lifter lifter2) {
        switch (rankingType) {
        case SNATCH:
            return lifter1.getSnatchRank().compareTo(lifter2.getSnatchRank());
        case CLEANJERK:
            return lifter1.getCleanJerkRank().compareTo(lifter2.getCleanJerkRank());
        case TOTAL:
            return lifter1.getRank().compareTo(lifter2.getRank());
        }
        return 0;
    }

}
