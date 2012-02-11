/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.decision;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.mobile.IRefereeConsole;
import org.concordiainternational.competition.timer.CountdownTimerListener;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.TimeStoppedNotificationReason;
import org.concordiainternational.competition.utils.EventHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.EventRouter;

/**
 * Registers decisions from referees and notifies interested UI Components
 * 
 * @author jflamy
 */
public class JuryDecisionController implements IDecisionController, CountdownTimerListener {

    private static final int RESET_DELAY = 5000;

    /**
     * 3 seconds to change decision (after all refs have selected)
     */
    private static final int DECISION_REVERSAL_DELAY = 4000;

	/**
	 * Time before displaying decision once all referees have pressed.
	 */
	//private static final int DECISION_DISPLAY_DELAY = 1000;

    Logger logger = LoggerFactory.getLogger(JuryDecisionController.class);

    Decision[] juryDecisions = new Decision[3];
    DecisionEventListener[] listeners = new DecisionEventListener[3];

    public JuryDecisionController(SessionData groupData) {
        for (int i = 0; i < juryDecisions.length; i++) {
            juryDecisions[i] = new Decision();
        }
    }

    long allDecisionsMadeTime = 0L; // all 3 referees have pressed
    int decisionsMade = 0;
    private EventRouter eventRouter;

	@SuppressWarnings("unused")
	private boolean blocked = true;
    
    /**
     * Enable jury devices.
     */
    @Override
	public void reset() {
        for (int i = 0; i < juryDecisions.length; i++) {
            juryDecisions[i].accepted = null;
            juryDecisions[i].time = 0L;
        }
        allDecisionsMadeTime = 0L; // all 3 referees have pressed
        decisionsMade = 0;
        fireEvent(new DecisionEvent(this, DecisionEvent.Type.RESET, System.currentTimeMillis(), juryDecisions));
    }


    /* (non-Javadoc)
     * @see org.concordiainternational.competition.decision.IDecisionController#decisionMade(int, boolean)
     */
    @Override
	public synchronized void decisionMade(int refereeNo, boolean accepted) {
    	if (isBlocked()) return;
    	
        final long currentTimeMillis = System.currentTimeMillis();
        long deltaTime = currentTimeMillis - allDecisionsMadeTime;
        if (decisionsMade == 3 && deltaTime > DECISION_REVERSAL_DELAY) {
            // too late to reverse decision
            logger.info("decision ignored from jury {}: {} (too late by {} ms)", new Object[] { refereeNo + 1,
                    (accepted ? "lift" : "no lift"), deltaTime - DECISION_REVERSAL_DELAY });
            return;
        }

        juryDecisions[refereeNo].accepted = accepted;
        juryDecisions[refereeNo].time = currentTimeMillis;
        logger.info("decision by jury {}: {}", refereeNo + 1, (accepted ? "lift" : "no lift"));

        decisionsMade = 0;
        int pros = 0;
        int cons = 0;
        for (int i = 0; i < juryDecisions.length; i++) {
            final Boolean accepted2 = juryDecisions[i].accepted;
            if (accepted2 != null) {
                decisionsMade++;
                if (accepted2) pros++;
                else cons++;
            }
        }

        if (decisionsMade == 2) {
                fireEvent(new DecisionEvent(this, DecisionEvent.Type.WAITING, currentTimeMillis, juryDecisions));
        } else if (decisionsMade == 3) {
            // broadcast the decision
            if (allDecisionsMadeTime == 0L) {
                // all 3 referees have just made a choice; schedule the display
                // in 3 seconds
                allDecisionsMadeTime = System.currentTimeMillis();
                scheduleDisplay(currentTimeMillis);
                scheduleBlock();
                scheduleReset();
            }
            // referees have changed their mind
            fireEvent(new DecisionEvent(this, DecisionEvent.Type.UPDATE, currentTimeMillis, juryDecisions));
        }
    }


    /**
     * @param currentTimeMillis
     * @param goodLift
     */
    private void scheduleDisplay(final long currentTimeMillis) {
    	// display right away
    	fireEvent(new DecisionEvent(JuryDecisionController.this, DecisionEvent.Type.SHOW, currentTimeMillis,
              juryDecisions));
//       Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                fireEvent(new DecisionEvent(JuryDecisionController.this, DecisionEvent.Type.SHOW, currentTimeMillis,
//                        juryDecisions));
//            }
//        }, DECISION_DISPLAY_DELAY);
    }
    
