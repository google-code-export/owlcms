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

import java.util.Date;
import java.util.MissingResourceException;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.components.CategorySelect;
import org.concordiainternational.competition.ui.components.ISO8601DateField;
import org.concordiainternational.competition.ui.components.PlatformSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

public class CommonFieldFactory extends DefaultFieldFactory {

    private static final long serialVersionUID = 8789528655171127108L;
    
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(CommonFieldFactory.class);

    private Application app;

    public CommonFieldFactory(HbnSessionManager app) {
        this.app = (Application) app;
    }


    
    /* (non-Javadoc)
     * @see com.vaadin.ui.DefaultFieldFactory#createField(com.vaadin.data.Item, java.lang.Object, com.vaadin.ui.Component)
     */
    @Override
	public Field createField(Item item, Object propertyId, Component uiContext) {
        Class<?> type = item.getItemProperty(propertyId).getType();
        Field field = createFieldByPropertyType(type);
        
        return adjustField(propertyId, uiContext, field);
	}


	/* (non-Javadoc)
	 * @see com.vaadin.ui.DefaultFieldFactory#createField(com.vaadin.data.Container, java.lang.Object, java.lang.Object, com.vaadin.ui.Component)
	 */
	@Override
    public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) { 	
        Property containerProperty = container.getContainerProperty(itemId, propertyId);
        Class<?> type = containerProperty.getType();
        Field field = createFieldByPropertyType(type);
        
        return adjustField(propertyId, uiContext, field);
    }

	/**
	 * Adjust formatting and caption for the field.
	 * @param propertyId
	 * @param uiContext
	 * @param f
	 * @return
	 */
	private Field adjustField(Object propertyId, Component uiContext, Field f) {
		try {
			String caption = Messages.getStringWithException("FieldName." + propertyId,
					CompetitionApplication.getCurrentLocale());
			f.setCaption(caption);
		} catch (MissingResourceException e) {
			f.setCaption(createCaptionByPropertyId(propertyId));
		}
		
        ((AbstractField) f).setImmediate(true);

        final String propertyIdString = (String) propertyId;
        if (propertyIdString.endsWith("Weight")) { //$NON-NLS-1$
            return checkFloat(f);
        }

        if (propertyIdString.contains("snatch") //$NON-NLS-1$
            || propertyIdString.contains("cleanJerk")) { //$NON-NLS-1$
            return checkInteger(f);
        }

        if (propertyIdString.contains("Time") && (f instanceof DateField)) { //$NON-NLS-1$
            return adjustDateHourField((DateField) f);
        }
        
        if (propertyIdString.contains("Hour") && (f instanceof DateField)) { //$NON-NLS-1$
            Field adjustedHourField = adjustDateHourField((DateField) f);
			return adjustedHourField;
        }
        
        if (propertyIdString.contains("Date") && (f instanceof DateField)) { //$NON-NLS-1$
            return adjustDateField((DateField) f);
        }
        
        if (propertyIdString.equals("categories")) { //$NON-NLS-1$
        	String caption = f.getCaption();
        	f = new CategorySelect();
        	f.setCaption(caption);
        	((CategorySelect) f).setMultiSelect(true);
        	return f;
        }
        
        if (propertyIdString.equals("category")) { //$NON-NLS-1$
        	String caption = f.getCaption();
        	f = new CategorySelect();
        	f.setCaption(caption);
        	((CategorySelect) f).setMultiSelect(false);
        	return f;
        }
        
        if (propertyIdString.equals("platform")) { //$NON-NLS-1$
        	String caption = f.getCaption();
        	f = new PlatformSelect();
        	f.setCaption(caption);
        	((PlatformSelect) f).setMultiSelect(false);
        	return f;
        }

        if (propertyIdString.contains("competition")) { //$NON-NLS-1$
        	f.setWidth("25em");
            return f;
        }
        
        if (propertyIdString.contains("Name")) { //$NON-NLS-1$
        	f.setWidth("15em");
            return f;
        }
        
        if (f instanceof TextField && (uiContext instanceof Form)) {
            ((TextField) f).setWidth("25em"); //$NON-NLS-1$
        }

        if (f instanceof TextField && (uiContext instanceof Table)) {
            ((TextField) f).setWidth("3em"); //$NON-NLS-1$
        }
           
        return f;
	}
    
    /**
     * Override the default.
     * @param type
     * @return
     */
    public static Field createFieldByPropertyType(Class<?> type) {
    	//logger.debug("creating {}",type);
        // Null typed properties can not be edited
        if (type == null) {
            return null;
        }

        // Date field
        if (Date.class.isAssignableFrom(type)) {
            final DateField df = new ISO8601DateField();
            df.setResolution(DateField.RESOLUTION_DAY);
            return df;
        }

        // Boolean field
        if (Boolean.class.isAssignableFrom(type)) {
            return new CheckBox();
        }
               
        if (Category.class.isAssignableFrom(type)) {
        	return new CategorySelect();
        }
        
        if (Platform.class.isAssignableFrom(type)) {
        	return new PlatformSelect();
        }

        return new TextField();
    }

    
    private Field adjustDateField(DateField f) {
        f.setDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
        f.setResolution(DateField.RESOLUTION_DAY);
        return f;
    }
    
    private Field adjustDateHourField(DateField f) {
        f.setDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$
        f.setResolution(DateField.RESOLUTION_MIN);
        return f;
    }
    
    @SuppressWarnings("unused")
	private InlineDateField adjustHourField(DateField f) {
    	String caption = f.getCaption();
    	f = new InlineDateField();
    	f.setCaption(caption);
        f.setDateFormat("HH:mm"); //$NON-NLS-1$
        f.setResolution(DateField.RESOLUTION_MIN);
        return (InlineDateField) f;
    }

    /**
     * @param f
     * @return
     */
    private Field checkFloat(final Field f) {
        f.setWidth("4em"); //$NON-NLS-1$
        f.addValidator(new DoubleValidator(Messages.getString("CommonFieldFactory.badNumberFormat", app.getLocale())) //$NON-NLS-1$
                // new Validator() {
                // private static final long serialVersionUID =
                // -4073378031354132670L;
                //
                // public boolean isValid(Object value) {
                // try {
                // System.err.println("validator called");
                // Float.parseFloat((String) value);
                // return true;
                // } catch (Exception e) {
                //					f.getWindow().showNotification(Messages.getString("CommonFieldFactory.badNumberFormat",app.getLocale())); //$NON-NLS-1$
                // f.setValue(0);
                // return false;
                // }
                // }
                //
                // public void validate(Object value) throws
                // InvalidValueException {
                // System.err.println("validate called");
                //				if (!isValid(value)) throw new InvalidValueException(Messages.getString("CommonFieldFactory.badNumberFormat",app.getLocale())); //$NON-NLS-1$
                // }
                // }
                );
        return f;
    }

    /**
     * @param f
     * @return
     */
    private Field checkInteger(final Field f) {
        f.setWidth("4em"); //$NON-NLS-1$
        f.addValidator(new Validator() {
            private static final long serialVersionUID = -4073378031354132670L;

            @Override
			public boolean isValid(Object value) {
                try {
                    Integer.parseInt((String) value);
                    return true;
                } catch (Exception e) {
                    f.getWindow().showNotification(
                        Messages.getString("CommonFieldFactory.badNumberFormat", app.getLocale())); //$NON-NLS-1$
                    f.setValue(0);
                    return false;
                }
            }

            @Override
			public void validate(Object value) throws InvalidValueException {
                if (!isValid(value))
                    throw new InvalidValueException(Messages.getString(
                        "CommonFieldFactory.badNumberFormat", app.getLocale())); //$NON-NLS-1$
            }
        });
        return f;
    }

}