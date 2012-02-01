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

import java.util.Date;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractLifterComparator {
    final private static Logger logger = LoggerFactory.getLogger(AbstractLifterComparator.class);

    int compareCategory(Lifter lifter1, Lifter lifter2) {
        Category lifter1Value = lifter1.getCategory();
        Category lifter2Value = lifter2.getCategory();
        if (lifter1Value == null && lifter2Value == null) return 0;
        if (lifter1Value == null) return -1;
        if (lifter2Value == null) return 1;

        int compare = compareGender(lifter1,lifter2);
        if (compare != 0) return compare;

        Double value1 = lifter1.getCategory().getMaximumWeight();
        Double value2 = lifter2.getCategory().getMaximumWeight();
        return value1.compareTo(value2);
    }

    int compareRegistrationCategory(Lifter lifter1, Lifter lifter2) {
        Category lifter1Category = lifter1.getRegistrationCategory();
        Category lifter2Category = lifter2.getRegistrationCategory();
        if (lifter1Category == null && lifter2Category == null) return 0;
        if (lifter1Category == null) return -1;
        if (lifter2Category == null) return 1;

        int compare = compareGender(lifter1,lifter2);
        if (compare != 0) return compare;

        Double value1 = lifter1Category.getMaximumWeight();
        Double value2 = lifter2Category.getMaximumWeight();
        return value1.compareTo(value2);
    }

    int compareFirstName(Lifter lifter1, Lifter lifter2) {
        String lifter1Value = lifter1.getFirstName();
        String lifter2Value = lifter2.getFirstName();
        if (lifter1Value == null && lifter2Value == null) return 0;
        if (lifter1Value == null) return -1;
        if (lifter2Value == null) return 1;
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareLastName(Lifter lifter1, Lifter lifter2) {
        String lifter1Value = lifter1.getLastName();
        String lifter2Value = lifter2.getLastName();
        if (lifter1Value == null && lifter2Value == null) return 0;
        if (lifter1Value == null) return -1;
        if (lifter2Value == null) return 1;
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareLotNumber(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Value = lifter1.getLotNumber();
        Integer lifter2Value = lifter2.getLotNumber();
        if (lifter1Value == null && lifter2Value == null) return 0;
        if (lifter1Value == null) return -1;
        if (lifter2Value == null) return 1;
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareAgeGroup(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Value = lifter1.getAgeGroup();
        Integer lifter2Value = lifter2.getAgeGroup();
        if (lifter1Value == null && lifter2Value == null) return 0;
        if (lifter1Value == null) return -1;
        if (lifter2Value == null) return 1;
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareGroup(Lifter lifter1, Lifter lifter2) {
        CompetitionSession lifter1Group = lifter1.getCompetitionSession();
        CompetitionSession lifter2Group = lifter2.getCompetitionSession();
        if (lifter1Group == null && lifter2Group == null) return 0;
        if (lifter1Group == null) return -1;
        if (lifter2Group == null) return 1;

        String lifter1Value = lifter1Group.getName();
        String lifter2Value = lifter2Group.getName();
        if (lifter1Value == null && lifter2Value == null) return 0;
        if (lifter1Value == null) return -1;
        if (lifter2Value == null) return 1;
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareGroupWeighInTime(Lifter lifter1, Lifter lifter2) {

        CompetitionSession lifter1Group = lifter1.getCompetitionSession();
        CompetitionSession lifter2Group = lifter2.getCompetitionSession();
        if (lifter1Group == null && lifter2Group == null) return 0;
        if (lifter1Group == null) return -1;
        if (lifter2Group == null) return 1;

        Date lifter1Date = lifter1Group.getWeighInTime();
        Date lifter2Date = lifter2Group.getWeighInTime();
        if (lifter1Date == null && lifter2Date == null) return 0;
        if (lifter1Date == null) return -1;
        if (lifter2Date == null) return 1;
        int compare = lifter1Date.compareTo(lifter2Date);
        if (compare != 0) return compare;

        String lifter1String = lifter1Group.getName();
        String lifter2String = lifter2Group.getName();
        if (lifter1String == null && lifter2String == null) return 0;
        if (lifter1String == null) return -1;
        if (lifter2String == null) return 1;
        return lifter1String.compareTo(lifter2String);
    }

    /**
     * Comparer les totaux des leveurs, si ils ont termin� tous leurs essais. Le
     * leveur ayant terminé va après, de manière à ce le premier à lever soit
     * toujours toujours le premier dans la liste.
     * 
     * @param lifter1
     * @param lifter2
     * @return -1,0,1 selon comparaison
     */
    int compareFinalResults(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Done = (lifter1.getAttemptsDone() >= 6 ? 1 : 0);
        Integer lifter2Done = (lifter2.getAttemptsDone() >= 6 ? 1 : 0);

        int compare = lifter1Done.compareTo(lifter2Done);
        if (compare != 0) return compare;

        // at this point both lifters are done, or both are not done.
        if (lifter1Done == 0) {
            // both are not done
            return 0;
        } else {
            // both are done, use descending order on total
            return -compareTotal(lifter1, lifter2);
        }

    }

    int compareLiftType(Lifter lifter1, Lifter lifter2) {
        // snatch comes before clean and jerk
        Integer lifter1Value = lifter1.getAttemptsDone();
        Integer lifter2Value = lifter2.getAttemptsDone();
        if (lifter1Value < 3) {
            lifter1Value = 0;
        } else {
            lifter1Value = 1;
        }
        if (lifter2Value < 3) {
            lifter2Value = 0;
        } else {
            lifter2Value = 1;
        }
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareRequestedWeight(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Value = lifter1.getNextAttemptRequestedWeight();
        Integer lifter2Value = lifter2.getNextAttemptRequestedWeight();
        if (lifter1Value == null || lifter1Value == 0) lifter1Value = 999; // place people with no
                                                   // declared weight at the end
        if (lifter2Value == null || lifter2Value == 0) lifter2Value = 999; // place people with no
                                                   // declared weight at the end
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareAttemptsDone(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Value = lifter1.getAttemptsDone();
        Integer lifter2Value = lifter2.getAttemptsDone();
        return lifter1Value.compareTo(lifter2Value);
    }

    /**
     * Return who lifted last, for real.
     * 
     * @param lifter1
     * @param lifter2
     * @return
     */
    int comparePreviousLiftOrder(Lifter lifter1, Lifter lifter2) {
        Date lifter1Value = lifter1.getLastLiftTime();
        Date lifter2Value = lifter2.getLastLiftTime();

        final Date longAgo = new Date(0L);
        if (lifter1Value == null) lifter1Value = longAgo;
        if (lifter2Value == null) lifter2Value = longAgo;

        return lifter1Value.compareTo(lifter2Value);
    }

    /**
     * Return who lifted last, ignoring lifters who are done lifting for this
     * part of the meet.
     * 
     * @param lifter1
     * @param lifter2
     * @return
     */
    int comparePreviousLiftOrderExceptAtEnd(Lifter lifter1, Lifter lifter2) {
        Date lifter1Value = lifter1.getLastLiftTime();
        Date lifter2Value = lifter2.getLastLiftTime();

        final Date longAgo = new Date(0L);
        if (lifter1Value == null) lifter1Value = longAgo;
        if (lifter2Value == null) lifter2Value = longAgo;

        // at start of snatch and start of clean and jerk, previous lift is
        // irrelevant.
        if (lifter1.getAttemptsDone() == 3) lifter1Value = longAgo;
        if (lifter2.getAttemptsDone() == 3) lifter2Value = longAgo;

        return lifter1Value.compareTo(lifter2Value);
    }

    int compareTotal(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Value = lifter1.getTotal();
        Integer lifter2Value = lifter2.getTotal();
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareBodyWeight(Lifter lifter1, Lifter lifter2) {
        Double lifter1Value = lifter1.getBodyWeight();
        Double lifter2Value = lifter2.getBodyWeight();
        final Double notWeighed = 0D;
        if (lifter1Value == null) lifter1Value = notWeighed;
        if (lifter2Value == null) lifter2Value = notWeighed;
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareSinclair(Lifter lifter1, Lifter lifter2) {
        String gender = lifter1.getGender();
        if (gender == null) return -1;
        int compare = gender.compareTo(lifter2.getGender());
        if (compare != 0) return compare;

        Double lifter1Value = lifter1.getSinclair();
        Double lifter2Value = lifter2.getSinclair();
        final Double notWeighed = 0D;
        if (lifter1Value == null) lifter1Value = notWeighed;
        if (lifter2Value == null) lifter2Value = notWeighed;
        // bigger sinclair comes first
        return -lifter1Value.compareTo(lifter2Value);
    }

    int compareCategorySinclair(Lifter lifter1, Lifter lifter2) {
        String gender = lifter1.getGender();
        if (gender == null) return -1;
        int compare = gender.compareTo(lifter2.getGender());
        if (compare != 0) return compare;

        Double lifter1Value = lifter1.getCategorySinclair();
        Double lifter2Value = lifter2.getCategorySinclair();
        final Double notWeighed = 0D;
        if (lifter1Value == null) lifter1Value = notWeighed;
        if (lifter2Value == null) lifter2Value = notWeighed;
        // bigger sinclair comes first
        return -lifter1Value.compareTo(lifter2Value);
    }

    int compareSMM(Lifter lifter1, Lifter lifter2) {
        String gender = lifter1.getGender();
        if (gender == null) return -1;
        int compare = gender.compareTo(lifter2.getGender());
        if (compare != 0) return compare;

        Double lifter1Value = lifter1.getSMM();
        Double lifter2Value = lifter2.getSMM();
        final Double notWeighed = 0D;
        if (lifter1Value == null) lifter1Value = notWeighed;
        if (lifter2Value == null) lifter2Value = notWeighed;
        // bigger sinclair comes first
        return -lifter1Value.compareTo(lifter2Value);
    }

    int compareLastSuccessfulLiftTime(Lifter lifter1, Lifter lifter2) {
        Date lifter1Value = lifter1.getLastSuccessfulLiftTime();
        Date lifter2Value = lifter2.getLastSuccessfulLiftTime();
        // safe to compare, no nulls.
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareForcedAsFirst(Lifter lifter1, Lifter lifter2) {
        // can't be nulls, method returns primitive boolean
        Boolean lifter1Value = lifter1.getForcedAsCurrent();
        Boolean lifter2Value = lifter2.getForcedAsCurrent();

        // true.compareTo(false) returns positive (i.e. greater). We want the
        // opposite.
        final int compare = -lifter1Value.compareTo(lifter2Value);
        return compare;
    }

    int compareBestCleanJerk(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Value = lifter1.getBestCleanJerk();
        Integer lifter2Value = lifter2.getBestCleanJerk();
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareBestSnatch(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Value = lifter1.getBestSnatch();
        Integer lifter2Value = lifter2.getBestSnatch();
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareBestLiftAttemptNumber(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Value = lifter1.getBestResultAttemptNumber();
        Integer lifter2Value = lifter2.getBestResultAttemptNumber();
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareBestSnatchAttemptNumber(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Value = lifter1.getBestSnatchAttemptNumber();
        Integer lifter2Value = lifter2.getBestSnatchAttemptNumber();
        return lifter1Value.compareTo(lifter2Value);
    }

    int compareBestCleanJerkAttemptNumber(Lifter lifter1, Lifter lifter2) {
        Integer lifter1Value = lifter1.getBestCleanJerkAttemptNumber();
        Integer lifter2Value = lifter2.getBestCleanJerkAttemptNumber();
        return lifter1Value.compareTo(lifter2Value);
    }

    /**
     * Compare absolute value of attempts prior to attempt "startingFrom" Start
     * comparing attempted weights at "startingFrom". If attempted weight
     * differ, smallest attempted weight comes first. If attempted weights are
     * same, go back one attempt and keep comparing.
     * 
     * startingFrom is exclusive endingWith is inclusive, and is used to the
     * previous attempts.
     * 
     * @param startingFrom
     * @param excludeSnatch
     *            to consider only cleanAndJerk
     * @param lifter1
     * @param lifter2
     * @return
     */
    int comparePreviousAttempts(int startingFrom, boolean excludeSnatch, Lifter lifter1, Lifter lifter2) {
        int compare = 0;
        boolean trace = false;
        if (trace)
            logger.trace("starting from {}, lifter1 {}, lifter2 {}", new Object[] { startingFrom, lifter1, lifter2 });
        if (startingFrom >= 6) {
            compare = ((Integer) Math.abs(Lifter.zeroIfInvalid(lifter1.getCleanJerk3ActualLift()))).compareTo(Math
                    .abs(Lifter.zeroIfInvalid(lifter2.getCleanJerk3ActualLift())));
            if (trace) logger.trace("essai 6: {}", compare);
            if (compare != 0) return compare;
        }
        if (startingFrom >= 5) {
            compare = ((Integer) Math.abs(Lifter.zeroIfInvalid(lifter1.getCleanJerk2ActualLift()))).compareTo(Math
                    .abs(Lifter.zeroIfInvalid(lifter2.getCleanJerk2ActualLift())));
            if (trace) logger.trace("essai 5: {}", compare);
            if (compare != 0) return compare;
        }
        if (startingFrom >= 4) {
            compare = ((Integer) Math.abs(Lifter.zeroIfInvalid(lifter1.getCleanJerk1ActualLift()))).compareTo(Math
                    .abs(Lifter.zeroIfInvalid(lifter2.getCleanJerk1ActualLift())));
            if (trace) logger.trace("essai 4: {}", compare);
            if (compare != 0) return compare;
        }
        if (excludeSnatch) {
            return 0;
        }
        if (startingFrom >= 3) {
            compare = ((Integer) Math.abs(Lifter.zeroIfInvalid(lifter1.getSnatch3ActualLift()))).compareTo(Math
                    .abs(Lifter.zeroIfInvalid(lifter2.getSnatch3ActualLift())));
            if (trace) logger.trace("essai 3: {}", compare);
            if (compare != 0) return compare;
        }
        if (startingFrom >= 2) {
            compare = ((Integer) Math.abs(Lifter.zeroIfInvalid(lifter1.getSnatch2ActualLift()))).compareTo(Math
                    .abs(Lifter.zeroIfInvalid(lifter2.getSnatch2ActualLift())));
            if (trace) logger.trace("essai 2: {}", compare);
            if (compare != 0) return compare;
        }
        if (startingFrom >= 1) {
            compare = ((Integer) Math.abs(Lifter.zeroIfInvalid(lifter1.getSnatch1ActualLift()))).compareTo(Math
                    .abs(Lifter.zeroIfInvalid(lifter2.getSnatch1ActualLift())));
            if (trace) logger.trace("essai 1: {}", compare);
            if (compare != 0) return compare;
        }
        return 0;
    }

    /**
     * @param lifter1
     * @param lifter2
     * @return
     */
    protected int compareGender(Lifter lifter1, Lifter lifter2) {
        String gender1 = lifter1.getGender();
        String gender2 = lifter2.getGender();
        if (gender1 == null && gender2 == null) return 0;
        if (gender1 == null) return -1;
        if (gender2 == null) return 1;
        // "F" is smaller than "M"
        return gender1.compareTo(gender2);
    }

    /**
     * @param lifter1
     * @param lifter2
     * @return
     */
    protected int compareClub(Lifter lifter1, Lifter lifter2) {
        String club1 = lifter1.getClub();
        String club2 = lifter2.getClub();
        if (club1 == null && club2 == null) return 0;
        if (club1 == null) return -1;
        if (club2 == null) return 1;
        return lifter1.getClub().compareTo(lifter2.getClub());
    }

}