    /**
     * 
     */
    private void scheduleBlock() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            	fireEvent(new DecisionEvent(JuryDecisionController.this, DecisionEvent.Type.BLOCK, System.currentTimeMillis(), juryDecisions));
            	setBlocked(true);
            }
        }, DECISION_REVERSAL_DELAY);
    }


	/**
     * 
     */
    private void scheduleReset() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                reset();
            }
        }, DECISION_REVERSAL_DELAY + RESET_DELAY);
    }

    /**
     * This method is the Java object for the method in the Listener interface.
     * It allows the framework to know how to pass the event information.
     */
    private static final Method DECISION_EVENT_METHOD = EventHelper.findMethod(DecisionEvent.class, 
    		// when receiving this type of event
        DecisionEventListener.class, // an object implementing this interface...
        "updateEvent"); // ... will be called with this method. //$NON-NLS-1$;

    /**
     * Broadcast a SessionData.event to all registered listeners
     * 
     * @param updateEvent
     *            contains the source (ourself) and the list of properties to be
     *            refreshed.
     */
    protected void fireEvent(DecisionEvent updateEvent) {
        // logger.trace("SessionData: firing event from groupData"+System.identityHashCode(this)+" first="+updateEvent.getCurrentLifter()+" eventRouter="+System.identityHashCode(eventRouter));
        // logger.trace("                        listeners"+eventRouter.dumpListeners(this));
        if (eventRouter != null) {
            eventRouter.fireEvent(updateEvent);
        }

    }

    /**
     * Register a new SessionData.Listener object with a SessionData in order to be
     * informed of updates.
     * 
     * @param listener
     */
    @Override
	public void addListener(DecisionEventListener listener) {
        logger.debug("add listener {}", listener); //$NON-NLS-1$
        getEventRouter().addListener(DecisionEvent.class, listener, DECISION_EVENT_METHOD);
    }

    /**
     * Remove a specific SessionData.Listener object
     * 
     * @param listener
     */
    @Override
	public void removeListener(DecisionEventListener listener) {
        if (eventRouter != null) {
            logger.debug("hide listener {}", listener); //$NON-NLS-1$
            eventRouter.removeListener(DecisionEvent.class, listener, DECISION_EVENT_METHOD);
        }
    }

    /*
     * General event framework: we implement the
     * com.vaadin.event.MethodEventSource interface which defines how a notifier
     * can call a method on a listener to signal an event an event occurs, and
     * how the listener can register/unregister itself.
     */

    /**
     * @return the object's event router.
     */
    private EventRouter getEventRouter() {
        if (eventRouter == null) {
            eventRouter = new EventRouter();
            logger.trace("new event router for JuryDecisionController " + System.identityHashCode(this) + " = " + System.identityHashCode(eventRouter)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return eventRouter;
    }

    // timer events
    @Override
    public void finalWarning(int timeRemaining) {
    }

    @Override
    public void forceTimeRemaining(int startTime, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
    }

    @Override
    public void initialWarning(int timeRemaining) {
    }

    @Override
    public void noTimeLeft(int timeRemaining) {
    }

    @Override
    public void normalTick(int timeRemaining) {
    }

    @Override
    public void pause(int timeRemaining, CompetitionApplication app, TimeStoppedNotificationReason reason) {
    }

    @Override
    public void start(int timeRemaining) {
    }

    @Override
    public void stop(int timeRemaining, CompetitionApplication app, TimeStoppedNotificationReason reason) {
    }

	@Override
	public void addListener(IRefereeConsole refereeConsole, int refereeIndex) {
		if (listeners[refereeIndex] != null) {
			logger.trace("removing previous JuryConsole listener {}",listeners[refereeIndex]);
			removeListener(listeners[refereeIndex]);
		}
		addListener(refereeConsole);
		listeners[refereeIndex] = refereeConsole;
		logger.trace("adding new JuryConsole listener {}",listeners[refereeIndex]);
	}

	@Override
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	@Override
	public boolean isBlocked() {
		//return blocked &&  (groupData.getCurrentSession() != null);
		return false; // does not matter for jury
	}


	@Override
	public Lifter getLifter() {
		return null;
	}

}
