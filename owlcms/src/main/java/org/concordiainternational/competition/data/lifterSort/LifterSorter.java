/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data.lifterSort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.persistence.Entity;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.Lifter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since
 * @author jflamy
 */
@Entity
public class LifterSorter implements Serializable {

    private static final long serialVersionUID = -3507146241019771820L;
    private static final Logger logger = LoggerFactory.getLogger(LifterSorter.class);

    public enum Ranking {
        SNATCH, CLEANJERK, TOTAL, COMBINED, SINCLAIR, CUSTOM
    }

    /**
     * Sort lifters according to official rules, creating a new list.
     * 
     * @see #liftingOrder(List)
     * @return lifters, ordered according to their lifting order
     */
    static public List<Lifter> liftingOrderCopy(List<Lifter> toBeSorted) {
        List<Lifter> sorted = new ArrayList<Lifter>(toBeSorted);

        liftingOrder(sorted);
        return sorted;
    }

    /**
     * Sort lifters according to official rules.
     * <p>
     * <li>Lowest weight goes first</li>
     * <li>At same weight, lower attempt goes first</li>
     * <li>At same weight and same attempt, whoever lifted first goes first</li>
     * <li>At first attempt of each lift, lowest lot number goes first if same
     * weight is requested</li>
     * </p>
     */
    static public void liftingOrder(List<Lifter> toBeSorted) {
        Collections.sort(toBeSorted, new LiftOrderComparator());
        int liftOrder = 1;
        for (Lifter curLifter : toBeSorted) {
            curLifter.setLiftOrderRank(liftOrder++);
        }
    }

    /**
     * Sort lifters according to official rules (in place) <tableToolbar> <li>by
     * category</li> <li>by lot number</li> </tableToolbar>
     */
    static public void displayOrder(List<Lifter> toBeSorted) {
        Collections.sort(toBeSorted, new DisplayOrderComparator());
    }

    /**
     * Sort lifters according to official rules, creating a new list.
     * 
     * @see #liftingOrder(List)
     * @return lifters, ordered according to their standard order
     */
    static public List<Lifter> displayOrderCopy(List<Lifter> toBeSorted) {
        List<Lifter> sorted = new ArrayList<Lifter>(toBeSorted);
        displayOrder(sorted);
        return sorted;
    }

    /**
     * Sort lifters according to official rules (in place) for the technical
     * meeting <tableToolbar> <li>by registration category</li> <li>by lot
     * number</li> </tableToolbar>
     */
    static public void registrationOrder(List<Lifter> toBeSorted) {
        Collections.sort(toBeSorted, new RegistrationOrderComparator());
    }

    /**
     * Sort lifters according to official rules, creating a new list.
     * 
     * @see #liftingOrder(List)
     * @return lifters, ordered according to their standard order for the
     *         technical meeting
     */
    static public List<Lifter> registrationOrderCopy(List<Lifter> toBeSorted) {
        List<Lifter> sorted = new ArrayList<Lifter>(toBeSorted);
        registrationOrder(sorted);
        return sorted;
    }

    /**
     * Sort lifters according to official rules (in place) for the technical
     * meeting <tableToolbar> <li>by registration category</li> <li>by lot
     * number</li> </tableToolbar>
     */
    static public void weighInOrder(List<Lifter> toBeSorted) {
        Collections.sort(toBeSorted, new WeighInOrderComparator());
    }

    /**
     * Sort lifters according to official rules, creating a new list.
     * 
     * @see #liftingOrder(List)
     * @return lifters, ordered according to their standard order for the
     *         technical meeting
     */
    static public List<Lifter> weighInOrderCopy(List<Lifter> toBeSorted) {
        List<Lifter> sorted = new ArrayList<Lifter>(toBeSorted);
        weighInOrder(sorted);
        return sorted;
    }

    /**
     * Sort lifters according to winning order, creating a new list.
     * 
     * @see #liftingOrder(List)
     * @return lifters, ordered according to their category and totalRank order
     */
    static public List<Lifter> resultsOrderCopy(List<Lifter> toBeSorted, Ranking rankingType) {
        List<Lifter> sorted = new ArrayList<Lifter>(toBeSorted);
        resultsOrder(sorted, rankingType);
        return sorted;
    }

