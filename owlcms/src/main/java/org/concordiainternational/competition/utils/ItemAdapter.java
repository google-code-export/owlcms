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

package org.concordiainternational.competition.utils;

import java.util.Locale;

import org.concordiainternational.competition.i18n.Messages;

import com.vaadin.data.Item;
import com.vaadin.data.hbnutil.HbnContainer.EntityItem;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Table;

public class ItemAdapter {

    /**
     * @param item
     * @return
     */
    @SuppressWarnings("rawtypes")
	public static Object getObject(final Item item) {
        Object obj = null;
        if (item instanceof EntityItem) {
            obj = ((EntityItem) item).getPojo();
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
