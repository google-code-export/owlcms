/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, 
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.vaadin.touchdiv.widgetset.client;

import com.google.gwt.core.client.Duration;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.lombardi.mobilesafari.event.TouchStartEvent;
import com.lombardi.mobilesafari.event.TouchStartHandler;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VLabel;

/**
 * Client-side widget that reacts to touch events.
 * Needed because mobile devices add a 300-400ms delay before sending a click event.
 * We need immediate reaction.
 * 
 * @author jflamy
 *
 */
public class VTouchDiv extends VLabel implements Paintable, MouseDownHandler, TouchStartHandler {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-touchdiv";

	public static final String CLICK_EVENT_IDENTIFIER = "touch";

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;
	
	protected boolean ignoreMouse = false;

	protected Duration duration = new Duration();
	
	private static boolean TIMER = false;
	
	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VTouchDiv() {
		// not needed unless extending GWT Widget directly
		//setElement(Document.get().createDivElement());	
		super();
		// This method call of the Paintable interface sets the component
		// style name in DOM tree
		setStyleName(CLASSNAME);
		
		addDomHandler(this, TouchStartEvent.getType());
		// MouseDown is sent on touch devices, 400ms after the touch start.
		addDomHandler(this, MouseDownEvent.getType());
		//addDomHandler(this, TouchEndEvent.getType());
	}
	
	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VTouchDiv(String text) {
		// see VTouchDiv() for details 
		super(text);
		setStyleName(CLASSNAME);
		addDomHandler(this, TouchStartEvent.getType());
		addDomHandler(this, MouseDownEvent.getType());
	}

    /**
     * Called whenever an update is received from the server 
     */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		super.updateFromUIDL(uidl, client);
		
		// Save reference to server connection object to be able to send
		// user interaction later
		this.client = client;

		// Save the client side identifier (paintable id) for the widget
		paintableId = uidl.getId();

		// Process attributes/variables from the server
		// The attribute names are the same as we used in 
		// paintContent on the server-side
		if (TIMER) {
			int clicks = uidl.getIntAttribute("clicks");
			getElement().setInnerHTML(clicks+" "+duration.elapsedMillis() +"ms\n");
		}

		
	}

 	/* (non-Javadoc)
 	 * @see com.lombardi.mobilesafari.event.TouchStartHandler#onTouchStart(com.lombardi.mobilesafari.event.TouchStartEvent)
 	 */
 	public void onTouchStart(TouchStartEvent e) {
		// copied from onClick
		// Send a variable change to the server side component so it knows the widget has been touched
		String type = "start";
		// as soon as we get a touch, we know we are on touch device
		ignoreMouse = true;
		if (TIMER) duration = new Duration();
		// The last parameter (immediate) tells that the update should be sent to the server
		// right away
		client.updateVariable(paintableId, CLICK_EVENT_IDENTIFIER, type, true);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent)
	 */
	public void onMouseDown(MouseDownEvent event) {
		// if we got a touch, the mouse down comes 400ms later, and is spurious.
		// we support mousedown for testing on regular browsers
		if (ignoreMouse) return;
		
		// Send a variable change to the server side component so it knows the widget has been clicked
		String type = "start";
		if (TIMER) duration = new Duration();
		// The last parameter (immediate) tells that the update should be sent to the server
		// right away
		client.updateVariable(paintableId, CLICK_EVENT_IDENTIFIER, type, true);
	}

}
