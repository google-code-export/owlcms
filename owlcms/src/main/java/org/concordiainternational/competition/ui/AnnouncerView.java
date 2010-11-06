/* 
 * Copyright ©2009 Jean-François Lamy
 * 
 * Licensed under the Open Software Licence, Version 3.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.opensource.org/licenses/osl-3.0.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.concordiainternational.competition.ui;

import java.text.MessageFormat;
import java.util.Locale;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.CompetitionSessionLookup;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.notifique.Notifique;
import org.vaadin.notifique.Notifique.Message;
import org.vaadin.overlay.CustomOverlay;

import com.vaadin.data.Item;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

/**
 * This class defines the screen layout for the announcer.
 * <p>
 * <ul>
 * The top part shows the current lifter information and the lifters in lifting
 * order. This list is actually the container from which data is pulled out.
 * </ul>
 * <ul>
 * Clicking in the lift list selects a lifter, whose detail in shown in the
 * bottom part.
 * </ul>
 * <ul>
 * Editing the bottom part triggers recalculation of the lifting order; this in
 * turn triggers an update event to all layouts that display a lifter list
 * (including this one).
 * </ul>
 * </p>
 * 
 * @author jflamy
 * 
 */
public class AnnouncerView extends VerticalSplitPanel implements ApplicationView, SessionData.UpdateEventListener, EditingView {
    private static final long serialVersionUID = 7881028819569705161L;
    private static final Logger logger = LoggerFactory.getLogger(AnnouncerView.class);
    public static final boolean PUSHING = true;

    private HorizontalLayout topPart;
    private LifterInfo announcerInfo;
    private LiftList liftList;
    private LifterCardEditor lifterCardEditor;
    private CompetitionApplication app;
    private boolean stickyEditor = false;
    private SessionData masterData;
    Mode mode;
    
    private String platformName;
    private String viewName;
    private String groupName;
	private Notifique notifications;

    public enum Mode {
        ANNOUNCER, TIMEKEEPER, MARSHALL, DISPLAY
    }

    /**
     * Create view.
     * 
     * @param mode
     */
    public AnnouncerView(boolean initFromFragment, String viewName, Mode mode) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        
        this.app = CompetitionApplication.getCurrent();
        this.mode = mode;

        if (platformName == null) {
        	// get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        }
        if (app.getPlatform() == null || !platformName.equals(app.getPlatformName())) {
        	app.setPlatformByName(platformName);
        }

        
        masterData = app.getMasterData(platformName);
        if (mode == Mode.ANNOUNCER) {
            final CompetitionSession currentGroup = masterData.getCurrentSession();
            masterData.setAnnouncerView(this);
            masterData.setMasterApplication(this.app);
            
            if (groupName != null && groupName.length() > 0) {
                switchGroup(new CompetitionSessionLookup(app).lookup(groupName));
            } else {
                app.setCurrentCompetitionSession(currentGroup);
                if (currentGroup != null) {
                	 groupName = currentGroup.getName();
                }
               
            }

        }

        // right hand side shows information that the announcer reads aloud
        announcerInfo = new LifterInfo("topPart", masterData, mode, this); //$NON-NLS-1$
        announcerInfo.addStyleName("currentLifterSummary"); //$NON-NLS-1$
        announcerInfo.setWidth(7.0F, Sizeable.UNITS_CM); //$NON-NLS-1$
        announcerInfo.setMargin(true);

        // left side is the lifting order, as well as the menu to switch groups.
        liftList = new LiftList(masterData, this, mode);
        liftList.table.setPageLength(15);
        liftList.table.setSizeFull();
        liftList.setSizeFull();

        topPart = new HorizontalLayout();
        
        setupNotifications();

        synchronized (app) {
        	boolean prevDisabled = app.getPusherDisabled();
        	app.setPusherDisabled(true);
			topPart.setSizeFull();
			topPart.addComponent(liftList);
			topPart.addComponent(announcerInfo);
			topPart.setComponentAlignment(announcerInfo, Alignment.TOP_LEFT);
			topPart.setExpandRatio(liftList, 100.0F);
			this.setFirstComponent(topPart);
			loadFirstLifterInfo(masterData,
					WebApplicationConfiguration.DEFAULT_STICKINESS);
			adjustSplitBarLocation();
			// we are now fully initialized
			masterData.setAllowAll(false);
			masterData.addListener(this);
			if (masterData.lifters.isEmpty()) {
				logger.debug(
						"switching masterData.lifters {}", masterData.lifters); //$NON-NLS-1$
				switchGroup(app.getCurrentCompetitionSession());
			} else {
				logger.debug(
						"not switching masterData.lifters {}", masterData.lifters); //$NON-NLS-1$
			}
			CompetitionApplication.getCurrent().getUriFragmentUtility().setFragment(getFragment(), false);
			app.setPusherDisabled(prevDisabled);
		}
		app.push();
    }