    /**
     * Sort lifters according to winning order.
     */
    static public void resultsOrder(List<Lifter> toBeSorted, Ranking rankingType) {
        Collections.sort(toBeSorted, new WinningOrderComparator(rankingType));
        int liftOrder = 1;
        for (Lifter curLifter : toBeSorted) {
            curLifter.setResultOrderRank(liftOrder++, rankingType);
        }
    }

    /**
     * @param lifters
     * @param sinclair
     */
    @SuppressWarnings("unused")
	private void teamPointsOrder(List<Lifter> toBeSorted, Ranking rankingType) {
        Collections.sort(toBeSorted, new TeamPointsOrderComparator(rankingType));
    }

    /**
     * @param lifters
     * @param combined
     */
    @SuppressWarnings("unused")
	private void combinedPointsOrder(List<Lifter> toBeSorted, Ranking rankingType) {
        Collections.sort(toBeSorted, new CombinedPointsOrderComparator(rankingType));
    }

    /**
     * Assign lot numbers at random.
     * 
     * @param toBeSorted
     */
    static public List<Lifter> drawLots(List<Lifter> toBeShuffled) {
        List<Lifter> shuffled = new ArrayList<Lifter>(toBeShuffled);
        Collections.shuffle(shuffled, new Random());
        assignLotNumbers(shuffled);
        return shuffled;
    }

    /**
     * Assign lot numbers, sequentially. Normally called by
     * {@link #drawLots(List)}.
     * 
     * @param shuffledList
     */
    static public void assignLotNumbers(List<Lifter> shuffledList) {
        int lotNumber = 1;
        for (Lifter curLifter : shuffledList) {
            curLifter.setLotNumber(lotNumber++);
        }
    }

    /**
     * Sets the current lifter as such (setCurrentLifter(true)), the others to
     * false
     * 
     * @param lifters
     *            Assumed to be already sorted in lifting order.
     */
    static public Lifter markCurrentLifter(List<Lifter> lifters) {
        if (!lifters.isEmpty()) {
            final Lifter firstLifter = lifters.get(0);
            firstLifter.setAsCurrentLifter(firstLifter.getAttemptsDone() < 6);
            for (Lifter lifter : lifters) {
                if (lifter != firstLifter) {
                    lifter.setAsCurrentLifter(false);
                }
                lifter.resetForcedAsCurrent();
            }
            return firstLifter;
        } else {
            return null;
        }
    }

    /**
     * Compute the number of lifts already done. During snatch, exclude
     * 
     * @param lifters
     *            Assumed to be already sorted in lifting order.
     */
    static public int countLiftsDone(List<Lifter> lifters) {
        if (!lifters.isEmpty()) {
            int totalSnatch = 0;
            int totalCJ = 0;
            boolean cJHasStarted = false;
            for (Lifter lifter : lifters) {
                totalSnatch += lifter.getSnatchAttemptsDone();
                totalCJ += lifter.getCleanJerkAttemptsDone();
                if (lifter.getCleanJerkTotal() > 0) {
                    cJHasStarted = true;
                }
            }
            if (cJHasStarted || totalSnatch >= lifters.size() * 3) {
                return totalCJ;
            } else {
                return totalSnatch;
            }
        } else {
            return 0;
        }
    }

    /**
     * Sort lifters according to who lifted last, creating a new list.
     * 
     * @see #liftTimeOrder(List)
     * @return lifters, ordered according to their lifting order
     * @param toBeSorted
     */
    static public List<Lifter> LiftTimeOrderCopy(List<Lifter> toBeSorted) {
        List<Lifter> sorted = new ArrayList<Lifter>(toBeSorted);
        liftTimeOrder(sorted);
        return sorted;
    }

    /**
     * Sort lifters according to who lifted last.
     */
    static public void liftTimeOrder(List<Lifter> toBeSorted) {
        Collections.sort(toBeSorted, new LiftTimeStampComparator());
    }

    /**
     * Sort lifters by team, gender and totalRank so team totals can be computed
     * 
     * @param lifters
     * @param rankingType
     *            what type of lift or total is being ranked
     * @return
     */
    public static List<Lifter> teamRankingOrderCopy(List<Lifter> toBeSorted, Ranking rankingType) {
        List<Lifter> sorted = new ArrayList<Lifter>(toBeSorted);
        teamRankingOrder(sorted, rankingType);
        return sorted;
    }

    /**
     * 
     * Sort lifters by team, gender and totalRank so team totals can be assigned
     * 
     * @param lifters
     * @return
     */
    static public void teamRankingOrder(List<Lifter> toBeSorted, Ranking rankingType) {
        Collections.sort(toBeSorted, new TeamRankingComparator(rankingType));
    }

