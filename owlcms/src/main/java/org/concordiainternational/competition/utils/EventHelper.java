/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.utils;

import java.lang.reflect.Method;

public class EventHelper {
    public static Method findMethod(Class<?> eventClass, Class<?> eventListenerClass, String eventProcessingMethodName) {
        try {
            final Method method = eventListenerClass.getDeclaredMethod(eventProcessingMethodName, eventClass);
            return method;
        } catch (final java.lang.NoSuchMethodException e) {
            throw new java.lang.RuntimeException(e);
        }
    }

}
