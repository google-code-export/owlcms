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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
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
    public static final String ATTEMPT_BOARD_VIEW = "attemptBoard"; //$NON-NLS-1$
    public static final String LIFT_ORDER_VIEW = "liftOrderBoard"; //$NON-NLS-1$
    public static final String PLATFORM_LIST = "platformList"; //$NON-NLS-1$
    public static final String REGISTRATION_LIST = "registrationList"; //$NON-NLS-1$
    public static final String RESULT_BOARD = "resultBoard"; //$NON-NLS-1$
    public static final String OREFEREE_CONSOLE = "oRefereeConsole"; //$NON-NLS-1$
    public static final String MREFEREE_CONSOLE = "refereeConsole"; //$NON-NLS-1$
    public static final String OJURY_CONSOLE = "oJuryConsole"; //$NON-NLS-1$
    public static final String MJURY_CONSOLE = "juryConsole"; //$NON-NLS-1$
    public static final String MPLATES_INFO = "platesInfo"; //$NON-NLS-1$
    public static final String SIMPLE_RESULT_BOARD = "simpleResultBoard"; //$NON-NLS-1$
    public static final String RESULT_VIEW = "resultView"; //$NON-NLS-1$
    public static final String TIMEKEEPER_VIEW = "timekeeperView"; //$NON-NLS-1$
    public static final String UPLOAD_VIEW = "uploadView"; //$NON-NLS-1$
    public static final String WEIGH_IN_LIST = "weighInList"; //$NON-NLS-1$
    public static final String REFEREE_TESTING = "decisionLights"; //$NON-NLS-1$
    public static final String JURY_LIGHTS = "juryLights"; //$NON-NLS-1$
    public static final String COUNTDOWN_DISPLAY = "countdownDisplay"; //$NON-NLS-1$
    public static final String SPREADSHEET_UPLOADER = "spreadsheetUpload";
    public static final String HOME = ""; // empty fragment //$NON-NLS-1$


    public Panel mainPanel;
    public Menu menu;
    public Window mainWindow;
    public ApplicationView currentView;

    private Map<String, CompetitionApplicationComponent> urlFragmentToView = new HashMap<String, CompetitionApplicationComponent>();
    private Platform platform;
    private Logger logger = LoggerFactory.getLogger(CompetitionApplicationComponents.class);

    public CompetitionApplicationComponents(Panel mainPanel, Menu menu, String platformName) {
        this.mainPanel = mainPanel;
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
        urlFragmentToView.put(ATTEMPT_BOARD_VIEW, new AttemptBoardComponent());
        urlFragmentToView.put(LIFT_ORDER_VIEW, new LifterBoardComponent());
        urlFragmentToView.put(PLATFORM_LIST, new PlatformListComponent());
        urlFragmentToView.put(REGISTRATION_LIST, new RegistrationListComponent());
        urlFragmentToView.put(RESULT_BOARD, new ResultBoardComponent());
        urlFragmentToView.put(SIMPLE_RESULT_BOARD, new SimpleResultBoardComponent());
        urlFragmentToView.put(RESULT_VIEW, new ResultViewComponent());
        urlFragmentToView.put(OREFEREE_CONSOLE, new RefereeConsoleComponent(false));
        urlFragmentToView.put(MREFEREE_CONSOLE, new RefereeConsoleComponent(true));
        urlFragmentToView.put(OJURY_CONSOLE, new JuryConsoleComponent(false));
        urlFragmentToView.put(MJURY_CONSOLE, new JuryConsoleComponent(true));
        urlFragmentToView.put(MPLATES_INFO, new PlatesInfoComponent());
        urlFragmentToView.put(TIMEKEEPER_VIEW, new TimekeeperViewComponent());
        urlFragmentToView.put(UPLOAD_VIEW, new SpreadsheetUploaderComponent());
        urlFragmentToView.put(WEIGH_IN_LIST, new WeighInListComponent());
    }

    /**
     * Lazy factory for application components.
     * 
     */
    private interface CompetitionApplicationComponent {
        ApplicationView get(boolean initFromFragment, String viewName) ;
    }

    /**
     * Lazy builder for competition editor.
     */
    private class CompetitionEditorComponent implements CompetitionApplicationComponent {
        private CompetitionEditor competitionEditor = null;

        @Override
		public CompetitionEditor get(boolean initFromFragment, String viewName) {
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
		public PlatformList get(boolean initFromFragment, String viewName) {
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
		public CategoryList get(boolean initFromFragment, String viewName) {
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
		public SessionList get(boolean initFromFragment, String viewName) {
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
		public WeighInList get(boolean initFromFragment, String viewName) {
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
		public ApplicationView get(boolean initFromFragment, String viewName) {
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
		public ApplicationView get(boolean initFromFragment, String viewName) {
            this.decisionLights = (new RefereeTesting(initFromFragment, viewName, false, false));
            return decisionLights;
        }
    }
    
    /**
     * Lazy builder for Jury Lights
     */
    private class JuryLightsComponent implements CompetitionApplicationComponent {
        private RefereeDecisions juryLights = null;

        @Override
		public ApplicationView get(boolean initFromFragment, String viewName) {
            this.juryLights = (new RefereeDecisions(initFromFragment, viewName, false, true));
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
		public ApplicationView get(boolean initFromFragment, String viewName) {
			if (mobile) {
				this.refereeConsole = (new MRefereeConsole(initFromFragment, viewName));
			} else {
				this.refereeConsole = (new ORefereeConsole(initFromFragment, viewName));
			}
            return (ApplicationView)refereeConsole;
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
		public ApplicationView get(boolean initFromFragment, String viewName) {
			if (mobile) {
				this.juryConsole = (new MJuryConsole(initFromFragment, viewName));
			} else {
				this.juryConsole = (new OJuryConsole(initFromFragment, viewName));
			}
            return (ApplicationView)juryConsole;
        }
    }
    
    /**
     * Lazy builder for Referee buttons
     */
    private class PlatesInfoComponent implements CompetitionApplicationComponent {
        private MPlatesInfoView platesInfoConsole = null;
        
        @Override
		public ApplicationView get(boolean initFromFragment, String viewName) {
			this.platesInfoConsole = (new MPlatesInfoView(initFromFragment, viewName));
            return (ApplicationView)platesInfoConsole;
        }
    }

    /**
     * Lazy builder for registration list.
     */
    private class RegistrationListComponent implements CompetitionApplicationComponent {
        private WeighInList weighInList = null;

        @Override
		public WeighInList get(boolean initFromFragment, String viewName) {
            this.weighInList = (new WeighInList(initFromFragment, viewName,true));
            return weighInList;
        }
    }

    /**
     * Lazy builder for spreadsheet upload page.
     */
    private class SpreadsheetUploaderComponent implements CompetitionApplicationComponent {
        private SpreadsheetUploader spreadsheetUploader = null;

        @Override
		public SpreadsheetUploader get(boolean initFromFragment, String viewName) {
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
		public AnnouncerView get(boolean initFromFragment, String viewName) {
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
		public AnnouncerView get(boolean initFromFragment, String viewName) {
            changesView = (new AnnouncerView(initFromFragment, viewName,Mode.MARSHALL));
            changesView.adjustSplitBarLocation();
            return changesView;
        }
    }

    /**
     * Lazy builder for time keeper view.
     */
    private class TimekeeperViewComponent implements CompetitionApplicationComponent {
        private AnnouncerView timekeeperView = null;

        @Override
		public AnnouncerView get(boolean initFromFragment, String viewName) {
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
		public ResultView get(boolean initFromFragment, String viewName) {
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
		public ResultFrame get(boolean initFromFragment, String viewName) {
            try {
                resultBoard = (new ResultFrame(initFromFragment, viewName,"jsp/resultBoard.jsp?platformName=")); //$NON-NLS-1$
            } catch (MalformedURLException e) {
                throw new SystemError(e);
            }
            return resultBoard;
        }
    }

    /**
     * Lazy builder for main result board.
     */
    private class SimpleResultBoardComponent implements CompetitionApplicationComponent {
        private ResultFrame resultBoard = null;

        @Override
		public ResultFrame get(boolean initFromFragment, String viewName) {
            try {
                resultBoard = (new ResultFrame(initFromFragment, viewName,"jsp/simpleResultBoard.jsp?platformName=")); //$NON-NLS-1$
            } catch (MalformedURLException e) {
                throw new SystemError(e);
            }
            return resultBoard;
        }
    }

    /**
     * Lazy builder for lift order board.
     */
    private class LifterBoardComponent implements CompetitionApplicationComponent {
        private ResultFrame liftOrderBoard = null;

        @Override
		public ResultFrame get(boolean initFromFragment, String viewName) {
            try {
                liftOrderBoard = (new ResultFrame(initFromFragment, viewName,"jsp/warmupRoom.jsp?platformName=")); //$NON-NLS-1$
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
		public ResultFrame get(boolean initFromFragment, String viewName) {
            try {
                summaryLifterView = (new ResultFrame(initFromFragment, viewName, "jsp/liftingOrder.jsp?platformName=")); //$NON-NLS-1$
            } catch (MalformedURLException e) {
                throw new SystemError(e);
            }
            return summaryLifterView;
        }
    }

    /**
     * Lazy builder for current lifter information
     */
    private class AttemptBoardComponent implements CompetitionApplicationComponent {
        // private ResultFrame currentLifterPanel = null;

        @Override
		public AttemptBoardView get(boolean initFromFragment, String viewName) {
            return new AttemptBoardView(initFromFragment, viewName);
        }
    }
    
    /**
     * Lazy builder for competition editor.
     */
    private class EmptyComponent implements CompetitionApplicationComponent {
        private EmptyView emptyView = null;

        @Override
		public EmptyView get(boolean initFromFragment, String viewName) {
            emptyView = (new EmptyView());
            return emptyView;
        }
    }

    public ApplicationView getViewByName(String fragment, boolean initFromFragment)  {
        int where = fragment.indexOf("/");
        String viewName = fragment;
        if (where != -1) {
            viewName = fragment.substring(0,where);
        }
        final CompetitionApplicationComponent component = urlFragmentToView.get(viewName);
        if (component != null) {
            final ApplicationView applicationView = component.get(initFromFragment,viewName);
//            LoggerUtils.logException(logger, new Exception("fragment "+fragment));
//            logger.warn("getViewByName returning {}",applicationView);
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
            logger.error(Messages.getString(
                "CompetitionApplicationComponents.PlatformNotFound", CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$
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
                throw new RuleViolationException("AnnouncerView.selectPlatformFirst"); //$NON-NLS-1$
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
                        Messages
                                .getString(
                                    "CompetitionApplicationComponents.MustDefineAtLeastOnePlatform", CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$
            }
        }
        return platformName;
    }
    

}