	/**
     * Update the lifter editor and the information panels with the first
     * lifter.
     * 
     * @param masterData
     */
    public void loadFirstLifterInfo(SessionData groupData) {
        final Lifter firstLifter = liftList.getFirstLifter();
        logger.debug("*** first lifter = {}", firstLifter); //$NON-NLS-1$
        final Item firstLifterItem = liftList.getFirstLifterItem();
        announcerInfo.loadLifter(firstLifter, groupData);
        updateLifterEditor(firstLifter, firstLifterItem);
        liftList.clearSelection();
    }

    /**
     * Update the lifter editor and the information panels with the first
     * lifter.
     * 
     * @param masterData
     */
    public void loadFirstLifterInfo(SessionData groupData, boolean sticky) {
        loadFirstLifterInfo(groupData);
        if (lifterCardEditor != null) {
            lifterCardEditor.setSticky(sticky);
        }
    }

    /**
     * Update the lifter editor and the information panels with the first
     * lifter.
     * 
     * @param masterData
     */
    public void editFirstLifterInfo(SessionData groupData, boolean sticky) {
        final Lifter firstLifter = liftList.getFirstLifter();
        final Item firstLifterItem = liftList.getFirstLifterItem();
        updateLifterEditor(firstLifter, firstLifterItem);
        if (lifterCardEditor != null) {
            lifterCardEditor.setSticky(sticky);
        }
        liftList.clearSelection();
    }

