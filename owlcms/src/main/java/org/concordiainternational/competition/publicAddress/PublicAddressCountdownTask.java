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
import java.util.TimerTask;

import org.concordiainternational.competition.ui.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple countdown task for Public Address screen.
 * Runnable task for counting down. run() is invoked every "decrement"
 * milliseconds. For convenience we count down in milliseconds.
 * 
 */
@SuppressWarnings("serial")
class PublicAddressCountdownTask extends TimerTask implements Serializable {


	final private static Logger logger = LoggerFactory.getLogger(PublicAddressCountdownTask.class);

    private final Timer countdownTimer;
    int ticks;
    long startMillis = System.currentTimeMillis();

    private int decrement; // milliseconds

    private boolean noTimeLeftSignaled = false;
    private int noTimeLeftTicks;

	private int startTime;

	private SessionData masterData;

    PublicAddressCountdownTask(Timer countdownTimer, int countdownFrom, int decrement, SessionData masterData) {
    	this.masterData = masterData;
    	this.startTime = countdownFrom;
        this.countdownTimer = countdownTimer;
        
        // round up to decrement interval (1000ms)
        this.ticks = roundUpCountdown(countdownFrom, decrement);
        this.decrement = decrement;

        this.noTimeLeftTicks = 0;

        int adjustedCountdown = countdownFrom;
        if (adjustedCountdown < noTimeLeftTicks) {
            logger.debug("already signaled no time left: {} <= {}", adjustedCountdown, noTimeLeftTicks);
            setNoTimeLeftSignaled(true);
        }
    }

	/**
	 * Round up to decrement interval (1000ms)
	 * @param countdownFrom
	 * @param decrement
	 * @return
	 */
	private int roundUpCountdown(int countdownFrom, int decrement) {
		if (countdownFrom <= 0) {
			return 0;
		} else if (countdownFrom % decrement == 0) {
			return countdownFrom;
		} else {
			return ((countdownFrom / decrement) * decrement) + decrement;
		}
		
	}
    
    /**
     * @return best available estimation of the time elapsed.
     */
    long getBestTimeRemaining() {
        return startTime - (System.currentTimeMillis() - startMillis);
    }

    /**
     * @return time elapsed, empirically less than 15ms late
     */
    public int getTimeRemaining() {
        return ticks;
    }

    /* (non-Javadoc)
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {
    	if (ticks <= noTimeLeftTicks && !getNoTimeLeftSignaled()) {
    		normalTick();
            noTimeLeft();
            setNoTimeLeftSignaled(true);
        } else {
            normalTick();
        }

        if (ticks <= 0) {
            countdownTimer.cancel();
        } else {
            ticks = ticks - decrement;
        }
    }

    
    private void normalTick() {
        logger.trace("normalTick: " + ticks / 1000 + " " + (System.currentTimeMillis() - startMillis)); //$NON-NLS-1$ //$NON-NLS-2$
        PublicAddressTimerEvent timerEvent = new PublicAddressTimerEvent();
        timerEvent.setRemainingMilliseconds(ticks);
		masterData.fireBlackBoardEvent(timerEvent);
    }

    private void noTimeLeft() {
        logger.warn("time over: " + ticks / 1000 + " " + (System.currentTimeMillis() - startMillis)); //$NON-NLS-1$ //$NON-NLS-2$
        PublicAddressTimerEvent timerEvent = new PublicAddressTimerEvent();
        timerEvent.setNoTimeLeft(true);
		masterData.fireBlackBoardEvent(timerEvent);
    }


    /**
     * @param noTimeLeftSignaled
     *            the noTimeLeftSignaled to set
     */
    private void setNoTimeLeftSignaled(boolean noTimeLeftSignaled) {
        this.noTimeLeftSignaled = noTimeLeftSignaled;
    }

    /**
     * @return the noTimeLeftSignaled
     */
    private boolean getNoTimeLeftSignaled() {
        return noTimeLeftSignaled;
    }

}