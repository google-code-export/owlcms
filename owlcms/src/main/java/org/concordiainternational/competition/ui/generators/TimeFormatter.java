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

package org.concordiainternational.competition.ui.generators;

public class TimeFormatter {

    public static String formatAsSeconds(Integer remainingMilliseconds) {
    	if (remainingMilliseconds == null) return "";
        int iSecs = getSeconds(remainingMilliseconds);
        int iMins = (iSecs / 60);
        int rSecs = (iSecs % 60);
        // if (true || iMins > 0) {
        return String.format("%1$d:%2$02d", iMins, rSecs); //$NON-NLS-1$
        // } else {
        //			return "  "+ Integer.toString(rSecs); //$NON-NLS-1$
        // }

    }

    /**
     * Compute the number of seconds left.
     * Note that we go up to the next integer to make sure that when the display
     * shows 2:00, 1:30, 0:30 and 0:00 for the first time that is the exact time
     * left. If the time left is 0:30.4, we want the clock to say 0:31, not 0:30.
     * 
     * @param remainingMilliseconds
     * @return the number of seconds, making sure that zero means "time is up".
     */
    public static int getSeconds(int remainingMilliseconds) {
        double dSecs = (remainingMilliseconds / 1000.0D);
        long roundedSecs = Math.round(dSecs);
        int iSecs;
        double delta = dSecs - roundedSecs;
		if (Math.abs(delta) < 0.001) {
            // 4.0009 is 4, not 5, for our purposes. We do not ever want 2:01
            // because of rounding errors
            iSecs = Math.round((float) dSecs);
        } else {
            iSecs = (int) Math.ceil(dSecs);
        }
        return iSecs;
    }
}
