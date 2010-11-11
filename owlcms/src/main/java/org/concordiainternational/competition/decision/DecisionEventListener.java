package org.concordiainternational.competition.decision;

/**
 * Listener interface for receiving <code>SessionData.DecisionEvent</code>s.
 */
public interface DecisionEventListener extends java.util.EventListener {

    /**
     * This method will be invoked when a SessionData.DecisionEvent is fired.
     * 
     * @param updateEvent
     *            the event that has occured.
     */
    public void updateEvent(DecisionEvent updateEvent);
}