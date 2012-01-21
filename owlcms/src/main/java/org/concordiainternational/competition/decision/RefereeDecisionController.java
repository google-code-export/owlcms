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
public class RefereeDecisionController implements CountdownTimerListener, IDecisionController {

    private static final int RESET_DELAY = 4000;

    /**
     * 3 seconds to change decision (after all refs have selected)
     */
    private static final int DECISION_REVERSAL_DELAY = 3000;

	/**
	 * Time before displaying decision once all referees have pressed.
	 */
	private static final int DECISION_DISPLAY_DELAY = 1000;

    Logger logger = LoggerFactory.getLogger(RefereeDecisionController.class);

    Decision[] refereeDecisions = new Decision[3];
    DecisionEventListener[] listeners = new DecisionEventListener[3];

    private SessionData groupData;
	private Tone downSignal = null;

    public RefereeDecisionController(SessionData groupData) {
        this.groupData = groupData;
        for (int i = 0; i < refereeDecisions.length; i++) {
            refereeDecisions[i] = new Decision();
        }
        try {
			downSignal = new Tone(groupData.getPlatform().getMixer(), 1100, 1200, 1.0);
		} catch (Exception e) {
			// leave as null if not able to emit.
		}
    }



    long allDecisionsMadeTime = 0L; // all 3 referees have pressed
    int decisionsMade = 0;
    private EventRouter eventRouter;

	private Boolean downSignaled = false;
	


	private boolean blocked = true;

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#reset()
	 */
    @Override
	public void reset() {
        for (int i = 0; i < refereeDecisions.length; i++) {
            refereeDecisions[i].accepted = null;
            refereeDecisions[i].time = 0L;
        }
        allDecisionsMadeTime = 0L; // all 3 referees have pressed
        decisionsMade = 0;
        downSignaled = false;
        groupData.setAnnouncerEnabled(true);
        fireEvent(new DecisionEvent(this, DecisionEvent.Type.RESET, System.currentTimeMillis(), refereeDecisions));
    }

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#decisionMade(int, boolean)
	 */
    @Override
	public synchronized void decisionMade(int refereeNo, boolean accepted) {
    	if (isBlocked()) return;

    	if (refereeDecisions[refereeNo] == null) {
    		logger.warn("decision ignored from referee {}: {} (not in a session)", 
        			refereeNo + 1,
                    (accepted ? "lift" : "no lift"));
        	return;
    	}
// can't happen: the device itself is disabled when a red is given.
//        if ((refereeDecisions[refereeNo].accepted != null) && !(refereeDecisions[refereeNo].accepted)) {
//        	// cannot reverse from red to white.
//        	logger.warn("decision ignored from referee {}: {} (prior decision was {})", 
//        			new Object[] { refereeNo + 1,
//                    (accepted ? "lift" : "no lift"), 
//                    refereeDecisions[refereeNo].accepted});
//        	return;
//        }
        
        final long currentTimeMillis = System.currentTimeMillis();
        long deltaTime = currentTimeMillis - allDecisionsMadeTime;
        if (decisionsMade == 3 && deltaTime > DECISION_REVERSAL_DELAY) {
            // too late to reverse decision
            logger.warn("decision ignored from referee {}: {} (too late by {} ms)", new Object[] { refereeNo + 1,
                    (accepted ? "lift" : "no lift"), deltaTime - DECISION_REVERSAL_DELAY });
            return;
        }


        
        refereeDecisions[refereeNo].accepted = accepted;
        refereeDecisions[refereeNo].time = currentTimeMillis;
        logger.info("decision by referee {}: {}", refereeNo + 1, (accepted ? "lift" : "no lift"));

        decisionsMade = 0;
        int pros = 0;
        int cons = 0;
        for (int i = 0; i < refereeDecisions.length; i++) {
            final Boolean accepted2 = refereeDecisions[i].accepted;
            if (accepted2 != null) {
                decisionsMade++;
                if (accepted2) pros++;
                else cons++;
            }
        }

        
        if (decisionsMade >= 2) {
        	// audible down signal is emitted right away by the main computer.
            // request lifter-facing display should display the "down" signal.
        	// also, downSignal() signals timeKeeper that time has been stopped if they
        	// had not stopped it manually.
            if (pros >= 2 || cons >= 2) {
            	synchronized (groupData.getTimer()) {
            		if (!downSignaled && downSignal != null) {
            			new Thread(new Runnable() {
							@Override
							public void run() {
								downSignal.emit();
								groupData.downSignal();
							}
						}).start();
						downSignaled = true;
            			fireEvent(new DecisionEvent(this,
            					DecisionEvent.Type.DOWN, currentTimeMillis,
            					refereeDecisions));
            			logger.warn("*** audible down signal");
            		}
            	}
            } else {
            	logger.debug("no majority");
                fireEvent(new DecisionEvent(this, DecisionEvent.Type.WAITING, currentTimeMillis, refereeDecisions));
            }
        } else {
            // Jury sees all changes, other displays will ignore this.
            synchronized (groupData.getTimer()) {
            	logger.debug("broadcasting");
            	fireEvent(new DecisionEvent(this, DecisionEvent.Type.UPDATE, currentTimeMillis, refereeDecisions));
            }
        }

        if (decisionsMade == 3) {
        	// NOTE: we wait for referee keypads to be blocked (see scheduleBlock)
        	// before sending the decision to groupData.
            
            // broadcast the decision
            if (allDecisionsMadeTime == 0L) {
                // all 3 referees have just made a choice; schedule the display
                // in 3 seconds
                allDecisionsMadeTime = System.currentTimeMillis();
                logger.info("all decisions made {}", allDecisionsMadeTime);
                groupData.setAnnouncerEnabled(false);
                scheduleDisplay(currentTimeMillis);
                scheduleBlock();
                scheduleReset();
            } else {
                // referees have changed their mind
            	logger.warn("three + change");
                fireEvent(new DecisionEvent(this, DecisionEvent.Type.UPDATE, currentTimeMillis, refereeDecisions));
            }
        }



    }

