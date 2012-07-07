/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.publicAddress;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.annotation.ListenerMethod;

/**
 * Events received by the screens that display public messages.
 * @author jflamy
 *
 */
public class PublicAddressTimerEvent implements Event {
	
	public interface MessageTimerListener extends Listener {
		@ListenerMethod
		public void timerUpdate(final PublicAddressTimerEvent event);
	}
	
	Integer remainingMilliseconds = null;
	Boolean noTimeLeft = false;
	
	public Integer getRemainingMilliseconds() {
		return remainingMilliseconds;
	}
	public void setRemainingMilliseconds(Integer remainingMilliseconds) {
		this.remainingMilliseconds = remainingMilliseconds;
	}
	public Boolean getNoTimeLeft() {
		return noTimeLeft;
	}
	public void setNoTimeLeft(Boolean noTimeLeft) {
		this.noTimeLeft = noTimeLeft;
	}
	

}
