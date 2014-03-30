/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.mobile.CombinedDecisions;
import org.concordiainternational.competition.mobile.IRefereeConsole;
import org.concordiainternational.competition.mobile.MJuryConsole;
import org.concordiainternational.competition.mobile.MPlatesInfoView;
import org.concordiainternational.competition.mobile.MRefereeConsole;
import org.concordiainternational.competition.mobile.MTimekeeperConsole;
import org.concordiainternational.competition.mobile.MobileHome;
import org.concordiainternational.competition.mobile.RefereeDecisions;
import org.concordiainternational.competition.spreadsheet.SpreadsheetUploader;
import org.concordiainternational.competition.ui.AnnouncerView.Mode;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.components.Menu;
import org.concordiainternational.competition.ui.components.ResultFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.SystemError;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

public class CompetitionApplicationComponents {
    public static final String ANNOUNCER_VIEW = "announcerView"; //$NON-NLS-1$
    public static final String SUMMARY_LIFT_ORDER_VIEW = "summaryLiftOrderView"; //$NON-NLS-1$
    public static final String CATEGORY_LIST = "categoryList"; //$NON-NLS-1$
    public static final String CHANGES_VIEW = "changesView"; //$NON-NLS-1$
    public static final String COMPETITION_EDITOR = "competitionEditor"; //$NON-NLS-1$
    public static final String GROUP_LIST = "groupList"; //$NON-NLS-1$
    public static final String PUBLIC_ATTEMPT_BOARD_VIEW = "publicAttemptBoard"; //$NON-NLS-1$
    public static final String LIFTER_ATTEMPT_BOARD_VIEW = "lifterAttemptBoard"; //$NON-NLS-1$
    public static final String LIFT_ORDER_VIEW = "liftOrderBoard"; //$NON-NLS-1$
    public static final String PLATFORM_LIST = "platformList"; //$NON-NLS-1$
    public static final String REGISTRATION_LIST = "registrationList"; //$NON-NLS-1$
    public static final String RESULT_BOARD = "resultBoard"; //$NON-NLS-1$
    public static final String OREFEREE_CONSOLE = "oRefereeConsole"; //$NON-NLS-1$
    public static final String MREFEREE_CONSOLE = "refereeConsole"; //$NON-NLS-1$
    public static final String OJURY_CONSOLE = "oJuryConsole"; //$NON-NLS-1$
    public static final String MJURY_CONSOLE = "juryConsole"; //$NON-NLS-1$
    public static final String MPLATES_INFO = "platesInfo"; //$NON-NLS-1$
    public static final String RESULT_VIEW = "resultView"; //$NON-NLS-1$
    public static final String TIMEKEEPER_VIEW = "timekeeperView"; //$NON-NLS-1$
    public static final String UPLOAD_VIEW = "uploadView"; //$NON-NLS-1$
    public static final String WEIGH_IN_LIST = "weighInList"; //$NON-NLS-1$
    public static final String REFEREE_TESTING = "decisionLights"; //$NON-NLS-1$
    public static final String JURY_LIGHTS = "juryLights"; //$NON-NLS-1$
    public static final String COUNTDOWN_DISPLAY = "countdownDisplay"; //$NON-NLS-1$
    public static final String SPREADSHEET_UPLOADER = "spreadsheetUpload";
    public static final String HOME = ""; // empty fragment //$NON-NLS-1$
    public static final String MOBILE_HOME = "mobileHome"; // empty fragment //$NON-NLS-1$
    public static final String MTIMEKEEPER_CONSOLE = "timekeeperConsole";

    private Panel mainPanel;
    public Menu menu;
    public Window mainWindow;
    public ApplicationView currentView;

    private Map<String, CompetitionApplicationComponent> urlFragmentToView = new HashMap<String, CompetitionApplicationComponent>();
    private Platform platform;
    private Logger logger = LoggerFactory.getLogger(CompetitionApplicationComponents.class);

