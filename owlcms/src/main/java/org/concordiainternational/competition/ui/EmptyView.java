package org.concordiainternational.competition.ui;

import java.net.URL;

import org.concordiainternational.competition.ui.components.ApplicationView;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

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

	@Override
	public void registerAsListener() {
		CompetitionApplication.getCurrent().getMainWindow().addListener((CloseListener) this);
	}

	@Override
	public void unregisterAsListener() {
		CompetitionApplication.getCurrent().getMainWindow().addListener((CloseListener) this);
	}
	
	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();	
	}

	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		registerAsListener();
		return null;
	}

}
