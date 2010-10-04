package org.concordiainternational.competition.ui.components;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.ui.DateField;

@SuppressWarnings("serial")
public class FormattingDateField extends DateField {
	static DateFormat msecFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	static DateFormat secFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static DateFormat minFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	static DateFormat hourFormat = new SimpleDateFormat("yyyy-MM-dd HH");
	static DateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
	static DateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
	static DateFormat yearFormat = new SimpleDateFormat("yyyy");
	
	@Override
	public String toString() {
		Date date = (Date)this.getValue();
		if (date == null) {
			return "";
		}
		switch (getResolution()) {
        case RESOLUTION_MSEC:
        	return msecFormat.format(date);

        case RESOLUTION_SEC:
        	return secFormat.format(date);

        case RESOLUTION_MIN:
        	return minFormat.format(date);
        	
        case RESOLUTION_HOUR:
        	return secFormat.format(date);
        	
        case RESOLUTION_DAY:
        	return dayFormat.format(date);
        	
        case RESOLUTION_MONTH:
        	return monthFormat.format(date);
        	
        case RESOLUTION_YEAR:
        	return yearFormat.format(date);
        
        default:
        	return minFormat.format(date);
        }
		
    }

}
