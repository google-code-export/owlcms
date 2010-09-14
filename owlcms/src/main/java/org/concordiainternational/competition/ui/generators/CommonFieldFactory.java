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

package org.concordiainternational.competition.ui.generators;

import org.concordiainternational.competition.i18n.Messages;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Validator;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

public class CommonFieldFactory extends DefaultFieldFactory {

    private static final long serialVersionUID = 8789528655171127108L;

    private Application app;

    public CommonFieldFactory(HbnSessionManager app) {
        this.app = (Application) app;
    }

    @Override
    public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {

        final Field f = super.createField(container, itemId, propertyId, uiContext);
        ((AbstractField) f).setImmediate(true);

        final String propertyIdString = (String) propertyId;
        if (propertyIdString.endsWith("Weight")) { //$NON-NLS-1$
            return checkFloat(f);
        }

        if (propertyIdString.contains("snatch") //$NON-NLS-1$
            || propertyIdString.contains("cleanJerk")) { //$NON-NLS-1$
            return checkInteger(f);
        }

        if (propertyIdString.contains("Time")) { //$NON-NLS-1$
            return adjustDateField((DateField) f);
        }

        if (f instanceof TextField && (container instanceof Table)) {
            ((TextField) f).setWidth("100%"); //$NON-NLS-1$
        }
        return f;

    }

    private Field adjustDateField(DateField f) {
        // TODO Auto-generated method stub
        f.setDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$
        f.setResolution(DateField.RESOLUTION_MIN);
        return f;
    }

    /**
     * @param f
     * @return
     */
    private Field checkFloat(final Field f) {
        f.setWidth("4em"); //$NON-NLS-1$
        f.addValidator(new DoubleValidator(Messages.getString("CommonFieldFactory.badNumberFormat", app.getLocale())) //$NON-NLS-1$
                // new Validator() {
                // private static final long serialVersionUID =
                // -4073378031354132670L;
                //
                // public boolean isValid(Object value) {
                // try {
                // System.err.println("validator called");
                // Float.parseFloat((String) value);
                // return true;
                // } catch (Exception e) {
                //					f.getWindow().showNotification(Messages.getString("CommonFieldFactory.badNumberFormat",app.getLocale())); //$NON-NLS-1$
                // f.setValue(0);
                // return false;
                // }
                // }
                //
                // public void validate(Object value) throws
                // InvalidValueException {
                // System.err.println("validate called");
                //				if (!isValid(value)) throw new InvalidValueException(Messages.getString("CommonFieldFactory.badNumberFormat",app.getLocale())); //$NON-NLS-1$
                // }
                // }
                );
        return f;
    }

    /**
     * @param f
     * @return
     */
    private Field checkInteger(final Field f) {
        f.setWidth("4em"); //$NON-NLS-1$
        f.addValidator(new Validator() {
            private static final long serialVersionUID = -4073378031354132670L;

            public boolean isValid(Object value) {
                try {
                    Integer.parseInt((String) value);
                    return true;
                } catch (Exception e) {
                    f.getWindow().showNotification(
                        Messages.getString("CommonFieldFactory.badNumberFormat", app.getLocale())); //$NON-NLS-1$
                    f.setValue(0);
                    return false;
                }
            }

            public void validate(Object value) throws InvalidValueException {
                if (!isValid(value))
                    throw new InvalidValueException(Messages.getString(
                        "CommonFieldFactory.badNumberFormat", app.getLocale())); //$NON-NLS-1$
            }
        });
        return f;
    }

}