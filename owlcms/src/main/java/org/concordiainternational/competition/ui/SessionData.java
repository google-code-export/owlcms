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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.data.lifterSort.LifterSorter.Ranking;
import org.concordiainternational.competition.decision.DecisionController;
import org.concordiainternational.competition.decision.DecisionController.Decision;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.nec.NECDisplay;
import org.concordiainternational.competition.publicAddress.PublicAddressCountdownTimer;
import org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent;
import org.concordiainternational.competition.publicAddress.PublicAddressMessageEvent.MessageDisplayListener;
import org.concordiainternational.competition.publicAddress.PublicAddressTimerEvent;
import org.concordiainternational.competition.publicAddress.PublicAddressTimerEvent.MessageTimerListener;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.utils.EventHelper;
import org.concordiainternational.competition.utils.IdentitySet;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.concordiainternational.competition.utils.NotificationManager;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.hibernate.StaleObjectStateException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.github.wolfie.blackboard.Blackboard;
import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.vaadin.data.Item;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;
import com.vaadin.event.EventRouter;
import com.vaadin.ui.Component;

/**
 * Data about a competition group.
 * <p>
 * Manages the lifting order, keeps tabs of which lifters have been called, who
 * is entitled to two minutes, etc. Also holds the master timer for the group.
 * </p>
 * 
 * @author jflamy
 */
public class SessionData implements Lifter.UpdateEventListener, Serializable {

    private static final long serialVersionUID = -7621561459948739065L;
    private static XLogger logger = XLoggerFactory.getXLogger(SessionData.class);
    public static final String MASTER_KEY = "GroupData_"; //$NON-NLS-1$

    public List<Lifter> lifters;
    /**
     * list of currently displayed lifters that, if updated, will notify us. We
     * use an IdentitySet because the same lifter can appear in two windows, as
     * two occurrences that are != but equals.
     */
    public Set<Object> notifiers = (new IdentitySet(5));
    private List<Lifter> liftTimeOrder;
    private List<Lifter> displayOrder;
    private List<Lifter> resultOrder;
    private CompetitionApplication app;
    private String currentPlatformName;
    private CompetitionSession currentSession;
    private Lifter currentLifter;
    public boolean needToUpdateNEC;

    private List<Lifter> currentDisplayOrder;
    private List<Lifter> currentLiftingOrder;

    private NotificationManager<SessionData, Lifter, Component> notificationManager;
    private int timeAllowed;
    private int liftsDone;

    private DecisionController decisionController = new DecisionController(this);

    boolean allowAll = false; // allow null group to mean all lifters.
    // will be set to true if the Timekeeping button is pressed.
    private boolean timeKeepingInUse = Competition.isMasters();
    
    private Lifter priorLifter;
    private Integer priorRequest;
    private Integer priorRequestNum;
    private boolean needToAnnounce = true;
	Blackboard blackBoardEventRouter = new Blackboard();

    public int getLiftsDone() {
        return liftsDone;
    }

    private SessionData(String platformName) {
        app = CompetitionApplication.getCurrent();
        lifters = new ArrayList<Lifter>();
        currentPlatformName = platformName;
        notificationManager = new NotificationManager<SessionData, Lifter, Component>(this);
        init();
    }

    /**
     * This constructor is only meant for unit tests.
     * 
     * @param lifters
     */
    public SessionData(List<Lifter> lifters) {
        app = CompetitionApplication.getCurrent();
        this.lifters = lifters;
        notificationManager = new NotificationManager<SessionData, Lifter, Component>(this);
        updateListsForLiftingOrderChange();
        init();
    }

    static private final Map<String, SessionData> platformToSessionData = new HashMap<String, SessionData>();

    public static SessionData getSingletonForPlatform(String platformName) {
        SessionData groupDataSingleton = platformToSessionData.get(platformName);

        if (groupDataSingleton == null) {
            groupDataSingleton = new SessionData(platformName);
            platformToSessionData.put(platformName, groupDataSingleton);
            groupDataSingleton.registerAsMasterData(platformName);
        }
        logger.debug("groupData = {}", groupDataSingleton); //$NON-NLS-1$
        return groupDataSingleton;
    }
    
    /**
     * @return information about a session, not connected to a platform.
     */
    public static SessionData getIndependentInstance() {
        SessionData independentData = new SessionData("");
        logger.debug("independentData = {}", independentData); //$NON-NLS-1$
        return independentData;
    }

