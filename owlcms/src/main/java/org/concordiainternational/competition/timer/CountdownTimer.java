/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.timer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.LifterInfo;
import org.concordiainternational.competition.ui.InteractionNotificationReason;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.ui.AbstractComponent;

/**
 * Master stopwatch for a competition session.
 * 
 * @author jflamy
 * 
 */
public class CountdownTimer implements Serializable {

    private static final long serialVersionUID = 8921641103581546472L;

    final static Logger logger = LoggerFactory.getLogger(CountdownTimer.class);

    private boolean platformFree;
    private Lifter owner = null;

    final private static int DECREMENT = 100; // milliseconds
    private int timeRemaining;
    private Timer timer = null;
    private CountdownTask countdownTask;

    /*
     * listeners : the display for the lifter and the buzzer controller are
     * treated specially as they require immediate attention.
     * then all other listeners are treated in whatever order.
     */
    private Set<CountdownTimerListener> listeners = new HashSet<CountdownTimerListener>();
    private CountdownTimerListener countdownDisplay = null;
    
    /**
     * Buzzer is controlled by LifterInfo on announcer console
     * (legacy design -- buzzer used to be on client side)
     */
    private LifterInfo masterBuzzer = null;

    private boolean started = false;

    public CountdownTimer() {
        logger.debug("new {}",this); //$NON-NLS-1$
    }