    /**
     * Check that lifter is one of the howMany previous lifters. The list of
     * lifters is assumed to have been sorted with {@link #liftTimeOrderCopy}
     * 
     * @see #liftingOrder(List)
     * @return true if lifter is found and meets criterion.
     * @param toBeSorted
     */
    static public boolean isRecentLifter(Lifter lifter, List<Lifter> sortedLifters, int howMany) {
        int rank = sortedLifters.indexOf(lifter);
        if (rank >= 0 && rank <= howMany - 1) return true;
        return false;
    }

    /**
     * Assign medals, sequentially.
     * 
     * @param sortedList
     */
    static public void assignMedals(List<Lifter> sortedList) {
        Category prevCategory = null;
        Integer prevAgeGroup = null;
        Integer curAgeGroup = null;

        int rank = 1;
        for (Lifter curLifter : sortedList) {
            Category curCategory = null;
            if (WinningOrderComparator.useRegistrationCategory) {
                curCategory = curLifter.getRegistrationCategory();
            } else {
                curCategory = curLifter.getCategory();
            }
            if (Competition.isMasters()) {
                curAgeGroup = curLifter.getAgeGroup();
            }

            if (!equals(curCategory, prevCategory) || !equals(curAgeGroup, prevAgeGroup)) {
                // category boundary has been crossed
                rank = 1;
            }

            if (curLifter.isInvited()) {
                logger.trace("lifter {}  totalRank={} total={}",
                		new Object[] { curLifter, -1, curLifter.getTotal() }); //$NON-NLS-1$
                curLifter.setRank(-1);
            } else if (rank <= 3 && curLifter.getTotal() > 0) {
                logger.trace("lifter {}  totalRank={} total={}",
                		new Object[] { curLifter, rank, curLifter.getTotal() }); //$NON-NLS-1$
                curLifter.setRank(rank);
                rank++;
            } else {
                logger.trace("lifter {}  totalRank={} total={}",
                		new Object[] { curLifter, 0, curLifter.getTotal() }); //$NON-NLS-1$
                curLifter.setRank(0);
                rank++;
            }
            prevCategory = curCategory;
            prevAgeGroup = curAgeGroup;
        }
    }

    /**
     * Assign ranks, sequentially.
     * 
     * @param sortedList
     */
    public void assignCategoryRanks(List<Lifter> sortedList, Ranking rankingType) {
        Category prevCategory = null;
        Integer prevAgeGroup = null;
        Integer curAgeGroup = null;

        int rank = 1;
        for (Lifter curLifter : sortedList) {
            Category curCategory = null;
            if (WinningOrderComparator.useRegistrationCategory || rankingType == Ranking.CUSTOM) {
                curCategory = curLifter.getRegistrationCategory();
            } else {
                curCategory = curLifter.getCategory();
            }
            if (Competition.isMasters()) {
                curAgeGroup = curLifter.getAgeGroup();
            }
            if (!equals(curCategory, prevCategory) || !equals(curAgeGroup, prevAgeGroup)) {
                // category boundary has been crossed
                rank = 1;
            }

            if (curLifter.isInvited() || !curLifter.getTeamMember()) {
                logger.trace("not counted {}  {}Rank={} total={} {}",
                		new Object[] { curLifter, rankingType, -1, curLifter.getTotal(), curLifter.isInvited() }); //$NON-NLS-1$
                setRank(curLifter, -1, rankingType);
                setPoints(curLifter, 0, rankingType);
            } else {
                // if (curLifter.getTeamMember()) {
                // setTeamRank(curLifter, 0, rankingType);
                // }
                final double rankingTotal = getRankingTotal(curLifter, rankingType);
                if (rankingTotal > 0) {
                    setRank(curLifter, rank, rankingType);
                    logger.trace("lifter {}  {}rank={} total={}",
                    		new Object[] { curLifter, rankingType, getRank(curLifter, rankingType), rankingTotal }); //$NON-NLS-1$
                    rank++;
                } else {
                    logger.trace("lifter {}  {}rank={} total={}",
                    		new Object[] { curLifter, rankingType, 0, rankingTotal }); //$NON-NLS-1$
                    setRank(curLifter, 0, rankingType);
                    rank++;
                }
                // final float points = computePoints(curLifter,rankingType);
                // setPoints(curLifter,points,rankingType);

            }
            prevCategory = curCategory;
            prevAgeGroup = curAgeGroup;
        }
    }

