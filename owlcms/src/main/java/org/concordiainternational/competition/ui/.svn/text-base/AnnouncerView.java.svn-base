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

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.CompetitionSessionLookup;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.data.Item;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.SplitPanel;

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
public class AnnouncerView extends SplitPanel implements ApplicationView, GroupData.UpdateEventListener, EditingView {
    private static final long serialVersionUID = 7881028819569705161L;
    private static final Logger logger = LoggerFactory.getLogger(AnnouncerView.class);
    public static final boolean PUSHING = true;

    private HorizontalLayout topPart;
    private LifterInfo announcerInfo;
    private LiftList liftList;
    private LifterCardEditor lifterCardEditor;
    private CompetitionApplication app;
    private boolean stickyEditor = false;
    private GroupData groupData;
    Mode mode;
    private ICEPush pusher;
    private String platformName;
    private String viewName;
    private String groupName;

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
            platformName = CompetitionApplicationComponents.initPlatformName();
        }
        groupData = GroupData.getInstance(platformName);
        if (mode == Mode.ANNOUNCER) {
            final CompetitionSession currentGroup = groupData.getCurrentCompetitionSession();
            groupData.setAnnouncerView(this);
            groupData.setMasterApplication(this.app);
            if (currentGroup == null && groupName != null && groupName.length() > 0) {
                switchGroup(new CompetitionSessionLookup(app).lookup(groupName));
            } else {
                app.setCurrentCompetitionSession(currentGroup); 
            }

        }

        // right hand side shows information that the announcer reads aloud
        announcerInfo = new LifterInfo("topPart", groupData, mode, this); //$NON-NLS-1$
        announcerInfo.addStyleName("currentLifterSummary"); //$NON-NLS-1$
        announcerInfo.setWidth(7.0F, Sizeable.UNITS_CM); //$NON-NLS-1$
        announcerInfo.setMargin(true);

        // left side is the lifting order, as well as the menu to switch groups.
        liftList = new LiftList(groupData, this, mode);
        liftList.table.setPageLength(15);
        liftList.table.setSizeFull();
        liftList.setSizeFull();

        topPart = new HorizontalLayout();
        if (PUSHING) {
            pusher = this.app.ensurePusher();
        } else {
            setupPolling();
        }
        topPart.setSizeFull();
        topPart.addComponent(liftList);
        topPart.addComponent(announcerInfo);
        topPart.setComponentAlignment(announcerInfo, Alignment.TOP_LEFT);
        topPart.setExpandRatio(liftList, 100.0F);

        this.setFirstComponent(topPart);
        loadFirstLifterInfo(groupData, WebApplicationConfiguration.DEFAULT_STICKINESS);

        adjustSplitBarLocation();

        // we are now fully initialized
        groupData.setAllowAll(false);
        groupData.addListener(this);
        if (groupData.lifters.isEmpty()) {
            logger.debug("switching groupData.lifters {}", groupData.lifters); //$NON-NLS-1$
            switchGroup(app.getCurrentCompetitionSession());
        } else {
            logger.debug("not switching groupData.lifters {}", groupData.lifters); //$NON-NLS-1$
        }
        
        CompetitionApplication.getCurrent().getUriFragmentUtility().setFragment(getFragment(), false);
        if (pusher != null) pusher.push();
    }

    /**
     * 
     */
    private void setupPolling() {
        ProgressIndicator refresher = new ProgressIndicator();
        refresher.setWidth("0pt"); //$NON-NLS-1$
        refresher.setPollingInterval(1000);
        refresher.addStyleName("invisible"); //$NON-NLS-1$
        topPart.addComponent(refresher);
    }

    /**
     * Update the lifter editor and the information panels with the first
     * lifter.
     * 
     * @param groupData
     */
    public void loadFirstLifterInfo(GroupData groupData) {
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
     * @param groupData
     */
    public void loadFirstLifterInfo(GroupData groupData, boolean sticky) {
        loadFirstLifterInfo(groupData);
        if (lifterCardEditor != null) {
            lifterCardEditor.setSticky(sticky);
        }
    }

    /**
     * Update the lifter editor and the information panels with the first
     * lifter.
     * 
     * @param groupData
     */
    public void editFirstLifterInfo(GroupData groupData, boolean sticky) {
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
            // no current lifter, remove bottom part if present.
            if (lifterCardEditor != null) {
                setSecondComponent(new Label("")); //$NON-NLS-1$
                groupData.getTimer().removeAllListeners(lifterCardEditor.lifterCardIdentification);
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
        loadFirstLifterInfo(groupData);
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
    public void editLifter(Lifter lifter, Item lifterItem) {
        updateLifterEditor(lifter, lifterItem);
    }

    /**
     * @return true if editor in bottom pane is pinned (not to be updated)
     */
    public boolean isStickyEditor() {
        return stickyEditor;
    }

    /**
     * Indicate that the editor at bottom must not be updated
     * 
     * @param freezeLifterCardEditor
     */
    public void setStickyEditor(boolean freezeLifterCardEditor) {
        setStickyEditor(freezeLifterCardEditor, true);
    }

    /**
     * Indicate that the editor at bottom must not be updated
     * 
     * @param freezeLifterCardEditor
     */
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
     * @see org.concordiainternational.competition.ui.GroupData.UpdateEventListener#updateEvent(org.concordiainternational.competition.ui.GroupData.UpdateEvent)
     */
    @Override
    public void updateEvent(GroupData.UpdateEvent updateEvent) {
        synchronized (app) {
            if (updateEvent.getForceRefresh()) {
                logger.debug(
                    "updateEvent() received in {} view -- forced refresh. ----------------------------------", mode); //$NON-NLS-1$
                refresh();
            } else {
                logger.debug(
                    "updateEvent() received in {} view  first is now: {}", this, updateEvent.getCurrentLifter()); //$NON-NLS-1$

                liftList.updateTable();
                loadFirstLifterInfo(groupData, WebApplicationConfiguration.DEFAULT_STICKINESS);

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
        if (pusher != null) {
            pusher.push();
        }
    }

    /**
     * Reload data according to this session's (CompetitionApplication) current
     * lifter group.
     * 
     * @param dataCurrentGroup
     */
    private void switchGroup(final CompetitionSession dataCurrentGroup) {
        if (mode == Mode.ANNOUNCER) {
            logger.debug("==============={} switching to group {}", mode, dataCurrentGroup); //$NON-NLS-1$
            logger.debug("==============={} modifying group data {}", groupData); //$NON-NLS-1$
            groupData.setCurrentGroup(dataCurrentGroup);
            CompetitionSession currentCompetitionSession = groupData.getCurrentCompetitionSession();
            if (currentCompetitionSession != null) {
                groupName = currentCompetitionSession.getName();
            } else {
                groupName = "";
            }
            CompetitionApplication.getCurrent().getUriFragmentUtility().setFragment(getFragment(), false);
        }
    }

    public void setCurrentGroup(CompetitionSession competitionSession) {
        setStickyEditor(false, false);
        switchGroup(competitionSession);
    }

    /**
     * @param groupData
     *            the groupData to set
     */
    public void setGroupData(GroupData groupData) {
        this.groupData = groupData;
    }

    /**
     * @return the groupData
     */
    public GroupData getGroupData() {
        return groupData;
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
    public String getFragment() {
        return viewName+"/"+platformName+"/"+groupName;
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
            throw new RuleViolationException("Error.PlatformNameIsMissing"); 
        }
        
        if (params.length >= 3) {
            groupName = params[2];
        } else {
            groupName = null;
        }
    }
}
