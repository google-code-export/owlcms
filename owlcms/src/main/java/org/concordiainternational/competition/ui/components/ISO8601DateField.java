/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.ui.DateField;

@SuppressWarnings("serial")
public class ISO8601DateField extends DateField {
    private static final String YEAR_FORMAT = "yyyy";
    private static final String MONTH_FORMAT = "yyyy-MM";
    private static final String DAY_FORMAT = "yyyy-MM-dd";
    private static final String HOUR_FORMAT = "yyyy-MM-dd HH";
    private static final String MIN_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String SEC_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String MSEC_FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";

    private SimpleDateFormat formatter = new SimpleDateFormat(MIN_FORMAT);

    @Override
    public String toString() {
        Date date = (Date) this.getValue();
        if (date == null) {
            return "";
        }
        return formatter.format(date);
    }

    @Override
    public String getDateFormat() {
        switch (getResolution()) {
        case RESOLUTION_MSEC:
            return MSEC_FORMAT;

        case RESOLUTION_SEC:
            return SEC_FORMAT;

        case RESOLUTION_MIN:
            return MIN_FORMAT;

        case RESOLUTION_HOUR:
            return HOUR_FORMAT;

        case RESOLUTION_DAY:
            return DAY_FORMAT;

        case RESOLUTION_MONTH:
            return MONTH_FORMAT;

        case RESOLUTION_YEAR:
            return YEAR_FORMAT;

        default:
            return MIN_FORMAT;
        }
    }

    @Override
    public void setResolution(int resolution) {
        super.setResolution(resolution);
        setDateFormat(this.getDateFormat());
        formatter = new SimpleDateFormat(this.getDateFormat());
    }

}
