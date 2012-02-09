/* 
 * Copyright ©2009 Jean-François Lamy
 * 
 * Licensed under the Open Software Licence, Version 3.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.opensource.org/licenses/osl-3.0.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
