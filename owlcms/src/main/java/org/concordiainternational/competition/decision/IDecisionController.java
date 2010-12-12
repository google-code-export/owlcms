package org.concordiainternational.competition.decision;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.IRefereeConsole;
import org.concordiainternational.competition.ui.TimeStoppedNotificationReason;

public interface IDecisionController {

	/**
	 * TODO call RefereeDecisionController.reset() when timer starts running.
	 */
	public abstract void reset();

	/**
	 * TODO ignore decisions until announcer has announced or clock has started running.
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