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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.lifterSort.WinningOrderComparator;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.publicAddress.PublicAddressForm;
import org.concordiainternational.competition.spreadsheet.CompetitionBook;
import org.concordiainternational.competition.spreadsheet.MastersGroupResults;
import org.concordiainternational.competition.spreadsheet.OutputSheetStreamSource;
import org.concordiainternational.competition.spreadsheet.ResultSheet;
import org.concordiainternational.competition.ui.components.SessionSelect;
import org.concordiainternational.competition.ui.generators.CommonColumnGenerator;
import org.concordiainternational.competition.ui.generators.LiftCellStyleGenerator;
import org.concordiainternational.competition.ui.list.GenericBeanList;
import org.concordiainternational.competition.utils.ItemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.SystemError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * This class displays the winning order for lifters.
 * 
 * @author jflamy
 * 
 */

public class ResultList extends GenericBeanList<Lifter> implements Property.ValueChangeListener, 
        EditableList {
    private static final Logger logger = LoggerFactory.getLogger(ResultList.class);
    private static final long serialVersionUID = -6455130090728823622L;
    private Application app = CompetitionApplication.getCurrent();
    private EditingView parentView;
    transient private SessionData data = null; // do not serialize
	private SessionSelect sessionSelect;

    private static String[] NATURAL_COL_ORDER = null;
    private static String[] COL_HEADERS = null;

    public ResultList(SessionData groupData, EditingView parentView) {
        super(CompetitionApplication.getCurrent(), Lifter.class, Messages.getString(
            "ResultList.title", CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$
        this.parentView = parentView;
        this.data = groupData;

        init();
    }

    /**
     * Clear the current selection from the table. This is done by the lift card
     * editor once it has loaded the right lifter.
     */
    @Override
	public void clearSelection() {
        table.select(null); // hide selection from table.
    }

    @Override
	@SuppressWarnings("unchecked")
    public Lifter getFirstLifter() {
        BeanItem<Lifter> item = (BeanItem<Lifter>) table.getItem(table.firstItemId());
        if (item != null) return (Lifter) item.getBean();
        return null;
    }

    @Override
	public Item getFirstLifterItem() {
        return table.getItem(table.firstItemId());
    }

    @Override
    public SessionData getGroupData() {
        return data;
    }

    @Override
    public void refresh() {
        logger.debug("start refresh ResultList**************{}"); //$NON-NLS-1$`
        sessionSelect.refresh();
        Table oldTable = table;

        // listeners to oldTable should listen no more (these listeners are
        // those
        // that need to know about users selecting a row).
        oldTable.removeListener(Class.class, oldTable);

        // populate the new table and connect it to us.
        populateAndConfigureTable();

        this.replaceComponent(oldTable, table);
        positionTable();
        setButtonVisibility();
        logger.debug("end refresh ResultList **************{}"); //$NON-NLS-1$
    }

    @Override
	public void setGroupData(SessionData data) {
        this.data = data;
    }

    /*
     * Value change, for a table, indicates that the currently selected row has
     * changed. This method is only called when the user explicitly clicks on a
     * lifter.
     * 
     * @see
     * com.vaadin.data.Property.ValueChangeListener#valueChange(com.vaadin.data
     * .Property.ValueChangeEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void valueChange(ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == table) {
            Item item = table.getItem(table.getValue());
            if (item == null) return;
            if (parentView != null) {
                Lifter lifter = (Lifter) ((BeanItem<Lifter>) item).getBean();

                // on explicit selection by user, ignore the "sticky" and
                // override, but don't reload the lifter info
                parentView.setStickyEditor(false, false);
                parentView.editLifter(lifter, item); // only bottom part
                                                     // changes.
            }
        }
    }

    @Override
    protected void addGeneratedColumns() {
        super.addGeneratedColumns();
        // the following columns will be read-only.
        final CommonColumnGenerator columnGenerator = new CommonColumnGenerator(app);
        table.addGeneratedColumn("totalRank", columnGenerator); //$NON-NLS-1$
        if (WinningOrderComparator.useRegistrationCategory) {
            table.addGeneratedColumn("registrationCategory", columnGenerator); //$NON-NLS-1$
        } else {
            table.addGeneratedColumn("category", columnGenerator); //$NON-NLS-1$
        }
        table.addGeneratedColumn("total", columnGenerator); //$NON-NLS-1$
        
        setExpandRatios();
    }

    @Override
    protected void createToolbarButtons(HorizontalLayout tableToolbar1) {
        // we do not call super because the default buttons are inappropriate.
        final Locale locale = app.getLocale();
        sessionSelect = new SessionSelect((CompetitionApplication) app, locale);
        tableToolbar1.addComponent(sessionSelect);

        {
            final Button resultSpreadsheetButton = new Button(Messages.getString("ResultList.ResultSheet", locale)); //$NON-NLS-1$
            final Button.ClickListener listener = new Button.ClickListener() { //$NON-NLS-1$
                private static final long serialVersionUID = -8473648982746209221L;

                @Override
				public void buttonClick(ClickEvent event) {
                    resultSpreadsheetButton.setComponentError(null);

                    if (!Competition.isMasters()) {
                        regularCompetition(locale);
                    } else {
                        mastersCompetition(locale);
                    }
                }

                /**
                 * @param locale1
                 * @throws RuntimeException
                 */
                private void regularCompetition(final Locale locale1) throws RuntimeException {
                    final OutputSheetStreamSource<ResultSheet> streamSource = new OutputSheetStreamSource<ResultSheet>(
                            ResultSheet.class, (CompetitionApplication) app, true);
                    if (streamSource.size() == 0) {
                        setComponentError(new SystemError(Messages.getString("ResultList.NoResults", locale1))); //$NON-NLS-1$
                        throw new RuntimeException(Messages.getString("ResultList.NoResults", locale1)); //$NON-NLS-1$
                    }

                    String now = new SimpleDateFormat("yyyy-MM-dd_HHmmss") //$NON-NLS-1$
                            .format(new Date());
                    ((UserActions) app).openSpreadsheet(streamSource, "results_" + now); //$NON-NLS-1$
                }

                /**
                 * @param locale1
                 * @throws RuntimeException
                 */
                private void mastersCompetition(final Locale locale1) throws RuntimeException {
                    final OutputSheetStreamSource<MastersGroupResults> streamSource = new OutputSheetStreamSource<MastersGroupResults>(
                            MastersGroupResults.class, (CompetitionApplication) app, true);
                    if (streamSource.size() == 0) {
                        setComponentError(new SystemError(Messages.getString("ResultList.NoResults", locale1))); //$NON-NLS-1$
                        throw new RuntimeException(Messages.getString("ResultList.NoResults", locale1)); //$NON-NLS-1$
                    }

                    String now = new SimpleDateFormat("yyyy-MM-dd_HHmmss") //$NON-NLS-1$
                            .format(new Date());
                    ((UserActions) app).openSpreadsheet(streamSource, "results_" + now); //$NON-NLS-1$
                }
            };
            resultSpreadsheetButton.addListener(listener);
            tableToolbar1.addComponent(resultSpreadsheetButton);       
        }

        {
            final Button teamResultSpreadsheetButton = new Button(Messages.getString(
                "ResultList.TeamResultSheet", locale)); //$NON-NLS-1$
            final Button.ClickListener teamResultClickListener = new Button.ClickListener() { //$NON-NLS-1$
                private static final long serialVersionUID = -8473648982746209221L;

                @Override
				public void buttonClick(ClickEvent event) {
                    teamResultSpreadsheetButton.setComponentError(null);
                    final OutputSheetStreamSource<CompetitionBook> streamSource = new OutputSheetStreamSource<CompetitionBook>(
                            CompetitionBook.class, (CompetitionApplication) app, true);
                    if (streamSource.size() == 0) {
                        setComponentError(new SystemError(Messages.getString("ResultList.NoResults", locale))); //$NON-NLS-1$
                        throw new RuntimeException(Messages.getString("ResultList.NoResults", locale)); //$NON-NLS-1$
                    }

                    String now = new SimpleDateFormat("yyyy-MM-dd_HHmmss") //$NON-NLS-1$
                            .format(new Date());
                    ((UserActions) app).openSpreadsheet(streamSource, "teamResults_" + now); //$NON-NLS-1$
                }
            };
            teamResultSpreadsheetButton.addListener(teamResultClickListener);
            tableToolbar1.addComponent(teamResultSpreadsheetButton);
        }

        final Button refreshButton = new Button(Messages.getString("ResultList.Refresh", locale)); //$NON-NLS-1$
        final Button.ClickListener refreshClickListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = 7744958942977063130L;

            @Override
			public void buttonClick(ClickEvent event) {
                logger.debug("reloading"); //$NON-NLS-1$
                data.setCurrentSession(data.getCurrentSession());
            }
        };
        refreshButton.addListener(refreshClickListener);
        tableToolbar1.addComponent(refreshButton);
        
        final Button editButton = new Button(Messages.getString("ResultList.edit", locale)); //$NON-NLS-1$
        final Button.ClickListener editClickListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = 7744958942977063130L;

            @Override
			public void buttonClick(ClickEvent event) {
            	editCompetitionSession(sessionSelect.getSelectedId(),sessionSelect.getSelectedItem());
            }
        };
        editButton.addListener(editClickListener);
        tableToolbar1.addComponent(editButton);
        
        final Button publicAddressButton = new Button(Messages.getString("LiftList.publicAddress", app.getLocale())); //$NON-NLS-1$
        final Button.ClickListener publicAddressClickListener = new Button.ClickListener() { //$NON-NLS-1$
            private static final long serialVersionUID = 7744958942977063130L;

            @Override
			public void buttonClick(ClickEvent event) {
            	CompetitionApplication current = CompetitionApplication.getCurrent();
            	SessionData masterData = current.getMasterData(current.getPlatformName());
				PublicAddressForm.editPublicAddress(ResultList.this, masterData);
            }
        };
        publicAddressButton.addListener(publicAddressClickListener);
        tableToolbar1.addComponent(publicAddressButton);
    }

    /**
     * @return Localized captions for properties in same order as in
     *         {@link #getColOrder()}
     */
    @Override
    protected String[] getColHeaders() {
        Locale locale = app.getLocale();
        if (COL_HEADERS != null) return COL_HEADERS;
        COL_HEADERS = new String[] { Messages.getString("Lifter.lotNumber", locale), //$NON-NLS-1$
                Messages.getString("Lifter.lastName", locale), //$NON-NLS-1$
                Messages.getString("Lifter.firstName", locale), //$NON-NLS-1$
                //Messages.getString("Lifter.gender", locale), //$NON-NLS-1$
                Messages.getString("Lifter.birthDate", locale), //$NON-NLS-1$
                Messages.getString("Lifter.totalRank", locale), //$NON-NLS-1$
                Messages.getString("Lifter.category", locale), //$NON-NLS-1$
                Messages.getString("Lifter.bodyWeight", locale), //$NON-NLS-1$
                Messages.getString("Lifter.club", locale), //$NON-NLS-1$
                Messages.getString("Lifter.snatch1", locale), //$NON-NLS-1$
                Messages.getString("Lifter.snatch2", locale), //$NON-NLS-1$
                Messages.getString("Lifter.snatch3", locale), //$NON-NLS-1$
                Messages.getString("Lifter.cleanJerk1", locale), //$NON-NLS-1$
                Messages.getString("Lifter.cleanJerk2", locale), //$NON-NLS-1$
                Messages.getString("Lifter.cleanJerk3", locale), //$NON-NLS-1$
                Messages.getString("Lifter.total", locale), //$NON-NLS-1$
        };
        return COL_HEADERS;
    }

    /**
     * @return Natural property order for Lifter bean. Used in tables and forms.
     */
    @Override
    protected String[] getColOrder() {
        if (NATURAL_COL_ORDER != null) return NATURAL_COL_ORDER;
        NATURAL_COL_ORDER = new String[] { "lotNumber", //$NON-NLS-1$
                "lastName", //$NON-NLS-1$
                "firstName", //$NON-NLS-1$
                //"gender", //$NON-NLS-1$
                "birthDate", //$NON-NLS-1$
                "totalRank", //$NON-NLS-1$
                (WinningOrderComparator.useRegistrationCategory ? "registrationCategory" //$NON-NLS-1$
                        : (Competition.isMasters() ? "mastersLongCategory" //$NON-NLS-1$
                                : "category")), //$NON-NLS-1$		
                "bodyWeight", //$NON-NLS-1$
                "club", //$NON-NLS-1$
                "snatch1ActualLift", //$NON-NLS-1$
                "snatch2ActualLift", //$NON-NLS-1$
                "snatch3ActualLift", //$NON-NLS-1$
                "cleanJerk1ActualLift", //$NON-NLS-1$
                "cleanJerk2ActualLift", //$NON-NLS-1$
                "cleanJerk3ActualLift", //$NON-NLS-1$
                "total", //$NON-NLS-1$
        };
        return NATURAL_COL_ORDER;
    }

    @Override
    protected void init() {
        super.init();
        table.setSizeFull();
        table.setNullSelectionAllowed(true);
        table.setNullSelectionItemId(null);
    }

    /**
     * Load container content to Table. We create a wrapper around the
     * HbnContainer so we can sort on transient properties and suchlike.
     */
    @Override
    protected void loadData() {
        List<Lifter> lifters = data.getResultOrder();
        if (lifters != null && !lifters.isEmpty()) {
            final BeanItemContainer<Lifter> cont = new BeanItemContainer<Lifter>(Lifter.class,lifters);
            table.setContainerDataSource(cont);
        }
    }

    /**
     * complete setup for table (after buildView has done its initial setup)
     */
    @Override
    protected void populateAndConfigureTable() {
        super.populateAndConfigureTable(); // this creates a new table and calls
                                           // loadData (below)
        if (table.size() > 0) {
            table.setEditable(false);
            table.addListener(this); // listen to selection events
            // set styling;
            table.setCellStyleGenerator(new LiftCellStyleGenerator(table));
            table.setCacheRate(0.1D);
        }

        this.updateTable();
        clearSelection();
    }

    @Override
    protected void setButtonVisibility() {
        // nothing needed here, as this list contains no buttons
    }

    void sortTableInResultOrder() {
        table.sort(new String[] { "resultOrderRank" }, new boolean[] { true }); //$NON-NLS-1$
    }

    /**
     * Sorts the lifters in the correct order in response to a change in the
     * data. Informs listeners that the order has been updated.
     */
    void updateTable() {
        // update our own user interface
        this.sortTableInResultOrder(); // this does not change the selected row.
        // final Item firstLifterItem = getFirstLifterItem();
        // table.select(firstLifterItem); // so we change it.
        // this.clearSelection();
    }
    
    private void editCompetitionSession(Object itemId, Item item) {
    	if (itemId == null) {
    		CompetitionApplication.getCurrent().getMainWindow().showNotification(
    			Messages.getString("ResultList.sessionNotSelected", CompetitionApplication.getCurrentLocale()),
    			Notification.TYPE_ERROR_MESSAGE);
    		return;
    	}
        SessionForm form = new SessionForm();
        
        form.setItemDataSource(item);
        form.setReadOnly(false);

        CompetitionSession competitionSession = (CompetitionSession) ItemAdapter.getObject(item);
        logger.warn("retrieved session {} {}",System.identityHashCode(competitionSession), competitionSession.getReferee3());
		Window editingWindow = new Window(competitionSession.getName());
        form.setWindow(editingWindow);
        form.setParentList(this);
        editingWindow.getContent().addComponent(form);
        app.getMainWindow().addWindow(editingWindow);
        editingWindow.setWidth("40em");
        editingWindow.center();
    }

}