    /**
     * @param currentTimeMillis
     * @param goodLift
     */
    private void scheduleDisplay(final long currentTimeMillis) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireEvent(new DecisionEvent(RefereeDecisionController.this, DecisionEvent.Type.SHOW, currentTimeMillis,
                        refereeDecisions));
            }
        }, DECISION_DISPLAY_DELAY);
    }
    
    /**
     * 
     */
    private void scheduleBlock() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            	// save the decision
                groupData.majorityDecision(refereeDecisions);
                
            	fireEvent(new DecisionEvent(RefereeDecisionController.this, DecisionEvent.Type.BLOCK, System.currentTimeMillis(), refereeDecisions));
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

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#addListener(org.concordiainternational.competition.decision.DecisionEventListener)
	 */
    @Override
	public void addListener(DecisionEventListener listener) {
        logger.debug("add listener {}", listener); //$NON-NLS-1$
        getEventRouter().addListener(DecisionEvent.class, listener, DECISION_EVENT_METHOD);
    }

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#removeListener(org.concordiainternational.competition.decision.DecisionEventListener)
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
            logger
                    .trace("new event router for RefereeDecisionController " + System.identityHashCode(this) + " = " + System.identityHashCode(eventRouter)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return eventRouter;
    }

    // timer events
    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#finalWarning(int)
	 */
	@Override
    public void finalWarning(int timeRemaining) {
    }

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#forceTimeRemaining(int, org.concordiainternational.competition.ui.CompetitionApplication, org.concordiainternational.competition.ui.TimeStoppedNotificationReason)
	 */
	@Override
    public void forceTimeRemaining(int startTime, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
    }

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#initialWarning(int)
	 */
	@Override
    public void initialWarning(int timeRemaining) {
    }

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#noTimeLeft(int)
	 */
	@Override
    public void noTimeLeft(int timeRemaining) {
    }

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#normalTick(int)
	 */
	@Override
    public void normalTick(int timeRemaining) {
    }

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#pause(int, org.concordiainternational.competition.ui.CompetitionApplication, org.concordiainternational.competition.ui.TimeStoppedNotificationReason)
	 */
	@Override
    public void pause(int timeRemaining, CompetitionApplication app, TimeStoppedNotificationReason reason) {
    }

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#start(int)
	 */
	@Override
    public void start(int timeRemaining) {
    }

    /* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#stop(int, org.concordiainternational.competition.ui.CompetitionApplication, org.concordiainternational.competition.ui.TimeStoppedNotificationReason)
	 */
	@Override
    public void stop(int timeRemaining, CompetitionApplication app, TimeStoppedNotificationReason reason) {
    }

	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.decision.IDecisionController#addListener(org.concordiainternational.competition.ui.IRefereeConsole, int)
	 */
	@Override
	public void addListener(IRefereeConsole refereeConsole, int refereeIndex) {
		if (listeners[refereeIndex] != null) {
			logger.trace("removing previous ORefereeConsole listener {}",listeners[refereeIndex]);
			removeListener(listeners[refereeIndex]);
		}
		addListener(refereeConsole);
		listeners[refereeIndex] = refereeConsole;
		logger.trace("adding new ORefereeConsole listener {}",listeners[refereeIndex]);
	}

	@Override
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	@Override
	public boolean isBlocked() {
		return blocked &&  (groupData.getCurrentSession() != null);
	}

	@Override
	public Lifter getLifter() {
		return groupData.getCurrentLifter();
	}
}
