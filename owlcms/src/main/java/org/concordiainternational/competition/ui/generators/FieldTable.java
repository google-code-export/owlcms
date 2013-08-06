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

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;

/**
 * @author jflamy
 *
 */
@SuppressWarnings("serial")
public class FieldTable extends Table {
	Logger logger = LoggerFactory.getLogger(FieldTable.class);
	DecimalFormat threeDecimals = new DecimalFormat("0.000", new DecimalFormatSymbols(CompetitionApplication.getCurrentLocale()));
	DecimalFormat twoDecimals = new DecimalFormat("0.00", new DecimalFormatSymbols(CompetitionApplication.getCurrentLocale()));

	/* Always use a field, so that the read-only version is consistent with the editing formatting.
	 * @see com.vaadin.ui.Table#getPropertyValue(java.lang.Object, java.lang.Object, com.vaadin.data.Property)
	 */
	@Override
	protected Object getPropertyValue(Object rowId, Object colId,
			Property property) {
        TableFieldFactory tableFieldFactory = getTableFieldFactory();
		if ( tableFieldFactory != null) {
            final Field f = tableFieldFactory.createField(getContainerDataSource(),
                    rowId, colId, this);
            if (f != null) {
                f.setPropertyDataSource(property);
                if (isEditable()) {
                	return f;
                } else {
                    //if (f instanceof DateField ) {
                        Object value = f.getValue();
                        if (value != null) {
                            return f.toString();   
                        }
                    //}
                }
            }
        }
		
        return formatPropertyValue(rowId, colId, property);
	}

	@Override
	protected String formatPropertyValue(Object rowId, Object colId,
			Property property) {
        // Format by property type
        if (property.getType() == Double.class) {
        	Double value = (Double) property.getValue();
        	if (value == null) value = 0.0;
			if (((String)colId).endsWith("inclair")) {          
                return threeDecimals.format(value);
        	} else {
        		return twoDecimals.format(value);
        	}
        }
		return super.formatPropertyValue(rowId, colId, property);
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Table#getColumnAlignment(java.lang.Object)
	 */
	@Override
	public String getColumnAlignment(Object propertyId) {
//		if (this.isEditable()) {
//			return ALIGN_CENTER;
//		} else {
			if (((String)propertyId).endsWith("Name")){
				return ALIGN_LEFT;
			} else {
				return ALIGN_RIGHT;
			}
//		}
	}
	
	


}
