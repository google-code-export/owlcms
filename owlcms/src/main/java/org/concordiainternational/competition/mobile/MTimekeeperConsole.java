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
import org.concordiainternational.competition.ui.SessionData.UpdateEventListener;
import org.concordiainternational.competition.ui.TimeStoppedNotificationReason;
import org.concordiainternational.competition.ui.SessionData.UpdateEvent;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.generators.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.touchdiv.TouchDiv;
import org.vaadin.touchdiv.TouchDiv.TouchEvent;
import org.vaadin.touchdiv.TouchDiv.TouchListener;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.terminal.gwt.server.WebBrowser;
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
public class MTimekeeperConsole extends VerticalLayout implements 
ApplicationView,
CountdownTimerListener,
CloseListener,
URIHandler {
    
    private static final long serialVersionUID = 1L;
    static final Logger timingLogger = LoggerFactory
            .getLogger("org.concordiainternational.competition.timer.TimingLogger"); //$NON-NLS-1$

    private HorizontalLayout top;
    private HorizontalLayout bottom;


    private SessionData groupData;
    private CompetitionApplication app = CompetitionApplication.getCurrent();

    private Logger logger = LoggerFactory.getLogger(MTimekeeperConsole.class);

    private Label timerDisplay;

    private String platformName;

    private String viewName;

    private TouchDiv start;
    private TouchDiv stop;
    private TouchDiv one;
    private TouchDiv two;
    
    private boolean blocked = false;

    private int prevTimeRemaining;
    private UpdateEventListener updateEventListener;


    public MTimekeeperConsole(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = CompetitionApplicationComponents.MTIMEKEEPER_CONSOLE;
        }

        this.app = CompetitionApplication.getCurrent();

        if (platformName == null) {
            // get the default platform name
            platformName = CompetitionApplicationComponents.initPlatformName();
        } else if (app.getPlatform() == null) {
            app.setPlatformByName(platformName);
        }
        
        synchronized (app) {
            boolean prevDisabled = app.getPusherDisabled();
            try {
                app.setPusherDisabled(true);
                groupData = app.getMasterData(platformName);

                app.getMainWindow().addURIHandler(this);
                registerAsListener();
                CompetitionApplication.getCurrent().getUriFragmentUtility().setFragment(getFragment(), false);
                init();
            } finally {
                app.setPusherDisabled(prevDisabled);
            }
        }
    }



    /**
     * 
     */
    protected void init() {
        synchronized (app) {
            this.setSizeFull();
            this.addStyleName("mtkPad");
            setupTop();
            setupBottom(); 
    
            this.addComponent(top);
            this.addComponent(bottom);
            this.setExpandRatio(top, 33.0F);
            this.setExpandRatio(bottom, 66.0F);
            
            enableStopStart(groupData.getTimer().isRunning());
        }
        app.push();
    }



    /**
     * 
     */
    private void setupTop() {
        timerDisplay = new Label("",Label.CONTENT_XHTML);
        
        int timeRemaining = groupData.getDisplayTime();
        updateTimeRemaining(timeRemaining);
        
        timerDisplay.setSizeFull();
        WebBrowser browser = (WebBrowser) app.getMainWindow().getTerminal();
        if (browser.getScreenHeight() < 600) {
            timerDisplay.setStyleName("mtkTimerDisplaySmall");
        } else {
            timerDisplay.setStyleName("mtkTimerDisplay");
        }
        
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

        start = new TouchDiv("<div id='mtkStartLabel'>GO<img src='../VAADIN/themes/m/images/playTriangle.png'></img></div>",Label.CONTENT_XHTML);
        start.setHeight("90%");
        start.setWidth("90%");
        start.addStyleName("mtkStart");
        start.addListener(new TouchListener(){

            @Override
            public void onTouch(TouchEvent event) {
                //startSelected();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startDoIt();
                    }
                }).start();
            }});

        stop = new TouchDiv("<div id='mtkStopLabel'>STOP</div>",Label.CONTENT_XHTML);
        stop.addStyleName("mtkStop");
        stop.setHeight("90%");
        stop.setWidth("90%");
        stop.addListener(new TouchListener(){

            @Override
            public void onTouch(TouchEvent event) {
                //stopSelected();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        stopDoIt();
                    }
                }).start();
            }});


        VerticalLayout minutes = new VerticalLayout();
        one = new TouchDiv("<div id='mtkOneLabel'>1</div>",Label.CONTENT_XHTML);
        one.addStyleName("mtkOne");
        one.setHeight("90%");
        one.setWidth("90%");
        one.addListener(new TouchListener(){

            @Override
            public void onTouch(TouchEvent event) {
                //stopSelected();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        oneMinuteDoIt();
                    }
                }).start();
            }});

        two = new TouchDiv("<div id='mtkTwoLabel'>2</div>",Label.CONTENT_XHTML);
        two.addStyleName("mtkTwo");
        two.setHeight("90%");
        two.setWidth("90%");
        //two.setSizeFull();
        two.addListener(new TouchListener(){

            @Override
            public void onTouch(TouchEvent event) {
                //stopSelected();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        twoMinutesDoIt();
                    }
                }).start();
            }});

        minutes.setHeight("95%");
        minutes.setWidth("95%");
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


    
    private void startDoIt() {
        logger.info("start clicked");
        groupData.setTimeKeepingInUse(true);
        if (groupData.getTimer().isRunning()) {
            // do nothing
            timingLogger.debug("start already running timer.isRunning()={}", true); //$NON-NLS-1$
        } else {
            setBlocked(false); // !!!!
            enableStopStart(true);
            timingLogger.debug("start starting timer.isRunning()={}", false); //$NON-NLS-1$
            groupData.startUpdateModel();
        }
    }

    private void stopDoIt() {
        logger.info("stop clicked");
        groupData.setTimeKeepingInUse(true);
        if (groupData.getTimer().isRunning()) {
            timingLogger.debug("stop stopping timer.isRunning()={}", true); //$NON-NLS-1$
            setBlocked(true);
            groupData.stopUpdateModel();
            enableStopStart(false);
        } else {
            timingLogger.debug("stop already stopped timer.isRunning()={}", false); //$NON-NLS-1$
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


    public void enableStopStart(boolean running) {
        synchronized (app) {
            if (!running) {
                start.setEnabled(true);
                stop.setEnabled(false);
                showTimerDisplay(false);
                
            } else {
                start.setEnabled(false);
                stop.setEnabled(true);
                showTimerDisplay(true);
            }
        }
    }

    private void showTimerDisplay(boolean show) {
        if (show) {
            timerDisplay.removeStyleName("blocked");
        } else {
            timerDisplay.addStyleName("blocked");
        }  
    }



    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.IRefereeConsole#refresh()
     */
    @Override
    public void refresh() {
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
        return viewName+"/"+(platformName == null ? "" : platformName);
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
        
        updateEventListener = new SessionData.UpdateEventListener() {

            @Override
            public void updateEvent(UpdateEvent updateEvent) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (app) {
                            int timeRemaining = groupData.getDisplayTime();
                            updateTimeRemaining(timeRemaining);
                            enableStopStart(false);
                            setBlocked(false);
                        }
                        app.push();
                    }
                }).start();
            }

        };
        groupData.addListener(updateEventListener); //$NON-NLS-1$ 
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
        if (updateEventListener != null) groupData.removeListener(updateEventListener);
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
        pushTime(0);  
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

        pushTime(timeRemaining);
    }



    public void pushTime(int timeRemaining) {
        synchronized (app) {
            if (!isBlocked()) {
                //logger.trace("not blocked");
                updateTimeRemaining(timeRemaining);
                showTimerDisplay(true);
            } else {
                //logger.trace("blocked");
            }
            setBlocked(false);
        }
        app.push();
        logger.debug("pushed time {}",timeRemaining);
    }



    @Override
    public void pause(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
        setBlocked(true); // don't process the next update from the timer.
        synchronized (app) {
            enableStopStart(false);
            if (timerDisplay != null) {
                showTimerDisplay(false);
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
                showTimerDisplay(true);
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
                showTimerDisplay(false);
            }
        }
        showNotification(originatingApp, reason);
        app.push();
    }


    @Override
    public void forceTimeRemaining(int timeRemaining, CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
        
        logger.debug("forceTimeRemaining {}", timeRemaining);
        if (timerDisplay == null) return;
        prevTimeRemaining = timeRemaining;

        synchronized (app) {
            updateTimeRemaining(timeRemaining);
            enableStopStart(false);
            setBlocked(false);
        }
        showNotification(originatingApp, reason);
        app.push();
    }



    public void updateTimeRemaining(int timeRemaining) {
        timerDisplay.setValue("<div id='mtkTimeLabel'>"+TimeFormatter.formatAsSeconds(timeRemaining)+"</div>");
    }



    private void showNotification(CompetitionApplication originatingApp, TimeStoppedNotificationReason reason) {
        // do nothing
        
    }



    boolean isBlocked() {
        return this.blocked;
    }



    void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }



}
