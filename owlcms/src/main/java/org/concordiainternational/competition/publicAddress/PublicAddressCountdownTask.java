/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.publicAddress;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import org.concordiainternational.competition.ui.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple countdown task for Public Address screen. Runnable task for counting down. run() is invoked every "decrement" milliseconds. For
 * convenience we count down in milliseconds.
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

    private IntermissionTimerEvent timerEvent;

    PublicAddressCountdownTask(Timer countdownTimer, int countdownFrom, int decrement, SessionData masterData) {
        this.masterData = masterData;
        this.startTime = countdownFrom;
        this.countdownTimer = countdownTimer;
        this.timerEvent = new IntermissionTimerEvent();

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
     * 
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

    /*
     * (non-Javadoc)
     * 
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
        timerEvent.setRemainingMilliseconds(ticks);
        masterData.fireBlackBoardEvent(timerEvent);
    }

    private void noTimeLeft() {
        logger.trace("time over: " + ticks / 1000 + " " + (System.currentTimeMillis() - startMillis)); //$NON-NLS-1$ //$NON-NLS-2$
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
