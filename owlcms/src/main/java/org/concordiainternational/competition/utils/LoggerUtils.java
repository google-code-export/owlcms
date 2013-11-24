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

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LoggerUtils {
    static Logger thisLogger = LoggerFactory.getLogger(LoggerUtils.class);

    public enum LoggingKeys {
        view,
        currentGroup
    }

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

    public static void mdcPut(LoggingKeys key, String value) {
        String view = MDC.get(LoggingKeys.view.name());
        String group = MDC.get(LoggingKeys.currentGroup.name());
        if (group != null && view == null) {
            thisLogger.debug("traceback {}", (Object[]) Thread.currentThread().getStackTrace());
        }
        MDC.put(key.toString(), value);
    }

    public static String mdcGet(String key) {
        return MDC.get(key);
    }

    public static void buttonSetup(final SessionData groupData) {
        CompetitionSession currentSession = groupData.getCurrentSession();
        LoggerUtils.mdcPut(LoggingKeys.currentGroup, currentSession != null ? currentSession.getName() : "");
        LoggerUtils.mdcPut(LoggingKeys.view, CompetitionApplication.getCurrent().getCurrentView().getLoggingId());
    }

    @SuppressWarnings("restriction")
    public static String getCallerClassName() {
        Class<?> callerClass = sun.reflect.Reflection.getCallerClass(5);
        return callerClass.getSimpleName();
    }

    public static void buttonSetup() {
        LoggerUtils.mdcPut(LoggingKeys.currentGroup, "*");
        LoggerUtils.mdcPut(LoggingKeys.view, CompetitionApplication.getCurrent().getCurrentView().getLoggingId());
    }

    public static void traceBack(Logger logger) {
        logException(logger, new Exception("traceBack"));
    }

    public static void traceBack(Logger logger, String whereFrom) {
        logException(logger, new Exception(whereFrom));
    }

}
