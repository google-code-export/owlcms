/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.mobile;

import java.net.URL;

import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.timer.CountdownTimer;
import org.concordiainternational.competition.timer.CountdownTimerListener;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.CompetitionApplicationComponents;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.TimeStoppedNotificationReason;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.touchdiv.TouchDiv;
import org.vaadin.touchdiv.TouchDiv.TouchEvent;
import org.vaadin.touchdiv.TouchDiv.TouchListener;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;


/**
 * @author jflamy
 *
 */
@SuppressWarnings("serial")
public class MTimekeeperConsole extends VerticalLayout implements CountdownTimerListener, ApplicationView, CloseListener, URIHandler {

    private static final long serialVersionUID = 1L;
    static final Logger timingLogger = LoggerFactory
            .getLogger("org.concordiainternational.competition.timer.TimingLogger"); //$NON-NLS-1$

    private HorizontalLayout top;
    private HorizontalLayout bottom;


    private SessionData groupData;
    private CompetitionApplication app = CompetitionApplication.getCurrent();

    private Logger logger = LoggerFactory.getLogger(MTimekeeperConsole.class);

    private Integer refereeIndex = null;

    private Label timerDisplay;

    private String platformName;

    private String viewName;

    private TouchDiv start;
    private TouchDiv stop;
    private TouchDiv one;
    private TouchDiv two;
    
    private boolean blocked = false;

    private int prevTimeRemaining;


    public MTimekeeperConsole(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }

        if (app == null) this.app = CompetitionApplication.getCurrent();

        if (platformName == null) {
            // get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        } else if (app.getPlatform() == null) {
            app.setPlatformByName(platformName);
        }

        if (groupData == null) groupData = app.getMasterData(platformName);

        app.getMainWindow().addURIHandler(this);
        registerAsListener();

