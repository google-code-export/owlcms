package org.concordiainternational.competition.ui.generators;

import com.vaadin.data.Property;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;

/**
 * @author jflamy
 *
 */
@SuppressWarnings("serial")
public class FieldTable extends Table {

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
                	return f.toString();
                }
            }
        }
		
        return formatPropertyValue(rowId, colId, property);
	}



}