    /**
     * Start the timer.
     */
    public void start() {
        logger.debug("enter start {} {}", this, getTimeRemaining()); //$NON-NLS-1$
        if (timer != null) {
            timer.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (owner == null) return;
        if (timeRemaining <= 0) {
            setTimeRemaining(0);
            return;
        }

        timer = new Timer();

        countdownTask = new CountdownTask(this, timeRemaining, DECREMENT);
        timer.scheduleAtFixedRate(countdownTask,
        		0, // start right away
        		DECREMENT); // 100ms precision is good enough
        
        final Set<CountdownTimerListener> listeners2 = getListeners();
        logger.trace("start: {}  - {} listeners", timeRemaining, listeners2.size()); //$NON-NLS-1$
        setStarted(true);
        if (countdownDisplay != null) {
            countdownDisplay.start(timeRemaining);
        }
        if (masterBuzzer != null) {
            masterBuzzer.start(timeRemaining);
        }
        for (CountdownTimerListener curListener : listeners2) {
        	// avoid duplicate notifications
        	if (curListener != masterBuzzer && curListener != countdownDisplay) {
        		curListener.start(timeRemaining);
        	}
        }
    }

    /**
     * Start the timer.
     */
    public void restart() {
        logger.debug("enter restart {} {}", getTimeRemaining()); //$NON-NLS-1$
        start();
    }

    /**
     * Stop the timer, without clearing the associated lifter.
     */
    public void pause() {
    	pause(InteractionNotificationReason.UNKNOWN);
    }
    
	@SuppressWarnings("unchecked")
	public void pause(InteractionNotificationReason reason) {
        logger.debug("enter pause {} {}", getTimeRemaining()); //$NON-NLS-1$
        if (countdownTask != null) {
            setTimeRemaining((int) countdownTask.getBestTimeRemaining());
        }
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        setStarted(false);
        logger.info("pause: {}", timeRemaining); //$NON-NLS-1$
        if (countdownDisplay != null) {
            countdownDisplay.pause(timeRemaining, CompetitionApplication.getCurrent(), reason);
        }
        if (masterBuzzer != null) {
            masterBuzzer.pause(timeRemaining, CompetitionApplication.getCurrent(), reason);
        }
        
        final HashSet<CountdownTimerListener> listenerSet = (HashSet<CountdownTimerListener>) getListeners();
		for (CountdownTimerListener curListener : (HashSet<CountdownTimerListener>)listenerSet.clone()) {
        	// avoid duplicate notifications
        	if (curListener != masterBuzzer && curListener != countdownDisplay) {
        		
        		// avoid notifying orphaned listeners
        		if (curListener instanceof AbstractComponent) {
        			AbstractComponent curComponent = (AbstractComponent)curListener;
        			Application curApp = curComponent.getApplication();
        			if (curApp == null) {
        				// application is null, the listener is an orphan
        				listenerSet.remove(curListener);
        			} else {
        				curListener.pause(timeRemaining, CompetitionApplication.getCurrent(), reason);
        			}
        		} else {
        			curListener.pause(timeRemaining, CompetitionApplication.getCurrent(), reason);
        		}
        	}
        }
	}

    /**
     * Stop the timer, clear the associated lifter.
     */
    public void stop() {
    	stop(InteractionNotificationReason.UNKNOWN);
    }
    
    public void stop(InteractionNotificationReason reason) {
        logger.debug("enter stop {} {}", getTimeRemaining()); //$NON-NLS-1$
        if (timer != null) timer.cancel();
        timer = null;
        if (countdownTask != null) {
            countdownTask.cancel();
            setTimeRemaining((int) countdownTask.getBestTimeRemaining());
        }
        setOwner(null);
        setStarted(false);
        logger.info("stop: {}", timeRemaining); //$NON-NLS-1$
        if (countdownDisplay != null) {
            countdownDisplay.stop(timeRemaining, CompetitionApplication.getCurrent(), reason);
        }
        if (masterBuzzer != null) {
            masterBuzzer.stop(timeRemaining, CompetitionApplication.getCurrent(), reason);
        }
        for (CountdownTimerListener curListener : getListeners()) {
        	// avoid duplicate notifications
        	if (curListener != masterBuzzer && curListener != countdownDisplay) {
        		if (curListener instanceof AbstractComponent) {
        			final Application application = ((AbstractComponent) curListener).getApplication();
					if (application != null) {
						curListener.stop(timeRemaining, CompetitionApplication.getCurrent(), reason);
					}
        		} else {
        			curListener.stop(timeRemaining, CompetitionApplication.getCurrent(), reason);
        		}
        	}
        }
    }

    /**
     * Set the remaining time explicitly. Meant to be called only by SessionData()
     * so that the time is reset correctly.
     */
    public void forceTimeRemaining(int remainingTime) {
    	forceTimeRemaining(remainingTime, InteractionNotificationReason.UNKNOWN);
    }
    
    public void forceTimeRemaining(int remainingTime,InteractionNotificationReason reason) {
        if (timer != null) timer.cancel();
        timer = null;
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        setTimeRemaining(remainingTime);
        logger.info("forceTimeRemaining: {}", getTimeRemaining()); //$NON-NLS-1$
        if (countdownDisplay != null) {
            countdownDisplay.forceTimeRemaining(timeRemaining, CompetitionApplication.getCurrent(), reason);
        }
        if (masterBuzzer != null) {
            masterBuzzer.forceTimeRemaining(timeRemaining, CompetitionApplication.getCurrent(), reason);
        }
        
        for (CountdownTimerListener curListener : getListeners()) {
        	// avoid duplicate notifications
        	if (curListener != masterBuzzer && curListener != countdownDisplay) {
        		if (curListener instanceof AbstractComponent) {
        			final Application application = ((AbstractComponent) curListener).getApplication();
					if (application != null) {
						curListener.forceTimeRemaining(getTimeRemaining(), CompetitionApplication.getCurrent(), reason);
					}
        		} else {
        			curListener.forceTimeRemaining(getTimeRemaining(), CompetitionApplication.getCurrent(), reason);
        		}
        	}
        }
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
    public void setTimeRemaining(int i) {
        logger.debug("setTimeRemaining {}" + i);
        this.timeRemaining = i;
    }

    // private void logException(Logger logger2, Exception exception) {
    // StringWriter sw = new StringWriter();
    // exception.printStackTrace(new PrintWriter(sw));
    // logger.info(sw.toString());
    // }

    public int getTimeRemaining() {
        return timeRemaining;
    }
    
    public Long getRunningTimeRemaining() {
        if (countdownTask != null) {
            return countdownTask.getBestTimeRemaining();
        } else {
            return null;
        }  
    }

    /**
     * Associate the timer with a lifter Set to null to indicate that the time
     * on the timer belongs to no-one in particular.
     * 
     * @param lifter
     */
    public void setOwner(Lifter lifter) {
        // logger.debug("setLifterAnnounced(): {}",lifter);
        // logException(logger,new Exception());
        this.owner = lifter;
    }

    public Lifter getOwner() {
        return owner;
    }

    public void setPlatformFree(boolean platformFree) {
        this.platformFree = platformFree;
    }

    public boolean isPlatformFree() {
        return platformFree;
    }

    public void addListener(CountdownTimerListener timerListener) {
        logger.debug("{} listened by {}", this, timerListener); //$NON-NLS-1$
        listeners.add(timerListener);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    public void removeListener(CountdownTimerListener timerListener) {
        listeners.remove(timerListener);
    }

    public Set<CountdownTimerListener> getListeners() {
        return listeners;
    }

    /**
     * @param countdownDisplay
     *            the countdownDisplay to set
     */
    public void setCountdownDisplay(CountdownTimerListener countdownDisplay) {
        logger.info("countdownDisplay={}", countdownDisplay);
        this.countdownDisplay = countdownDisplay;
    }

    /**
     * @return the countdownDisplay
     */
    public CountdownTimerListener getCountdownDisplay() {
        return countdownDisplay;
    }

    /**
     * @param lifterInfo
     */
    public void setMasterBuzzer(LifterInfo lifterInfo) {
        this.masterBuzzer = lifterInfo;
    }

    /**
     * @return the masterBuzzer
     */
    public LifterInfo getMasterBuzzer() {
        return masterBuzzer;
    }

    @Test
    public void doCountdownTest() {
        try {
            setTimeRemaining(4000);
            final Lifter testLifter = new Lifter();
            setOwner(testLifter);
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
            setOwner(testLifter);
            restart();
            Thread.sleep(3000);
        } catch (InterruptedException t) {
            logger.error(t.getMessage());
        }
    }

	public void cancel() {
		logger.debug("cancelling timer");
		this.timer.cancel();
	}

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }




}
