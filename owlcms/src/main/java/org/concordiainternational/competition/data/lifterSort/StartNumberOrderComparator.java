/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data.lifterSort;

import java.util.Comparator;

import org.concordiainternational.competition.data.Lifter;

/**
 * This comparator is used for the technical meeting sheet. It is based on the registration category
 * 
 * @author jflamy
 * 
 */
public class StartNumberOrderComparator extends AbstractLifterComparator implements Comparator<Lifter> {

    @Override
    public int compare(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        compare = compareCategory(lifter1, lifter2);
        if (compare != 0)
            return compare;

        compare = compareLotNumber(lifter1, lifter2);
        if (compare != 0)
            return compare;

        return compare;
    }

}