    /**
     * @param app
     * @param lifter
     */
    private void updateLifterEditor(final Lifter lifter, final Item lifterItem) {
        // if (stickyEditor) return; // editor has asked not to be affected
        if (lifter != null) {
            if (lifterCardEditor == null) {
                lifterCardEditor = new LifterCardEditor(liftList, this);
                lifterCardEditor.loadLifter(lifter, lifterItem);
                this.setSecondComponent(lifterCardEditor);
            } else {
                lifterCardEditor.loadLifter(lifter, lifterItem);
            }

            lifterCardEditor.setSticky(WebApplicationConfiguration.DEFAULT_STICKINESS); // make
                                                                                        // sticky
        } else {
            // no current lifter, hide bottom part if present.
            if (lifterCardEditor != null) {
                setSecondComponent(new Label("")); //$NON-NLS-1$
                masterData.getTimer().removeAllListeners(lifterCardEditor.lifterCardIdentification);
                lifterCardEditor = null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.Refreshable#refresh()
     */
    @Override
    public void refresh() {
        logger.debug("start refresh ----------{}", mode); //$NON-NLS-1$
        CategoryLookup.getSharedInstance().reload();
        liftList.refresh();
        setStickyEditor(false, false);
        loadFirstLifterInfo(masterData);
        logger.debug("end refresh ----------{}", mode); //$NON-NLS-1$
    }

    /**
     * Set the split bar location
     */
    void adjustSplitBarLocation() {
        // compute percentage of split bar.
        float height = app.getMainWindow().getHeight();
        if (height > 0) {
            this.setSplitPosition((int) ((height - 225) * 100 / height));
        } else {
            this.setSplitPosition(65);
        }
    }

    /**
     * Load the designated lifter in the bottom pane
     * 
     * @param lifter
     * @param lifterItem
     */
    @Override
	public void editLifter(Lifter lifter, Item lifterItem) {
        updateLifterEditor(lifter, lifterItem);
    }

    /**
     * @return true if editor in bottom pane is pinned (not to be updated)
     */
    @Override
	public boolean isStickyEditor() {
        return stickyEditor;
    }

    /**
     * Indicate that the editor at bottom must not be updated
     * 
     * @param freezeLifterCardEditor
     */
    @Override
	public void setStickyEditor(boolean freezeLifterCardEditor) {
        setStickyEditor(freezeLifterCardEditor, true);
    }

    /**
     * Indicate that the editor at bottom must not be updated
     * 
     * @param freezeLifterCardEditor
     */
    @Override
	public void setStickyEditor(boolean freezeLifterCardEditor, boolean reloadLifterInfo) {
        // logger.debug("is frozen: {}",freezeLifterCardEditor);
        boolean wasSticky = this.stickyEditor;
        this.stickyEditor = freezeLifterCardEditor;
        // was sticky, no longer is
        if (reloadLifterInfo && wasSticky && !freezeLifterCardEditor) loadFirstLifterInfo(masterData, false);
        if (lifterCardEditor != null) lifterCardEditor.setSticky(freezeLifterCardEditor);
    }

    /**
     * Copied from interface. Lift order has changed. Update the lift list and
     * editor in the bottom part of the view.
     * 
     * @see org.concordiainternational.competition.ui.SessionData.UpdateEventListener#updateEvent(org.concordiainternational.competition.ui.SessionData.UpdateEvent)
     */
    @Override
    public void updateEvent(SessionData.UpdateEvent updateEvent) {
        synchronized (app) {
            if (updateEvent.getForceRefresh()) {
                logger.debug(
                    "updateEvent() received in {} view -- forced refresh. ----------------------------------", mode); //$NON-NLS-1$
                refresh();
            } else {
                logger.debug(
                    "updateEvent() received in {} view  first is now: {}", this, updateEvent.getCurrentLifter()); //$NON-NLS-1$

                liftList.updateTable();
                loadFirstLifterInfo(masterData, WebApplicationConfiguration.DEFAULT_STICKINESS);

                // update the info on the left side of the bottom part. This
                // depends on the liftList info
                // which has just changed.
                if (lifterCardEditor != null && lifterCardEditor.lifterCardIdentification != null && !stickyEditor) {
                    lifterCardEditor.lifterCardIdentification.loadLifter(updateEvent.getCurrentLifter(), liftList
                            .getGroupData());
                }

                // updateLifterEditor(updateEvent.getCurrentLifter(),
                // liftList.getFirstLifterItem());
            }
        }
        app.push();
    }

    /**
     * Reload data according to this session's (CompetitionApplication) current
     * lifter group.
     * 
     * @param newSession
     */
    private void switchGroup(final CompetitionSession newSession) {
    	CompetitionSession oldSession = masterData.getCurrentSession();
        boolean switching = oldSession != newSession;
        
        if (mode == Mode.ANNOUNCER) {
            

			if (switching) {
	            logger.warn("=============== switching from {} to group {}", oldSession, newSession); //$NON-NLS-1$
	            logger.warn("=============== modifying group data {}", masterData, (newSession != null ? newSession.getName() : null)); //$NON-NLS-1$
            	masterData.setCurrentSession(newSession);
            }
            
            CompetitionSession currentCompetitionSession = masterData.getCurrentSession();
            if (currentCompetitionSession != null) {
                groupName = currentCompetitionSession.getName();
            } else {
                groupName = "";
            }
            
            if (switching) {
            	CompetitionApplication.getCurrent().getUriFragmentUtility().setFragment(getFragment(), false);
            }
        }
    }

    @Override
	public void setCurrentSession(CompetitionSession competitionSession) {
        setStickyEditor(false, false);
        switchGroup(competitionSession);
    }

    /**
     * @param masterData
     *            the masterData to set
     */
    @Override
	public void setSessionData(SessionData sessionData) {
        this.masterData = sessionData;
    }

    /**
     * @return the masterData
     */
    public SessionData getGroupData() {
        return masterData;
    }

    public void selectFirstLifter() {
        liftList.clearSelection();
    }

    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.components.ApplicationView#needsMenu()
     */
    @Override
    public boolean needsMenu() {
        return true;
    }


    /**
     * @return
     */
    @Override
	public String getFragment() {
        return viewName+"/"+(platformName == null ? "" : platformName)+"/"+(groupName == null ? "" : groupName);
    }
    

    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.components.ApplicationView#setParametersFromFragment(java.lang.String)
     */
    @Override
    public void setParametersFromFragment() {
        String frag = CompetitionApplication.getCurrent().getUriFragmentUtility().getFragment();
        String[] params = frag.split("/");
        if (params.length >= 1) {
            viewName = params[0];
        } else {
            throw new RuleViolationException("Error.ViewNameIsMissing"); 
        }
        
        if (params.length >= 2) {
            platformName = params[1];
        } else {
        	platformName = CompetitionApplicationComponents.initPlatformName();
        }
        
        if (params.length >= 3) {
            groupName = params[2];
        } else {
            groupName = null;
        }
    }
    
    private void setupNotifications() {
    	// Notification area. Full width.
    	notifications = new Notifique(true);
    	notifications.setWidth("100%");
    	notifications.setVisibleCount(3);
    	
    	// Hide messages when clicked anywhere (not only with the close
    	// button)
    	notifications.setClickListener(new Notifique.ClickListener() {
    	    private static final long serialVersionUID = 1L;

    	    @Override
    	    public void messageClicked(Message message) {
    	        message.hide();
    	    }
    	});
    	
    	// Display as overlay in top of the main window
    	Window mainWindow = CompetitionApplication.getCurrent().getMainWindow();
		CustomOverlay ol = new CustomOverlay(notifications, mainWindow);
		ol.addStyleName("timeStoppedNotifications");
		mainWindow.addComponent(ol);
    	
    	//notifications.add((Resource)null,"1!",true,Notifique.Styles.VAADIN_ORANGE,true);
	}



	public void displayNotification(Mode mode2, TimeStoppedNotificationReason reason) {
		Locale locale = app.getLocale();
		String message = MessageFormat.format(
				Messages.getString("TimeStoppedNotificationReason.NotificationFormat", locale),
				Messages.getString("LiftList."+mode2.name(), locale),
				Messages.getString("TimeStoppedNotificationReason."+reason.name(),locale));
		notifications.add((Resource)null,message,true,Notifique.Styles.VAADIN_ORANGE,true);
	}
}
