/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.mobile;

import java.util.List;

import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.CompetitionApplicationComponents;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

@SuppressWarnings("serial")
public class MobileHome extends VerticalLayout implements ApplicationView {

    public static final String BUTTON_WIDTH = "6em"; //$NON-NLS-1$
    public static final String BUTTON_NARROW_WIDTH = "4em"; //$NON-NLS-1$
    public static final String BUTTON_HEIGHT = "2em"; //$NON-NLS-1$

    private CompetitionApplication app;

    private List<Platform> platforms;

    private String platformName;

    private String viewName;

    private static Logger logger = LoggerFactory.getLogger(MobileHome.class);

    public MobileHome(boolean initFromFragment, String viewName) {

        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }

        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());
        app = CompetitionApplication.getCurrent();
        platforms = Platform.getAll();
        if (platforms.size() > 1) {
            final MPlatformSelect platformSelection = new MPlatformSelect();
            this.addComponent(platformSelection);
        }
        final MRefereeSelect refereeSelection = new MRefereeSelect();
        this.addComponent(refereeSelection);
        final MRefereeDecisions refereeDecisions = new MRefereeDecisions();
        this.addComponent(refereeDecisions);
        final MTimeKeeper timekeeper = new MTimeKeeper();
        this.addComponent(timekeeper);
        final MJurySelect jurySelection = new MJurySelect();
        this.addComponent(jurySelection);
        final MJuryDecisions juryDecisions = new MJuryDecisions();
        this.addComponent(juryDecisions);
        final MPlatesInfo platesInfo = new MPlatesInfo();
        this.addComponent(platesInfo);
        this.setStyleName("mobileMenu"); //$NON-NLS-1$

        Component filler = new VerticalLayout();
        this.addComponent(filler);
        this.setExpandRatio(filler, 1.0F);

        this.setSpacing(true);
        this.setMargin(true);
        this.setSizeFull();

        app.getMainWindow().executeJavaScript("scrollTo(0,1)"); //$NON-NLS-1$
    }

    public class MRefereeDecisions extends HorizontalLayout {
        public MRefereeDecisions() {
            this.setSpacing(true);
            final Label label = new Label(Messages.getString("MobileMenu.RefDecisions", app.getLocale())); //$NON-NLS-1$
            this.addComponent(label);
            this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
            final NativeButton button = new NativeButton(
                    Messages.getString("MobileMenu.Display", app.getLocale()), new Button.ClickListener() { //$NON-NLS-1$
                        @Override
                        public void buttonClick(ClickEvent event) {
                            RefereeDecisions refereeDecisions = createRefereeDecisions();
                            app.setMainPanelContent(refereeDecisions);
                        }
                    });
            button.setWidth(BUTTON_WIDTH);
            button.setHeight(BUTTON_HEIGHT);
            this.addComponent(button);
        }
    }

    public class MTimeKeeper extends HorizontalLayout {
        public MTimeKeeper() {
            this.setSpacing(true);
            final Label label = new Label(Messages.getString("MobileMenu.Timekeeper", app.getLocale())); //$NON-NLS-1$
            this.addComponent(label);
            this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
            final NativeButton button = new NativeButton(
                    Messages.getString("MobileMenu.Display", app.getLocale()), new Button.ClickListener() { //$NON-NLS-1$
                        @Override
                        public void buttonClick(ClickEvent event) {
                            MTimekeeperConsole timekeeperConsole = new MTimekeeperConsole(false, "Timekeeper");
                            app.setMainPanelContent(timekeeperConsole);
                        }
                    });
            button.setWidth(BUTTON_WIDTH);
            button.setHeight(BUTTON_HEIGHT);
            this.addComponent(button);
        }
    }

    public class MJuryDecisions extends HorizontalLayout {
        public MJuryDecisions() {
            this.setSpacing(true);
            final Label label = new Label(Messages.getString("MobileMenu.JuryDecisions", app.getLocale())); //$NON-NLS-1$
            this.addComponent(label);
            this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
            
            final NativeButton button2 = new NativeButton(
                    Messages.getString("MobileMenu.Display", app.getLocale()), new Button.ClickListener() { //$NON-NLS-1$
                        @Override
                        public void buttonClick(ClickEvent event) {
                            JuryDecisions juryDecisions = createJuryDecisions();
                            app.setMainPanelContent(juryDecisions);
                        }
                    });
            button2.setWidth(BUTTON_WIDTH);
            button2.setHeight(BUTTON_HEIGHT);
            this.addComponent(button2);
        }
    }

    public class MPlatesInfo extends HorizontalLayout {
        public MPlatesInfo() {
            this.setSpacing(true);
            final Label label = new Label(Messages.getString("MobileMenu.Plates", app.getLocale())); //$NON-NLS-1$
            this.addComponent(label);
            this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
            final NativeButton button = new NativeButton(
                    Messages.getString("MobileMenu.Display", app.getLocale()), new Button.ClickListener() { //$NON-NLS-1$
                        @Override
                        public void buttonClick(ClickEvent event) {
                            MPlatesInfoView plates = new MPlatesInfoView(false, Messages.getString(
                                    "MobileMenu.PlatesTitle", app.getLocale())); //$NON-NLS-1$
                            app.setMainPanelContent(plates);
                        }
                    });
            button.setWidth(BUTTON_WIDTH);
            button.setHeight(BUTTON_HEIGHT);
            this.addComponent(button);
        }
    }

    public class MJuryMemberSelect extends HorizontalLayout {

    }

    public class MPlatformSelect extends HorizontalLayout {
        public MPlatformSelect() {
            final Label label = new Label(Messages.getString("MobileMenu.Platforms", app.getLocale())); //$NON-NLS-1$
            this.addComponent(label);
            this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);

            for (Platform platform : platforms) {
                final String platformName1 = platform.getName();
                final NativeButton button = new NativeButton(platformName1, new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        app.setPlatformByName(platformName1);
                        SessionData masterData = app.getMasterData(platformName1);
                        logger.debug("new platform={}, new group = {}", platformName1, masterData.getCurrentSession()); //$NON-NLS-1$
                        app.setCurrentCompetitionSession(masterData.getCurrentSession());
                    }
                });
                button.setWidth(BUTTON_WIDTH);
                button.setHeight(BUTTON_HEIGHT);
                this.addComponent(button);
            }
        }
    }

    public class MRefereeSelect extends HorizontalLayout {

        MRefereeSelect() {
            this.setSpacing(true);
            final Label label = new Label(Messages.getString("MobileMenu.Referee", app.getLocale())); //$NON-NLS-1$
            this.addComponent(label);
            this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
            final NativeButton button1 = new NativeButton("1", new Button.ClickListener() { //$NON-NLS-1$
                        @Override
                        public void buttonClick(ClickEvent event) {
                            CompetitionApplication.getCurrent().displayMRefereeConsole(0);
                        }
                    });
            button1.setWidth(BUTTON_NARROW_WIDTH);
            button1.setHeight(BUTTON_HEIGHT);
            this.addComponent(button1);

            final NativeButton button2 = new NativeButton("2", new Button.ClickListener() { //$NON-NLS-1$
                        @Override
                        public void buttonClick(ClickEvent event) {
                            CompetitionApplication.getCurrent().displayMRefereeConsole(1);
                        }
                    });
            button2.setWidth(BUTTON_NARROW_WIDTH);
            button2.setHeight(BUTTON_HEIGHT);
            this.addComponent(button2);

            final NativeButton button3 = new NativeButton("3", new Button.ClickListener() { //$NON-NLS-1$
                        @Override
                        public void buttonClick(ClickEvent event) {
                            CompetitionApplication.getCurrent().displayMRefereeConsole(2);
                        }
                    });
            button3.setWidth(BUTTON_NARROW_WIDTH);
            button3.setHeight(BUTTON_HEIGHT);
            this.addComponent(button3);
        }
    }

    public class MJurySelect extends HorizontalLayout {

        MJurySelect() {
            this.setSpacing(true);
            final Label label = new Label(Messages.getString("MobileMenu.Jury", app.getLocale())); //$NON-NLS-1$
            this.addComponent(label);
            this.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
            final NativeButton button1 = new NativeButton("1", new Button.ClickListener() { //$NON-NLS-1$
                        @Override
                        public void buttonClick(ClickEvent event) {
                            CompetitionApplication.getCurrent().displayMJuryConsole(0);
                        }
                    });
            button1.setWidth(BUTTON_NARROW_WIDTH);
            button1.setHeight(BUTTON_HEIGHT);
            this.addComponent(button1);

            final NativeButton button2 = new NativeButton("2", new Button.ClickListener() { //$NON-NLS-1$
                        @Override
                        public void buttonClick(ClickEvent event) {
                            CompetitionApplication.getCurrent().displayMJuryConsole(1);
                        }
                    });
            button2.setWidth(BUTTON_NARROW_WIDTH);
            button2.setHeight(BUTTON_HEIGHT);
            this.addComponent(button2);

            final NativeButton button3 = new NativeButton("3", new Button.ClickListener() { //$NON-NLS-1$
                        @Override
                        public void buttonClick(ClickEvent event) {
                            CompetitionApplication.getCurrent().displayMJuryConsole(2);
                        }
                    });
            button3.setWidth(BUTTON_NARROW_WIDTH);
            button3.setHeight(BUTTON_HEIGHT);
            this.addComponent(button3);
        }
    }

    // /**
    // * @return
    // */
    // private ORefereeConsole createRefConsole() {
    // ORefereeConsole refConsole = new ORefereeConsole(false, "Refereeing");
    // return refConsole;
    // }

    /**
     * @param refIndex
     * @return
     */
    private RefereeDecisions createRefereeDecisions() {
        RefereeDecisions decisionLights = new RefereeDecisions(false, CompetitionApplicationComponents.JURY_LIGHTS, false); //$NON-NLS-1$
        return decisionLights;
    }

    /**
     * @param refIndex
     * @return
     */
    private JuryDecisions createJuryDecisions() {
        JuryDecisions decisionLights = new JuryDecisions(false, CompetitionApplicationComponents.JURY_LIGHTS); //$NON-NLS-1$
        return decisionLights;
    }
    
    
    @Override
    public void refresh() {
    }

    @Override
    public boolean needsMenu() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.IRefereeConsole#getFragment()
     */
    @Override
    public String getFragment() {
        return viewName + (platformName == null ? "" : "/" + platformName);
    }

    /*
     * (non-Javadoc)
     * 
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

    @Override
    public void windowClose(CloseEvent e) {
        unregisterAsListener();
    }

    @Override
    public boolean needsBlack() {
        return false;
    }

    /**
     * Register all listeners for this app.
     */
    @Override
    public void registerAsListener() {
        logger.debug("{} listening to window close events", this);
        app.getMainWindow().addListener((CloseListener) this);
    }

    /**
     * Undo all registrations in {@link #registerAsListener()}.
     */
    @Override
    public void unregisterAsListener() {
        logger.debug("{} stopped to window close events", this);
        app.getMainWindow().removeListener((CloseListener) this);
    }

    private static int classCounter = 0; // per class
    private final int instanceId = classCounter++; // per instance

    @Override
    public String getInstanceId() {
        return Long.toString(instanceId);
    }

    @Override
    public String getLoggingId() {
        return viewName + getInstanceId();
    }

}