    /**
     * Assign ranks, sequentially.
     * 
     * @param sortedList
     */
    public void assignSinclairRanksAndPoints(List<Lifter> sortedList, Ranking rankingType) {
        String prevGender = null;
        // String prevAgeGroup = null;
        int rank = 1;
        for (Lifter curLifter : sortedList) {
            final String curGender = curLifter.getGender();
            // final Integer curAgeGroup = curLifter.getAgeGroup();
            if (!equals(curGender, prevGender)
            // || !equals(curAgeGroup,prevAgeGroup)
            ) {
                // category boundary has been crossed
                rank = 1;
            }

            if (curLifter.isInvited() || !curLifter.getTeamMember()) {
                logger.trace("invited {}  {}rank={} total={} {}",
                		new Object[] { curLifter, rankingType, -1, curLifter.getTotal(), curLifter.isInvited() }); //$NON-NLS-1$
                setRank(curLifter, -1, rankingType);
                setPoints(curLifter, 0, rankingType);
            } else {
                setTeamRank(curLifter, 0, rankingType);
                final double rankingTotal = getRankingTotal(curLifter, rankingType);
                if (rankingTotal > 0) {
                	setRank(curLifter, rank, rankingType);
                    logger.trace("lifter {}  {}rank={} {}={} total={}",
                    		new Object[] { curLifter, rankingType, rank, rankingTotal }); //$NON-NLS-1$
                    rank++;
                } else {
                    logger.trace("lifter {}  {}rank={} total={}",
                    		new Object[] { curLifter, rankingType, 0, rankingTotal }); //$NON-NLS-1$
                    setRank(curLifter, 0, rankingType);
                    rank++;
                }
                final float points = computePoints(curLifter, rankingType);
                setPoints(curLifter, points, rankingType);
            }
            prevGender = curGender;
        }
    }

    /**
     * @param curLifter
     * @param i
     * @param rankingType
     */
    public void setRank(Lifter curLifter, int i, Ranking rankingType) {
        switch (rankingType) {
        case SNATCH:
            curLifter.setSnatchRank(i);
            break;
        case CLEANJERK:
            curLifter.setCleanJerkRank(i);
            break;
        case TOTAL:
            curLifter.setTotalRank(i);
            break;
        case SINCLAIR:
            curLifter.setSinclairRank(i);
            break;
        case CUSTOM:
            curLifter.setCustomRank(i);
            break;
        }
    }

    /**
     * Assign ranks, sequentially.
     * 
     * @param sortedList
     */
    public void assignRanksWithinTeam(List<Lifter> sortedList, Ranking rankingType) {
        String prevTeam = null;
        // String prevAgeGroup = null;
        int rank = 1;
        for (Lifter curLifter : sortedList) {
            final String curTeam = curLifter.getClub() + "_" + curLifter.getGender();
            // final Integer curAgeGroup = curLifter.getAgeGroup();
            if (!equals(curTeam, prevTeam)
            // || !equals(curAgeGroup,prevAgeGroup)
            ) {
                // category boundary has been crossed
                rank = 1;
            }

            if (curLifter.isInvited() || !curLifter.getTeamMember()) {
                setTeamRank(curLifter, -1, rankingType);
            } else {
                if (getRankingTotal(curLifter, rankingType) > 0) {
                    setTeamRank(curLifter, rank, rankingType);
                    rank++;
                } else {
                    setTeamRank(curLifter, 0, rankingType);
                    rank++;
                }
            }
            prevTeam = curTeam;
        }
    }

    /**
     * @param curLifter
     * @param points
     * @param rankingType
     */
    private void setPoints(Lifter curLifter, float points, Ranking rankingType) {
        logger.trace(curLifter + " " + rankingType + " points=" + points);
        switch (rankingType) {
        case SNATCH:
        	curLifter.setSnatchPoints(points);
        	break;
        case CLEANJERK:
        	curLifter.setCleanJerkPoints(points);
        	break;
        case TOTAL:
        	curLifter.setTotalPoints(points);
        	break;
        case CUSTOM:
        	curLifter.setCustomPoints(points); 
        	break;
        case COMBINED:
        	return; // computed
        }
    }

