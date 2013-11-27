/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.generators;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.utils.ItemAdapter;

import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;

public class LiftCellStyleGenerator implements CellStyleGenerator {
    private static final long serialVersionUID = 698507270413719482L;
    private Table table;

    public LiftCellStyleGenerator(Table table) {
        this.table = table;
    }

    @Override
    public String getStyle(Object itemId, Object propertyId) {
        // final Item item = table.getItem(itemId);
        // final Property uiProp = item.getItemProperty(propertyIdString);
        // System.err.println("table.getValue "+table.getValue()+" itemId"+itemId);

        if (itemId.equals(table.getValue()))
            return null;
        if (propertyId == null) {
            // no propertyId, styling row
            return null;
        } else {
            final Lifter lifter = (Lifter) ItemAdapter.getObject(table, itemId);
            final String propertyIdString = (String) propertyId;
            final Property sourceProp = table.getContainerProperty(itemId, propertyIdString);
            if (propertyIdString.contains("ActualLift")) { //$NON-NLS-1$
                if (sourceProp != null) {
                    final String value = (String) sourceProp.getValue();
                    if (value != null) {
                        int intValue = WeightFormatter.safeParseInt(value);
                        if (!value.isEmpty()) {
                            if (intValue > 0) {
                                return "success"; //$NON-NLS-1$
                            } else {
                                return "fail"; //$NON-NLS-1$
                            }
                        }
                    }
                } else {
                    return null;
                }
            } else if (propertyIdString.equals("total")) { //$NON-NLS-1$
                if (sourceProp != null) {
                    // use the lifter because there is no property.
                    final Integer value = lifter.getTotal();
                    if (value != null) {
                        if (value > 0) {
                            return "total"; //$NON-NLS-1$
                        }
                    }
                }
            } else if (propertyIdString.contains("Name") || propertyIdString.contains("Requested")) { //$NON-NLS-1$ //$NON-NLS-2$
                if (lifter.isCurrentLifter())
                    return "current"; //$NON-NLS-1$
            }
        }
        return null;
    }
}
