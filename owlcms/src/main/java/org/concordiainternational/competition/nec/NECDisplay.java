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

import static org.junit.Assert.assertEquals;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drive an old-style 3x20 LED Scoreboard Model unknown, reverse-engineered from
 * legacy control application.
 * 
 * @author jflamy
 * 
 */
public class NECDisplay implements Serializable {
    private static final long serialVersionUID = 127161162371907083L;
    final static Logger logger = LoggerFactory.getLogger(NECDisplay.class);
    final static String charsetName = "US-ASCII"; //$NON-NLS-1$
    transient private SerialPort serialPort; // do not serialize
    private String comPortName;


	private boolean opened = false;
    private Lifter currentLifter;


    public NECDisplay() throws NoSuchPortException, PortInUseException, IOException, UnsupportedCommOperationException {
    }

    private void init(String comPortName) throws NoSuchPortException, PortInUseException, IOException,
            UnsupportedCommOperationException {
        if (opened) return;
        if (comPortName == null) comPortName = "COM1"; //$NON-NLS-1$
        this.serialPort = openPort(comPortName);
    }

    /**
     * @param curLifter
     * @throws IOException
     */
    public void writeLifterInfo(final Lifter curLifter, boolean weightOnly) {
        if (curLifter == null) return;
        if (!weightOnly) this.setCurrentLifter(curLifter);

        if (weightOnly) {
            try {
                final String weight = curLifter.getNextAttemptRequestedWeight() + "kg"; //$NON-NLS-1$
                String padding = "                     "; //$NON-NLS-1$
                padding = padding.substring(0, padding.length() - 1 - weight.length());
                writeStrings("", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    padding + weight);
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
                throw new RuntimeException(e);
            }
        } else {
            try {
                final String lastName = fixAccents(curLifter.getLastName()).toUpperCase();
                String firstName = fixAccents(curLifter.getFirstName());
                if (firstName.length() > 16) firstName = firstName.substring(0, 16);
                final String weight = curLifter.getNextAttemptRequestedWeight() + "kg"; //$NON-NLS-1$
                final String curTry = Messages.getString(
                    "NECDisplay.Attempt", CompetitionApplication.getCurrentLocale()) + ((curLifter.getAttemptsDone() % 3) + 1); //$NON-NLS-1$
                final String curClub = curLifter.getClub().toUpperCase();
                String padding = "                     "; //$NON-NLS-1$

                int padding2Length = padding.length() - firstName.length() - curClub.length();
                String padding2 = padding.substring(0, (padding2Length) - 1);
                int padding3Length = padding.length() - weight.length() - curTry.length();
                String padding3 = padding.substring(0, (padding3Length) - 1);

                // Following lines are used if 3 items are shown on same line
                // String padding3a = padding.substring(0,(padding3Length/2)-1);
                // String padding3b =
                // padding.substring(0,padding3Length-padding3a.length()-1);

                writeStrings(lastName, firstName + padding2 + curClub, curTry + padding3 + weight);
            } catch (Exception e) {
                LoggerUtils.logException(logger, e);
                throw new RuntimeException(e);
            }
        }

        return;
    }

