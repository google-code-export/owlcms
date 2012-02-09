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

package org.concordiainternational.competition.timer;

import java.io.Serializable;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable task for counting down. run() is invoked every "decrement"
 * milliseconds. For convenience we count down in milliseconds.
 * 
 */
class CountdownTask extends TimerTask implements Serializable {

    private static final long serialVersionUID = -2967275874759395049L;

    final private static Logger logger = LoggerFactory.getLogger(CountdownTask.class);

    private final CountdownTimer countdownTimer;
    int ticks;
    long startMillis = System.currentTimeMillis();
    private int startTime;
    private int decrement; // milliseconds

    private boolean firstWarningSignaled = false;
    private boolean finalWarningSignaled = false;
    private boolean noTimeLeftSignaled = false;

    private int finalWarningTick;
    private int firstWarningTick;
    private int noTimeLeftTicks;

    final static int firstWarning = 90; // seconds
    final static int lastWarning = 30; // seconds

    CountdownTask(CountdownTimer countdownTimer, int countdownFrom, int decrement) {
        this.countdownTimer = countdownTimer;
        this.startTime = countdownFrom;
        // round up to decrement interval (100ms)
        this.ticks = roundUpCountdown(countdownFrom, decrement);
        this.decrement = decrement;


        this.firstWarningTick = firstWarning * 1000;
        this.finalWarningTick = lastWarning * 1000;
        this.noTimeLeftTicks = 0;

        int adjustedCountdown = countdownFrom;
        if (adjustedCountdown < firstWarningTick) {
            logger.debug("already beyond first: {} <= {}", adjustedCountdown, firstWarningTick);
            setFirstWarningSignaled(true);
        }
        if (adjustedCountdown < finalWarningTick) {
            logger.debug("already beyond last: {} <= {}", adjustedCountdown, finalWarningTick);
            setFinalWarningSignaled(true);
        }
        if (adjustedCountdown < noTimeLeftTicks) {
            logger.debug("already signaled no time left: {} <= {}", adjustedCountdown, noTimeLeftTicks);
            setNoTimeLeftSignaled(true);
        }
    }
    
	/**
	 * Round up to decrement interval (1000ms)
	 * @param countdownFrom
	 * @param decrement1
	 * @return
	 */
	private int roundUpCountdown(int countdownFrom, int decrement1) {
		if (countdownFrom <= 0) {
			return 0;
		} else if (countdownFrom % decrement1 == 0) {
			return countdownFrom;
		} else {
			return ((countdownFrom / decrement1) * decrement1) + decrement1;
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

    @Override
    public void run() {
        if (ticks <= firstWarningTick && !getFirstWarningSignaled()) {
            initialWarning();
            setFirstWarningSignaled(true);
        } else if (ticks <= finalWarningTick && !getFinalWarningSignaled()) {
            finalWarning();
            setFinalWarningSignaled(true);
        } else if (ticks <= noTimeLeftTicks && !getNoTimeLeftSignaled()) {
            noTimeLeft();
            setNoTimeLeftSignaled(true); // buzzer has already sounded.
        } else if (ticks >= 0) {
            normalTick();
        }

        // leave the timer running for one second extra
        // Under linux, cancelling the timer also cancel the sounds
        if (ticks <= -1000) {
            this.countdownTimer.timer.cancel();
        } else {
            ticks = ticks - decrement;
        }
    }

    private void normalTick() {
        logger.trace("normalTick: " + ticks / 1000 + " " + (System.currentTimeMillis() - startMillis)); //$NON-NLS-1$ //$NON-NLS-2$
        final CountdownTimerListener countdownDisplay = countdownTimer.getCountdownDisplay();
        final CountdownTimerListener masterBuzzer = countdownTimer.getMasterBuzzer();
        if (countdownDisplay != null) {
            countdownDisplay.normalTick((int) getBestTimeRemaining());
        }
        if (masterBuzzer != null) {
            masterBuzzer.normalTick((int) getBestTimeRemaining());
        }
        for (CountdownTimerListener curListener : countdownTimer.getListeners()) {
            curListener.normalTick((int) getBestTimeRemaining());
        }
    }

    private void initialWarning() {
        logger.info("initial warning: " + ticks / 1000 + " " + (System.currentTimeMillis() - startMillis)); //$NON-NLS-1$ //$NON-NLS-2$
        final CountdownTimerListener countdownDisplay = countdownTimer.getCountdownDisplay();
        final CountdownTimerListener masterBuzzer = countdownTimer.getMasterBuzzer();
        if (countdownDisplay != null) {
            countdownDisplay.initialWarning((int) getBestTimeRemaining());
        }
        if (masterBuzzer != null) {
            masterBuzzer.initialWarning((int) getBestTimeRemaining());
        }
        for (CountdownTimerListener curListener : countdownTimer.getListeners()) {
            curListener.initialWarning((int) getBestTimeRemaining());
        }
    }

    private void finalWarning() {
        logger.info("final warning: " + ticks / 1000 + " " + (System.currentTimeMillis() - startMillis)); //$NON-NLS-1$ //$NON-NLS-2$
        final CountdownTimerListener countdownDisplay = countdownTimer.getCountdownDisplay();
        final CountdownTimerListener masterBuzzer = countdownTimer.getMasterBuzzer();
        if (countdownDisplay != null) {
            countdownDisplay.finalWarning((int) getBestTimeRemaining());
        }
        if (masterBuzzer != null) {
            masterBuzzer.finalWarning((int) getBestTimeRemaining());
        }
        for (CountdownTimerListener curListener : countdownTimer.getListeners()) {
            curListener.finalWarning((int) getBestTimeRemaining());
        }
    }

    private void noTimeLeft() {
        logger.info("time over: " + ticks / 1000 + " " + (System.currentTimeMillis() - startMillis)); //$NON-NLS-1$ //$NON-NLS-2$
        final CountdownTimerListener countdownDisplay = countdownTimer.getCountdownDisplay();
        final CountdownTimerListener masterBuzzer = countdownTimer.getMasterBuzzer();
        if (countdownDisplay != null) {
            countdownDisplay.noTimeLeft((int) getBestTimeRemaining());
        }
        if (masterBuzzer != null) {
            masterBuzzer.noTimeLeft((int) getBestTimeRemaining());
        }
        for (CountdownTimerListener curListener : countdownTimer.getListeners()) {
            curListener.noTimeLeft((int) getBestTimeRemaining());
        }
    }

    /**
     * @param firstWarningSignaled
     *            the firstWarningSignaled to set
     */
    private void setFirstWarningSignaled(boolean firstWarningSignaled) {
        this.firstWarningSignaled = firstWarningSignaled;
    }

    /**
     * @return the firstWarningSignaled
     */
    private boolean getFirstWarningSignaled() {
        return firstWarningSignaled;
    }

    /**
     * @param finalWarningSignaled
     *            the finalWarningSignaled to set
     */
    private void setFinalWarningSignaled(boolean finalWarningSignaled) {
        // logger.debug("setting finalWarningSignaled={}",finalWarningSignaled);
        // LoggerUtils.logException(logger,new Exception("wtf"));
        this.finalWarningSignaled = finalWarningSignaled;
    }

    /**
     * @return the finalWarningSignaled
     */
    private boolean getFinalWarningSignaled() {
        return finalWarningSignaled;
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