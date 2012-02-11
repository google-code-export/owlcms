/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.annotation.ListenerMethod;
import com.vaadin.ui.Component;

public class PlatesInfoEvent implements Event {
	
	public Component source;
	
	PlatesInfoEvent(Component source) {
		this.source = source;
	}

	public interface PlatesInfoListener extends Listener {
		@ListenerMethod
		public void plateLoadingUpdate(final PlatesInfoEvent event);
	}

}
