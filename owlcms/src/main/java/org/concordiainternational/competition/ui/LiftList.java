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

import java.util.List;
import java.util.Locale;

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.AnnouncerView.Mode;
import org.concordiainternational.competition.ui.components.GroupSelect;
import org.concordiainternational.competition.ui.generators.CommonColumnGenerator;
import org.concordiainternational.competition.ui.generators.LiftCellStyleGenerator;
import org.concordiainternational.competition.ui.list.GenericBeanList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;

/**
 * This class displays the lifting order for lifters for the announcer,
 * timekeeper and marshal consoles.
 * 
 * @author jflamy
 * 
 */
public class LiftList extends GenericBeanList<Lifter> implements 
		Property.ValueChangeListener, // change in table value = change in selected row
        EditableList {

    static final Logger logger = LoggerFactory.getLogger(LiftList.class);
    private static final long serialVersionUID = 148461976217706535L;
    private EditingView parentView;
    transient private GroupData data = null; // do not serialize

    private static String[] NATURAL_COL_ORDER = null;
    private static String[] COL_HEADERS = null;

    private Mode mode;

    public LiftList(GroupData groupData, EditingView parentView, AnnouncerView.Mode mode) {
        super(CompetitionApplication.getCurrent(), Lifter.class, buildCaption(mode, groupData)); //$NON-NLS-1$
        logger.trace("new."); //$NON-NLS-1$
        this.parentView = parentView;
        this.mode = mode;
        data = groupData;
        init();
    }

    /**
     * @param mode
     * @return
     */
    private static String buildCaption(AnnouncerView.Mode mode, GroupData groupData) {
        final String role = Messages.getString(
            "LiftList." + mode.toString(), CompetitionApplication.getCurrent().getLocale()); //$NON-NLS-1$
        final String currentPlatformName = " " + CompetitionApplication.getCurrent().getPlatformName(); //$NON-NLS-1$
        return role + currentPlatformName;
    }

    /**
     * Clear the current selection from the table. This is done by the lift card
     * editor once it has loaded the right lifter.
     */
    public void clearSelection() {
        table.select(null); // remove selection from table.
    }

    @SuppressWarnings("unchecked")
    public Lifter getFirstLifter() {
        BeanItem<Lifter> item = (BeanItem<Lifter>) table.getItem(table.firstItemId());
        if (item != null) return (Lifter) item.getBean();
        return null;
    }

    public Item getFirstLifterItem() {
        return table.getItem(table.firstItemId());
    }

    @Override
    public GroupData getGroupData() {
        return data;
    }

    @Override
    public void refresh() {
        logger.debug("start refresh liftList**************{}", mode); //$NON-NLS-1$
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
        logger.debug("end refresh liftList**************{}", mode); //$NON-NLS-1$
    }

    @Override
    public void setGroupData(GroupData data) {
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
                // clearSelection();
                parentView.setStickyEditor(true); // since the user selected
                                                  // explicitly, ignore changes
                                                  // by others.
            }
        }
    }

    @Override
    protected void addGeneratedColumns() {
        // the following columns will be read-only.
        final CommonColumnGenerator columnGenerator = new CommonColumnGenerator(app);
        table.addGeneratedColumn("snatch1ActualLift", columnGenerator); //$NON-NLS-1$
        table.addGeneratedColumn("snatch2ActualLift", columnGenerator); //$NON-NLS-1$
        table.addGeneratedColumn("snatch3ActualLift", columnGenerator); //$NON-NLS-1$
        table.addGeneratedColumn("cleanJerk1ActualLift", columnGenerator); //$NON-NLS-1$
        table.addGeneratedColumn("cleanJerk2ActualLift", columnGenerator); //$NON-NLS-1$
        table.addGeneratedColumn("cleanJerk3ActualLift", columnGenerator); //$NON-NLS-1$
        table.addGeneratedColumn("category", columnGenerator); //$NON-NLS-1$
        table.addGeneratedColumn("total", columnGenerator); //$NON-NLS-1$
        
        setExpandRatios();
    }

    @Override
    protected void createToolbarButtons(HorizontalLayout tableToolbar) {
        // we do not call super() because the default buttons are inappropriate.
        if (mode == AnnouncerView.Mode.ANNOUNCER) {
            GroupSelect groupSelect = new GroupSelect((CompetitionApplication) app, app.getLocale());
            tableToolbar.addComponent(groupSelect);

            final Button refreshButton = new Button(Messages.getString("ResultList.Refresh", app.getLocale())); //$NON-NLS-1$
            final Button.ClickListener refreshClickListener = new Button.ClickListener() { //$NON-NLS-1$
                private static final long serialVersionUID = 7744958942977063130L;

                public void buttonClick(ClickEvent event) {
                    logger.debug("reloading"); //$NON-NLS-1$
                    data.setCurrentGroup(data.getCurrentCompetitionSession());
                }
            };
            refreshButton.addListener(refreshClickListener);
            tableToolbar.addComponent(refreshButton);
        }
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
                Messages.getString("Lifter.category", locale), //$NON-NLS-1$
                Messages.getString("Lifter.bodyWeight", locale), //$NON-NLS-1$
                Messages.getString("Lifter.club", locale), //$NON-NLS-1$
                Messages.getString("Lifter.nextRequestedWeight", locale), //$NON-NLS-1$
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
                (Competition.isMasters() ? "mastersLongCategory" //$NON-NLS-1$
                        : "category"), //$NON-NLS-1$
                "bodyWeight", //$NON-NLS-1$
                "club", //$NON-NLS-1$
                "nextAttemptRequestedWeight", //$NON-NLS-1$
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

        logger.debug("loadData for {}, size={}", mode, data.lifters.size()); //$NON-NLS-1$
        logger.debug("data={}", data); //$NON-NLS-1$
        List<Lifter> lifters = data.getAttemptOrder();
        if (lifters != null && !lifters.isEmpty()) {
            final BeanItemContainer<Lifter> cont = new BeanItemContainer<Lifter>(lifters);
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

    void sortTableInLiftingOrder() {
        table.sort(new String[] { "liftOrderRank" }, new boolean[] { true }); //$NON-NLS-1$
    }

    /**
     * Sorts the lifters in the correct order in response to a change in the
     * data. Informs listeners that the order has been updated.
     */
    void updateTable() {
        // update our own user interface
        this.sortTableInLiftingOrder(); // this does not change the selected
                                        // row.
        final Item firstLifterItem = getFirstLifterItem();
        table.select(firstLifterItem); // so we change it.
        this.clearSelection();
    }

}
