/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.generators;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to pretty print values (for JSP in particular).
 * 
 * @author jflamy
 * 
 */
public class TryFormatter {

    private static Logger logger = LoggerFactory.getLogger(TryFormatter.class);

    /**
     * @param accessorName
     * @param attributeObject
     * @return
     * @throws RuntimeException
     */
    public static String htmlFormatTry(List<Lifter> lifters, Lifter lifter) {
        Locale locale = Locale.CANADA_FRENCH;

        String suffix = ""; //$NON-NLS-1$
        final Lifter firstLifter = lifters.get(0);
        if (firstLifter.getAttemptsDone() < 3 && lifter.getAttemptsDone() >= 3) {
            // the current lifter is done snatch whereas the top lifter on the
            // board is still doing snatch.
            suffix = Messages.getString("TryFormatter.shortCleanJerk", locale); //$NON-NLS-1$
        }
        final int currentTry = 1 + (lifter.getAttemptsDone() >= 3 ? lifter.getCleanJerkAttemptsDone() : lifter
                .getSnatchAttemptsDone());
        if (currentTry > 3) {
            return Messages.getString("TryFormatter.Done", locale); //$NON-NLS-1$
        } else {
            String tryInfo = currentTry + suffix;
            return tryInfo;
        }
    }

    public static String htmlFormatLiftsDone(int number, Locale locale) {

        final String formatString = Messages.getString("ResultList.attemptsDone", locale);
        logger.debug("lifts done = {}  format={}", number, formatString);
        return MessageFormat.format(formatString, //$NON-NLS-1$
            number);
    }

    /**
     * @param locale
     * @param currentTry
     * @return
     */
    public static String formatTry(Lifter lifter, final Locale locale, final int currentTry) {
        String tryInfo = MessageFormat.format(Messages.getString("LifterInfo.tryNumber", locale), //$NON-NLS-1$
            currentTry, (lifter.getAttemptsDone() >= 3 ? Messages.getString("Common.shortCleanJerk", locale) //$NON-NLS-1$
                    : Messages.getString("Common.shortSnatch", locale))); //$NON-NLS-1$
        return tryInfo;
    }

}
