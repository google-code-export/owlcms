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
