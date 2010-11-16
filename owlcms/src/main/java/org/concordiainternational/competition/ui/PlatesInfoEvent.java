package org.concordiainternational.competition.ui;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.annotation.ListenerMethod;
import com.vaadin.ui.Component;

public class PlatesInfoEvent implements Event {
	
	public Component source;
	
	PlatesInfoEvent(Component source) {
		this.source = source;
	}

	public interface PlatesInfoListener extends Listener {
		@ListenerMethod
		public void plateLoadingUpdate(final PlatesInfoEvent event);
	}

}
