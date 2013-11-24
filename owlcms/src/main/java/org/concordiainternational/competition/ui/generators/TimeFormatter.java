/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.generators;

public class TimeFormatter {

    public static String formatAsSeconds(Integer remainingMilliseconds) {
        if (remainingMilliseconds == null)
            return "";
        if (remainingMilliseconds < 0) {
            remainingMilliseconds = 0;
        }
        int iSecs = getSeconds(remainingMilliseconds);
        int iMins = (iSecs / 60);
        int rSecs = (iSecs % 60);

        return String.format("%1$d:%2$02d", iMins, rSecs); //$NON-NLS-1$

    }

    /**
     * Compute the number of seconds left. Note that we go up to the next integer to make sure that when the display shows 2:00, 1:30, 0:30
     * and 0:00 for the first time that is the exact time left. If the time left is 0:30.4, we want the clock to say 0:31, not 0:30.
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
