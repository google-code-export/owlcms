/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.CompetitionSessionLookup;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.spreadsheet.JXLSJurySheet;
import org.concordiainternational.competition.spreadsheet.JXLSLifterCard;
import org.concordiainternational.competition.spreadsheet.JXLSStartingList;
import org.concordiainternational.competition.spreadsheet.JXLSWeighInSheet;
import org.concordiainternational.competition.spreadsheet.JXLSWorkbookStreamSource;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.components.SessionSelect;
import org.concordiainternational.competition.ui.generators.CommonColumnGenerator;
import org.concordiainternational.competition.ui.generators.LiftCellStyleGenerator;
import org.concordiainternational.competition.ui.list.LifterHbnList;
import org.concordiainternational.competition.utils.ItemAdapter;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.vaadin.terminal.SystemError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Window.Notification;

public class WeighInList extends LifterHbnList implements ApplicationView, Bookmarkable {

    private static final long serialVersionUID = -6455130090728823622L;
    private boolean registration = false;
    private String viewName;
    private SessionSelect sessionSelect;

    public WeighInList(boolean initFromFragment, String viewName, boolean registration) {
        super(CompetitionApplication.getCurrent(), Messages.getString("WeighInList.title", CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());

        this.registration = registration;
        init();
        if (registration) {
            this.setTableCaption(Messages.getString("WeighInList.adminTitle", app.getLocale())); //$NON-NLS-1$
            Component newToolbar = this.createTableToolbar();
            this.setTableToolbar(newToolbar);
        } else {
            table.removeGeneratedColumn("actions"); //$NON-NLS-1$
        }
        CompetitionApplication.getCurrent().getUriFragmentUtility().setFragment(getFragment(), false);

        registerAsListener();
    }

    @Override
    protected void addGeneratedColumns() {
        super.addGeneratedColumns();
        table.addGeneratedColumn("category", new CommonColumnGenerator(app)); //$NON-NLS-1$
        table.addGeneratedColumn("registrationCategory", new CommonColumnGenerator(app)); //$NON-NLS-1$
        table.addGeneratedColumn("competitionSession", new CommonColumnGenerator(app)); //$NON-NLS-1$
        table.addGeneratedColumn("teamMember", new CommonColumnGenerator(app)); //$NON-NLS-1$
        table.addGeneratedColumn("qualifyingTotal", new CommonColumnGenerator(app)); //$NON-NLS-1$
        table.setColumnCollapsingAllowed(true);
        String localeString = System.getProperty("owlcms.locale");
        
        // use full date except in Quebec
        if ("fr_CA".equals(localeString)) {
            table.setColumnCollapsed("birthDate", false);
            table.setColumnCollapsed("fullBirthDate", true);
        } else {
            table.setColumnCollapsed("birthDate", true);
            table.setColumnCollapsed("fullBirthDate", false);
        }

        setExpandRatios();
    }

    /**
     * Load container content to Table
     */
    @Override
    protected void loadData() {
        // load all lifters
        final LifterContainer cont = new LifterContainer((CompetitionApplication) app, false);
        // cont.sort(new String[]{"registrationCategory","lotNumber"}, new
        // boolean[]{true,true});
        table.setContainerDataSource(cont);
    }

    @Override
    public void clearCache() {
        ((LifterContainer) table.getContainerDataSource()).clearCache();
    }

    @Override
    protected void createToolbarButtons(HorizontalLayout tableToolbar1) {

        sessionSelect = new SessionSelect((CompetitionApplication) app, app.getLocale(), this);
        tableToolbar1.addComponent(sessionSelect);
        super.createToolbarButtons(tableToolbar1);

        final Locale locale = app.getLocale();

        if (this.registration) {
            addRowButton = new Button(Messages.getString("Common.addRow", app.getLocale()), this, "newItem"); //$NON-NLS-1$ //$NON-NLS-2$
            tableToolbar1.addComponent(addRowButton);

            // draw lot numbers
            final Button drawLotsButton = drawLotsButton(locale);
            tableToolbar1.addComponent(drawLotsButton);

            // produce start list for technical meeting, includes all lifters.
            // false = show unweighed lifters
            final Button startListButton = startListButton(locale, false);
            tableToolbar1.addComponent(startListButton);

            // produce start list for technical meeting, includes all lifters.
            // false = show unweighed lifters
            final Button lifterCardsButton = lifterCardsButton(locale, false);
            tableToolbar1.addComponent(lifterCardsButton);

            // clear all lifters.
            final Button clearAllButton = clearAllButton(locale);
            tableToolbar1.addComponent(clearAllButton);

            // reset all lifters.
            final Button resetAllButton = resetAllButton(locale);
            tableToolbar1.addComponent(resetAllButton);

        } else {
            // weigh-in screen

            // clear all start numbers.
            final Button clearStartButton = clearStartButton(locale);
            tableToolbar1.addComponent(clearStartButton);

            // assign start numbers.
            final Button assignStartNumbersButton = assignStartNumberButton(locale);
            tableToolbar1.addComponent(assignStartNumbersButton);

            // produce list of lifters that were actually weighed-in.
            // true = exclude unweighed lifters
            final Button weighInListButton = weighInListButton(locale, true);
            tableToolbar1.addComponent(weighInListButton);

            // produce list of lifters that were actually weighed-in.
            // true = exclude unweighed lifters
            final Button juryListButton = juryListButton(locale, true);
            tableToolbar1.addComponent(juryListButton);

            final Button editSessionButton = editSessionButton(locale);
            tableToolbar1.addComponent(editSessionButton);
        }

        final Button refreshButton = refreshButton(locale);
        tableToolbar1.addComponent(refreshButton);

    }

    private Button editSessionButton(final Locale locale) {

        final Button editButton = new Button(Messages.getString("ResultList.edit", locale)); //$NON-NLS-1$
        final Button.ClickListener editClickListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = 7744958942977063130L;

            @Override
            public void buttonClick(ClickEvent event) {
                if (!currentSessionSelected())
                    return;
                editCompetitionSession(sessionSelect.getSelectedId(), sessionSelect.getSelectedItem());
            }
        };
        editButton.addListener(editClickListener);
        return editButton;
    }