    public CompetitionApplicationComponents(Menu menu, String platformName) {
        this.mainPanel = null;
        this.menu = menu;
        this.setPlatformByName(platformName);

        urlFragmentToView.put(HOME, new EmptyComponent());
        urlFragmentToView.put(ANNOUNCER_VIEW, new AnnouncerViewComponent());
        urlFragmentToView.put(SUMMARY_LIFT_ORDER_VIEW, new SummaryLiftOrderViewComponent());
        urlFragmentToView.put(CATEGORY_LIST, new CategoryListComponent());
        urlFragmentToView.put(CHANGES_VIEW, new ChangesViewComponent());
        urlFragmentToView.put(COMPETITION_EDITOR, new CompetitionEditorComponent());
        urlFragmentToView.put(COUNTDOWN_DISPLAY, new CountdownDisplayComponent());
        urlFragmentToView.put(REFEREE_TESTING, new RefereeTestingComponent());
        urlFragmentToView.put(JURY_LIGHTS, new JuryLightsComponent());
        urlFragmentToView.put(GROUP_LIST, new GroupListComponent());
        urlFragmentToView.put(PUBLIC_ATTEMPT_BOARD_VIEW, new PublicAttemptBoardComponent());
        urlFragmentToView.put(LIFTER_ATTEMPT_BOARD_VIEW, new LifterAttemptBoardComponent());
        urlFragmentToView.put(LIFT_ORDER_VIEW, new LiftOrderComponent());
        urlFragmentToView.put(PLATFORM_LIST, new PlatformListComponent());
        urlFragmentToView.put(REGISTRATION_LIST, new RegistrationListComponent());
        urlFragmentToView.put(RESULT_BOARD, new ResultBoardComponent());
        urlFragmentToView.put(RESULT_VIEW, new ResultViewComponent());
        urlFragmentToView.put(OREFEREE_CONSOLE, new RefereeConsoleComponent(false));
        urlFragmentToView.put(MREFEREE_CONSOLE, new RefereeConsoleComponent(true));
        urlFragmentToView.put(MTIMEKEEPER_CONSOLE, new TimekeeperConsoleComponent());
        urlFragmentToView.put(OJURY_CONSOLE, new JuryConsoleComponent(false));
        urlFragmentToView.put(MJURY_CONSOLE, new JuryConsoleComponent(true));
        urlFragmentToView.put(MPLATES_INFO, new PlatesInfoComponent());
        urlFragmentToView.put(TIMEKEEPER_VIEW, new TimekeeperConsoleComponent() /* new TimekeeperViewComponent() */);
        urlFragmentToView.put(UPLOAD_VIEW, new SpreadsheetUploaderComponent());
        urlFragmentToView.put(WEIGH_IN_LIST, new WeighInListComponent());
        urlFragmentToView.put(MOBILE_HOME, new MobileHomeComponent());
    }

    /**
     * Lazy factory for application components.
     * 
     */
    private interface CompetitionApplicationComponent {
        ApplicationView getView(boolean initFromFragment, String viewName, String stylesheet);
    }

    /**
     * Lazy builder for competition editor.
     */
    private class CompetitionEditorComponent implements CompetitionApplicationComponent {
        private CompetitionEditor competitionEditor = null;

        @Override
        public CompetitionEditor getView(boolean initFromFragment, String viewName, String stylesheetName) {
            competitionEditor = (new CompetitionEditor(initFromFragment, viewName));
            return competitionEditor;
        }
    }

    /**
     * Lazy builder for platformName list.
     */
    private class PlatformListComponent implements CompetitionApplicationComponent {
        private PlatformList platformList = null;

