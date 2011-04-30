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

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.Lifter;

/**
 * This comparator is used for the standard display board. It returns the same
 * order throughout the competition.
 * 
 * @author jflamy
 * 
 */
public class DisplayOrderComparator extends AbstractLifterComparator implements Comparator<Lifter> {

    @Override
    public int compare(Lifter lifter1, Lifter lifter2) {
        int compare = 0;

        if (Competition.isMasters()) {
            compare = compareAgeGroup(lifter1, lifter2);
            if (compare != 0) return -compare;
        }

        if (WinningOrderComparator.useRegistrationCategory) {
            compare = compareRegistrationCategory(lifter1, lifter2);
        } else {
            compare = compareCategory(lifter1, lifter2);
        }
        if (compare != 0) return compare;

        compare = compareLotNumber(lifter1, lifter2);
        if (compare != 0) return compare;

        compare = compareLastName(lifter1, lifter2);
        if (compare != 0) return compare;

        compare = compareFirstName(lifter1, lifter2);
        if (compare != 0) return compare;

        return compare;
    }

}
