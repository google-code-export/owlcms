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
	
	Integer remainingMilliseconds = 0;
	Boolean noTimeLeft = false;
	
	public Integer getRemainingMilliseconds() {
		return remainingMilliseconds;
	}
	public void setRemainingMilliseconds(Integer remainingSeconds) {
		this.remainingMilliseconds = remainingSeconds;
	}
	public Boolean getNoTimeLeft() {
		return noTimeLeft;
	}
	public void setNoTimeLeft(Boolean noTimeLeft) {
		this.noTimeLeft = noTimeLeft;
	}
	

}
