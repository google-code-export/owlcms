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

        if (itemId.equals(table.getValue())) return null;
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
                if (lifter.isCurrentLifter()) return "current"; //$NON-NLS-1$
            }
        }
        return null;
    }
}
