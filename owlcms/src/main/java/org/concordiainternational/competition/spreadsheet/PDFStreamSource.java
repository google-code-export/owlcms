/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.spreadsheet;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.vaadin.terminal.StreamResource;

/**
 * Encapsulate a PDF as a StreamSource so that it can be used as a source of data when the user clicks on a link. This class converts the
 * output stream produced by the writePDF method to an input stream that the Vaadin framework can consume.
 */
@SuppressWarnings("serial")
public class PDFStreamSource implements StreamResource.StreamSource {

    @Override
    public InputStream getStream() {
        try {
            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        writePdf(out);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            return in;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected void writePdf(PipedOutputStream out) {
        // call PDF library to write on "out"
    }

}
