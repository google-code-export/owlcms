/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, 
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.vaadin.touchdiv;


import org.vaadin.touchdiv.TouchDiv.TouchEvent;
import org.vaadin.touchdiv.TouchDiv.TouchListener;

import com.vaadin.Application;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class TouchDivApplication extends Application {
	Integer i = 0;
	
	@Override
	public void init() {
		Window mainWindow = new Window("TouchDiv Application");

		final TouchDiv c = new TouchDiv("Touchez-moi. \u2714");
		
		c.addListener(new TouchListener() {
			public void onTouch(TouchEvent event) {
				i++;
				c.setValue(i);
			}
		});
		
		c.setHeight("2em");
		mainWindow.addComponent(c);
		setMainWindow(mainWindow);
	}

}
