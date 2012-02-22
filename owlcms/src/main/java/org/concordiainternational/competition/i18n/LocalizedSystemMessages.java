/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.i18n;

import java.util.Locale;

import com.vaadin.Application;
import com.vaadin.Application.CustomizedSystemMessages;

/**
 * CustomizedSystemMessages extension that relies on a properties file. This
 * class also maintains the language for each user, so that different users can
 * get the message in their own language (once the session is under way).
 * 
 * @author jflamy
 */
public class LocalizedSystemMessages extends CustomizedSystemMessages {
    private static final long serialVersionUID = 295825699005962978L;

    /**
     * Locale associated with the current thread (ThreadLocal maintains a map
     * from the thread an associated value so there is in effect one thread
     * Locale per thread.)
     */
    static ThreadLocal<Locale> threadLocale = new ThreadLocal<Locale>();

    /**
     * Sets the default language used for system messages emitted before the
     * application has fully initialized, for example on a server restart. This
     * will happen if the system message is issued before
     * {@link #setThreadLocale(Locale)} has been called by the application
     * (normally in an override of {@link Application#init()}.
     * 
     * Override this method if the JVM default locale (often en_US if running on
     * a server) is inappropriate.
     * 
     * @return locale for messages issued before the Application has set its own
     *         locale.
     */
    protected Locale getDefaultSystemMessageLocale() {
        return Locale.getDefault();
    }

    /**
     * Called by the application if it wishes to have its
     * LocalizedSystemMessages shown in the user's language.
     * 
     * @param application
     * @param locale
     */
    public void setThreadLocale(Locale locale) {
        threadLocale.set(locale);
    }

    /**
     * Get the application's current locale.
     * 
     * @param application
     * @return locale for the application
     */
    public Locale getThreadLocale() {
        Locale loc = threadLocale.get();
        if (loc == null) {
            return getDefaultSystemMessageLocale();
        } else {
            return loc;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.Application.SystemMessages#getCommunicationErrorCaption()
     */
    @Override
    public String getCommunicationErrorCaption() {
        return (communicationErrorNotificationEnabled ? Messages.getString(
            "LocalizedSystemMessages.communicationErrorCaption", getThreadLocale()) //$NON-NLS-1$
                : null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.Application.SystemMessages#getCommunicationErrorMessage()
     */
    @Override
    public String getCommunicationErrorMessage() {
        return (communicationErrorNotificationEnabled ? Messages.getString(
            "LocalizedSystemMessages.communicationErrorMessage", getThreadLocale()) //$NON-NLS-1$
                : null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.Application.SystemMessages#getInternalErrorCaption()
     */
    @Override
    public String getInternalErrorCaption() {
        return (internalErrorNotificationEnabled ? Messages.getString(
            "LocalizedSystemMessages.internalErrorCaption", getThreadLocale()) //$NON-NLS-1$
                : null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.Application.SystemMessages#getInternalErrorMessage()
     */
    @Override
    public String getInternalErrorMessage() {
        return (internalErrorNotificationEnabled ? Messages.getString(
            "LocalizedSystemMessages.internalErrorMessage", getThreadLocale()) //$NON-NLS-1$
                : null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.Application.SystemMessages#getOutOfSyncCaption()
     */
    @Override
    public String getOutOfSyncCaption() {
        return (outOfSyncNotificationEnabled ? Messages.getString(
            "LocalizedSystemMessages.outOfSyncCaption", getThreadLocale()) //$NON-NLS-1$
                : null);
    }

    /**
     * @return the notification message, or null for no message
     */
    @Override
    public String getOutOfSyncMessage() {
        return (outOfSyncNotificationEnabled ? Messages.getString(
            "LocalizedSystemMessages.outOfSyncMessage", getThreadLocale()) //$NON-NLS-1$
                : null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.Application.SystemMessages#getSessionExpiredCaption()
     */
    @Override
    public String getSessionExpiredCaption() {
        return (sessionExpiredNotificationEnabled ? Messages.getString(
            "LocalizedSystemMessages.sessionExpiredCaption", getThreadLocale()) //$NON-NLS-1$
                : null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.Application.SystemMessages#getSessionExpiredMessage()
     */
    @Override
    public String getSessionExpiredMessage() {
        return (sessionExpiredNotificationEnabled ? Messages.getString(
            "LocalizedSystemMessages.sessionExpiredMessage", getThreadLocale()) //$NON-NLS-1$
                : null);
    }

}
