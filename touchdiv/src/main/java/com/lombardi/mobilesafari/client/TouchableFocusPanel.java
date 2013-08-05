/*
 * Copyright (c) 2010 Alex Moffat
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.lombardi.mobilesafari.client;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FocusPanel;
import com.lombardi.mobilesafari.event.HasTouchHandlers;
import com.lombardi.mobilesafari.event.TouchCancelEvent;
import com.lombardi.mobilesafari.event.TouchCancelHandler;
import com.lombardi.mobilesafari.event.TouchEndEvent;
import com.lombardi.mobilesafari.event.TouchEndHandler;
import com.lombardi.mobilesafari.event.TouchMoveEvent;
import com.lombardi.mobilesafari.event.TouchMoveHandler;
import com.lombardi.mobilesafari.event.TouchStartEvent;
import com.lombardi.mobilesafari.event.TouchStartHandler;

/**
 * Extension of focus panel to add touch handlers.
 *
 * @author amoffat Alex Moffat
 */
public class TouchableFocusPanel extends FocusPanel implements HasTouchHandlers {

    public HandlerRegistration addTouchStartHandler(TouchStartHandler handler) {
        return addDomHandler(handler, TouchStartEvent.getType());
    }

    public HandlerRegistration addTouchMoveHandler(TouchMoveHandler handler) {
        return addDomHandler(handler, TouchMoveEvent.getType());
    }

    public HandlerRegistration addTouchEndHandler(TouchEndHandler handler) {
        return addDomHandler(handler, TouchEndEvent.getType());
    }

    public HandlerRegistration addTouchCancelHandler(TouchCancelHandler handler) {
        return addDomHandler(handler, TouchCancelEvent.getType());
    }
}
