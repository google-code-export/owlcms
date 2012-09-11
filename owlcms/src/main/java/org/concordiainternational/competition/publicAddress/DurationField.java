/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.publicAddress;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.FieldWrapper;

import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;

/**
 * Wrap an Integer such that it is displayed and read as a time duration (mm:ss).
 * @author jflamy
 *
 */
@SuppressWarnings("serial")
public class DurationField extends FieldWrapper<Integer>{

	private Logger logger = LoggerFactory.getLogger(DurationField.class);

	protected DurationField(Field wrappedField, Class<? extends Integer> propertyType) {
		super(wrappedField, propertyType);
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.addComponent(wrappedField);
        setCompositionRoot(layout);
	}
	

    /* Format the integer as a duration.
     * @see com.vaadin.ui.FieldWrapper#format(java.lang.Object)
     */
    @Override
    protected Object format(Integer value) {
        // Format milliseconds as a text field
    	logger.debug("formatting {}",value);
    	return TimeFormatter.formatAsSeconds(value*1000);
    }

    /* Parse the duration as a number of seconds.
     * @see com.vaadin.ui.FieldWrapper#parse(java.lang.Object)
     */
    @Override
    protected Integer parse(Object formattedValue) throws ConversionException {
    	//LoggerUtils.logException(logger, new Exception("trace"));
    	
        // Take a String and turn it into an Integer
    	Integer parsedValue;
    	Date parsedDate;

    	SimpleDateFormat minSecs = new SimpleDateFormat("mm:ss");
    	TimeZone gmt = TimeZone.getTimeZone("GMT");
		minSecs.setTimeZone(gmt);
    	minSecs.setLenient(false);
		String stringValue = (String) formattedValue;
		try {
			if (stringValue.length() <= 5 ){
				parsedDate = minSecs.parse(stringValue);
				// date parsing 
				long parsedTime = parsedDate.getTime();
				new Date(0);
				parsedValue = (int) (parsedTime)/1000;
				logger.debug("formatted value1 date={} millis={}",parsedDate,parsedValue*1000);
			} else {
				SimpleDateFormat hrMinSecs = new SimpleDateFormat("HH:mm:ss");
				hrMinSecs.setTimeZone(gmt);
				hrMinSecs.setLenient(false);
				parsedDate = hrMinSecs.parse(stringValue);
				long parsedTime = parsedDate.getTime();
				parsedValue = (int) (parsedTime)/1000;
				logger.debug("formatted value2 date={} millis={}",parsedDate,parsedValue*1000);
			}
		} catch (ParseException e) {
			throw new ConversionException(e);
		}
		return parsedValue;
    }


	@Override
	public String toString() {
		return (String)format(getValue());
	}

}
