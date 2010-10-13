package org.concordiainternational.competition.publicAddress;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Field;
import com.vaadin.ui.FieldWrapper;
import com.vaadin.ui.VerticalLayout;

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
	

    @Override
    protected Object format(Integer value) {
        // Format milliseconds as a text field
    	logger.debug("formatting {}",value);
    	return TimeFormatter.formatAsSeconds(value*1000);
    }

    @Override
    protected Integer parse(Object formattedValue) throws ConversionException {
    	//LoggerUtils.logException(logger, new Exception("trace"));
    	
        // Take a String and turn it into an Integer
    	Integer parsedValue;
    	Date parsedDate;

    	SimpleDateFormat minSecs = new SimpleDateFormat("mm:ss");
    	minSecs.setLenient(false);
		String stringValue = (String) formattedValue;
		try {
			if (stringValue.length() <= 5 ){
				parsedDate = minSecs.parse(stringValue);
				long parsedTime = parsedDate.getTime();
				parsedValue = (int) (parsedTime)/1000 - 18000;
				logger.debug("formatted value1 date={} millis={}",parsedDate,parsedValue*1000);
			} else {
				SimpleDateFormat hrMinSecs = new SimpleDateFormat("HH:mm:ss");
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
