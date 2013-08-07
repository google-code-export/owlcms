/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.timer;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.TimeStoppedNotificationReason;

public interface CountdownTimerListener {

    void finalWarning(int timeRemaining);

    void initialWarning(int timeRemaining);

    void noTimeLeft(int timeRemaining);

    void normalTick(int timeRemaining);

    /**
     * timer has been stopped, lifter is still associated with timer.
     * 
     * @param timeRemaining
     * @param reason 
     * @param competitionApplication 
     */
    void pause(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason);

    void start(int timeRemaining);

    /**
     * timer has been stopped and associated lifter has been cleared.
     * 
     * @param timeRemaining
     */
    void stop(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason);

    /**
     * someone is forcing the amount of time.
     * 
     * @param startTime
     */
    void forceTimeRemaining(int startTime, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason);

}
