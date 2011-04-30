package org.concordiainternational.competition.mobile;

import java.net.URL;

import org.concordiainternational.competition.decision.DecisionEvent;
import org.concordiainternational.competition.decision.DecisionEventListener;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.ui.Window.CloseEvent;

public interface IRefereeConsole extends DecisionEventListener {

	@Override
	public abstract void updateEvent(final DecisionEvent updateEvent);

	public abstract void refresh();

	/**
	 * @param refereeIndex
	 */
	public abstract void setIndex(int refereeIndex);

	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.components.ApplicationView#needsMenu()
	 */
	public abstract boolean needsMenu();

	/**
	 * @return
	 */
	public abstract String getFragment();

	/* (non-Javadoc)
	 * @see org.concordiainternational.competition.ui.components.ApplicationView#setParametersFromFragment(java.lang.String)
	 */
	public abstract void setParametersFromFragment();

	/* Will be called when page is loaded.
	 * @see com.vaadin.terminal.URIHandler#handleURI(java.net.URL, java.lang.String)
	 */
	public abstract DownloadStream handleURI(URL context, String relativeUri);

	/* Will be called when page is unloaded (including on refresh).
	 * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
	 */
	public abstract void windowClose(CloseEvent e);

}