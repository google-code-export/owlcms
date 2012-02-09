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

package org.concordiainternational.competition.spreadsheet;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.vaadin.terminal.StreamResource;

/**
 * Encapsulate a PDF as a StreamSource so that it can be used as a source of
 * data when the user clicks on a link. This class converts the output stream
 * produced by the writePDF method to an input stream that the Vaadin framework
 * can consume.
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