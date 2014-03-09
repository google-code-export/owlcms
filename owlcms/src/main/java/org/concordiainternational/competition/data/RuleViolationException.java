/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data;

import java.text.MessageFormat;
import java.util.Locale;

import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.Localized;

import com.vaadin.data.Validator.InvalidValueException;

public class RuleViolationException extends InvalidValueException implements Localized {
    private static final long serialVersionUID = 8965943679108964933L;
    private String messageKey;
    private Object[] messageFormatData;
    private Locale locale;

    public RuleViolationException(String s, Object... objs) {
        super(s);
        this.messageKey = s;
        this.messageFormatData = objs;
    }

    public RuleViolationException(Locale l, String s, Object... objs) {
        super(s);
        this.setLocale(l);
        this.messageKey = s;
        this.messageFormatData = objs;
    }

    @Override
    public String getMessage() {
        return getLocalizedMessage();
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public String getLocalizedMessage() {
        final Locale locale1 = (this.locale == null ? CompetitionApplication.getDefaultLocale() : this.locale);
        final String messageTemplate = Messages.getString(this.messageKey, locale1);
        return MessageFormat.format(messageTemplate, messageFormatData);
    }

    public String getLocalizedMessage(Locale locale1) {
        final String messageTemplate = Messages.getString(this.messageKey, locale1);
        return MessageFormat.format(messageTemplate, messageFormatData);
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

}
