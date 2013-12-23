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
import com.vaadin.service.ApplicationContext.TransactionListener;

/**
 * Example application for using localized SystemMessages configured from a properties file.
 * 
 * @author jflamy
 */
@SuppressWarnings("serial")
public class LocalizedApplication extends Application {

    private static LocalizedSystemMessages localizedMessages;

    @Override
    public void init() {
        // change the system message language in case any are shown while "init"
        // is running.
        LocalizedSystemMessages msg = (LocalizedSystemMessages) getSystemMessages();
        msg.setThreadLocale(this.getLocale()); // by default getLocale() comes
                                               // from the user's browser.

        // the following defines what will happen before and after each http
        // request.
        attachHttpRequestListener();

        // create the initial look.
        // buildMainLayout();
    }

    /**
     * Get localized SystemMessages for this application.
     * 
     * <p>
     * This method is static; we need to call {@link LocalizedSystemMessages#setThreadLocale(Locale)} to change the language that will be
     * used for this thread. This is typically done in a {@link TransactionListener#transactionStart(Application, Object)} method in order
     * to associate the Locale with the thread processing the HTTP request.
     * </p>
     * 
     * @return the LocalizedSystemMessages for this application
     */
    public static SystemMessages getSystemMessages() {
        if (localizedMessages == null)
            localizedMessages = new LocalizedSystemMessages() {
                @Override
                protected Locale getDefaultSystemMessageLocale() {
                    return Locale.CANADA_FRENCH;
                }
            };
        return localizedMessages;
    }

    /**
     * Attach a listener for the begin and end of every HTTP request in the session. (Vaadin "transaction" equals "http request".)
     */
    private void attachHttpRequestListener() {
        getContext().addTransactionListener(new TransactionListener() {
            private static final long serialVersionUID = 316709294485669937L;

            @Override
            public void transactionEnd(Application application, Object transactionData) {
                // hygiene - prevent memory leak.
                ((LocalizedSystemMessages) getSystemMessages()).removeThreadLocale();
            }

            @Override
            public void transactionStart(Application application, Object transactionData) {
                // force system messages to appear in user's lanquage
                ((LocalizedSystemMessages) getSystemMessages()).setThreadLocale(getLocale());
            }
        });
    }

}