    private Button clearStartButton(final Locale locale) {
        final Button clearStartButton = new Button(Messages.getString("WeighInList.clearStartNumbers", locale)); //$NON-NLS-1$
        final Button.ClickListener clearStartListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -8473648982746209221L;

            @Override
            public void buttonClick(ClickEvent event) {
                // if (!currentSessionSelected()) return;
                clearStartButton.setComponentError(null);
                clearStartNumbers();
            }
        };
        clearStartButton.addListener(clearStartListener);
        return clearStartButton;
    }

    private Button assignStartNumberButton(final Locale locale) {
        final Button assignStartNumbersButton = new Button(Messages.getString("WeighInList.assignStartNumbers", locale)); //$NON-NLS-1$
        final Button.ClickListener assignStartNumbersListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -8473648982746209221L;

            @Override
            public void buttonClick(ClickEvent event) {
                if (!currentSessionSelected())
                    return;
                assignStartNumbersButton.setComponentError(null);
                assignStartNumbersLifters();
            }
        };
        assignStartNumbersButton.addListener(assignStartNumbersListener);
        return assignStartNumbersButton;
    }

    /**
     * @param locale
     * @return
     */
    private Button weighInListButton(final Locale locale, final boolean excludeNotWeighed) {
        final Button weighInListButton = new Button(Messages.getString("WeighInList.StartingWeightSheet", locale)); //$NON-NLS-1$
        final Button.ClickListener listener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -8473648982746209221L;

            @Override
            public void buttonClick(ClickEvent event) {
                if (!currentSessionSelected())
                    return;
                weighInListButton.setComponentError(null);
                final JXLSWorkbookStreamSource streamSource = new JXLSWeighInSheet(excludeNotWeighed);
                // final OutputSheetStreamSource<WeighInSheet> streamSource = new OutputSheetStreamSource<WeighInSheet>(
                // WeighInSheet.class, (CompetitionApplication) app, excludeNotWeighed);
                if (streamSource.size() == 0) {
                    weighInListButton.setComponentError(new SystemError(Messages.getString("WeighInList.NoLifters", locale))); //$NON-NLS-1$
                    throw new RuntimeException(Messages.getString("WeighInList.NoLifters", locale)); //$NON-NLS-1$
                }

                String now = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date()); //$NON-NLS-1$
                ((UserActions) app).openSpreadsheet(streamSource, Messages.getString("WeighInList.WeighInPrefix", locale) + now); //$NON-NLS-1$
            }
        };
        weighInListButton.addListener(listener);
        return weighInListButton;
    }

    private Button drawLotsButton(final Locale locale) {
        final Button drawLotsButton = new Button(Messages.getString("WeighInList.drawLots", locale)); //$NON-NLS-1$
        final Button.ClickListener drawLotsListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -8473648982746209221L;

            @Override
            public void buttonClick(ClickEvent event) {
                drawLotsButton.setComponentError(null);
                drawLots();
            }
        };
        drawLotsButton.addListener(drawLotsListener);
        return drawLotsButton;
    }

    private Button clearAllButton(final Locale locale) {
        final Button clearAllButton = new Button(Messages.getString("WeighInList.deleteLifters", locale)); //$NON-NLS-1$
        final Button.ClickListener clearAllListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -8473648982746209221L;

            @Override
            public void buttonClick(ClickEvent event) {
                clearAllButton.setComponentError(null);
                clearAllLifters();
            }
        };
        clearAllButton.addListener(clearAllListener);
        return clearAllButton;
    }

    private Button resetAllButton(final Locale locale) {
        final Button resetAllButton = new Button(Messages.getString("WeighInList.resetLifters", locale)); //$NON-NLS-1$
        final Button.ClickListener resetAllListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -8473648982746209221L;

            @Override
            public void buttonClick(ClickEvent event) {
                resetAllButton.setComponentError(null);
                resetAllLifters();
                fullReload();
            }
        };
        resetAllButton.addListener(resetAllListener);
        return resetAllButton;
    }

    /**
     * @param locale
     * @return
     */
    private Button lifterCardsButton(final Locale locale, final boolean excludeNotWeighed) {
        final Button lifterCardsButton = new Button(Messages.getString("WeighInList.LifterCardsButton", locale)); //$NON-NLS-1$
        final Button.ClickListener listener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -8473648982746209221L;

            @Override
            public void buttonClick(ClickEvent event) {
                lifterCardsButton.setComponentError(null);
                final JXLSWorkbookStreamSource streamSource = new JXLSLifterCard();
                // final OutputSheetStreamSource<LifterCardSheet> streamSource = new OutputSheetStreamSource<LifterCardSheet>(
                // LifterCardSheet.class, (CompetitionApplication) app, excludeNotWeighed);
                if (streamSource.size() == 0) {
                    lifterCardsButton.setComponentError(new SystemError(Messages.getString("WeighInList.NoLifters", locale))); //$NON-NLS-1$
                    throw new RuntimeException(Messages.getString("WeighInList.NoLifters", locale)); //$NON-NLS-1$
                }

                String now = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date()); //$NON-NLS-1$
                ((UserActions) app).openSpreadsheet(streamSource, Messages.getString("WeighInList.CardsPrefix", locale) + now); //$NON-NLS-1$
            }
        };
        lifterCardsButton.addListener(listener);
        return lifterCardsButton;
    }

    /**
     * @param locale
     * @return
     */
    private Button juryListButton(final Locale locale, final boolean excludeNotWeighed) {
        final Button juryListButton = new Button(Messages.getString("JuryList.JurySheet", locale)); //$NON-NLS-1$
        final Button.ClickListener listener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -8473648982746209221L;

            @Override
            public void buttonClick(ClickEvent event) {
                if (!currentSessionSelected())
                    return;

                juryListButton.setComponentError(null);
                final JXLSWorkbookStreamSource streamSource = new JXLSJurySheet(excludeNotWeighed);
                // final OutputSheetStreamSource<JurySheet> streamSource = new OutputSheetStreamSource<JurySheet>(
                // JurySheet.class, (CompetitionApplication) app, excludeNotWeighed);
                if (streamSource.size() == 0) {
                    juryListButton.setComponentError(new SystemError(Messages
                            .getString("WeighInList.NoLifters", locale))); //$NON-NLS-1$
                    throw new RuntimeException(Messages.getString("WeighInList.NoLifters", locale)); //$NON-NLS-1$
                }

                String now = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date()); //$NON-NLS-1$
                ((UserActions) app).openSpreadsheet(streamSource, Messages.getString("WeighInList.JuryPrefix", locale) + now); //$NON-NLS-1$
            }
        };
        juryListButton.addListener(listener);
        return juryListButton;
    }

    /**
     * @param locale
     * @return
     */
    private Button startListButton(final Locale locale, final boolean excludeNotWeighed) {
        final Button startListButton = new Button(Messages.getString("WeighInList.StartSheet", locale)); //$NON-NLS-1$
        final Button.ClickListener listener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = -8473648982746209221L;

            @Override
            public void buttonClick(ClickEvent event) {
                startListButton.setComponentError(null);
                final JXLSWorkbookStreamSource streamSource = new JXLSStartingList();
                // final OutputSheetStreamSource<StartList> streamSource = new OutputSheetStreamSource<StartList>(
                // StartList.class, (CompetitionApplication) app, false);
                if (streamSource.size() == 0) {
                    startListButton.setComponentError(new SystemError(Messages.getString("WeighInList.NoLifters", locale))); //$NON-NLS-1$
                    throw new RuntimeException(Messages.getString("WeighInList.NoLifters", locale)); //$NON-NLS-1$
                }

                String now = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date()); //$NON-NLS-1$
                ((UserActions) app).openSpreadsheet(streamSource, Messages.getString("WeighInList.StartListPrefix", locale) + now); //$NON-NLS-1$
            }
        };
        startListButton.addListener(listener);
        return startListButton;
    }

    private Button refreshButton(final Locale locale) {
        final Button refreshButton = new Button(Messages.getString("ResultList.Refresh", locale)); //$NON-NLS-1$
        final Button.ClickListener refreshClickListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = 7744958942977063130L;

            @Override
            public void buttonClick(ClickEvent event) {
                fullReload();
            }
        };
        refreshButton.addListener(refreshClickListener);
        return refreshButton;
    }

    protected void assignStartNumbersLifters() {
        final List<Lifter> list = currentSessionLifters();
        if (list != null) {
            List<Lifter> lifters = LifterSorter.startNumberOrderCopy(list);
            LifterSorter.assignStartNumbers(lifters);
        }
        this.refresh();
    }

    protected List<Lifter> currentSessionLifters() {
        if (currentSessionSelected()) {
            return currentSessionliftersIfSelectedAllIfNot();
        } else {
            return null;
        }
    }

    protected boolean currentSessionSelected() {
        CompetitionSession currentCompetitionSession = CompetitionApplication.getCurrent().getCurrentCompetitionSession();
        if (currentCompetitionSession != null) {
            return true;
        } else {
            CompetitionApplication.getCurrent().getMainWindow().showNotification(
                    Messages.getString("WeighInList.sessionRequired", CompetitionApplication.getCurrentLocale()),
                    Notification.TYPE_ERROR_MESSAGE);
            return false;
        }
    }

    protected void clearStartNumbers() {
        final List<Lifter> list = currentSessionliftersIfSelectedAllIfNot();
        if (list == null)
            return;

        for (Lifter curLifter : list) {
            curLifter.setStartNumber(0);
        }
        this.refresh();
    }

    public List<Lifter> currentSessionliftersIfSelectedAllIfNot() {
        return allLifters(true);
    }

    /**
     * Draw lot numbers for all lifters.
     */
    protected void drawLots() {
        final List<Lifter> list = allLifters(false);
        LifterSorter.drawLots(list);
        this.refresh();
    }

    /**
     * Delete lifters from current group (all lifters if no current group)
     */
    protected void clearAllLifters() {
        final Session session = CompetitionApplication.getCurrent().getHbnSession();
        final List<Lifter> list = currentSessionliftersIfSelectedAllIfNot();
        for (Lifter curLifter : list) {
            session.delete(curLifter);
        }
        this.refresh();
    }

    /**
     * Delete lifters from current group (all lifters if no current group)
     */
    protected void resetAllLifters() {
        final List<Lifter> list = currentSessionliftersIfSelectedAllIfNot();
        for (Lifter curLifter : list) {

            // curLifter.setCleanJerk1Declaration("");
            curLifter.setCleanJerk1AutomaticProgression("");
            curLifter.setCleanJerk1Change1("");
            curLifter.setCleanJerk1Change2("");
            curLifter.setCleanJerk1ActualLift("");

            curLifter.setCleanJerk2Declaration("");
            curLifter.setCleanJerk2AutomaticProgression("");
            curLifter.setCleanJerk2Change1("");
            curLifter.setCleanJerk2Change2("");
            curLifter.setCleanJerk2ActualLift("");

            curLifter.setCleanJerk3Declaration("");
            curLifter.setCleanJerk3AutomaticProgression("");
            curLifter.setCleanJerk3Change1("");
            curLifter.setCleanJerk3Change2("");
            curLifter.setCleanJerk3ActualLift("");

            // curLifter.setSnatch1Declaration("");
            curLifter.setSnatch1AutomaticProgression("");
            curLifter.setSnatch1Change1("");
            curLifter.setSnatch1Change2("");
            curLifter.setSnatch1ActualLift("");

            curLifter.setSnatch2Declaration("");
            curLifter.setSnatch2AutomaticProgression("");
            curLifter.setSnatch2Change1("");
            curLifter.setSnatch2Change2("");
            curLifter.setSnatch2ActualLift("");

            curLifter.setSnatch3Declaration("");
            curLifter.setSnatch3AutomaticProgression("");
            curLifter.setSnatch3Change1("");
            curLifter.setSnatch3Change2("");
            curLifter.setSnatch3ActualLift("");

        }
        this.refresh();
    }

    @SuppressWarnings("unchecked")
    protected List<Lifter> allLifters(boolean restrictToCurrentGroup) {
        final CompetitionApplication compApp = (CompetitionApplication) app;
        final Session session = compApp.getHbnSession();
        Criteria crit = session.createCriteria(Lifter.class);
        final CompetitionSession currentGroup = compApp.getCurrentCompetitionSession();
        final String name = (currentGroup != null ? (String) currentGroup.getName() : null);
        if (restrictToCurrentGroup && currentGroup != null) {
            crit.createCriteria("competitionSession").add( //$NON-NLS-1$
                    Restrictions.eq("name", name)); //$NON-NLS-1$
        }
        return (List<Lifter>) crit.list();
    }

    private String[] NATURAL_COL_ORDER = null;
    private String[] COL_HEADERS = null;
    private String platformName;
    private String groupName;

    /**
     * @return Natural property order for Category bean. Used in tables and forms.
     */
    @Override
    protected String[] getColOrder() {
        if (NATURAL_COL_ORDER != null)
            return NATURAL_COL_ORDER;
        if (registration) {
            NATURAL_COL_ORDER = new String[] { "membership", //$NON-NLS-1$
                    "lotNumber", //$NON-NLS-1$
                    "lastName", //$NON-NLS-1$
                    "firstName", //$NON-NLS-1$
                    "gender", //$NON-NLS-1$
                    "birthDate", //$NON-NLS-1$
                    "fullBirthDate", //$NON-NLS-1$
                    "club", //$NON-NLS-1$
                    "registrationCategory", //$NON-NLS-1$
                    "competitionSession", //$NON-NLS-1$
                    "qualifyingTotal", //$NON-NLS-1$
                    "teamMember", //$NON-NLS-1$
                    "actions" // in table mode, actions come last. //$NON-NLS-1$
            };
        } else {
            NATURAL_COL_ORDER = new String[] { "membership", //$NON-NLS-1$
                    "startNumber", //$NON-NLS-1$
                    "lotNumber", //$NON-NLS-1$
                    "lastName", //$NON-NLS-1$
                    "firstName", //$NON-NLS-1$
                    "gender", //$NON-NLS-1$
                    "birthDate", //$NON-NLS-1$
                    "fullBirthDate", //$NON-NLS-1$
                    "club", //$NON-NLS-1$
                    "registrationCategory", //$NON-NLS-1$
                    "category", //$NON-NLS-1$
                    "bodyWeight", //$NON-NLS-1$
                    "snatch1Declaration", //$NON-NLS-1$
                    "cleanJerk1Declaration", //$NON-NLS-1$
                    "competitionSession", //$NON-NLS-1$
                    "actions" // in table mode, actions come last. //$NON-NLS-1$
            };
        }
        return NATURAL_COL_ORDER;
    }

    /**
     * @return Localized captions for properties in same order as in {@link #getColOrder()}
     */
    @Override
    protected String[] getColHeaders() {
        final Locale locale = app.getLocale();
        if (COL_HEADERS != null)
            return COL_HEADERS;
        if (registration) {
            COL_HEADERS = new String[] { Messages.getString("Lifter.membership", locale), //$NON-NLS-1$	
                    Messages.getString("Lifter.lotNumber", locale), //$NON-NLS-1$	
                    Messages.getString("Lifter.lastName", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.firstName", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.gender", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.birthDate", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.fullBirthDate", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.club", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.registrationCategory", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.group", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.qualifyingTotal", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.teamMember", locale), //$NON-NLS-1$
                    Messages.getString("Common.actions", locale), //$NON-NLS-1$
            };
        } else {
            COL_HEADERS = new String[] { Messages.getString("Lifter.membership", locale), //$NON-NLS-1$	
                    Messages.getString("Lifter.startNumber", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.lotNumber", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.lastName", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.firstName", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.gender", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.birthDate", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.fullBirthDate", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.club", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.registrationCategory", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.category", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.bodyWeight", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.snatch1Declaration", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.cleanJerk1Declaration", locale), //$NON-NLS-1$
                    Messages.getString("Lifter.group", locale), //$NON-NLS-1$
                    Messages.getString("Common.actions", locale), //$NON-NLS-1$
            };
        }
        return COL_HEADERS;
    }

    @Override
    protected void populateAndConfigureTable() {
        super.populateAndConfigureTable();
        table.setColumnExpandRatio("lastName", 100F);
        table.setColumnExpandRatio("firstName", 100F);
        if (table.size() > 0) {
            // set styling;
            table.setCellStyleGenerator(new LiftCellStyleGenerator(table));
        }
    }

    /**
     * Make sure that if the table is editable its actions are visible.
     */
    @Override
    protected void setDefaultActions() {
        if (table != null) {
            if (!table.isEditable()) {
                table.removeGeneratedColumn("actions"); //$NON-NLS-1$
                table.setSizeFull();
            } else {
                if (registration)
                    this.addDefaultActions();
            }
        }
    }

    /*
     * Add a new item with correct default values.
     * 
     * @see org.concordiainternational.competition.ui.list.GenericList#newItem()
     */
    @Override
    public Object newItem() {
        Object itemId = super.newItem();
        final CompetitionSession currentGroup = CompetitionApplication.getCurrent().getCurrentCompetitionSession();
        if (currentGroup != null) {
            Lifter lifter = (Lifter) ItemAdapter.getObject(table, itemId);
            lifter.setCompetitionSession(currentGroup);
            lifter.setBodyWeight(0.0D);
            lifter.setSnatch1Declaration(""); //$NON-NLS-1$
            lifter.setCleanJerk1Declaration(""); //$NON-NLS-1$
            lifter.setLotNumber(99);
        }
        refresh();
        table.setEditable(true);
        table.setValue(itemId);
        setButtonVisibility();
        return itemId;
    }

    @Override
    public void refresh() {
        super.refresh();
        setButtonVisibility();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.components.ApplicationView#needsMenu()
     */
    @Override
    public boolean needsMenu() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.Bookmarkable#getFragment()
     */
    @Override
    public String getFragment() {
        CompetitionSession session = CompetitionApplication.getCurrent().getCurrentCompetitionSession();
        String sessionName = null;
        if (session != null) {
            sessionName = session.getName();
        }
        return viewName + "/" + (platformName == null ? "" : platformName) + "/" + (sessionName == null ? "" : sessionName);
    }

    /*
     * (non-Javadoc)
     * 
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
            final CompetitionApplication cApp = (CompetitionApplication) app;
            cApp.setCurrentCompetitionSession(new CompetitionSessionLookup(cApp).lookup(groupName));
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
        app.getMainWindow().removeListener((CloseListener) this);
    }

    @Override
    public void windowClose(CloseEvent e) {
        unregisterAsListener();
    }

    // @Override
    // public DownloadStream handleURI(URL context, String relativeUri) {
    // registerAsListener();
    // return null;
    // }

    public void fullReload() {
        CategoryLookup.getSharedInstance().reload();

        WeighInList.this.removeComponent(table);
        populateAndConfigureTable();
        WeighInList.this.addComponent(table);
        positionTable();
        setButtonVisibility();
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