    /**
     * @return
     */
    private void init() {
    	blackBoardEventRouter.register(MessageDisplayListener.class, PublicAddressMessageEvent.class);
    	blackBoardEventRouter.register(MessageTimerListener.class, PublicAddressTimerEvent.class);
        // logger.entry();
        // loadData();
        // logger.exit();
    }

    /**
     * This method reloads the underlying data. Beware that only "master" views
     * are meant to do this, such as AnnouncerView when mode = ANNOUNCER, or the
     * results view to edit results after a group is over.
     * 
     * "slave" views such as the MARSHAL, TIMEKEEPER views should never call this method.
     */
    void loadData() {
        currentSession = this.getCurrentSession();
        if (currentSession == null && !allowAll) {
            // make it so we have to select a group
            lifters = new ArrayList<Lifter>();
            logger.debug("current group is empty"); //$NON-NLS-1$
        } else {
            logger.debug("loading data for group {}", currentSession); //$NON-NLS-1$
            final LifterContainer hbnCont = new LifterContainer(app);
            // hbnCont will filter automatically to application.getCurrentGroup
            lifters = hbnCont.getAllPojos();
        }
    }

    /**
     * @return the lifter who lifted most recently
     */
    public Lifter getPreviousLifter() {
        if (getLiftTimeOrder() == null) {
            setLiftTimeOrder(LifterSorter.LiftTimeOrderCopy(lifters));
        }
        if (getLiftTimeOrder().size() == 0) return null;
        // if (logger.isDebugEnabled())
        // System.err.println(AllTests.longDump(liftTimeOrder));

        Lifter lifter = getLiftTimeOrder().get(0);
        if (lifter.getLastLiftTime() == null) return null;
        return lifter;

        // // we want the most recent, who will be at the end.
        // // skip people who have not lifted.
        // Lifter lifter = null;
        // for (int index = lifters.size()-1; index >= 0; index--) {
        // lifter = liftTimeOrder.get(index);
        // if (lifter.getLastLiftTime() != null) break;
        // }
        // return lifter;
    }

    /**
     * Saves changes made to object to Hibernate Session. Note that run is most
     * likely detached due session-per-request patterns so we'll use merge.
     * Actual database update will happen by Vaadin's transaction listener in
     * the end of request.
     * 
     * If one wanted to make sure that this operation will be successful a
     * (Hibernate) transaction commit and error checking ought to be done.
     * 
     * @param object
     */
    public void persistPojo(Object object) {
        try {
            ((HbnSessionManager) app).getHbnSession().merge(object);
        } catch (StaleObjectStateException e) {
            throw new RuntimeException(Messages.getString("SessionData.UserHasBeenDeleted", CompetitionApplication
                    .getCurrentLocale()));
        }
    }

    /**
     * Sort the various lists to reflect new lifting order.
     */
    public void updateListsForLiftingOrderChange() {
        logger.debug("updateListsForLiftingOrderChange"); //$NON-NLS-1$

        sortLists();
        publishLists();
        notifyListeners();
    }

    /**
     * 
     */
    private void sortLists() {
        logger.debug("sortLists"); //$NON-NLS-1$

        displayOrder = LifterSorter.displayOrderCopy(lifters);
        setLiftTimeOrder(LifterSorter.LiftTimeOrderCopy(lifters));
        setResultOrder(LifterSorter.resultsOrderCopy(lifters, Ranking.TOTAL));
        LifterSorter.assignMedals(getResultOrder());
        this.liftsDone = LifterSorter.countLiftsDone(lifters);

        LifterSorter.liftingOrder(lifters);
        this.needToUpdateNEC = false;
        currentLifter = LifterSorter.markCurrentLifter(lifters);

        Integer currentRequest = (currentLifter != null ? currentLifter.getNextAttemptRequestedWeight() : null);
        Integer currentRequestNum = (currentLifter != null ? currentLifter.getAttemptsDone() : null);

        logger
                .debug("new/old {}/{}  {}/{}  {}/{}", //$NON-NLS-1$
                    new Object[] { currentLifter, priorLifter, currentRequest, priorRequest, currentRequestNum,
                            priorRequestNum });

        setNeedToAnnounce(currentLifter != priorLifter || priorRequest != currentRequest
            || priorRequestNum != currentRequestNum);
        if (getNeedToAnnounce()) {
            // stop the timer if it was running, as if the "Change Weight"
            // button had been used
            final CountdownTimer timer2 = getTimer();
            if (timer2 != null && timer2.isRunning()) {
                timer2.pause();
            } // stop time something is happening.
            setTimeAllowed(timeAllowed(currentLifter));
            needToUpdateNEC = true;
            logger
                    .debug(
                        "paused time, needToUpdateNec = true, timeAllowed={}, timeRemaining={}", timeAllowed, timer2.getTimeRemaining()); //$NON-NLS-1$
        } else {
            needToUpdateNEC = false;
            logger.debug("needToUpdateNEC = false"); //$NON-NLS-1$
        }

        if (currentLifter != null) {
            // copy values from current lifter.
            priorLifter = currentLifter;
            priorRequest = (currentLifter != null ? currentLifter.getNextAttemptRequestedWeight() : null);
            priorRequestNum = (currentLifter != null ? currentLifter.getAttemptsDone() : null);
        } else {
            priorLifter = null;
            priorRequest = null;
            priorRequestNum = null;
        }
    }

