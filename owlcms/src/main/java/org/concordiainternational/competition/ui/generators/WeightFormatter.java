/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.generators;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;

/**
 * Utility class to pretty print values.
 * 
 * @author jflamy
 * 
 */
public class WeightFormatter {

    public static String formatWeight(String value) {
        value = value.trim();
        if (value.isEmpty())
            return value;
        try {
            int intValue = parseInt(value);
            return formatWeight(intValue);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * @param value
     * @param intValue
     * @return
     */
    public static String formatWeight(Integer intValue) {
        if (intValue == 0)
            return "-"; //$NON-NLS-1$
        else if (intValue > 0)
            return Integer.toString(intValue);
        else
            return "(" + (-intValue) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @param accessorName
     * @param attributeObject
     * @return
     * @throws RuntimeException
     */
    public static String htmlFormatWeight(String value) {
        value = value.trim();
        if (value == null)
            return "<td class='empty'></td>"; //$NON-NLS-1$
        if (value.isEmpty())
            return "<td class='empty'></td>"; //$NON-NLS-1$
        try {
            int intValue = parseInt(value);
            if (intValue == 0)
                return "<td class='fail'>&ndash;</td>"; //$NON-NLS-1$
            else if (intValue > 0)
                return "<td class='success'>" + value + "</td>"; //$NON-NLS-1$ //$NON-NLS-2$
            else
                return "<td class='fail'>(" + (-intValue) + ")</td>"; //$NON-NLS-1$ //$NON-NLS-2$
        } catch (NumberFormatException e) {
            return "<td class='other'>" + value + "</td>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    static String weightFormat = Messages.getString("WeightFormatter.WeightFormat", CompetitionApplication.getDefaultLocale()); //$NON-NLS-1$
    static DecimalFormat weightFormatter = new DecimalFormat(weightFormat, new DecimalFormatSymbols(Locale.US));

    /**
     * @param accessorName
     * @param attributeObject
     * @return
     * @throws RuntimeException
     */
    public static String htmlFormatBodyWeight(Double value) {
        ;
        if (value == null)
            return "<td class='narrow'></td>"; //$NON-NLS-1$
        try {
            String stringValue = formatBodyWeight(value);
            return "<td class='narrow'>" + stringValue + "</td>"; //$NON-NLS-1$ //$NON-NLS-2$
        } catch (NumberFormatException e) {
            return "<td class='other'>" + value + "</td>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @param value
     * @return
     * @throws NumberFormatException
     */
    public static String formatBodyWeight(Double value) throws NumberFormatException {
        String stringValue = weightFormatter.format(Math.abs(value));
        return stringValue;
    }

    public static int parseInt(String value) throws NumberFormatException {
        if (value.startsWith("(") && value.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
            // accounting style number
            return Integer.parseInt(value.substring(1, value.length() - 2));
        } else {
            if (value.trim().isEmpty())
                return 0;
            return Integer.parseInt(value);
        }
    }

    public static float parseFloat(String value) throws NumberFormatException {
        if (value.startsWith("(") && value.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
            // accounting style number
            return Float.parseFloat(value.substring(1, value.length() - 2));
        } else {
            if (value.trim().isEmpty())
                return 0;
            return Float.parseFloat(value);
        }
    }

    public static boolean validate(String value) {
        try {
            if (value.startsWith("(") && value.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
                // accounting style number
                Integer.parseInt(value.substring(1, value.length() - 2));
            } else {
                if (value.trim().isEmpty())
                    return true;
                Integer.parseInt(value);
            }
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static int safeParseInt(String value) {
        try {
            if (value.startsWith("(") && value.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
                // accounting style number
                return Integer.parseInt(value.substring(1, value.length() - 2));
            } else {
                if (value.trim().isEmpty())
                    return 0;
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

}
