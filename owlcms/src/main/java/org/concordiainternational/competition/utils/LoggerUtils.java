/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

public class LoggerUtils {

    public static void logException(Logger logger, Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        logger.info(sw.toString());
    }

    public static void logErrorException(Logger logger, Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        logger.error(sw.toString());
    }
}
