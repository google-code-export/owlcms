/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, 
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.vaadin.touchdiv;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Server side component for the VTouchDiv widget.
 */
@com.vaadin.ui.ClientWidget(org.vaadin.touchdiv.widgetset.client.VTouchDiv.class)
public class TouchDiv extends Label {

	private static final long serialVersionUID = 6519499045428460318L;
	private static final boolean TIMER = false;
	private static Method ONTOUCH_METHOD;
	private int clicks = 0;

	public TouchDiv() {
		super("",Label.CONTENT_TEXT);
	}
	
	public TouchDiv(String content) {
		super(content,Label.CONTENT_TEXT);
	}
	
	public TouchDiv(String content, int mode) {
		super(content, mode);
	}
	
	public TouchDiv(TouchListener listener) {
		super("",Label.CONTENT_TEXT);
		addListener(listener);
	}
	
	public TouchDiv(String content, TouchListener listener) {
		super(content,Label.CONTENT_TEXT);
		addListener(listener);
	}
	
	public TouchDiv(String content, TouchListener listener, int mode) {
		super(content, mode);
		addListener(listener);
	}
	
    static {
        try {
            ONTOUCH_METHOD = TouchListener.class.getDeclaredMethod(
                    "onTouch", new Class[] { TouchEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException(
                    "Internal error finding callback methods in TouchDiv");
        }
    }

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		// Paint any component specific content by setting attributes
		// These attributes can be read in updateFromUIDL in the widget.
		target.addAttribute("clicks", clicks);

		// We could also set variables in which values can be returned
		// but declaring variables here is not required
	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		// Variables set by the widget are returned in the "variables" map.
		final String value = (String) variables.get("touch");
		if ("start".equals(value)) {
			// When the user has touched the component we increase the 
			// click count, update the message and request a repaint so 
			// the changes are sent back to the client.
			fireEvent(new TouchEvent(this));
			if (TIMER) {
				clicks++;				
			}
			requestRepaint();
		}
	}
	
    @SuppressWarnings("serial")
	public class TouchEvent extends Component.Event {
        private TouchEventType type;

		/**
         * New instance of text change event.
         * 
         * @param source
         *            the Source of the event.
         */
        public TouchEvent(Component source) {
            super(source);
            this.setType(TouchEventType.TOUCHSTART);
        }
        
        public TouchEvent(Component source, TouchEventType type) {
            super(source);
            this.setType(type);
        }

		public void setType(TouchEventType type) {
			this.type = type;
		}

		public TouchEventType getType() {
			return type;
		}
    }
    
    public interface TouchListener extends Serializable {
        public void onTouch(TouchEvent event);
    }

    public void addListener(TouchListener listener) {
        addListener(TouchEvent.class, listener, ONTOUCH_METHOD);
    }
    
    public void removeListener(TouchListener listener) {
        removeListener(TouchEvent.class, listener, "onTouch");
    }

}
