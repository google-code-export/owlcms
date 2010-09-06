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

package org.concordiainternational.competition.nec;

/*
 * @(#)SimpleWrite.java	1.12 98/06/25 SMI
 * 
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license
 * to use, modify and redistribute this software in source and binary
 * code form, provided that i) this copyright notice and license appear
 * on all copies of the software; and ii) Licensee does not utilize the
 * software in a manner which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THE
 * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS
 * BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING
 * OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control
 * of aircraft, air traffic, aircraft navigation or aircraft
 * communications; or in the design, construction, operation or
 * maintenance of any nuclear facility. Licensee represents and
 * warrants that it will not use or redistribute the Software for such
 * purposes.
 */
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version 1.10, 08/04/00
 */
public class SimpleWrite {
    private static Logger logger = LoggerFactory.getLogger(SimpleWrite.class);
    static Enumeration<?> portList;
    static CommPortIdentifier portId;
    static SerialPort serialPort;
    static OutputStream outputStream;
    static boolean outputBufferEmptyFlag = false;

    /**
     * Method declaration
     * 
     * 
     * @param args
     * 
     * @see
     */
    public static void main(String[] args) {
        boolean portFound = false;
        String defaultPort = "COM3"; //$NON-NLS-1$

        if (args.length > 0) {
            defaultPort = args[0];
        }

        portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            logger.trace("trying port " + portId.getName()); //$NON-NLS-1$
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

                if (portId.getName().equals(defaultPort)) {
                    logger.debug("Found port " + defaultPort); //$NON-NLS-1$

                    portFound = true;

                    try {
                        serialPort = (SerialPort) portId.open("HCompetition", 2000); //$NON-NLS-1$
                    } catch (PortInUseException e) {
                        System.out.println("Port in use."); //$NON-NLS-1$
                        continue;
                    }

                    portSetup();

                    try {
                        String messageString = "test"; //$NON-NLS-1$
                        outputStream.write(messageString.getBytes());
                        // outputStream.write(NECDisplay.encode("line1",
                        // "line2", "line3"));
                    } catch (IOException e) {
                    }

                    portCleanUp();
                }
            }
        }

        if (!portFound) {
            logger.error("port " + defaultPort + " not found."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
	 * 
	 */
    private static void portSetup() {
        try {
            outputStream = serialPort.getOutputStream();
        } catch (IOException e) {
        }

        try {
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT);
        } catch (UnsupportedCommOperationException e) {
        }

        try {
            serialPort.notifyOnOutputEmpty(true);
        } catch (Exception e) {
            logger.error("Error setting event notification"); //$NON-NLS-1$
            logger.error(e.toString());
            System.exit(-1);
        }
    }

    /**
	 * 
	 */
    private static void portCleanUp() {
        try {
            Thread.sleep(2000); // Be sure data is xferred before
            // closing
        } catch (Exception e) {
        }
        serialPort.close();
        System.exit(1);
    }

}