    /**
     * @param curLifter
     * @param rankingType
     * @return
     */
    private float computePoints(Lifter curLifter, Ranking rankingType) {
        switch (rankingType) {
        case SNATCH:
            return pointsFormula(curLifter.getSnatchRank(), curLifter);
        case CLEANJERK:
            return pointsFormula(curLifter.getCleanJerkRank(), curLifter);
        case TOTAL:
            return pointsFormula(curLifter.getTotalRank(), curLifter);
        case CUSTOM:
            return pointsFormula(curLifter.getCustomRank(), curLifter);            
        case COMBINED:
            return pointsFormula(curLifter.getSnatchRank(), curLifter)
                + pointsFormula(curLifter.getCleanJerkRank(), curLifter)
                + pointsFormula(curLifter.getTotalRank(), curLifter);
        }
        return 0;
    }

    /**
     * @param rank
     * @param curLifter
     * @return
     */
    private float pointsFormula(Integer rank, Lifter curLifter) {
        if (rank == null || rank <= 0) return 0;
        if (rank == 1) return 28;
        if (rank == 2) return 25;
        return 26 - rank;
    }

    /**
     * @param curLifter
     * @param i
     * @param rankingType
     */
    public static void setTeamRank(Lifter curLifter, int i, Ranking rankingType) {
        switch (rankingType) {
        case SNATCH:
            curLifter.setTeamSnatchRank(i);
            break;
        case CLEANJERK:
            curLifter.setTeamCleanJerkRank(i);
            break;
        case TOTAL:
            curLifter.setTeamTotalRank(i);
            break;
        case SINCLAIR:
            curLifter.setTeamSinclairRank(i);
            break;
        case COMBINED:
            return; // there is no combined rank
        }
    }

    /**
     * @param curLifter
     * @param rankingType
     * @return
     */
    public Integer getRank(Lifter curLifter, Ranking rankingType) {
        switch (rankingType) {
        case SNATCH:
            return curLifter.getSnatchRank();
        case CLEANJERK:
            return curLifter.getCleanJerkRank();
        case SINCLAIR:
            return curLifter.getSinclairRank();
        case TOTAL:
            return curLifter.getRank();
        case CUSTOM:
            return curLifter.getCustomRank();            
        }
        return 0;
    }

    /**
     * @param curLifter
     * @param rankingType
     * @return
     */
    private static double getRankingTotal(Lifter curLifter, Ranking rankingType) {
        switch (rankingType) {
        case SNATCH:
            return curLifter.getBestSnatch();
        case CLEANJERK:
            return curLifter.getBestCleanJerk();
        case TOTAL:
            return curLifter.getTotal();
        case SINCLAIR:
            return curLifter.getSinclair();
        case CUSTOM:
            return curLifter.getCustomScore();
        case COMBINED:
            return 0D; // no such thing
        }
        return 0D;
    }

    static private boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return false; // o1 is null but not o2
    }

//    public Collection<Team> fullResults(List<Lifter> lifters) {
//        resultsOrder(lifters, Ranking.SNATCH);
//        assignCategoryRanksAndPoints(lifters, Ranking.SNATCH);
//        teamPointsOrder(lifters, Ranking.SNATCH);
//        assignRanksWithinTeam(lifters, Ranking.SNATCH);
//
//        resultsOrder(lifters, Ranking.CLEANJERK);
//        assignCategoryRanksAndPoints(lifters, Ranking.CLEANJERK);
//        teamPointsOrder(lifters, Ranking.CLEANJERK);
//        assignRanksWithinTeam(lifters, Ranking.CLEANJERK);
//
//        resultsOrder(lifters, Ranking.TOTAL);
//        assignCategoryRanksAndPoints(lifters, Ranking.TOTAL);
//        teamPointsOrder(lifters, Ranking.TOTAL);
//        assignRanksWithinTeam(lifters, Ranking.TOTAL);
//
//        combinedPointsOrder(lifters, Ranking.COMBINED);
//        assignCategoryRanksAndPoints(lifters, Ranking.COMBINED);
//        teamPointsOrder(lifters, Ranking.COMBINED);
//        assignRanksWithinTeam(lifters, Ranking.COMBINED);
//
//        resultsOrder(lifters, Ranking.SINCLAIR);
//        assignCategoryRanksAndPoints(lifters, Ranking.SINCLAIR);
//        teamPointsOrder(lifters, Ranking.SINCLAIR);
//        assignRanksWithinTeam(lifters, Ranking.SINCLAIR);
//
//        HashSet<Team> teams = new HashSet<Team>();
//        return new TreeSet<Team>(teams);
//    }

}