    private String level1Encode(String string1, String string2, String string3) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer(100);
        sb.append((char) 27);
        sb.append("M@"); //$NON-NLS-1$
        sb.append((char) 27);
        sb.append("FJ0"); //$NON-NLS-1$
        sb.append((char) 27);
        sb.append("D"); //$NON-NLS-1$
        sb.append("~E0"); // original VB76 code has a comment containing "~F0" //$NON-NLS-1$
        String curString = string1;
        if (curString.length() > 20) {
            curString = curString.substring(0, 19);
        }
        sb.append(curString);
        sb.append("~K0"); //$NON-NLS-1$
        curString = string2;
        if (curString.length() > 20) {
            curString = curString.substring(0, 19);
        }
        sb.append(curString);
        sb.append("~K0"); //$NON-NLS-1$
        curString = string3;
        if (curString.length() > 20) {
            curString = curString.substring(0, 19);
        }
        sb.append(curString);
        sb.append((char) 13);
        return sb.toString();
    }

    private byte[] encode(String string1, String string2, String string3) throws UnsupportedEncodingException {
        final String level1String = level1Encode(string1, string2, string3);
        byte[] level1ByteArray = level1String.getBytes(charsetName);
        byte checkSum = checksum(level1ByteArray);
        final char level1Length = (char) level1String.length();

        StringBuffer level2StringBuffer = new StringBuffer(100);
        level2StringBuffer.setLength(0);
        level2StringBuffer.append((char) 2);
        level2StringBuffer.append((char) level1Length);
        level2StringBuffer.append((char) 0);
        level2StringBuffer.append(level1String);
        level2StringBuffer.append((char) 3);
        level2StringBuffer.append((char) checkSum);
        level2StringBuffer.append((char) 4);

        return level2StringBuffer.toString().getBytes(charsetName);
    }

    // private static String hexadecimal(String input) throws
    // UnsupportedEncodingException {
    // return hexadecimal(input, "US-ASCII");
    // }

    // private static String hexadecimal(String input, String charsetName)
    // throws UnsupportedEncodingException {
    // if (input == null)
    // throw new NullPointerException();
    // return asHex(input.getBytes(charsetName));
    // }

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray(); //$NON-NLS-1$

    public static String asHex(byte[] buf) {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

    private static byte checksum(byte[] byteArray) {
        byte xorValue = 0;
        for (int i = 0; i < byteArray.length; i++) {
            xorValue = (byte) (xorValue ^ byteArray[i]);
        }
        return (byte) (xorValue | 32);
    }

    /**
     * Open the named serial port and configure it with the required parameters
     * 
     * @throws NoSuchPortException
     * @throws PortInUseException
     * @throws IOException
     * @throws UnsupportedCommOperationException
     * @throws NoSuchPortException
     * 
     */
    private SerialPort openPort(String comPortName) throws PortInUseException, IOException,
            UnsupportedCommOperationException, NoSuchPortException {

        CommPortIdentifier portId;
        try {
            portId = CommPortIdentifier.getPortIdentifier(comPortName);
        } catch (NoSuchPortException e) {
            logger.trace("running on COM1"); //$NON-NLS-1$
            portId = CommPortIdentifier.getPortIdentifier("COM1"); //$NON-NLS-1$
        }
        serialPort = (SerialPort) portId.open(NECDisplay.class.getName(), 200); // 200ms
                                                                                // timeout
        serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        serialPort.notifyOnOutputEmpty(false);

        opened = true;
        return serialPort;
    }

    public void close() {
        if (serialPort != null && opened) {
            serialPort.close();
            opened = false;
        }
    }

    /**
     * Write to a serial device using a separate thread.
     * 
     */
    private class StringWriter implements Runnable {
        private String[] strings;
        private NECDisplay display;

        public StringWriter(NECDisplay display, String[] strings) {
            this.strings = strings;
            this.display = display;
        }

        @Override
        public void run() {
            synchronized (display) {
                OutputStream out = null;
                try {
                    init(display.comPortName);
                    out = display.serialPort.getOutputStream();
                    String string1 = strings[0] != null ? strings[0] : ""; //$NON-NLS-1$
                    String string2 = strings[1] != null ? strings[1] : ""; //$NON-NLS-1$
                    String string3 = strings[2] != null ? strings[2] : ""; //$NON-NLS-1$
                    if (string1.length() > 20) string1 = string1.substring(0, 20);
                    if (string2.length() > 20) string2 = string2.substring(0, 20);
                    if (string3.length() > 20) string3 = string3.substring(0, 20);
                    // write, flush and log just to make sure it all goes out.
                    out.write(encode(string1, string2, string3));
                    out.flush();
                    logger
                            .warn("\n" + string1 + "\n" + string2 + "\n" + string3 + "\n.........|.........| written on " + serialPort.getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                        }
                        display.close();
                    }
                }

            }
        }

    };

    /**
     * Write to the NEC Done in a separate thread because with some hardware
     * there is perceptible delay.
     * 
     * @param strings
     * @throws IOException
     * @throws NoSuchPortException
     * @throws PortInUseException
     * @throws UnsupportedCommOperationException
     */
    public void writeStrings(String... strings) throws IOException, NoSuchPortException, PortInUseException,
            UnsupportedCommOperationException {
        if (logger.isTraceEnabled()) LoggerUtils.logException(logger, new Exception("whocalls writeStrings")); //$NON-NLS-1$
        new Thread(new StringWriter(this, strings)).start();
    }

    @Test
    public void checkEncoding() throws UnsupportedEncodingException {
        String c1 = level1Encode("          ", "          ", "          "); // 104 104 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        byte[] bytes = c1.getBytes(charsetName);
        assertEquals(checksum(bytes), 104);
        assertEquals(
            "0231001b4d401b464a301b447e4530202020202020202020207e4b30202020202020202020207e4b30202020202020202020200d036804", //$NON-NLS-1$
            asHex(encode("          ", "          ", "          "))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        String c2 = level1Encode("          a", "          b", "          c");// 8 40 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        bytes = c2.getBytes(charsetName);
        assertEquals(checksum(bytes), 40);
        assertEquals(
            "0234001b4d401b464a301b447e453020202020202020202020617e4b3020202020202020202020627e4b3020202020202020202020630d032804", //$NON-NLS-1$
            asHex(encode("          a", "          b", "          c"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        String c3 = level1Encode("01234567890123456789", "abcdefghijklmnopqrst", "!\"/$%?&*()_+=-[]<>Éé");// 35 35 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals(checksum(c3.getBytes(charsetName)), 35);
    }

    public Lifter getCurrentLifter() {
        return currentLifter;
    }

    public void setCurrentLifter(Lifter currentLifter) {
        this.currentLifter = currentLifter;
    }

    /**
     * Remove accented characters This uses the Unicode definition of accent
     * (works for all languages and all characters)
     * 
     * @param accentedString
     * @return the input, with accented characters replaced by the corresponding
     *         non-accented characters.
     */
    private String fixAccents(String accentedString) {
        // convert characters with accents into letter-accent pairs
        String separateAccents = java.text.Normalizer.normalize(accentedString, java.text.Normalizer.Form.NFD);
        // hide the non-ascii characters (the accents)
        return separateAccents.replaceAll("[^\\p{ASCII}]", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testStrings() throws IOException, NoSuchPortException, PortInUseException,
            UnsupportedCommOperationException {
        comPortName = "COM3"; //$NON-NLS-1$
        writeStrings("", "", "          17 KG"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        try {
            Thread.sleep(2000);
            writeStrings("BRASSARD", //$NON-NLS-1$
                // .........!.........!
                "Augustin         FHQ", //$NON-NLS-1$
                // .........!.........!
                "ESSAI 1        100kg"); //$NON-NLS-1$
        } catch (InterruptedException e) {
        }

        try {
            Thread.sleep(2000);
            writeStrings("LAMY", //$NON-NLS-1$
                // .........!.........!
                fixAccents("Jean-François    C-I"), //$NON-NLS-1$
                // .........!.........!
                "ESSAI 1         70kg"); //$NON-NLS-1$
        } catch (InterruptedException e) {
        }

        try {
            Thread.sleep(2000);
            writeStrings(fixAccents("BEAUDOIN-DE L'ESPÉRANCE"), //$NON-NLS-1$
                // .........!.........!
                "Jeanne-Baptiste  C-I", //$NON-NLS-1$
                // .........!.........!
                "ESSAI 1         70kg"); //$NON-NLS-1$
        } catch (InterruptedException e) {
            System.err.println("interrupted"); //$NON-NLS-1$
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

    }
    
    public String getComPortName() {
		return comPortName;
	}

	public void setComPortName(String comPortName) throws NoSuchPortException, PortInUseException, IOException, UnsupportedCommOperationException {
		this.comPortName = comPortName;
		init(comPortName);
	}

}
