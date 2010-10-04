/* 
 * Copyright ©2009 Jean-François Lamy
 * 
 * Licensed under the Apache Licence, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.concordiainternational.competition.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "i18n.messages"; //$NON-NLS-1$

    // private static final ResourceBundle RESOURCE_BUNDLE =
    // ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key, Locale locale) {
        try {
            // ResourceBundle caches the bundles, so this is not as inefficient
            // as it seems.
            return ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key);
        } catch (MissingResourceException e) {
            return '«' + key + '»';
        }
    }
    
    public static String getStringWithException(String key, Locale locale) throws MissingResourceException {
    	return ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key);
    }
}
