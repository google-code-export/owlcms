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

/**
 * This comparator is used to highlight the lifters that have lifted recently,
 * and are likely to request changes to the automatic progression. It simply
 * sorts according to time stamp, if available. Else lot number is used.
 * 
 * @author jflamy
 * 
 */
public class LiftTimeStampComparator extends AbstractLifterComparator implements Comparator<Lifter> {

    public LiftTimeStampComparator() {
    }

    @Override
    public int compare(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        compare = comparePreviousLiftOrder(lifter1, lifter2);
        if (compare != 0) return -compare;

        compare = compareLotNumber(lifter1, lifter2);
        if (compare != 0) return compare;

        return compare;
    }

}
