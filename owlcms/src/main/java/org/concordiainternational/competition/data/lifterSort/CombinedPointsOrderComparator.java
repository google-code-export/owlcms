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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Medal ordering.
 * 
 * @author jflamy
 * 
 */
public class CombinedPointsOrderComparator extends AbstractLifterComparator implements Comparator<Lifter> {

    final static Logger logger = LoggerFactory.getLogger(CombinedPointsOrderComparator.class);

    private Ranking rankingType;

    public CombinedPointsOrderComparator(Ranking rankingType) {
        this.rankingType = rankingType;
    }

    @Override
    public int compare(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        switch (rankingType) {
        case SNATCH:
            return compareSnatchResultOrder(lifter1, lifter2);
        case CLEANJERK:
            return compareCleanJerkResultOrder(lifter1, lifter2);
        case TOTAL:
            return compareTotalResultOrder(lifter1, lifter2);
        case SINCLAIR:
            return compareSinclairResultOrder(lifter1, lifter2);
        }

        return compare;
    }

    /**
     * Determine who ranks first. If the body weights are the same, the lifter
     * who reached total first is ranked first.
     * 
     * @param lifter1
     * @param lifter2
     * @return
     */
    public int compareTotalResultOrder(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        if (WinningOrderComparator.useRegistrationCategory) {
            compare = compareRegistrationCategory(lifter1, lifter2);
        } else {
            compare = compareCategory(lifter1, lifter2);
        }
        if (compare != 0) return compare;

        compare = compareTotal(lifter1, lifter2);
        if (compare != 0) return -compare; // we want reverse order - smaller
                                           // comes after

        compare = compareBodyWeight(lifter1, lifter2);
        if (compare != 0) return compare; // smaller lifter wins

        compare = compareBestCleanJerk(lifter1, lifter2);
        if (compare != 0) return compare; // smallest clean and jerk wins (i.e.
                                          // best snatch wins !)

        compare = compareBestCleanJerkAttemptNumber(lifter1, lifter2);
        if (compare != 0) return compare; // earlier best attempt wins

        // note that when comparing total, we do NOT consider snatch. At this
        // stage, both lifters have
        // done the same weight at the same attempt. We are trying to determine
        // who did the attempt first.
        // So if the best attempt was the first one, we must NOT consider snatch
        // results when doing this determination
        compare = comparePreviousAttempts(lifter1.getBestResultAttemptNumber(), true, lifter1, lifter2);
        if (compare != 0) return compare; // compare attempted weights (prior to
                                          // best attempt), smaller first

        // The IWF referee examination example shows a case where the lifter in
        // the earlier group is not
        // given the ranking; according to the answers, lot number alone is
        // used.
        // compare = compareGroup(lifter1, lifter2);
        // if (compare != 0) return compare; // if split groups, smallest group
        // wins -- lifted earlier

        compare = compareLotNumber(lifter1, lifter2);
        if (compare != 0) return compare; // if equality within a group,
                                          // smallest lot number wins

        return compare;
    }

    public int compareSnatchResultOrder(Lifter lifter1, Lifter lifter2) {
        boolean trace =
        // (
        // (lifter1.getFirstName().equals("Yvon") &&
        // lifter2.getFirstName().equals("Anthony"))
        // ||
        // (lifter2.getFirstName().equals("Yvon") &&
        // lifter1.getFirstName().equals("Anthony"))
        // );
        false;
        int compare = 0;

        if (trace) logger.warn("lifter1 {};  lifter2 {}", lifter1.getFirstName(), lifter2.getFirstName());

        if (WinningOrderComparator.useRegistrationCategory) {
            compare = compareRegistrationCategory(lifter1, lifter2);
        } else {
            compare = compareCategory(lifter1, lifter2);
        }
        if (trace) logger.warn("compareCategory {}", compare);
        if (compare != 0) return compare;

        compare = compareBestSnatch(lifter1, lifter2);
        if (trace) logger.warn("compareBestSnatch {}", compare);
        if (compare != 0) return -compare; // smaller snatch is less good

        compare = compareBodyWeight(lifter1, lifter2);
        if (trace) logger.warn("compareBodyWeight {}", compare);
        if (compare != 0) return compare; // smaller lifter wins

        compare = compareBestSnatchAttemptNumber(lifter1, lifter2);
        if (trace) logger.warn("compareBestSnatchAttemptNumber {}", compare);
        if (compare != 0) return compare; // earlier best attempt wins

        compare = comparePreviousAttempts(lifter1.getBestSnatchAttemptNumber(), false, lifter1, lifter2);
        if (trace) logger.warn("comparePreviousAttempts {}", compare);
        if (compare != 0) return compare; // compare attempted weights (prior to
                                          // best attempt), smaller first

        // The referee examination example shows a case where the lifter in the
        // earlier group is not
        // given the ranking.
        // compare = compareGroup(lifter1, lifter2);
        // if (trace) logger.warn("compareGroup {}",compare);
        // if (compare != 0) return compare; // if split groups, smallest group
        // wins -- lifted earlier

        compare = compareLotNumber(lifter1, lifter2);
        if (trace) logger.warn("compareLotNumber {}", compare);
        if (compare != 0) return compare; // if equality within a group,
                                          // smallest lot number wins

        return compare;
    }