    /**
     * Notify the listeners that the lifting order has changed.
     */
    void notifyListeners() {
        // notify listeners to pick up the new information.
        final Lifter firstLifter = lifters.size() > 0 ? lifters.get(0) : null;
        logger.debug("notifyListeners() firing event, first lifter={}", firstLifter); //$NON-NLS-1$
        fireEvent(new UpdateEvent(this, firstLifter));
    }

    /**
     * Make the lists visible to all (including JSPs)
     */
    void publishLists() {
        // make results available to all (including JSPs)
        final CompetitionApplication competitionApplication = (CompetitionApplication) app;
        final String platformName = app.getPlatformName();
        final CompetitionSession currentGroup = competitionApplication.getCurrentCompetitionSession();
        String name = (currentGroup != null ? (String) currentGroup.getName() : null);
        ServletContext sCtx = competitionApplication.getServletContext();
        if (sCtx != null) {
            logger.debug("current group for platformName " + platformName + " = " + name); //$NON-NLS-1$ //$NON-NLS-2$
            currentLiftingOrder = getAttemptOrder();
            currentDisplayOrder = getDisplayOrder();
            sCtx.setAttribute("groupData_" + platformName, this); //$NON-NLS-1$
        }
    }

    public List<Lifter> getLifters() {
        return lifters;
    }

    /**
     * @return lifters in standard display order
     */
    public List<Lifter> getDisplayOrder() {
        return displayOrder;
    }

    /**
     * @return lifters in lifting order
     */
    public List<Lifter> getAttemptOrder() {
        return lifters;
    }

    /**
     * Check if lifter is following himself, and that no other lifter has been
     * announced since (if time starts running for another lifter, then the two
     * minute privilege is lost).
     * 
     * @param lifter
     * 
     */
    public int timeAllowed(Lifter lifter) {
        final Set<LifterCall> calledLifters = getStartedLifters();
        logger.debug("timeAllowed start"); //$NON-NLS-1$
        // if clock was running for the current lifter, return the remaining
        // time.
        if (getTimer().getOwner() == lifter) {
            logger.debug("timeAllowed current lifter {} was running.", lifter); //$NON-NLS-1$
            int timeRemaining = getTimer().getTimeRemaining();
            if (timeRemaining < 0) timeRemaining = 0;
            // if the decision was not entered, and timer has run to 0, we still
            // want to see 0
            // if (timeRemaining > 0
            // //&& timeRemaining != 60000 && timeRemaining != 120000
            // ) {
            logger.info("resuming time for lifter {}: {} ms remaining", lifter, timeRemaining); //$NON-NLS-1$
            return timeRemaining;
            // }
        }
        logger.debug("not current lifter"); //$NON-NLS-1$
        final Lifter previousLifter = getPreviousLifter();
        if (previousLifter == null) {
            logger.debug("A twoMinutes (not): previousLifter null: calledLifters={} lifter={}", //$NON-NLS-1$
                new Object[] { calledLifters, lifter });
            return 60000;
        } else if (lifter.getAttemptsDone() % 3 == 0) {
            // no 2 minutes if starting snatch or starting c-jerk
            logger.debug("B twoMinutes (not): first attempt lifter={}", lifter); //$NON-NLS-1$
            return 60000;
        } else if (calledLifters == null || calledLifters.isEmpty()) {
            if (lifter.equals(previousLifter)) {
                logger.debug("C twoMinutes : calledLifters={} lifter={} previousLifter={}", //$NON-NLS-1$
                    new Object[] { calledLifters, lifter, previousLifter });
                // setTimerForTwoMinutes(lifter);
                return 120000;
            } else {
                logger.debug("D twoMinutes (not): calledLifters={} lifter={} previousLifter={}", //$NON-NLS-1$
                    new Object[] { calledLifters, lifter, previousLifter });
                return 60000;
            }
        } else if (lifter.equals(previousLifter) && (calledLifters.size() == 1 && calledLifters.contains(lifter))) {
            // we are the previous lifter and no other lifter besides ourself
            // has been called.
            logger.debug("E twoMinutes: calledLifters={} lifter={} previousLifter={}", //$NON-NLS-1$
                new Object[] { calledLifters, lifter, previousLifter });
            // setTimerForTwoMinutes(lifter);
            return 120000;
        } else {
            logger.debug("F twoMinutes (not) : calledLifters={} lifter={} previousLifter={}", //$NON-NLS-1$
                new Object[] { calledLifters, lifter, previousLifter });
            return 60000;
        }
    }

