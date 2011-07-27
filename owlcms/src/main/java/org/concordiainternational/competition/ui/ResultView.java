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

import java.net.URL;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.CompetitionSessionLookup;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * This class defines the screen layout for the competition secretary.
 * <p>
 * <ul>
 * The top part shows the current lifter information and the lifters in lifting
 * order. This list is actually the container from which data is pulled out.
 * </ul>
 * <ul>
 * Clicking in the lift list selects a lifter, whose detail in shown in the
 * bottom part.
 * </ul>
 * </p>
 * 
 * @author jflamy
 * 
 */
public class ResultView extends VerticalSplitPanel implements ApplicationView, SessionData.UpdateEventListener, EditingView {
    private static final long serialVersionUID = 7881028819569705161L;
    private static final Logger logger = LoggerFactory.getLogger(ResultView.class);

    private HorizontalLayout topPart;
    private ResultList resultList;
    private LifterCardEditor lifterCardEditor;
    private CompetitionApplication app;
    private boolean stickyEditor = false;
    private SessionData groupData;
    private String platformName;
    private String viewName;
    private String groupName;

    /**
     * Create view.
     * 
     * @param mode
     */
    public ResultView(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }

        this.app = CompetitionApplication.getCurrent();
        
        if (platformName == null) {
        	// get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        }
        if (app.getPlatform() == null || !platformName.equals(app.getPlatformName())) {
        	app.setPlatformByName(platformName);
        }
        
        this.app = CompetitionApplication.getCurrent();
        groupData = SessionData.getIndependentInstance();
        final CompetitionSession currentGroup = groupData.getCurrentSession();
        if (groupName != null && groupName.length() > 0) {
            switchGroup(new CompetitionSessionLookup(app).lookup(groupName));
        } else {
            app.setCurrentCompetitionSession(currentGroup);
            if (currentGroup != null) {
            	 groupName = currentGroup.getName();
            }
           
        }

        // left side is the lifting order, as well as the menu to switch groups.
        resultList = new ResultList(groupData, this);
        resultList.table.setPageLength(15);
        resultList.table.setSizeFull();
        resultList.setSizeFull();

        topPart = new HorizontalLayout();
        topPart.setSizeFull();
        topPart.addComponent(resultList);
        topPart.setExpandRatio(resultList, 100.0F);

        this.setFirstComponent(topPart);
        loadFirstLifterInfo(groupData, false);

        adjustSplitBarLocation();

        synchronized (app) {
        	boolean prevDisabled = app.getPusherDisabled();
        	app.setPusherDisabled(true);
	        // we are now fully initialized
	        groupData.addListener(this);
	        groupData.setAllowAll(true);
			if (groupData.lifters.isEmpty()) {
				logger.debug(
						"switching masterData.lifters {}", groupData.lifters); //$NON-NLS-1$
				switchGroup(app.getCurrentCompetitionSession());
			} else {
				logger.debug(
						"not switching masterData.lifters {}", groupData.lifters); //$NON-NLS-1$
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
     * @param groupData1
     */
    public void loadFirstLifterInfo(SessionData groupData1) {
        final Lifter firstLifter = resultList.getFirstLifter();
        final Item firstLifterItem = resultList.getFirstLifterItem();
        updateLifterEditor(firstLifter, firstLifterItem);
    }

    /**
     * Update the lifter editor and the information panels with the first
     * lifter.
     * 
     * @param groupData1
     */
    public void loadFirstLifterInfo(SessionData groupData1, boolean sticky) {
        loadFirstLifterInfo(groupData1);
        if (lifterCardEditor != null) {
            boolean buf = lifterCardEditor.ignoreChanges;
            lifterCardEditor.ignoreChanges = true;
            lifterCardEditor.setSticky(sticky);
            lifterCardEditor.ignoreChanges = buf;
        }
    }

    /**
     * @param app
     * @param lifter
     */
    private void updateLifterEditor(final Lifter lifter, final Item lifterItem) {
        // if (stickyEditor) return; // editor has asked not to be affected
        if (lifter != null) {
            if (lifterCardEditor == null) {
                lifterCardEditor = new LifterCardEditor(resultList, this);
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
        logger.debug("start refresh ----------"); //$NON-NLS-1$
        CategoryLookup.getSharedInstance().reload();
        resultList.refresh();
        loadFirstLifterInfo(groupData);
        logger.debug("end refresh ----------"); //$NON-NLS-1$
    }

    /**
     * Set the split bar location
     */
    void adjustSplitBarLocation() {
        // compute percentage of split bar.
        float height = app.getMainWindow().getHeight();
        if (height > 0) this.setSplitPosition((int) ((height - 225) * 100 / height));
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
        if (reloadLifterInfo && wasSticky && !freezeLifterCardEditor) loadFirstLifterInfo(groupData, false);
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
    	// FIXME: this throws an IllegalStateException
    	// Strange because this pattern is used everywhere else
//        new Thread(new Runnable() {
//			@Override
//			public void run() {
				synchronized (app) {
					if (updateEvent.getForceRefresh()) {
						logger.trace("updateEvent() received in Result view -- refreshing. ----------------------------------"); //$NON-NLS-1$
						refresh();
						return;
					}
					logger.trace("updateEvent() received in ResultView"); //$NON-NLS-1$

					resultList.updateTable();
					// loadFirstLifterInfo(groupData,WebApplicationConfiguration.DEFAULT_STICKINESS);

					// update the info on the left side of the bottom part. This depends
					// on the liftList info
					// which has just changed.
					if (lifterCardEditor != null
							&& lifterCardEditor.lifterCardIdentification != null
							&& !stickyEditor) {
						lifterCardEditor.lifterCardIdentification.loadLifter(
								lifterCardEditor.getLifter(),
								resultList.getGroupData());
					}
					// updateLifterEditor(updateEvent.getCurrentLifter(),
					// liftList.getFirstLifterItem());
				}
				app.push();
			}
//		}).start();
//    }

    /**
     * Reload data according to this session's (CompetitionApplication) current
     * lifter group.
     * 
     * @param dataCurrentGroup
     */
    private void switchGroup(final CompetitionSession dataCurrentGroup) {
        logger.warn("===============ResultView switching to group {}", dataCurrentGroup); //$NON-NLS-1$
        groupData.setCurrentSession(dataCurrentGroup);
        if (dataCurrentGroup != null) {
            groupName = dataCurrentGroup.getName();
        } else {
            groupName = "";
        }
        CompetitionApplication.getCurrent().getUriFragmentUtility().setFragment(getFragment(), false);
    }

    @Override
	public void setCurrentSession(CompetitionSession competitionSession) {
        setStickyEditor(false, false);
        switchGroup(competitionSession);
    }

    /**
     * @param groupData
     *            the groupData to set
     */
    @Override
	public void setSessionData(SessionData groupData) {
        this.groupData = groupData;
    }

    /**
     * @return the groupData
     */
    public SessionData getSessionData() {
        return groupData;
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


	@Override
	public void registerAsListener() {
		app.getMainWindow().addListener((CloseListener) this);
	}

	@Override
	public void unregisterAsListener() {
		app.getMainWindow().addListener((CloseListener) this);
	}
	
	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();	
	}
	
	/* Called on refresh.
	 * @see com.vaadin.terminal.URIHandler#handleURI(java.net.URL, java.lang.String)
	 */
	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		registerAsListener();
		return null;
	}

}