    public int compareCleanJerkResultOrder(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        if (WinningOrderComparator.useRegistrationCategory) {
            compare = compareRegistrationCategory(lifter1, lifter2);
        } else {
            compare = compareCategory(lifter1, lifter2);
        }
        if (compare != 0) return compare;

        compare = compareBestCleanJerk(lifter1, lifter2);
        if (compare != 0) return -compare; // smaller is less good

        compare = compareBodyWeight(lifter1, lifter2);
        if (compare != 0) return compare; // smaller lifter wins

        compare = compareBestCleanJerkAttemptNumber(lifter1, lifter2);
        if (compare != 0) return compare; // earlier best attempt wins

        compare = comparePreviousAttempts(lifter1.getBestCleanJerkAttemptNumber(), true, lifter1, lifter2);
        if (compare != 0) return compare; // compare attempted weights (prior to
                                          // best attempt), smaller first

        // The referee examination example shows a case where the lifter in the
        // earlier group is not
        // given the ranking.
        // compare = compareGroup(lifter1, lifter2);
        // if (compare != 0) return compare; // if split groups, smallest group
        // wins -- lifted earlier

        compare = compareLotNumber(lifter1, lifter2);
        if (compare != 0) return compare; // if equality within a group,
                                          // smallest lot number wins

        return compare;
    }

    /**
     * Determine who ranks first. If the body weights are the same, the lifter
     * who reached total first is ranked first.
     * 
     * @param lifter1
     * @param lifter2
     * @return
     */
    public int compareSinclairResultOrder(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        compare = compareSinclair(lifter1, lifter2);
        if (compare != 0) return compare;

        compare = compareBodyWeight(lifter1, lifter2);
        if (compare != 0) return compare; // smaller lifter wins

        compare = compareBestCleanJerk(lifter1, lifter2);
        if (compare != 0) return compare; // smallest clean and jerk wins (i.e.
                                          // best snatch wins !)

        compare = compareBestCleanJerkAttemptNumber(lifter1, lifter2);
        if (compare != 0) return compare; // earlier best attempt wins

        // note that when comparing total, we do NOT consider snatch. At this
        // stage, both lifters have
        // done the same weight at the same attempt. We are trying to determine
        // who did the attempt first.
        // So if the best attempt was the first one, we must NOT consider snatch
        // results when doing this determination
        compare = comparePreviousAttempts(lifter1.getBestResultAttemptNumber(), true, lifter1, lifter2);
        if (compare != 0) return compare; // compare attempted weights (prior to
                                          // best attempt), smaller first

        // The IWF referee examination example shows a case where the lifter in
        // the earlier group is not
        // given the ranking; according to the answers, lot number alone is
        // used.
        // compare = compareGroup(lifter1, lifter2);
        // if (compare != 0) return compare; // if split groups, smallest group
        // wins -- lifted earlier

        compare = compareLotNumber(lifter1, lifter2);
        if (compare != 0) return compare; // if equality within a group,
                                          // smallest lot number wins

        return compare;
    }
}