    /**
     * @param lifter
     */
    @SuppressWarnings("unused")
    private void setTimerForTwoMinutes(Lifter lifter) {
        logger.warn("setting timer owner to {}", lifter);
        getTimer().stop();
        getTimer().setOwner(lifter); // so time is kept for this lifter after
                                     // switcheroo
        getTimer().setTimeRemaining(120000);
    }

    Set<LifterCall> startedLifters = new HashSet<LifterCall>();
    private boolean forcedByTimekeeper = false;

    public class LifterCall {
        public Date callTime;
        public Lifter lifter;

        LifterCall(Date callTime, Lifter lifter) {
            this.callTime = callTime;
            this.lifter = lifter;
        }

        @Override
        public String toString() {
            return lifter.toString() + "_" + callTime.toString(); //$NON-NLS-1$
        }
    }

    public void callLifter(Lifter lifter) {
        // beware: must call timeAllowed *before* setLifterAnnounced.

        // check if the timekeeper has forced the timer setting for the next
        // announce
        // if so leave it alone.
        final int timeRemaining = getTimer().getTimeRemaining();
        if (isForcedByTimekeeper() && (timeRemaining == 120000 || timeRemaining == 60000)) {
            setForcedByTimekeeper(false, timeRemaining);
            logger.info("call of lifter {} : {}ms FORCED BY TIMEKEEPER", lifter, timeRemaining); //$NON-NLS-1$
        } else {
            final int remaining = getTimeAllowed(); // timeAllowed(lifter);
            if (!getTimeKeepingInUse()) getTimer().setTimeRemaining(remaining);
            logger.info("call of lifter {} : {}ms ", lifter, remaining); //$NON-NLS-1$
            setForcedByTimeKeeper(false);
        }

        displayLifterInfo(lifter);

        decisionController.reset(); 

        if (startTimeAutomatically) {
            startTimer(lifter,this,getTimer());
        } else if (!getTimeKeepingInUse()) {
            setLifterAsHavingStarted(lifter);
            logger.warn("timekeeping NOT in use, setting lifter {} as owner", lifter);
            getTimer().setOwner(lifter);
        } 
    }

    /**
     * @param lifter
     */
    public void setLifterAsHavingStarted(Lifter lifter) {
        startedLifters.add(new LifterCall(new Date(), lifter));
    }

    /**
     * @param b
     */
    private void setForcedByTimeKeeper(boolean b) {
        this.forcedByTimekeeper = b;
    }

    /**
     * @param lifter
     */
    public void displayLifterInfo(Lifter lifter) {
        if (lifter.getAttemptsDone() >= 6) {
            displayNothing();
        } else {
            if (getNECDisplay() != null) getNECDisplay().writeLifterInfo(lifter, false);
        }
    }

    public void displayWeight(Lifter lifter) {
        LoggerUtils.logException(logger, new Exception("whocalls")); //$NON-NLS-1$
        if (lifter.getAttemptsDone() >= 6) {
            displayNothing();
        } else {
            if (getNECDisplay() != null) getNECDisplay().writeLifterInfo(lifter, true);
        }
    }

