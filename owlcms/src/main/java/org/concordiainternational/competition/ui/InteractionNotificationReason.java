/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

public enum InteractionNotificationReason {
    UNKNOWN,
    STOP_START_BUTTON,
    FORCE_AS_CURRENT,
    CURRENT_LIFTER_CHANGE_DONE,
    CURRENT_LIFTER_CHANGE_STARTED,
    LIFTER_WITHDRAWAL,
    REFEREE_DECISION,
    NOT_ANNOUNCED,
    NO_TIMER,
    CLOCK_EXPIRED
}
