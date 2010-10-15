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

package org.concordiainternational.competition.ui.components;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.CompetitionApplicationComponents;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.LoadWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class Menu extends MenuBar implements Serializable {
    private static final long serialVersionUID = -3809346951739483448L;
    protected static final Logger logger = LoggerFactory.getLogger(Menu.class);
    private LoadWindow loadComputerWindow;

    public Menu() {
        final CompetitionApplication competitionApplication = CompetitionApplication.getCurrent();
        final Locale locale = competitionApplication.getLocale();

        MenuBar menu = this;
        menu.setWidth("100%"); //$NON-NLS-1$

        MenuItem console = createConsoleMenu(menu, competitionApplication, locale);
        createAnnouncerMenuItem(console, locale);
        createChangesMenuItem(console, locale);
        createTimeKeeperMenuItem(console, locale);

        MenuItem projectors = createProjectorsMenuItem(menu, competitionApplication, locale);
        createDisplayMenuItem(projectors, competitionApplication, locale);
        createSimpleDisplayMenuItem(projectors, competitionApplication, locale);
        projectors.addSeparator();
        createLiftOrderMenuItem(projectors, competitionApplication, locale);
        createSummaryLiftOrderMenuItem(projectors, competitionApplication, locale);
        projectors.addSeparator();
        createAttemptBoardMenuItem(projectors, competitionApplication, locale);
        projectors.addSeparator();
        createCountdownDisplayMenuItem(projectors, competitionApplication, locale);
        

        createLoadComputerMenuItem(menu, competitionApplication, locale);

        MenuItem decisions = createDecisionMenuItem(menu, competitionApplication, locale);
        createDecisionLightsMenuItem(decisions, competitionApplication, locale);
        createJuryLightsMenuItem(decisions, competitionApplication, locale);
        decisions.addSeparator();
        createRefereeMenuItem(decisions, competitionApplication, locale, 0);
        createRefereeMenuItem(decisions, competitionApplication, locale, 1);
        createRefereeMenuItem(decisions, competitionApplication, locale, 2);

        createResultsMenuItem(menu, competitionApplication, locale);

        createWeighInsMenuItem(menu, competitionApplication, locale);

        MenuItem administration = createAdminMenuItem(menu, competitionApplication, locale);
        createCompetitionMenuItem(administration, competitionApplication, locale);
        createPlatformsMenuItem(administration, competitionApplication, locale);
        createCategoriesMenuItem(administration, competitionApplication, locale);
        createGroupsMenuItem(administration, competitionApplication, locale);
        administration.addSeparator();
        createUploadMenuItem(administration, competitionApplication, locale);
        createLiftersMenuItem(administration, competitionApplication, locale);
        administration.addSeparator();
        createRestartMenuItem(administration, competitionApplication, locale);

        if (Platform.getSize() > 1) {
            MenuItem platforms = createPlatformsMenuItem(menu, competitionApplication, locale);
            createPlatformSelectionMenuItems(platforms, competitionApplication, locale);
        }

    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createConsoleMenu(MenuBar menu, final CompetitionApplication competitionApplication, final Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.Console", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            null);
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createAnnouncerMenuItem(MenuItem menu, final Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.Announcer", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            new Command() {
                private static final long serialVersionUID = -547788870764317931L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    CompetitionApplication.getCurrent().doDisplay(CompetitionApplicationComponents.ANNOUNCER_VIEW);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createChangesMenuItem(MenuItem menu, final Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.Changes", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            new Command() {
                private static final long serialVersionUID = -547788870764317931L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    CompetitionApplication.getCurrent().doDisplay(CompetitionApplicationComponents.CHANGES_VIEW);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createTimeKeeperMenuItem(MenuItem menu, final Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.TimeKeeper", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            new Command() {
                private static final long serialVersionUID = -547788870764317931L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    CompetitionApplication.getCurrent().doDisplay(CompetitionApplicationComponents.TIMEKEEPER_VIEW);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createResultsMenuItem(MenuBar menu, final CompetitionApplication competitionApplication, final Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.Results", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            new Command() {
                private static final long serialVersionUID = 5577281157225515360L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.RESULT_VIEW);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createLoadComputerMenuItem(MenuBar menu, final CompetitionApplication competitionApplication,
            final Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.Load", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            new Command() {
                private static final long serialVersionUID = 5577281157225515360L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    if (getLoadComputerWindow() == null) {
                        displayLoadComputerWindow();
                    } else {
                        closeLoadComputerWindow();
                    }
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createWeighInsMenuItem(MenuBar menu, final CompetitionApplication competitionApplication, final Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.WeighIn", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/users.png"),
            new Command() {
                private static final long serialVersionUID = 3563330867710192233L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.WEIGH_IN_LIST);
                }
            });
    }

    private MenuItem createProjectorsMenuItem(MenuBar menu, CompetitionApplication competitionApplication, Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.Projectors", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            null);
    }

    /**
     * @param competitionApplication
     * @param locale
     * @return
     */
    private MenuItem createDisplayMenuItem(MenuItem projectors, final CompetitionApplication competitionApplication,
            final Locale locale) {
        return projectors.addItem(Messages.getString("CompetitionApplication.Display", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            new Command() {
                private static final long serialVersionUID = -4179990860181438187L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.RESULT_BOARD);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     * @return
     */
    private MenuItem createSimpleDisplayMenuItem(MenuItem projectors, final CompetitionApplication competitionApplication,
            final Locale locale) {
        return projectors.addItem(Messages.getString("CompetitionApplication.SimpleDisplay", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            new Command() {
                private static final long serialVersionUID = -4179990860181438187L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.SIMPLE_RESULT_BOARD);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createSummaryLiftOrderMenuItem(MenuItem projectors, final CompetitionApplication competitionApplication,
            final Locale locale) {
        return projectors.addItem(Messages.getString("CompetitionApplication.SummaryLiftOrder", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            new Command() {
                private static final long serialVersionUID = 5658882232799685230L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.SUMMARY_LIFT_ORDER_VIEW);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createAttemptBoardMenuItem(MenuItem projectors, final CompetitionApplication competitionApplication,
            final Locale locale) {
        return projectors.addItem(Messages.getString("CompetitionApplication.AttemptBoard", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            new Command() {
                private static final long serialVersionUID = 5658882232799685230L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.ATTEMPT_BOARD_VIEW);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createLiftOrderMenuItem(MenuItem projectors, final CompetitionApplication competitionApplication,
            final Locale locale) {
        return projectors.addItem(Messages.getString("CompetitionApplication.LiftOrder", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            new Command() {
                private static final long serialVersionUID = 5658882232799685230L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.LIFT_ORDER_VIEW);
                }
            });
    }

    private MenuItem createAdminMenuItem(MenuBar menu, CompetitionApplication competitionApplication, Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.Administration", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            null);
    }

    private MenuItem createPlatformsMenuItem(MenuBar menu, CompetitionApplication competitionApplication, Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.Platforms", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            null);
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createPlatformsMenuItem(MenuItem administration, final CompetitionApplication competitionApplication,
            final Locale locale) {
        return administration.addItem(Messages.getString("CompetitionApplication.Platforms", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            new Command() {
                private static final long serialVersionUID = -3184587992763328917L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.PLATFORM_LIST);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createCompetitionMenuItem(MenuItem administration, final CompetitionApplication competitionApplication,
            final Locale locale) {
        return administration.addItem(Messages.getString("CompetitionApplication.Competition", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            new Command() {
                private static final long serialVersionUID = -3184587992763328917L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.COMPETITION_EDITOR);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createCategoriesMenuItem(MenuItem administration, final CompetitionApplication competitionApplication,
            final Locale locale) {
        return administration.addItem(Messages.getString("CompetitionApplication.Categories", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            new Command() {
                private static final long serialVersionUID = -6471211259031643832L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.CATEGORY_LIST);
                }
            });
    }

    /**
     * @param competitionApplication
     * @param locale
     */
    private MenuItem createGroupsMenuItem(MenuItem administration, final CompetitionApplication competitionApplication,
            final Locale locale) {
        return administration.addItem(Messages.getString("CompetitionApplication.Groups", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/folder-add.png"),
            new Command() {
                private static final long serialVersionUID = -6740574252795556971L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.GROUP_LIST);
                }
            });
    }

    private MenuItem createLiftersMenuItem(MenuItem administration,
            final CompetitionApplication competitionApplication, Locale locale) {
        return administration.addItem(Messages.getString("CompetitionApplication.Lifters", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/users.png"),
            new Command() {
                private static final long serialVersionUID = 3563330867710192233L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.REGISTRATION_LIST);
                }
            });
    }

    private MenuItem createRestartMenuItem(MenuItem administration,
            final CompetitionApplication competitionApplication, Locale locale) {
        return administration.addItem(Messages.getString("Restart.Restart", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/users.png"),
            new Command() {
                private static final long serialVersionUID = 3563330867710192233L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    displayRestartConfirmation();
                }
            });
    }

    protected void restart() {
        ServletContext sCtx = CompetitionApplication.getCurrent().getServletContext();
        String configFilePath = sCtx.getRealPath("/WEB-INF/web.xml"); //$NON-NLS-1$
        File configFile = new File(configFilePath);
        logger.info("restarting by touching {}", configFile); //$NON-NLS-1$
        configFile.setLastModified(System.currentTimeMillis());
    }

    private MenuItem createUploadMenuItem(MenuItem administration, final CompetitionApplication competitionApplication,
            Locale locale) {
        return administration.addItem(Messages.getString("CompetitionApplication.Upload", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/users.png"),
            new Command() {
                private static final long serialVersionUID = 3563330867710192233L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.UPLOAD_VIEW);
                }
            });

    }

    private void createPlatformSelectionMenuItems(MenuItem platforms,
            final CompetitionApplication competitionApplication, Locale locale) {
        for (final Platform platform : Platform.getAll()) {
            final String name = (platform.getName() == null ? "?" : platform.getName()); //$NON-NLS-1$
            platforms.addItem(name, null, // new
                                          // ThemeResource("icons/32/users.png"),
                new Command() {
                    private static final long serialVersionUID = 3563330867710192233L;

                    @Override
                    public void menuSelected(MenuItem selectedItem) {
                        Menu.this.setComponentError(null); // erase error
                                                           // marker;
                        competitionApplication.setPlatformByName(name);
                        SessionData masterData = competitionApplication.getMasterData(name);
                        logger.debug("new platform={}, new group = {}", name, masterData.getCurrentSession()); //$NON-NLS-1$
                        competitionApplication.setCurrentCompetitionSession(masterData.getCurrentSession());
                    }
                });
        }
    }

    private MenuItem createDecisionMenuItem(MenuBar menu, final CompetitionApplication competitionApplication,
            Locale locale) {
        return menu.addItem(Messages.getString("CompetitionApplication.Refereeing", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            null);
    }

    private MenuItem createDecisionLightsMenuItem(MenuItem item, final CompetitionApplication competitionApplication,
            Locale locale) {
        return item.addItem(Messages.getString("CompetitionApplication.DecisionLights", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            new Command() {
                private static final long serialVersionUID = 5577281157225515360L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.DECISION_LIGHTS);
                }
            });
    }
    
    private MenuItem createJuryLightsMenuItem(MenuItem item, final CompetitionApplication competitionApplication,
            Locale locale) {
        return item.addItem(Messages.getString("CompetitionApplication.JuryLights", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            new Command() {
                private static final long serialVersionUID = 5577281157225515360L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.JURY_LIGHTS);
                }
            });
    }

    private MenuItem createCountdownDisplayMenuItem(MenuItem item, final CompetitionApplication competitionApplication,
            Locale locale) {
        return item.addItem(Messages.getString("CompetitionApplication.CountdownDisplay", locale), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            new Command() {
                private static final long serialVersionUID = 5577281157225515360L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.doDisplay(CompetitionApplicationComponents.COUNTDOWN_DISPLAY);
                }
            });
    }

    private MenuItem createRefereeMenuItem(MenuItem item, final CompetitionApplication competitionApplication,
            Locale locale, final int refereeIndex) {
        return item.addItem(Messages.getString("CompetitionApplication.Referee", locale) + " " + (refereeIndex + 1), //$NON-NLS-1$
            null, // new ThemeResource("icons/32/document.png"),
            new Command() {
                private static final long serialVersionUID = 5577281157225515360L;

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    competitionApplication.displayRefereeConsole(refereeIndex);
                }
            });
    }

    public void displayRestartConfirmation() {

        // Create the window...
        final Window subwindow = new Window(Messages.getString(
            "Restart.ConfirmationDialogTitle", getApplication().getLocale())); //$NON-NLS-1$
        // ...and make it modal
        subwindow.setModal(true);
        subwindow.setWidth("10cm"); //$NON-NLS-1$

        // Configure the windws layout; by default a VerticalLayout
        VerticalLayout layout = (VerticalLayout) subwindow.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        // Add some content; a label and a close-button
        final Label message = new Label(
                Messages.getString("Restart.Confirmation", getApplication().getLocale()), Label.CONTENT_XHTML); //$NON-NLS-1$
        subwindow.addComponent(message);

        final Button close = new Button(
                Messages.getString("Restart.Cancel", getApplication().getLocale()), new Button.ClickListener() { //$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    // inline click-listener
                    public void buttonClick(ClickEvent event) {
                        // close the window by removing it from the main window
                        getApplication().getMainWindow().removeWindow(subwindow);
                    }
                });

        final Button ok = new Button(Messages.getString("Restart.Restart", getApplication().getLocale()));
        ok.addListener(new Button.ClickListener() { //$NON-NLS-1$
                    private static final long serialVersionUID = 1L;

                    // inline click-listener
                    public void buttonClick(ClickEvent event) {
                        // close the window by removing it from the main window
                        restart();
                        message.setValue(Messages.getString("Restart.InProgress", getApplication().getLocale()));//$NON-NLS-1$
                        // getApplication().getMainWindow().removeWindow(subwindow);
                        // close.setVisible(false);
                        ok.setVisible(false);
                        close.setCaption(Messages.getString("Common.done", getApplication().getLocale()));

                    }
                });

        // The components added to the window are actually added to the window's
        // layout; you can use either. Alignments are set using the layout
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(ok);
        buttons.addComponent(close);
        buttons.setSpacing(true);
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);

        getApplication().getMainWindow().addWindow(subwindow);
    }

    public void displayLoadComputerWindow() {
        if (loadComputerWindow != null) {
            loadComputerWindow.setVisible(true);
        } else {
            loadComputerWindow = new LoadWindow(this);
            getApplication().getMainWindow().addWindow(loadComputerWindow);
        }
    }

    public void closeLoadComputerWindow() {
        getApplication().getMainWindow().removeWindow(getLoadComputerWindow());
        getLoadComputerWindow().close();
        setLoadComputerWindow(null);
    }

    /**
     * @param loadComputerWindow
     *            the loadComputerWindow to set
     */
    public void setLoadComputerWindow(LoadWindow loadComputerWindow) {
        this.loadComputerWindow = loadComputerWindow;
    }

    /**
     * @return the loadComputerWindow
     */
    public LoadWindow getLoadComputerWindow() {
        return loadComputerWindow;
    }
}
