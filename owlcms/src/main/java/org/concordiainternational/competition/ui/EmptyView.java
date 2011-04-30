package org.concordiainternational.competition.ui;

import org.concordiainternational.competition.ui.components.ApplicationView;

import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class EmptyView extends VerticalLayout implements ApplicationView {

	@Override
	public void refresh() {
	}

	@Override
	public boolean needsMenu() {
		return true;
	}

	@Override
	public void setParametersFromFragment() {
	}

	@Override
	public String getFragment() {
		return "";
	}

}
