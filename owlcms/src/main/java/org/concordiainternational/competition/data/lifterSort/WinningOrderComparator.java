/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data.lifterSort;

import java.util.Comparator;

import org.concordiainternational.competition.data.Competition;
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
public class WinningOrderComparator extends AbstractLifterComparator implements Comparator<Lifter> {

    final static Logger logger = LoggerFactory.getLogger(WinningOrderComparator.class);

    /**
     * Normally we use the computed category; but for special tournaments we
     * want to override this. For example, a tournament may feature "beginners"
     * category with special judging rules. It is then possible to assign
     * different registration categories to these persons without messing up the
     * regular competition.
     */
    public static boolean useRegistrationCategory = false;
    public static boolean useCategorySinclair = true;

    private Ranking rankingType;

    public WinningOrderComparator(Ranking rankingType) {
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
        case CUSTOM:
            return compareCustomResultOrder(lifter1, lifter2);    
        case SINCLAIR:
            if (Competition.isMasters()) {
                return compareSMMResultOrder(lifter1, lifter2);
            } else {
                if (useCategorySinclair) {
                    return compareCategorySinclairResultOrder(lifter1, lifter2);
                } else {
                    return compareSinclairResultOrder(lifter1, lifter2);
                }
            }

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

        if (Competition.isMasters()) {
            compare = compareGender(lifter1, lifter2);
            if (compare != 0) return compare;

            compare = compareAgeGroup(lifter1, lifter2);
            if (compare != 0) return -compare;
        }

        if (useRegistrationCategory) {
            compare = compareRegistrationCategory(lifter1, lifter2);
        } else {
            compare = compareCategory(lifter1, lifter2);
        }
        if (compare != 0) return compare;

        compare = compareTotal(lifter1, lifter2);
        if (compare != 0) return -compare; // we want reverse order - smaller
                                           // comes after

        return sharedCoefficientComparison(lifter1, lifter2);
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

        if (trace) logger.trace("lifter1 {};  lifter2 {}", lifter1.getFirstName(), lifter2.getFirstName());

        if (useRegistrationCategory) {
            compare = compareRegistrationCategory(lifter1, lifter2);
        } else {
            compare = compareCategory(lifter1, lifter2);
        }
        if (trace) logger.trace("compareCategory {}", compare);
        if (compare != 0) return compare;

        compare = compareBestSnatch(lifter1, lifter2);
        if (trace) logger.trace("compareBestSnatch {}", compare);
        if (compare != 0) return -compare; // smaller snatch is less good

        compare = compareBodyWeight(lifter1, lifter2);
        if (trace) logger.trace("compareBodyWeight {}", compare);
        if (compare != 0) return compare; // smaller lifter wins

        compare = compareBestSnatchAttemptNumber(lifter1, lifter2);
        if (trace) logger.trace("compareBestSnatchAttemptNumber {}", compare);
        if (compare != 0) return compare; // earlier best attempt wins

        compare = comparePreviousAttempts(lifter1.getBestSnatchAttemptNumber(), false, lifter1, lifter2);
        if (trace) logger.trace("comparePreviousAttempts {}", compare);
        if (compare != 0) return compare; // compare attempted weights (prior to
                                          // best attempt), smaller first

        // The referee examination example shows a case where the lifter in the
        // earlier group is not
        // given the ranking.
        // compare = compareGroup(lifter1, lifter2);
        // if (trace) logger.trace("compareGroup {}",compare);
        // if (compare != 0) return compare; // if split groups, smallest group
        // wins -- lifted earlier

        compare = compareLotNumber(lifter1, lifter2);
        if (trace) logger.trace("compareLotNumber {}", compare);
        if (compare != 0) return compare; // if equality within a group,
                                          // smallest lot number wins

        return compare;
    }

    public int compareCleanJerkResultOrder(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        if (useRegistrationCategory) {
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

        return sharedCoefficientComparison(lifter1, lifter2);
    }

    /**
     * Determine who ranks first. If the body weights are the same, the lifter
     * who reached total first is ranked first.
     * 
     * @param lifter1
     * @param lifter2
     * @return
     */
    public int compareCategorySinclairResultOrder(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        compare = compareCategorySinclair(lifter1, lifter2);
        if (compare != 0) return compare;

        return sharedCoefficientComparison(lifter1, lifter2);
    }

    /**
     * Determine who ranks first. If the body weights are the same, the lifter
     * who reached total first is ranked first.
     * 
     * @param lifter1
     * @param lifter2
     * @return
     */
    public int compareSMMResultOrder(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        compare = compareSMM(lifter1, lifter2);
        if (compare != 0) return compare;

        return sharedCoefficientComparison(lifter1, lifter2);
    }

    /**
     * Processing shared between all coefficient-based rankings
     * 
     * @param lifter1
     * @param lifter2
     * @return
     */
    private int sharedCoefficientComparison(Lifter lifter1, Lifter lifter2) {
        int compare;
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
    
    /**
     * Determine who ranks first. If the body weights are the same, the lifter
     * who reached total first is ranked first.
     * 
     * This variant allows judges to award a score based on a formula, with bonuses
     * or penalties, manually.  Used for the under-12 championship in Quebec.
     * 
     * @param lifter1
     * @param lifter2
     * @return
     */
    public int compareCustomResultOrder(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        if (Competition.isMasters()) {
            compare = compareGender(lifter1, lifter2);
            if (compare != 0) return compare;

            compare = compareAgeGroup(lifter1, lifter2);
            if (compare != 0) return -compare;
        }

        compare = compareRegistrationCategory(lifter1, lifter2);
        if (compare != 0) return compare;
        
        compare = compareCustomScore(lifter1, lifter2);
        if (compare != 0) return -compare; // we want reverse order - smaller comes after

        compare = compareTotal(lifter1, lifter2);
        if (compare != 0) return -compare; // we want reverse order - smaller comes after

        return sharedCoefficientComparison(lifter1, lifter2);
    }

   
}
