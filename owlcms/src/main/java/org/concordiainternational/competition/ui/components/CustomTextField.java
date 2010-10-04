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

package org.concordiainternational.competition.ui.components;

import java.util.ArrayList;
import java.util.Iterator;

import com.vaadin.data.Buffered;
import com.vaadin.data.Validator;
import com.vaadin.terminal.CompositeErrorMessage;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.ui.TextField;

public class CustomTextField extends TextField {
    private static final long serialVersionUID = 7340431723259557435L;

    /**
     * Error messages shown by the fields are composites of the error message
     * thrown by the superclasses (that is the component error message),
     * validation errors and buffered source errors.
     * 
     * @see com.vaadin.ui.AbstractComponent#getErrorMessage()
     */
    @Override
    public ErrorMessage getErrorMessage() {

        /*
         * Check validation errors only if automatic validation is enabled.
         * Empty, required fields will generate a validation error containing
         * the requiredError string. For these fields the exclamation mark will
         * be hidden but the error must still be sent to the client.
         */
        ErrorMessage validationError = null;
        if (isValidationVisible()) {
            try {
                validate();
            } catch (Validator.InvalidValueException e) {
                if (!e.isInvisible()) {
                    validationError = e;
                }
            }
        }

        // Check if there are any systems errors
        final ErrorMessage superError = super.getErrorMessage();

        // Return if there are no errors at all
        if (superError == null && validationError == null) {
            return null;
        }

        ErrorMessage error;
        // get rid of the private exception from AbstractField that is
        // systematically
        // included (private Buffered.SourceException
        // currentBufferedSourceException)
        if (superError instanceof CompositeErrorMessage) {
            ArrayList<ErrorMessage> newErrors = new ArrayList<ErrorMessage>();
            final CompositeErrorMessage compositeError = (CompositeErrorMessage) superError;
            ErrorMessage em = null;
            for (Iterator<?> iterator = compositeError.iterator(); iterator.hasNext();) {
                em = (ErrorMessage) iterator.next();
                if (!(em instanceof Buffered.SourceException)) {
                    newErrors.add(em);
                }
            }
            // our version of terminalError has already added a UserError in
            // this case
            // so there is always at least an error.
            if (newErrors.size() >= 1) {
                error = new CompositeErrorMessage(newErrors);
            } else {
                if (superError instanceof Throwable) ((Throwable) superError).printStackTrace();
                error = superError;
            }

        } else {
            error = new CompositeErrorMessage(new ErrorMessage[] { superError, validationError });
        }
        setComponentError(null);
        return error;
    }
}
