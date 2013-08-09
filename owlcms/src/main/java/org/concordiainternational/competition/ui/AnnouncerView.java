/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Window.Notification;

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
public class AnnouncerView extends VerticalSplitPanel implements
	ApplicationView,
	SessionData.UpdateEventListener,
	EditingView,
	Notifyable,
	Window.CloseListener,
	URIHandler
	{
	private static final long serialVersionUID = 7881028819569705161L;
    private static final Logger logger = LoggerFactory.getLogger(AnnouncerView.class);
    public static final boolean PUSHING = true;

	/** remove message after this delay (ms) */
    private static final int messageRemovalMs = 5000;
    
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
        logger.trace("constructor");
        
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
        //announcerInfo.setWidth(7.0F, Sizeable.UNITS_CM); //$NON-NLS-1$
        announcerInfo.setMargin(true,true,false,false);
        announcerInfo.setSizeFull();

        // left side is the lifting order, as well as the menu to switch groups.
        // note: not used in timekeeper view, but until we refactor the code
        // there is legacy information found inside the list that should not be there
        // so we leave it there.
        liftList = new LiftList(masterData, this, mode);
        liftList.table.setPageLength(15);
        liftList.table.setSizeFull();
        liftList.setSizeFull();
        liftList.setMargin(false,false,true,true);

        topPart = new HorizontalLayout();
        topPart.setMargin(false);
        setupNotifications();

        synchronized (app) {
        	boolean prevDisabled = app.getPusherDisabled();
        	app.setPusherDisabled(true);
			topPart.setSizeFull();
			if (mode != Mode.TIMEKEEPER) {
				topPart.addComponent(liftList);				
				topPart.setExpandRatio(liftList, 100.0F);
			}

			announcerInfo.setSizeUndefined();
			announcerInfo.setWidth("36ex");
			topPart.addComponent(announcerInfo);
			
			if (mode != Mode.TIMEKEEPER) {			
				topPart.setExpandRatio(announcerInfo, 3.5F);
			}
			topPart.setComponentAlignment(announcerInfo, Alignment.TOP_RIGHT);

			this.setMargin(false);
			this.setFirstComponent(topPart);
			loadFirstLifterInfo(masterData,
					WebApplicationConfiguration.DEFAULT_STICKINESS);
			adjustSplitBarLocation();
			// we are now fully initialized
			masterData.setAllowAll(false);
			
			// URI handler must remain, so is not part of the register/unRegister pair
			app.getMainWindow().addURIHandler(this);
			registerAsListener();
			
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
                masterData.noCurrentLifter();
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
        masterData.getRefereeDecisionController().reset();
        loadFirstLifterInfo(masterData);
        logger.debug("end refresh ----------{}", mode); //$NON-NLS-1$
    }

    /**
     * Set the split bar location
     */
    void adjustSplitBarLocation() {
    	if (mode == Mode.TIMEKEEPER) {
    		this.setSplitPosition(0, true);
    	} else {
    		// compute percentage of split bar.
    		float height = app.getMainWindow().getHeight();
    		if (height > 0) {
    			this.setSplitPosition((int) ((height - 225) * 100 / height));
    		} else {
    			this.setSplitPosition(65);
    		}
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
    public void updateEvent(final SessionData.UpdateEvent updateEvent) {
        new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (app) {
					if (updateEvent.getForceRefresh()) {
						logger.debug("updateEvent() received in {} view -- forced refresh. ----------------------------------", mode); //$NON-NLS-1$
						refresh();
					} else {
						Lifter currentLifter = updateEvent.getCurrentLifter();
                        logger.debug("updateEvent() received in {} view  first is now: {}", AnnouncerView.this.getMode(), currentLifter); //$NON-NLS-1$
						
						liftList.updateTable();
						loadFirstLifterInfo(masterData,
								WebApplicationConfiguration.DEFAULT_STICKINESS);
						

						// update the info on the left side of the bottom part. This
						// depends on the liftList info
						// which has just changed.
						if (lifterCardEditor != null
								&& lifterCardEditor.lifterCardIdentification != null
								&& !stickyEditor) {
							lifterCardEditor.lifterCardIdentification
									.loadLifter(currentLifter,
											liftList.getGroupData());
						}

						// updateLifterEditor(updateEvent.getCurrentLifter(),
						// liftList.getFirstLifterItem());
					}
				}
				app.push();
			}
		}).start();
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
	            logger.debug("=============== switching from {} to group {}", oldSession, newSession); //$NON-NLS-1$
	            logger.debug("=============== modifying group data {}", masterData, (newSession != null ? newSession.getName() : null)); //$NON-NLS-1$
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



	public void displayNotification(Mode mode2, InteractionNotificationReason reason) {
		Locale locale = app.getLocale();
		String message;
		String reasonDetails = Messages.getString("TimeStoppedNotificationReason."+reason.name(),locale);
		Lifter curLifter = masterData.getCurrentLifter();
		Integer curWeight = curLifter.getNextAttemptRequestedWeight();
		
        if (mode2 == null) {
            message = MessageFormat.format(
                    Messages.getString("TimeStoppedNotificationReason.NotificationFormatShort", locale),
                    reasonDetails,
                    curLifter.getLastName(),
                    curLifter.getFirstName(),
                    curWeight);
		} else {
			message = MessageFormat.format(
					Messages.getString("TimeStoppedNotificationReason.NotificationFormat", locale),
					Messages.getString("LiftList."+mode2.name(), locale),
					reasonDetails,
					curLifter.getLastName(),
					curLifter.getFirstName(),
					curWeight);
		}
		final Message addedMessage = notifications.add((Resource)null,message,true,Notifique.Styles.VAADIN_ORANGE,true);
		switch (reason) {
		case CURRENT_LIFTER_CHANGE:
			// the announcer must acknowledge explicitly
			if (this.mode != Mode.ANNOUNCER) {
				scheduleMessageRemoval(addedMessage, messageRemovalMs);
			}	
			break;
		default:
			// remove automatically
			scheduleMessageRemoval(addedMessage, messageRemovalMs);
			break;
		}
	}


	/**
	 * @param addedMessage
	 * @param i 
	 */
	public void scheduleMessageRemoval(final Message addedMessage, int msgRemovalMs) {
		new Timer().schedule(new TimerTask(){
			@Override
			public void run() {
				// remove message, push to client.
				if (addedMessage.isVisible()) {
					synchronized (app) {
						addedMessage.hide();
					}
					app.push();
				}
			}	
		}, msgRemovalMs);
	}
	
	/**
	 * Register all handlers that listen to model or outside events.
	 */
	@Override
	public void registerAsListener() {
		logger.debug("registering listeners");
		masterData.addListener(this);

        // listen to close events
        app.getMainWindow().addListener((CloseListener)this);
	}
	
	/**
	 * 
	 */
	@Override
	public void unregisterAsListener() {
		logger.debug("unregistering listeners");
		masterData.removeListener(this);
		announcerInfo.unregisterAsListener();
        if (lifterCardEditor != null) {
            lifterCardEditor.unregisterAsListener();
        }
        // stop listening to close events
        app.getMainWindow().removeListener((CloseListener)this);
	}



	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();
	}


	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		logger.debug("registering URI listeners");
		registerAsListener();
		return null;
	}


	/**
	 * @return the notifications
	 */
	public Notifique getNotifications() {
		return notifications;
	}

    /**
	 * @return the liftList
	 */
	public LiftList getLiftList() {
		return liftList;
	}


    @Override
    public void showNotificationForLifter(Lifter lifter, Notification notification, boolean unlessCurrent) {
        logger.debug("lifter {} unlessCurrent{}",lifter,unlessCurrent);
        if (!unlessCurrent) {
            // always show notification
            app.getMainWindow().showNotification(notification);
        } else if (lifter != masterData.getCurrentLifter()) {
            // not the current lifter, show the notification
            app.getMainWindow().showNotification(notification);
        }
    }


    public Mode getMode() {
        return mode;
    }


    @Override
    public boolean needsBlack() {
        return false;
    }
}