    /**
     * Clear the LED display
     */
    private void displayNothing() {
        if (getNECDisplay() != null) try {
            getNECDisplay().writeStrings("", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } catch (Exception e) {
            // nothing to do
        }
    }

    public List<Lifter> getLiftTimeOrder() {
        return liftTimeOrder;
    }

    public void liftDone(Lifter lifter, boolean success) {
        logger.debug("lift done: notifiers={}", notifiers); //$NON-NLS-1$
        startedLifters.clear();

        final CountdownTimer timer2 = getTimer();
        timer2.setOwner(null);
        timer2.stop(); // in case timekeeper has failed to stop it.
        timer2.setTimeRemaining(0);
    }

    public Set<LifterCall> getStartedLifters() {
        return startedLifters;
    }

    CountdownTimer timer;

    public CountdownTimer getTimer() {
        if (timer == null) {
            timer = new CountdownTimer();
        };
        return timer;
    }

    /**
     * @param newCurrentSession
     *            the currentSession to set
     */
    void setCurrentSession(CompetitionSession newCurrentSession) {
        logger.debug("{} setting group to {}", this, newCurrentSession); //$NON-NLS-1$
        if (app.getCurrentCompetitionSession() != newCurrentSession) {
            // we are not always called from the application, but we must remain
            // in sync.
            // we cannot systematically call app.setCurrentSession else we get
            // infinite loop.
            app.setCurrentCompetitionSession(newCurrentSession);
        }
        this.currentSession = newCurrentSession;
        loadData();
        sortLists();
        publishLists();
        setTimeKeepingInUse(false); // will switch to true if Start/stop is
                                    // used.
        // tell listeners to refresh.
        fireEvent(new UpdateEvent(this, true));
    }

    /**
     * @return the currentSession
     */
    public CompetitionSession getCurrentSession() {
        return currentSession;
    }

    public String getCurrentPlatformName() {
        return currentPlatformName;
    }

    /**
     * @param forcedByTimekeeper
     *            the forcedByTimekeeper to set
     * @param timeRemaining
     */
    public void setForcedByTimekeeper(boolean forcedByTimekeeper, int timeRemaining) {
        getTimer().forceTimeRemaining(timeRemaining);
        setForcedByTimeKeeper(forcedByTimekeeper);
    }

    /**
     * @return the forcedByTimekeeper
     */
    public boolean isForcedByTimekeeper() {
        return forcedByTimekeeper;
    }

    public void startTimeAutomatically(boolean b) {
        // if we start time automatically, announcing a lifter is the same as starting
        // the clock
        startTimeAutomatically = b;
    }

    /**
     * Remember which view is the current announcer view.
     * 
     * @param announcerView
     */
    public void setAnnouncerView(AnnouncerView announcerView) {
        this.announcerView = announcerView;
    }

    public AnnouncerView getAnnouncerView() {
        return announcerView;
    }

    public void setMasterApplication(CompetitionApplication app2) {
        logger.warn("setting master application {}", app2);
        this.masterApplication = app2;
    }

    public CompetitionApplication getMasterApplication() {
        return masterApplication;
    }

    /**
     * @return the currentDisplayOrder
     */
    public List<Lifter> getCurrentDisplayOrder() {
        return currentDisplayOrder;
    }

    /**
     * @return the currentLiftingOrder
     */
    public List<Lifter> getCurrentLiftingOrder() {
        return currentLiftingOrder;
    }

    public NECDisplay getNECDisplay() {
        Platform platform = app.getPlatform();
        if (platform != null && platform.getHasDisplay()) {
            return WebApplicationConfiguration.necDisplay;
        } else {
            return null;
        }
    }

    /* *********************************************************************************
     * UpdateEvent framework.
     */

    private EventRouter eventRouter = new EventRouter();
    private boolean startTimeAutomatically = false;
    private AnnouncerView announcerView;
    private CompetitionApplication masterApplication;
    private boolean announcerEnabled = true;
	public Item publicAddressItem;
	private PublicAddressCountdownTimer publicAddressTimer = new PublicAddressCountdownTimer(this);


	public boolean getAnnouncerEnabled() {
        return announcerEnabled;
    }

    /**
     * SessionData events all derive from this.
     */
    public class UpdateEvent extends EventObject {
        private static final long serialVersionUID = -126644150054472005L;
        private Lifter currentLifter;
        private boolean refreshRequest;

        /**
         * Constructs a new event with a specified source component.
         * 
         * @param source
         *            the source component of the event.
         * @param propertyIds
         *            that have been updated.
         */
        public UpdateEvent(SessionData source, Lifter currentLifter) {
            super(source);
            this.currentLifter = currentLifter;
        }

        public UpdateEvent(SessionData source, boolean refreshRequest) {
            super(source);
            this.refreshRequest = refreshRequest;
        }

        public Lifter getCurrentLifter() {
            return currentLifter;
        }

        public boolean getForceRefresh() {
            return refreshRequest;
        }

    }

    /**
     * Listener interface for receiving <code>SessionData.UpdateEvent</code>s.
     */
    public interface UpdateEventListener extends java.util.EventListener {

        /**
         * This method will be invoked when a SessionData.UpdateEvent is fired.
         * 
         * @param updateEvent
         *            the event that has occured.
         */
        public void updateEvent(SessionData.UpdateEvent updateEvent);
    }

    /**
     * This method is the Java object for the method in the Listener interface.
     * It allows the framework to know how to pass the event information.
     */
    private static final Method LIFTER_EVENT_METHOD = EventHelper.findMethod(UpdateEvent.class, // when
                                                                                                // receiving
                                                                                                // this
                                                                                                // type
                                                                                                // of
                                                                                                // event
        UpdateEventListener.class, // an object implementing this interface...
        "updateEvent"); // ... will be called with this method. //$NON-NLS-1$;

    /**
     * Broadcast a SessionData.event to all registered listeners
     * 
     * @param updateEvent
     *            contains the source (ourself) and the list of properties to be
     *            refreshed.
     */
    protected void fireEvent(UpdateEvent updateEvent) {
        // logger.trace("SessionData: firing event from groupData"+System.identityHashCode(this)+" first="+updateEvent.getCurrentLifter()+" eventRouter="+System.identityHashCode(eventRouter));
        // logger.trace("                        listeners"+eventRouter.dumpListeners(this));
        if (eventRouter != null) {
            eventRouter.fireEvent(updateEvent);
        }

    }

    /**
     * Register a new SessionData.Listener object with a SessionData in order to be
     * informed of updates.
     * 
     * @param listener
     */
    public void addListener(UpdateEventListener listener) {
        logger.debug("group data : add listener {}", listener); //$NON-NLS-1$
        getEventRouter().addListener(UpdateEvent.class, listener, LIFTER_EVENT_METHOD);
    }

    /**
     * Remove a specific SessionData.Listener object
     * 
     * @param listener
     */
    public void removeListener(UpdateEventListener listener) {
        if (eventRouter != null) {
            logger.debug("group data : hide listener {}", listener); //$NON-NLS-1$
            eventRouter.removeListener(UpdateEvent.class, listener, LIFTER_EVENT_METHOD);
        }
    }

    /*
     * General event framework: we implement the
     * com.vaadin.event.MethodEventSource interface which defines how a notifier
     * can call a method on a listener to signal an event an event occurs, and
     * how the listener can register/unregister itself.
     */

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.MethodEventSource#addListener(java.lang.Class,
     * java.lang.Object, java.lang.reflect.Method)
     */
    @SuppressWarnings("rawtypes")
	public void addListener(Class eventType, Object object, Method method) {
        getEventRouter().addListener(eventType, object, method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.MethodEventSource#addListener(java.lang.Class,
     * java.lang.Object, java.lang.String)
     */
    @SuppressWarnings("rawtypes")
	public void addListener(Class eventType, Object object, String methodName) {
        getEventRouter().addListener(eventType, object, methodName);
    }

    /**
     * @return the object's event router.
     */
    private EventRouter getEventRouter() {
        if (eventRouter == null) {
            eventRouter = new EventRouter();
            logger
                    .trace("new event router for groupData " + System.identityHashCode(this) + " = " + System.identityHashCode(eventRouter)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return eventRouter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.MethodEventSource#removeListener(java.lang.Class,
     * java.lang.Object)
     */
    @SuppressWarnings("rawtypes")
	public void removeListener(Class eventType, Object target) {
        if (eventRouter != null) {
            eventRouter.removeListener(eventType, target);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.MethodEventSource#removeListener(java.lang.Class,
     * java.lang.Object, java.lang.reflect.Method)
     */
    @SuppressWarnings("rawtypes")
	public void removeListener(Class eventType, Object target, Method method) {
        if (eventRouter != null) {
            eventRouter.removeListener(eventType, target, method);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.MethodEventSource#removeListener(java.lang.Class,
     * java.lang.Object, java.lang.String)
     */
    @SuppressWarnings("rawtypes")
	public void removeListener(Class eventType, Object target, String methodName) {
        if (eventRouter != null) {
            eventRouter.removeListener(eventType, target, methodName);
        }
    }

    public void removeAllListeners() {
        if (eventRouter != null) {
            eventRouter.removeAllListeners();
        }
    }

    /**
     * Change who the lift list is listening to, unless the notifier being
     * removed is the top in the list.
     * 
     * @param lifter
     * @param editor
     * @param firstLifter
     */
    public void stopListeningTo(final Lifter lifter, Component editor) {
        if (lifter == null) return;
        notificationManager.removeEditor(lifter, editor);
    }

    /**
     * Change who the lift list is listening to.
     * 
     * @param lifter
     * @param editor
     */
    public void listenToLifter(final Lifter lifter, Component editor) {
        if (lifter == null) return;
        notificationManager.addEditor(lifter, editor);
    }

    /**
     * Makes this class visible to other sessions so they can call addListener .
     * 
     * @param platformName
     */
    void registerAsMasterData(String platformName) {
        // make ourselves visible to other parts of the web application (e.g.
        // JSP pages).
        final ServletContext servletContext = ((CompetitionApplication) app).getServletContext();
        if (servletContext != null) {
            servletContext.setAttribute(SessionData.MASTER_KEY + platformName, this);
            logger.info("Master data registered for platform {}={}", platformName, this); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Copied from interface. React to lifter changes by recomputing the lists.
     * 
     * @see org.concordiainternational.competition.data.Lifter.UpdateEventListener#updateEvent(org.concordiainternational.competition.data.Lifter.UpdateEvent)
     */
    @Override
    public void updateEvent(Lifter.UpdateEvent updateEvent) {
        logger.debug("lifter {}, changed {}", updateEvent.getSource(), updateEvent.getPropertyIds()); //$NON-NLS-1$
        updateListsForLiftingOrderChange();
        persistPojo(updateEvent.getSource());
    }

    public Lifter getCurrentLifter() {
        return currentLifter;
    }

    /**
     * @param liftTimeOrder
     *            the liftTimeOrder to set
     */
    void setLiftTimeOrder(List<Lifter> liftTimeOrder) {
        this.liftTimeOrder = liftTimeOrder;
    }

    /**
     * @param resultOrder
     *            the resultOrder to set
     */
    void setResultOrder(List<Lifter> resultOrder) {
        this.resultOrder = resultOrder;
    }

    /**
     * @return the resultOrder
     */
    List<Lifter> getResultOrder() {
        return resultOrder;
    }

    /**
     * Register the fact that component comp is now editing newLifter instead of
     * previousLifter
     * 
     * @param newLifter
     * @param previousLifter
     * @param comp
     */
    public void trackEditors(Lifter newLifter, Lifter previousLifter, Component comp) {
        logger.debug("previousLifter = {}, lifter = {}", previousLifter, newLifter);; //$NON-NLS-1$
        if (previousLifter != newLifter) {
            // stopListeningTo actually waits until no editor is left to stop
            // listening
            stopListeningTo(previousLifter, comp);
        }
        if (newLifter != null) {
            listenToLifter(newLifter, comp);
        }
    }

    /**
     * @param timeAllowed
     *            the timeAllowed to set
     */
    private void setTimeAllowed(int timeAllowed) {
        this.timeAllowed = timeAllowed;
    }

    /**
     * @return the timeAllowed
     */
    public int getTimeAllowed() {
        return timeAllowed;
    }

    public boolean getAllowAll() {
        return allowAll;
    }

    public void setAllowAll(boolean allowAll) {
        this.allowAll = allowAll;
    }

    public DecisionController getDecisionController() {
        return decisionController;
    }

    public void majorityDecision(Decision[] refereeDecisions) {
        final Lifter currentLifter2 = getCurrentLifter();
        int pros = 0;
        for (int i = 0; i < refereeDecisions.length; i++) {
            if (refereeDecisions[i].accepted) pros++;
        }
        final boolean success = pros >= 2;
        liftDone(currentLifter2, success);
        if (success) {
            logger.info("Referee decision: GOOD lift");
            if (currentLifter2 != null) currentLifter2.successfulLift();
        } else {
            logger.info("Referee decision: NO lift");
            if (currentLifter2 != null) currentLifter2.failedLift();
        }

    }

    public void setAnnouncerEnabled(boolean b) {
        announcerEnabled = b;
    }

    /**
     * @param timeKeepingInUse
     *            the timeKeepingInUse to set
     */
    public void setTimeKeepingInUse(boolean timeKeepingInUse) {
        this.timeKeepingInUse = timeKeepingInUse;
    }

    /**
     * @return the timeKeepingInUse
     */
    public boolean getTimeKeepingInUse() {
        return timeKeepingInUse;
    }

    /**
     * @param needToAnnounce
     *            the needToAnnounce to set
     */
    public void setNeedToAnnounce(boolean needToAnnounce) {
        this.needToAnnounce = needToAnnounce;
    }

    /**
     * @return the needToAnnounce
     */
    public boolean getNeedToAnnounce() {
        return needToAnnounce;
    }

    /**
     * @return
     */
    public boolean getStartTimeAutomatically() {
        return startTimeAutomatically;
    }

    /**
     * @param lifter
     * @param groupData
     */
    public void manageTimerOwner(Lifter lifter, SessionData groupData, CountdownTimer timing) {
        // first time we use the timekeeper button or that we announce
        // with the automatic start determines that
        // there is a timekeeper and that timekeeper runs clock.
        groupData.setTimeKeepingInUse(true);
            
        if (lifter != timing.getOwner()) {
            final int remaining = groupData.getTimeAllowed();
            timing.setTimeRemaining(remaining);
            timing.setOwner(lifter); // enforce rule 6.5.15 -- lifter
                              // only gets 2 minutes if clock did
                              // not start for someone else
            logger.debug("timekeeping in use, setting lifter {} as owner", lifter);
        }
    }

    /**
     * @param lifter
     * @param groupData
     */
    public void startTimer(Lifter lifter, SessionData groupData,CountdownTimer timing) {
        manageTimerOwner(lifter,groupData, timing);
        timing.start();
    }
    
    public Item getPublicAddressItem() {
		return publicAddressItem;
	}

	public void setPublicAddressItem(Item publicAddressItem) {
		this.publicAddressItem = publicAddressItem;
	}
	

	public void clearPublicAddressDisplay() {
		PublicAddressMessageEvent event = new PublicAddressMessageEvent();
		// more intuitive if hiding the display does not stop the timer.
		// publicAddressTimer.stop();
		event.setHide(true);
		fireBlackBoardEvent(event);
	}


	public void displayPublicAddress() {
		PublicAddressCountdownTimer timer = (PublicAddressCountdownTimer) publicAddressItem.getItemProperty("remainingSeconds").getValue();
		int remainingMilliseconds = timer.getRemainingMilliseconds();
		
		// tell the registered browsers to pop-up the message area
		PublicAddressMessageEvent messageEvent = new PublicAddressMessageEvent();
		messageEvent.setHide(false);
		messageEvent.setTitle((String) publicAddressItem.getItemProperty("title").getValue());
		messageEvent.setMessage((String) publicAddressItem.getItemProperty("message").getValue());
		messageEvent.setRemainingMilliseconds(remainingMilliseconds);
		fireBlackBoardEvent(messageEvent);
		
		// tell the message areas to display the initial time
		PublicAddressTimerEvent timerEvent = new PublicAddressTimerEvent();
		timerEvent.setRemainingMilliseconds(remainingMilliseconds);
		fireBlackBoardEvent(timerEvent);

	}

	/**
	 * @param event
	 */
	public void fireBlackBoardEvent(Event event) {
		blackBoardEventRouter.fire(event);
	}
	
	public void addBlackBoardListener(Listener listener) {
		blackBoardEventRouter.addListener(listener);
	}

	public void removeBlackBoardListener(Listener listener) {
		blackBoardEventRouter.removeListener(listener);
	}


	public PublicAddressCountdownTimer getPublicAddressTimer() {
		return publicAddressTimer;
	}

//	public void setPublicAddressTimer(PublicAddressCountdownTimer publicAddressTimer) {
//		this.publicAddressTimer = publicAddressTimer;
//	}

}