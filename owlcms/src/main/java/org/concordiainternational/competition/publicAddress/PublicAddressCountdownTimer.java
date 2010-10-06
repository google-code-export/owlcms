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

package org.concordiainternational.competition.publicAddress;

import java.io.Serializable;
import java.util.Timer;

import org.concordiainternational.competition.ui.SessionData;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the count-down task used for the PublicAddress screen.
 * This class is independent from the count-down timer used for managing lifters,
 * and is much simpler.
 * 
 * @author jflamy
 * 
 */
public class PublicAddressCountdownTimer implements Serializable {

    private static final long serialVersionUID = 8921641103581546472L;

    final static Logger logger = LoggerFactory.getLogger(PublicAddressCountdownTimer.class);
    final private static int DECREMENT = 1000; // milliseconds
    private int startTime;
    Timer timer = null;
    private PublicAddressCountdownTask countdownTask;

	private SessionData masterData;

    public PublicAddressCountdownTimer(SessionData masterData) {
    	this.masterData = masterData;
        logger.debug("new"); //$NON-NLS-1$
    }

    /**
     * Start the timer.
     */
    public void start() {
        logger.debug("enter start {}", getTimeRemaining()); //$NON-NLS-1$
        if (timer != null) {
            timer.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (startTime <= 0) {
            startTime = 0;
            return;
        }

        timer = new Timer();
        countdownTask = new PublicAddressCountdownTask(timer, startTime, DECREMENT, masterData);
        timer.scheduleAtFixedRate(countdownTask, 0, // start right away
            DECREMENT);
        logger.warn("start: {}", startTime); //$NON-NLS-1$  
    }

    /**
     * Start the timer.
     */
    public void restart() {
        logger.debug("enter restart {} {}", getTimeRemaining()); //$NON-NLS-1$
        start();
    }

    /**
     * Stop the timer
     */
    public void pause() {
        logger.debug("enter pause {} {}", getTimeRemaining()); //$NON-NLS-1$
        if (timer != null) timer.cancel();
        timer = null;
        if (countdownTask != null) {
            countdownTask.cancel();
            startTime = (int) countdownTask.getBestTimeRemaining();
            countdownTask = null;
        }
        logger.warn("pause: {}", startTime); //$NON-NLS-1$
    }

    /**
     * Stop the timer.
     * Same as Pause, in this case.
     */
    public void stop() {
        logger.debug("enter stop {} {}", getTimeRemaining()); //$NON-NLS-1$
        pause();
        logger.warn("stop: {}", startTime); //$NON-NLS-1$
    }

    /**
     * Set the remaining time explicitly. Meant to be called only by SessionData()
     * so that the time is reset correctly.
     */
    public void forceTimeRemaining(int remainingTime) {
        if (timer != null) timer.cancel();
        timer = null;
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        this.startTime = remainingTime;
        logger.warn("forceTimeRemaining: {}", getTimeRemaining()); //$NON-NLS-1$
    }

    /**
     * @return true if the timer is actually counting down.
     */
    public boolean isRunning() {
        return timer != null;
    }

    /**
     * Set the time remaining for the next start.
     * 
     * @param i
     */
    private void setTimeRemaining(int i) {
        logger.debug("setTimeRemaining {}" + i);
        this.startTime = i;
    }

    private int getTimeRemaining() {
        return startTime;
    }


    @Test
    public void doCountdownTest() {
        try {
            setTimeRemaining(4000);
            start();
            long now = System.currentTimeMillis();
            System.out
                    .println("after scheduling: " + countdownTask.getTimeRemaining() + " " + (System.currentTimeMillis() - now)); //$NON-NLS-1$ //$NON-NLS-2$
            Thread.sleep(1000);
            pause();
            System.out
                    .println("pause after 1000: " + countdownTask.getTimeRemaining() + " " + (System.currentTimeMillis() - now)); //$NON-NLS-1$ //$NON-NLS-2$
            Thread.sleep(1000);
            System.out
                    .println("restart after 2000: " + countdownTask.getTimeRemaining() + " " + (System.currentTimeMillis() - now)); //$NON-NLS-1$ //$NON-NLS-2$
            restart();
            Thread.sleep(3000);
        } catch (InterruptedException t) {
            logger.error(t.getMessage());
        }
    }


}
