/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import org.concordiainternational.competition.data.Lifter;

import com.vaadin.data.Item;

public interface EditableList {

    public Lifter getFirstLifter();

    public Item getFirstLifterItem();

    public SessionData getGroupData();

    public void setGroupData(SessionData data);

    public void clearSelection();

    public void deleteItem(Object targetId);
}
