/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
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
