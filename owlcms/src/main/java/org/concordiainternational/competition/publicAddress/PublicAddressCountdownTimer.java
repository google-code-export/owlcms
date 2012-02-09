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
import java.util.Date;
import java.util.Timer;

import org.concordiainternational.competition.ui.SessionData;
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
    private int requestedSeconds;
    private int remainingSeconds;
    Timer timer = null;
    private PublicAddressCountdownTask countdownTask;

	private SessionData masterData;

	private Date endTime = new Date();

	private boolean paused = false;

    public PublicAddressCountdownTimer(SessionData masterData) {
    	this.masterData = masterData;
        logger.debug("new"); //$NON-NLS-1$
    }

    /**
     * Start the timer.
     */
    public void start() {
        logger.debug("enter start {}", getRequestedSeconds()); //$NON-NLS-1$
        if (timer != null) {
            timer.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (requestedSeconds <= 0) {
            requestedSeconds = 0;
            return;
        }
        remainingSeconds = requestedSeconds;
        timer = new Timer();
        countdownTask = new PublicAddressCountdownTask(timer, requestedSeconds*1000, DECREMENT, masterData);
        timer.scheduleAtFixedRate(countdownTask, 0, // start right away
            DECREMENT);
    }

    /**
     * Restart the timer after a pause, else start from scratch.
     */
    public void restart() {
        logger.debug("enter restart paused={}", paused); //$NON-NLS-1$
        if (paused) {
        	unPause();
        } else {
        	start();
        }
    }

	/**
	 * restart after pause.
	 */
	private void unPause() {
		logger.debug("enter unPause remainingSeconds={}", remainingSeconds); //$NON-NLS-1$
        paused = false;
		if (timer != null) {
            timer.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (remainingSeconds <= 0) {
        	remainingSeconds = 0;
            return;
        }

        timer = new Timer();
        countdownTask = new PublicAddressCountdownTask(timer, remainingSeconds*1000, DECREMENT, masterData);
        timer.scheduleAtFixedRate(countdownTask, 0, // start right away
            DECREMENT);
        

	}

    /**
     * Stop the timer such that it can be restarted.
     */
    public void pause() {
        logger.debug("enter pause remainingMillis={}", getRemainingMilliseconds()); //$NON-NLS-1$
        paused = true;
        if (timer != null) timer.cancel();
        timer = null;
        if (countdownTask != null) {
            countdownTask.cancel();
            remainingSeconds = (int) Math.round(countdownTask.getBestTimeRemaining()/1000D);
            countdownTask = null;
        }
    }

	/**
	 * 
	 */
	private void fireTimerEvent(int remainingSeconds1) {
		PublicAddressTimerEvent timerEvent = new PublicAddressTimerEvent();
        timerEvent.setRemainingMilliseconds(remainingSeconds1*1000);
        masterData.fireBlackBoardEvent(timerEvent);
	}

    /**
     * Stop the timer.
     * Same as Pause, in this case.
     */
    public void stop() {
        logger.debug("enter stop remainingMillis={}", getRemainingMilliseconds()); //$NON-NLS-1$
        pause();
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
    public void setRequestedSeconds(int i) {
        logger.debug("setRequestedSeconds {}",i);
        stop();
        paused = false;
        this.requestedSeconds = i;
        this.endTime = new Date(System.currentTimeMillis()+(i*1000));
        fireTimerEvent(requestedSeconds);
    }

    public int getRequestedSeconds() {
        return requestedSeconds;
    }

    public void setEndTime(Date endTime) {
    	logger.debug("setEndTime {}",endTime);
    	stop();
    	paused = false;
    	this.endTime = endTime;
    	if (endTime == null) return;
    	long deltaMillis = endTime.getTime() - System.currentTimeMillis();
    	if (deltaMillis > 0) {
    		requestedSeconds = (int) Math.round(deltaMillis / 1000D);
    	} else {
    		requestedSeconds = 0;
    	}
        fireTimerEvent(requestedSeconds);
    }
    
    public Date getEndTime() {
    	return endTime;
    }
    
    public int getRemainingMilliseconds() {
    	if (isRunning()) {
    		return (int) countdownTask.getBestTimeRemaining();
    	} else {
    		return remainingSeconds*1000;
    	}
    }


}