        @Override
        public PlatformList getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.platformList = (new PlatformList(initFromFragment, viewName));
            return platformList;
        }
    }

    /**
     * Lazy builder for category list.
     */
    private class CategoryListComponent implements CompetitionApplicationComponent {
        private CategoryList categoryList = null;

        @Override
        public CategoryList getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.categoryList = (new CategoryList(initFromFragment, viewName));
            return categoryList;
        }
    }

    /**
     * Lazy builder for group list.
     */
    private class GroupListComponent implements CompetitionApplicationComponent {
        private SessionList groupList = null;

        @Override
        public SessionList getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.groupList = (new SessionList(initFromFragment, viewName));
            return groupList;
        }
    }

    /**
     * Lazy builder for weigh-in list.
     */
    private class WeighInListComponent implements CompetitionApplicationComponent {
        private WeighInList weighInList = null;

        @Override
        public WeighInList getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.weighInList = (new WeighInList(initFromFragment, viewName, false));
            return weighInList;
        }
    }

    /**
     * Lazy builder for Countdown
     */
    private class CountdownDisplayComponent implements CompetitionApplicationComponent {
        private CountdownDisplay countdownDisplay = null;

        @Override
        public ApplicationView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.countdownDisplay = (new CountdownDisplay(initFromFragment, viewName));
            return countdownDisplay;
        }
    }

    /**
     * Lazy builder for Decision Lights
     */
    private class RefereeTestingComponent implements CompetitionApplicationComponent {
        private RefereeTesting decisionLights = null;

        @Override
        public ApplicationView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.decisionLights = (new RefereeTesting(initFromFragment, viewName, false, false));
            return decisionLights;
        }
    }

    /**
     * Lazy builder for Jury Lights
     */
    private class JuryLightsComponent implements CompetitionApplicationComponent {
        private CombinedDecisions juryLights = null;

        @Override
        public ApplicationView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.juryLights = (new CombinedDecisions(initFromFragment, viewName));
            return juryLights;
        }
    }

    /**
     * Lazy builder for Referee buttons
     */
    private class RefereeConsoleComponent implements CompetitionApplicationComponent {
        private IRefereeConsole refereeConsole = null;
        private boolean mobile;

        public RefereeConsoleComponent(boolean mobile) {
            this.mobile = mobile;
        }

        @Override
        public ApplicationView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            if (mobile) {
                this.refereeConsole = (new MRefereeConsole(initFromFragment, viewName));
            } else {
                this.refereeConsole = (new ORefereeConsole(initFromFragment, viewName));
            }
            return (ApplicationView) refereeConsole;
        }
    }

    /**
     * Lazy builder for Timekeeper buttons
     */
    private class TimekeeperConsoleComponent implements CompetitionApplicationComponent {
        private MTimekeeperConsole timekeeperConsole = null;

        @Override
        public ApplicationView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.timekeeperConsole = (new MTimekeeperConsole(initFromFragment, viewName));
            return (ApplicationView) timekeeperConsole;
        }
    }

    /**
     * Lazy builder for Jury Decision display
     */
    private class JuryConsoleComponent implements CompetitionApplicationComponent {
        private IRefereeConsole juryConsole = null;
        private boolean mobile;

        public JuryConsoleComponent(boolean mobile) {
            this.mobile = mobile;
        }

        @Override
        public ApplicationView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            if (mobile) {
                this.juryConsole = (new MJuryConsole(initFromFragment, viewName));
            } else {
                this.juryConsole = (new OJuryConsole(initFromFragment, viewName));
            }
            return (ApplicationView) juryConsole;
        }
    }

    /**
     * Lazy builder for Mobile Home Page
     */
    private class MobileHomeComponent implements CompetitionApplicationComponent {
        private MobileHome mobileHome = null;

        @Override
        public ApplicationView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.mobileHome = (new MobileHome(initFromFragment, viewName));
            return (ApplicationView) mobileHome;
        }
    }

    /**
     * Lazy builder for Referee buttons
     */
    private class PlatesInfoComponent implements CompetitionApplicationComponent {
        private MPlatesInfoView platesInfoConsole = null;

        @Override
        public ApplicationView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.platesInfoConsole = (new MPlatesInfoView(initFromFragment, viewName));
            return (ApplicationView) platesInfoConsole;
        }
    }

    /**
     * Lazy builder for registration list.
     */
    private class RegistrationListComponent implements CompetitionApplicationComponent {
        private WeighInList weighInList = null;

        @Override
        public WeighInList getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.weighInList = (new WeighInList(initFromFragment, viewName, true));
            return weighInList;
        }
    }

    /**
     * Lazy builder for spreadsheet upload page.
     */
    private class SpreadsheetUploaderComponent implements CompetitionApplicationComponent {
        private SpreadsheetUploader spreadsheetUploader = null;

        @Override
        public SpreadsheetUploader getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.spreadsheetUploader = (new SpreadsheetUploader(initFromFragment, viewName));
            return spreadsheetUploader;
        }
    }

    /**
     * Lazy builder for announcer view.
     */
    private class AnnouncerViewComponent implements CompetitionApplicationComponent {
        private AnnouncerView announcerView = null;

        @Override
        public AnnouncerView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            announcerView = (new AnnouncerView(initFromFragment, viewName, Mode.ANNOUNCER));
            announcerView.adjustSplitBarLocation();
            return announcerView;
        }
    }

    /**
     * Lazy builder for change management (marshal) view.
     */
    private class ChangesViewComponent implements CompetitionApplicationComponent {
        private AnnouncerView changesView = null;

        @Override
        public AnnouncerView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            changesView = (new AnnouncerView(initFromFragment, viewName, Mode.MARSHALL));
            changesView.adjustSplitBarLocation();
            return changesView;
        }
    }

    /**
     * Lazy builder for time keeper view.
     */
    @SuppressWarnings("unused")
    private class TimekeeperViewComponent implements CompetitionApplicationComponent {
        private AnnouncerView timekeeperView = null;

        @Override
        public AnnouncerView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            timekeeperView = (new AnnouncerView(initFromFragment, viewName, Mode.TIMEKEEPER));
            timekeeperView.adjustSplitBarLocation();
            return timekeeperView;
        }
    }

    /**
     * Lazy builder for registration list.
     */
    private class ResultViewComponent implements CompetitionApplicationComponent {
        private ResultView resultView = null;

        @Override
        public ResultView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            this.resultView = (new ResultView(initFromFragment, viewName));
            return resultView;
        }
    }

    /**
     * Lazy builder for main result board.
     */
    private class ResultBoardComponent implements CompetitionApplicationComponent {
        private ResultFrame resultBoard = null;

        @Override
        public ResultFrame getView(boolean initFromFragment, String viewName, String newStyleSheetName) {
            try {
                Locale locale = CompetitionApplication.getCurrentLocale();
                String localeSuffix = "";
                if ("en".equals(locale.getLanguage())) {
                    localeSuffix = "-en";
                }
                resultBoard = (new ResultFrame(initFromFragment, viewName,
                        "jsp/resultBoard" + localeSuffix + ".jsp?platformName=", newStyleSheetName)); //$NON-NLS-1$
                if (newStyleSheetName != null && !initFromFragment) {
                    resultBoard.setStylesheetName(newStyleSheetName);
                }
            } catch (MalformedURLException e) {
                throw new SystemError(e);
            }
            return resultBoard;
        }
    }

    /**
     * Lazy builder for lift order board.
     */
    private class LiftOrderComponent implements CompetitionApplicationComponent {
        private ResultFrame liftOrderBoard = null;

        @Override
        public ResultFrame getView(boolean initFromFragment, String viewName, String newStylesheetName) {
            try {
                Locale locale = CompetitionApplication.getCurrentLocale();
                String localeSuffix = "";
                if ("en".equals(locale.getLanguage())) {
                    localeSuffix = "-en";
                }
                liftOrderBoard = (new ResultFrame(initFromFragment, viewName,
                        "jsp/warmupRoom" + localeSuffix + ".jsp?platformName=", newStylesheetName)); //$NON-NLS-1$
                if (newStylesheetName != null && !initFromFragment) {
                    liftOrderBoard.setStylesheetName(newStylesheetName);
                }
            } catch (MalformedURLException e) {
                throw new SystemError(e);
            }
            return liftOrderBoard;
        }
    }

    /**
     * Lazy builder for lift order board.
     */
    private class SummaryLiftOrderViewComponent implements CompetitionApplicationComponent {
        private ResultFrame summaryLifterView = null;

        @Override
        public ResultFrame getView(boolean initFromFragment, String viewName, String newStylesheetName) {
            try {
                Locale locale = CompetitionApplication.getCurrentLocale();
                String localeSuffix = "";
                if ("en".equals(locale.getLanguage())) {
                    localeSuffix = "-en";
                }
                summaryLifterView = (new ResultFrame(initFromFragment, viewName,
                        "jsp/liftingOrder" + localeSuffix + ".jsp?platformName=", newStylesheetName)); //$NON-NLS-1$
                if (newStylesheetName != null && !initFromFragment) {
                    summaryLifterView.setStylesheetName(newStylesheetName);
                }
            } catch (MalformedURLException e) {
                throw new SystemError(e);
            }
            return summaryLifterView;
        }
    }

    /**
     * Lazy builder for current lifter information
     */
    private class PublicAttemptBoardComponent implements CompetitionApplicationComponent {
        // private ResultFrame currentLifterPanel = null;

        @Override
        public AttemptBoardView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            return new AttemptBoardView(initFromFragment, viewName, true, stylesheetName);
        }
    }

    /**
     * Lazy builder for current lifter information
     */
    private class LifterAttemptBoardComponent implements CompetitionApplicationComponent {
        // private ResultFrame currentLifterPanel = null;

        @Override
        public AttemptBoardView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            return new AttemptBoardView(initFromFragment, viewName, false, stylesheetName);
        }
    }

    /**
     * Lazy builder for competition editor.
     */
    private class EmptyComponent implements CompetitionApplicationComponent {
        private EmptyView emptyView = null;

        @Override
        public EmptyView getView(boolean initFromFragment, String viewName, String stylesheetName) {
            emptyView = (new EmptyView());
            emptyView.setSizeFull();
            return emptyView;
        }
    }

    public ApplicationView getViewByName(String fragment, boolean initFromFragment) {
        return getViewByName(fragment, initFromFragment, null);
    }

    public ApplicationView getViewByName(String fragment, boolean initFromFragment, String stylesheet) {
        if (fragment == null || fragment.trim().isEmpty()) {
            return new EmptyView();
        }

        int where = fragment.indexOf("/");
        String viewName = fragment;
        if (where != -1) {
            viewName = fragment.substring(0, where);
        }
        final CompetitionApplicationComponent component = urlFragmentToView.get(viewName);
        if (component != null) {
            final ApplicationView applicationView = component.getView(initFromFragment, viewName, stylesheet);
            return applicationView;
        } else {
            throw new RuntimeException(Messages.getString(
                    "CompetitionApplicationComponents.ViewNotFound", CompetitionApplication.getCurrentLocale()) + viewName); //$NON-NLS-1$
        }
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Platform getPlatform() {
        return platform;
    }

    /**
     * @param platformName
     *            the platformName to set
     */
    public void setPlatformByName(String platformName) {
        logger.debug("enter +++++"); //$NON-NLS-1$
        if (platformName == null) {
            setPlatform(null);
            return;
        }
        final Platform platform1 = Platform.getByName(platformName);
        if (platform1 != null) {
            final CompetitionApplication app = CompetitionApplication.getCurrent();
            SessionData masterData = app.getMasterData(platformName);
            final CompetitionSession currentGroup = masterData.getCurrentSession();
            logger.debug("new current group {}", currentGroup); //$NON-NLS-1$
            setPlatform(platform1);
            if (currentView instanceof EditingView) {
                ((EditingView) currentView).setSessionData(masterData);
            }
            app.setCurrentCompetitionSession(currentGroup);
        } else {
            logger.error(
                    Messages.getString("CompetitionApplicationComponents.PlatformNotFound", CompetitionApplication.getCurrentLocale()), platformName); //$NON-NLS-1$
        }
        logger.debug("finish +++++"); //$NON-NLS-1$
    }

    /**
     * @return the platformName
     */
    public String getPlatformName() {
        if (platform != null) {
            logger.debug("{}", platform.getName()); //$NON-NLS-1$
            return platform.getName();
        } else {
            logger.debug("getPlatformName platform=null"); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * @return
     * @throws RuleViolationException
     */
    public static String initPlatformName() throws RuleViolationException {
        final CompetitionApplication app = CompetitionApplication.getCurrent();
        String platformName = app.getPlatformName();
        if (platformName == null) {
            List<Platform> platforms = Platform.getAll();
            if (platforms.size() == 1) {
                app.components.setPlatform(platforms.get(0));
                platformName = app.getPlatformName();
            } else {
                throw new RuleViolationException(CompetitionApplication.getCurrent().getLocale(), "AnnouncerView.selectPlatformFirst"); //$NON-NLS-1$
            }
        }
        return platformName;
    }

    /**
     * @return
     * @throws RuleViolationException
     */
    public static String firstPlatformName() throws RuleViolationException {
        final CompetitionApplication app = CompetitionApplication.getCurrent();
        String platformName = app.getPlatformName();
        if (platformName == null) {
            List<Platform> platforms = Platform.getAll();
            if (platforms.size() >= 1) {
                app.components.setPlatform(platforms.get(0));
                platformName = app.getPlatformName();
            } else {
                throw new RuleViolationException(
                        Messages.getString(
                                "CompetitionApplicationComponents.MustDefineAtLeastOnePlatform",
                                CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$
            }
        }
        return platformName;
    }

    public Panel getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(Panel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public void setCurrentView(ApplicationView c) {
        this.currentView = c;
    }

    public ApplicationView getCurrentView() {
        return this.currentView;
    }

}
