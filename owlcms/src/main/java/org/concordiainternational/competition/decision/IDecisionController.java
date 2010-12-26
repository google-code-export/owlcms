package org.concordiainternational.competition.decision;

import org.concordiainternational.competition.mobile.IRefereeConsole;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.TimeStoppedNotificationReason;

public interface IDecisionController {

	/**
	 * Need to block decisions if a session is underway, unblocking when lifter is announced
	 * or time has started.
	 */
	public abstract boolean isBlocked();
	public void setBlocked(boolean blocked);
	
	public abstract void reset();

	/**
	 * Record a decision made by the officials, broacasting to the listeners.
	 * @param refereeNo
	 * @param accepted
	 */
	public abstract void decisionMade(int refereeNo, boolean accepted);

	/**
	 * Register a new SessionData.Listener object with a SessionData in order to be
	 * informed of updates.
	 * 
	 * @param listener
	 */
	public abstract void addListener(DecisionEventListener listener);

	/**
	 * Remove a specific SessionData.Listener object
	 * 
	 * @param listener
	 */
	public abstract void removeListener(DecisionEventListener listener);

	// timer events
	public abstract void finalWarning(int timeRemaining);

	public abstract void forceTimeRemaining(int startTime,
			CompetitionApplication originatingApp,
			TimeStoppedNotificationReason reason);

	public abstract void initialWarning(int timeRemaining);

	public abstract void noTimeLeft(int timeRemaining);

	public abstract void normalTick(int timeRemaining);

	public abstract void pause(int timeRemaining, CompetitionApplication app,
			TimeStoppedNotificationReason reason);

	public abstract void start(int timeRemaining);

	public abstract void stop(int timeRemaining, CompetitionApplication app,
			TimeStoppedNotificationReason reason);

	public abstract void addListener(IRefereeConsole refereeConsole,
			int refereeIndex);

}