        init();
    }



    /**
     * 
     */
    protected void init() {
        this.setSizeFull();
        this.addStyleName("mtkPad");
        setupTop();
        setupBottom(); 

        this.addComponent(top);
        this.addComponent(bottom);
        this.setExpandRatio(top, 20.0F);
        this.setExpandRatio(bottom, 80.0F);

    }



    /**
     * 
     */
    private void setupTop() {
        timerDisplay = new Label();
        timerDisplay.setValue(null);
        timerDisplay.setSizeFull();
        timerDisplay.setStyleName("mtkTimerDisplay");
        if (top == null) {
            top = new HorizontalLayout();
        } else {
            top.removeAllComponents();
        }
        top.addComponent(timerDisplay);
        top.setComponentAlignment(timerDisplay, Alignment.MIDDLE_CENTER);
        top.setSizeFull();
        top.setMargin(false);
        top.addStyleName("mtkTop");
    }

    /**
     */
    private void setupBottom() {
        if (bottom == null) {
            bottom = new HorizontalLayout();
        }
        bottom.setSizeFull();
        bottom.setMargin(true);
        bottom.setSpacing(true);

        start = new TouchDiv("");
        start.setHeight("90%");
        start.setWidth("90%");
        start.addStyleName("mtkStart");
        start.addListener(new TouchListener(){

            @Override
            public void onTouch(TouchEvent event) {
                startSelected();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startDoIt();
                    }
                }).start();
            }});

        stop = new TouchDiv("");
        stop.addStyleName("mtkStop");
        stop.setHeight("90%");
        stop.setWidth("90%");
        stop.addListener(new TouchListener(){

            @Override
            public void onTouch(TouchEvent event) {
                stopSelected();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        stopDoIt();
                    }
                }).start();
            }});


        VerticalLayout minutes = new VerticalLayout();
        one = new TouchDiv("1");
        one.addStyleName("mtkOne");
        one.setHeight("90%");
        one.setWidth("90%");
        one.addListener(new TouchListener(){

            @Override
            public void onTouch(TouchEvent event) {
                stopSelected();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        oneMinuteDoIt();
                    }
                }).start();
            }});

        two = new TouchDiv("2");
        two.addStyleName("mtkTwo");
        two.setHeight("90%");
        two.setWidth("90%");
        two.addListener(new TouchListener(){

            @Override
            public void onTouch(TouchEvent event) {
                stopSelected();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        twoMinutesDoIt();
                    }
                }).start();
            }});

        minutes.addComponent(one);
        minutes.setComponentAlignment(one,Alignment.MIDDLE_CENTER);
        minutes.addComponent(two);
        minutes.setComponentAlignment(two,Alignment.MIDDLE_CENTER);
        minutes.setExpandRatio(one,50.0F);
        minutes.setExpandRatio(two,50.0F);

        bottom.addComponent(start);
        bottom.setComponentAlignment(start,Alignment.MIDDLE_CENTER);
        bottom.addComponent(stop);
        bottom.setComponentAlignment(stop,Alignment.MIDDLE_CENTER);
        bottom.addComponent(minutes);
        bottom.setComponentAlignment(minutes,Alignment.MIDDLE_CENTER);

        bottom.setExpandRatio(start,50.0F);
        bottom.setExpandRatio(stop,50.0F);
        bottom.setExpandRatio(minutes,50.0F);

    }

    /**
     * 
     */
    private void stopSelected() {
        stop.removeStyleName("mtkStopUnselected");
        start.removeStyleName("mtkStartSelected");

        stop.addStyleName("mtkStopSelected");
        start.addStyleName("mtkStopUnselected");
        
        stop.setValue("");
        start.setValue("GO");
    }

    /**
     * 
     */
    private void startSelected() {
        stop.removeStyleName("mtkStopSelected");
        start.removeStyleName("mtkStartUnselected");

        stop.addStyleName("mtkStopUnselected");
        start.addStyleName("mtkStartSelected");

        stop.setValue("STOP");
        start.setValue("");
        disable();
    }

    
    private void startDoIt() {
        logger.info("start clicked");
        groupData.setTimeKeepingInUse(true);
        if (groupData.getTimer().isRunning()) {
            // do nothing
            timingLogger.debug("start timer.isRunning()={}", true); //$NON-NLS-1$
        } else {
            setBlocked(false); // !!!!
            enableStopStart(true);
            timingLogger.debug("start timer.isRunning()={}", false); //$NON-NLS-1$
            groupData.startUpdateModel();

        }
    }

    private void stopDoIt() {
        logger.info("stop clicked");
        groupData.setTimeKeepingInUse(true);
        if (groupData.getTimer().isRunning()) {
            timingLogger.debug("stop timer.isRunning()={}", true); //$NON-NLS-1$
            setBlocked(true);
            groupData.stopUpdateModel();
            enableStopStart(false);
        } else {
            timingLogger.debug("stop timer.isRunning()={}", false); //$NON-NLS-1$
            // do nothing.
        }
    }

    private void oneMinuteDoIt() {
        timingLogger.debug("oneMinute"); //$NON-NLS-1$

        // call group data first because this resets the timers
        logger.info("resetting to one minute for {}", groupData.getCurrentLifter()); //$NON-NLS-1$
        groupData.oneMinuteUpdateModel();
        enableStopStart(false);
    }

    private void twoMinutesDoIt() {
        timingLogger.debug("twoMinutes"); //$NON-NLS-1$

        // call group data first because this resets the timers
        logger.info("resetting to two minutes for {}", groupData.getCurrentLifter()); //$NON-NLS-1$
        groupData.twoMinuteUpdateModel();
        enableStopStart(false);
    }


    /**
     * 
     */
    protected void disable() {
        stop.setEnabled(false);
        start.setEnabled(false);
        timerDisplay.setStyleName("blocked");
    }



    public void enableStopStart(boolean running) {
        if (!running) {
            stop.setEnabled(false);
            stop.removeStyleName("primary");
            start.setEnabled(true);         
            start.addStyleName("primary");
        } else {
            start.setEnabled(false);
            start.removeStyleName("primary");
            stop.setEnabled(true);          
            stop.addStyleName("primary");
        } 
    }

    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.IRefereeConsole#refresh()
     */
    @Override
    public void refresh() {
    }


    /**
     * 
     */
    protected void doResetBottom() {
        timerDisplay.setEnabled(true);
        timerDisplay.setValue("");
        timerDisplay.setStyleName("timekeeperDisplay");
    }


    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.components.ApplicationView#needsMenu()
     */
    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.IRefereeConsole#needsMenu()
     */
    @Override
    public boolean needsMenu() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.IRefereeConsole#getFragment()
     */
    @Override
    public String getFragment() {
        return viewName+"/"+(platformName == null ? "" : platformName)+"/"+((int)this.refereeIndex+1);
    }


    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.IRefereeConsole#setParametersFromFragment()
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
    }


    /**
     * Register all listeners for this app.
     * Exception: do not register the URIHandler here.
     */
    @Override
    public void registerAsListener() {
        unregisterAsListenerDoIt();
        logger.debug("registering as listener");
        app.getMainWindow().addListener((CloseListener)this);
        CountdownTimer timer = groupData.getTimer();
        if (timer != null) timer.addListener(this);
    }


    /**
     * Undo all registrations in {@link #registerAsListener()}.
     */
    @Override
    public void unregisterAsListener() {
        logger.debug("unregistering as listener");
        unregisterAsListenerDoIt();
    }


    public void unregisterAsListenerDoIt() {
        app.getMainWindow().removeListener((CloseListener)this);
        // timer countdown events
        CountdownTimer timer = groupData.getTimer();    
        if (timer != null) timer.removeListener(this);
    }


    /* Will be called when page is loaded.
     * @see com.vaadin.terminal.URIHandler#handleURI(java.net.URL, java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.IRefereeConsole#handleURI(java.net.URL, java.lang.String)
     */
    @Override
    public DownloadStream handleURI(URL context, String relativeUri) {
        registerAsListener();
        app.getMainWindow().executeJavaScript("scrollTo(0,1)");
        return null;
    }


    /* Will be called when page is unloaded (including on refresh).
     * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
     */
    @Override
    public void windowClose(CloseEvent e) {
        unregisterAsListener();
    }



    @Override
    public void finalWarning(int timeRemaining) {
        // do nothing      
    }



    @Override
    public void initialWarning(int timeRemaining) {
        // do nothing
    }



    @Override
    public void noTimeLeft(int timeRemaining) {
        // do nothing    
    }


    @Override
    public void normalTick(int timeRemaining) {
        if (timerDisplay == null) {
            setBlocked(false);
            return;
        } else if (TimeFormatter.getSeconds(prevTimeRemaining) == TimeFormatter.getSeconds(timeRemaining)) {
            prevTimeRemaining = timeRemaining;
            setBlocked(false);
            return;
        } else {
            prevTimeRemaining = timeRemaining;
        }

        synchronized (app) {
            if (!isBlocked()) {
                timerDisplay.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
                timerDisplay.setEnabled(true);
            }
            setBlocked(false);
        }
        app.push();
    }



    @Override
    public void pause(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
        setBlocked(true); // don't process the next update from the timer.
        synchronized (app) {
            enableStopStart(false);
            if (timerDisplay != null) {
                timerDisplay.setEnabled(false);
            }
        }
        showNotification(originatingApp, reason);
        app.push();
    }



    @Override
    public void start(int timeRemaining) {
        setBlocked(false);
        synchronized (app) {
            enableStopStart(true);
            if (timerDisplay != null) {
                timerDisplay.setEnabled(true);
            }
        }
        app.push();
    }


    @Override
    public void stop(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {

        setBlocked(true); // don't process the next update from the timer.
        synchronized (app) {
            enableStopStart(false);
            if (timerDisplay != null) {
                timerDisplay.setEnabled(false);
            }
        }
        showNotification(originatingApp, reason);
        app.push();
    }


    @Override
    public void forceTimeRemaining(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
        if (timerDisplay == null) return;
        prevTimeRemaining = timeRemaining;

        synchronized (app) {
            timerDisplay.setEnabled(false); // show that timer has stopped.
            timerDisplay.setValue(TimeFormatter.formatAsSeconds(timeRemaining));
            enableStopStart(false);
            setBlocked(false);
        }
        showNotification(originatingApp, reason);
        app.push();
    }



    private void showNotification(CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
        // do nothing
        
    }



    boolean isBlocked() {
        return blocked;
    }



    void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }



}
