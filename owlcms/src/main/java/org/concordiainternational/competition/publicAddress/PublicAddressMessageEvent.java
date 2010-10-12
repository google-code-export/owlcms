package org.concordiainternational.competition.publicAddress;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.annotation.ListenerMethod;

/**
 * Events received by the screens that display public messages.
 * @author jflamy
 *
 */
public class PublicAddressMessageEvent implements Event {
	
	public interface MessageDisplayListener extends Listener {
		@ListenerMethod
		public void messageUpdate(final PublicAddressMessageEvent event);
	}
	
	String title;
	String message;
	Boolean hide = false;
	Integer remainingMilliseconds;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Boolean setHide() {
		return hide;
	}
	public void setHide(Boolean remove) {
		this.hide = remove;
	}
	public Integer getRemainingMilliseconds() {
		return remainingMilliseconds;
	}
	public void setRemainingMilliseconds(Integer remainingMilliseconds) {
		this.remainingMilliseconds = remainingMilliseconds;
	}


}
