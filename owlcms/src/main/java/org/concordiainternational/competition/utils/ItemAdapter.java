/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.utils;

import java.util.Locale;

import org.concordiainternational.competition.i18n.Messages;
import org.vaadin.addons.beantuplecontainer.BeanTupleItem;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Table;

public class ItemAdapter {

    /**
     * @param item
     * @return
     */
	public static Object getObject(final Item item) {
        Object obj = null;
        if (item instanceof BeanTupleItem) {
            obj = ((BeanTupleItem) item).getTuple();
        } else if (item instanceof BeanItem) {
            obj = ((BeanItem<?>) item).getBean();
        } else {
            throw new ClassCastException(Messages.getString(
                "ItemAdapter.NeitherBeanItemNorEntityItem", Locale.getDefault()) + item.getClass() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (obj == null)
            throw new AssertionError(Messages.getString("ItemAdapter.ItemHasNoAttachedObject", Locale.getDefault())); //$NON-NLS-1$
        return obj;
    }

    public static Object getObject(Table table, Object itemId) {
        Item item = table.getItem(itemId);
        return getObject(item);
    }
}
