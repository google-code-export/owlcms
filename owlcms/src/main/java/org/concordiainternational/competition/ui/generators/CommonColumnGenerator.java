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

package org.concordiainternational.competition.ui.generators;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryContainer;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.ItemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.data.hbnutil.HbnContainer.EntityItem;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

/**
 * @author jflamy
 * 
 */
public class CommonColumnGenerator implements Table.ColumnGenerator {
    private static final long serialVersionUID = 6573562545694966025L;
    private static final Logger logger = LoggerFactory.getLogger(CommonColumnGenerator.class);

    private CategoryContainer activeCategories;
    private HbnContainer<Platform> platforms;
    private HbnContainer<CompetitionSession> competitionSessions;
    private CompetitionApplication app;

    public CommonColumnGenerator(Application app) {
        activeCategories = new CategoryContainer((HbnSessionManager) app, true);
        platforms = new HbnContainer<Platform>(Platform.class, (HbnSessionManager) app);
        competitionSessions = new HbnContainer<CompetitionSession>(CompetitionSession.class, (HbnSessionManager) app);
        this.app = (CompetitionApplication) app;
    }

    @Override
	public Component generateCell(Table table, Object itemId, Object propertyId) {
        final Item item = table.getItem(itemId);
        final String propertyIdString = (String) propertyId;
        final Property uiProp = item.getItemProperty(propertyIdString);
        final Property prop = table.getContainerProperty(itemId, propertyIdString);
        // System.err.println("generating cell for : "+propertyIdString);
        if (propertyIdString.equals("categories")) { //$NON-NLS-1$ //$NON-NLS-2$
            return generateCategoriesCell(table, itemId, uiProp, prop);
        } else if (propertyIdString.equals("category")) { //$NON-NLS-1$ //$NON-NLS-2$
            return generateCategoryCell(table, itemId, item, propertyIdString);
        } else if (propertyIdString.equals("registrationCategory")) { //$NON-NLS-1$ //$NON-NLS-2$
            return generateRegistrationCategoryCell(table, itemId, uiProp, prop);
        } else if (propertyIdString.contains("ActualLift")) { //$NON-NLS-1$ //$NON-NLS-2$
            return generateLiftCell(table, itemId, uiProp, prop);
        } else if (propertyIdString.equals("total")) { //$NON-NLS-1$
            Lifter lifter = getLifter(item);
            return new Label(WeightFormatter.formatWeight((lifter.getTotal())));
        } 
//        else if (propertyIdString.contains("Time")) { //$NON-NLS-1$
//            return generateTimeCell(prop);
//        }
        else if (propertyIdString.equals("platform")) { //$NON-NLS-1$
            return generatePlatformCell(table, itemId, uiProp, prop);
        } else if (propertyIdString.equals("competitionSession")) { //$NON-NLS-1$
            return generateGroupCell(table, itemId, uiProp, prop);
        } else if (propertyIdString.equals("totalRank")) { //$NON-NLS-1$
            return generateRankCell(prop);
        } else if (propertyIdString.equals("teamMember")) { //$NON-NLS-1$
            return generateBooleanCell(table, itemId, uiProp, prop);
        } else if (propertyIdString.equals("qualifyingTotal")) { //$NON-NLS-1$
            return generateIntegerCell(table, itemId, uiProp, prop);
        } else {
            return generateDefaultCell(prop);
        }
    }

    /**
     * @param prop
     * @return
     */
    private Component generateBooleanCell(Table table, Object itemId, final Property uiProp, final Property sourceProp) {
        Field msField = new CheckBox();
        msField.setCaption("");
        msField.setWidth("2ex"); //$NON-NLS-1$
        if (sourceProp.getValue() == null) sourceProp.setValue(true);
        msField.setPropertyDataSource(sourceProp);
        msField.setReadOnly(!table.isEditable());
        return msField;
    }

    /**
     * @param table
     * @param itemId
     * @param uiProp
     * @param sourceProp
     * @return
     */
    private Component generateIntegerCell(Table table, Object itemId, final Property uiProp, final Property sourceProp) {
        Field msField = new TextField();
        msField.setCaption("");
        msField.setWidth("4ex"); //$NON-NLS-1$
        if (sourceProp.getValue() == null) sourceProp.setValue(true);
        msField.setPropertyDataSource(sourceProp);
        msField.setReadOnly(!table.isEditable());
        return msField;
    }

    /**
     * @param table
     * @param itemId
     * @param item
     * @param propertyIdString
     * @return
     */
    private Component generateCategoryCell(Table table, Object itemId, final Item item, final String propertyIdString) {
        Component c = null;
        if (item instanceof EntityItem) {
            c = dynamicCategoryLabel(table, item, itemId, propertyIdString);
        } else {
            c = categoryLabel(table, item, itemId, propertyIdString);
        }

        return c;
    }

    /**
     * @param table
     * @param itemId
     * @param uiProp
     * @param sourceProp
     * @return
     */
    private Component generateLiftCell(Table table, Object itemId, final Property uiProp, final Property sourceProp) {
        final String value = (String) sourceProp.getValue();
        if (value != null && value.trim().length() > 0) {
            int intValue = WeightFormatter.safeParseInt(value);
            final Label label = new Label((WeightFormatter.formatWeight(value)));
            if (!value.isEmpty()) {
                if (intValue > 0) {
                    label.addStyleName("success"); //$NON-NLS-1$
                } else {
                    label.addStyleName("fail"); //$NON-NLS-1$
                }
                return label;
            }
        }
        return new Label(""); //$NON-NLS-1$
    }

    /**
     * @param table
     * @param itemId
     * @param uiProp
     * @param prop
     * @return
     */
    private Component generatePlatformCell(Table table, Object itemId, final Property uiProp, final Property prop) {
        final Object value = uiProp.getValue();
        if (!table.isEditable()) {
            if (table.getContainerDataSource() instanceof HbnContainer<?>) {
                if (value == null) return new Label("-"); //$NON-NLS-1$
                HbnContainer<Platform>.EntityItem<Platform> platform = (platforms.getItem(value));
                Platform platformBean = (Platform) platform.getPojo();
                return new Label(platformBean.getName());
            } else {
                throw new UnsupportedOperationException(Messages.getString("CommonColumnGenerator.0", app.getLocale())); //$NON-NLS-1$
            }
        } else {
            // Field msField = getPlatformComboboxFor(itemId);
            // msField.setPropertyDataSource(prop);
            // return msField;
            return getPlatformPopUpFor(itemId, uiProp);
        }
    }

    /**
     * @param table
     * @param itemId
     * @param uiProp
     * @param prop
     * @return
     */
    private Component generateGroupCell(Table table, Object itemId, Property uiProp, Property prop) {
        final Object value = uiProp.getValue();
        if (!table.isEditable()) {
            if (table.getContainerDataSource() instanceof HbnContainer<?>) {
                if (value == null) return new Label("-"); //$NON-NLS-1$
                HbnContainer<CompetitionSession>.EntityItem<CompetitionSession> competitionSession = (competitionSessions.getItem(value));
                CompetitionSession groupBean = (CompetitionSession) competitionSession.getPojo();
                return new Label(groupBean.getName());
            } else {
                throw new UnsupportedOperationException(Messages.getString("CommonColumnGenerator.0", app.getLocale())); //$NON-NLS-1$
            }
        } else {
            // Field msField = getGroupComboboxFor(itemId);
            // msField.setPropertyDataSource(prop);
            // return msField;
            return getGroupPopUpFor(itemId, uiProp);
        }
    }

    /**
     * @param prop
     * @return
     */
    private Component generateDefaultCell(final Property prop) {
        final Object value = prop.getValue();
        if (value == null) {
            final Label nullItem = new Label(Messages.getString("Common.emptyList", app.getLocale())); //$NON-NLS-1$
            return nullItem;
        }
        return new Label(value.toString());
    }

    /**
     * No longer CommonFieldFactory handles this.
     * @param prop
     * @return
     */
    @SuppressWarnings("unused")
	private Component generateTimeCell(final Property prop) {
        DateField timeField = new DateField();
        timeField.setPropertyDataSource(prop);
        return adjustDateField(timeField);
    }

    /**
     * @param prop
     * @return
     */
    @SuppressWarnings("unchecked")
    private Component generateRankCell(final Property prop) {
        TextField rankField = new TextField();
        rankField.setWidth("3em"); //$NON-NLS-1$
        rankField.setReadOnly(true);
        rankField.setPropertyDataSource(new PropertyFormatter(prop) {
            private static final long serialVersionUID = 1L;

            @Override
            public String format(Object value) {
                if (value == null) return ""; //$NON-NLS-1$
                try {
                    int rank = Integer.valueOf(value.toString());
                    if (rank == 0) {
                        return ""; //$NON-NLS-1$
                    } else if (rank > 0) {
                        return Integer.toString(rank);
                    } else return Messages.getString(
                        "CommonColumnGenerator.InvitedAbbreviation", CompetitionApplication.getCurrentLocale()); //$NON-NLS-1$
                } catch (NumberFormatException e) {
                    return "?"; //$NON-NLS-1$
                }
            }

            @Override
            public Object parse(String formattedValue) throws Exception {
                if (formattedValue == null) return 0;
                if (formattedValue.trim().isEmpty()) {
                    return 0;
                } else {
                    Integer value = null;
                    try {
                        value = Integer.decode(formattedValue);
                    } catch (NumberFormatException e) {
                        value = -1;
                    }
                    return value;
                }
            }
        });
        return rankField;
    }

    /**
     * @param table
     * @param itemId
     * @param uiProp
     * @param prop
     * @return
     */
    private Component generateRegistrationCategoryCell(Table table, Object itemId, final Property uiProp,
            final Property prop) {
        if (!table.isEditable()) {
            final Object value = prop.getValue();
            return new Label(categoryItemDisplayString(value)); //$NON-NLS-1$
        } else {
            // Field msField = getCategoryComboboxFor(itemId);
            Component msField = getCategoryPopUpFor(itemId, uiProp);
            msField.setWidth("12ex"); //$NON-NLS-1$
            // msField.setPropertyDataSource(prop);
            return msField;
        }
    }

    /**
     * @param value
     * @param curCategory
     * @return
     */
    private String categoryItemDisplayString(final Object value) {
        Category curCategory = null;
        if (value != null) {
            if (value instanceof Category) {
                curCategory = (Category) value;
            } else {
                curCategory = (Category) ItemAdapter.getObject(activeCategories.getItem(value));
            }
        }
        return (curCategory != null ? curCategory.getName() : Messages
                .getString("Common.noneSelected", app.getLocale()));
    }

    /**
     * @param value
     * @param curCategory
     * @return
     */
    private String groupItemDisplayString(final Object value) {
        CompetitionSession curGroup = null;
        if (value != null) {
            if (value instanceof CompetitionSession) {
                curGroup = (CompetitionSession) value;
            } else {
                curGroup = (CompetitionSession) ItemAdapter.getObject(competitionSessions.getItem(value));
            }
        }
        return (curGroup != null ? curGroup.getName() : Messages.getString("Common.noneSelected", app.getLocale()));
    }

    /**
     * @param table
     * @param itemId
     * @param uiProp
     * @param prop
     * @return
     */
    @SuppressWarnings("unchecked")
    private Component generateCategoriesCell(Table table, Object itemId, final Property uiProp, final Property prop) {
        final Object value = uiProp.getValue();
        if (!table.isEditable()) {
            if (table.getContainerDataSource() instanceof HbnContainer) {
                return new Label(categorySetToString((Set<Long>) value));
            } else {
                throw new UnsupportedOperationException(Messages.getString("CommonColumnGenerator.1", app.getLocale())); //$NON-NLS-1$
            }
        } else {
            // Field msField = getCategoriesMultiSelectFor(itemId);
            // msField.setPropertyDataSource(prop);
            // return msField;
            return getCategoriesPopUpFor(itemId, uiProp);
        }
    }

    public Map<Object, ListSelect> groupIdToCategoryList = new HashMap<Object, ListSelect>();
    private boolean firstMultiSelect = true;


    /**
     * Selection list when multiple categories need to be selected (e.g. which
     * categories belong to the same group)
     * 
     * @param itemId
     * @return
     */
    public Field getCategoriesMultiSelectFor(Object itemId) {
        ListSelect list = null;
        if (itemId != null) {
            list = groupIdToCategoryList.get(itemId);
        }
        if (list == null) {
            list = new ListSelect();
            setupCategorySelection(list, activeCategories, firstMultiSelect);
            if (firstMultiSelect) {
                firstMultiSelect = false;
            }
            list.setMultiSelect(true);
            list.setRows(4);
            groupIdToCategoryList.put(itemId, list);
        }
        return list;
    }

    /**
     * Selection when only one category is relevant (e.g. when entering
     * registration category for a lifter)
     * 
     * @param itemId
     * @param prop
     * @return
     */
    @SuppressWarnings("serial")
    public Component getCategoriesPopUpFor(Object itemId, final Property prop) {
        ListSelect categoryListSelect = null;
        if (itemId == null) {
            categoryListSelect = lifterIdToCategorySelect.get(itemId);
        }
        if (categoryListSelect == null) {
            categoryListSelect = new ListSelect();
            setupCategorySelection(categoryListSelect, activeCategories, firstCategoryComboBox);
            if (firstCategoryComboBox) {
                firstCategoryComboBox = false;
            }
            //categoryListSelect.setRequiredError(Messages.getString("CommonFieldFactory.youMustListSelectACategory", app.getLocale())); //$NON-NLS-1$

            categoryListSelect.setMultiSelect(true);
            categoryListSelect.setNullSelectionAllowed(true);
            categoryListSelect.setRows(activeCategories.size() + nbRowsForNullValue(categoryListSelect));
            lifterIdToCategorySelect.put(itemId, categoryListSelect);
        }
        categoryListSelect.setPropertyDataSource(prop);
        categoryListSelect.setWriteThrough(true);
        categoryListSelect.setImmediate(true);
        // categoryListSelect.addListener(new ValueChangeListener() {
        // @Override
        // public void valueChange(ValueChangeEvent event) {
        // //System.err.println("getCategoriesPopUpFor selected: "+event.getProperty().toString());
        // if (event.getProperty() == null){prop.setValue(null);}
        // }
        // });
        final ListSelect selector = categoryListSelect;
        PopupView popup = new PopupView(new PopupView.Content() {

            @SuppressWarnings("unchecked")
            @Override
            public String getMinimizedValueAsHTML() {
                final Object value = prop.getValue();
                if (value instanceof Long) {
                    return categoryItemDisplayString((Long) value);
                } else {
                    return categorySetToString((Set<Long>) value);
                }

            }

            @Override
            public Component getPopupComponent() {
                return selector;
            }
        });
        popup.setHideOnMouseOut(true);
        return popup;
    }

    /**
     * Set-up shared behaviour for category combo boxes and multi-selects.
     * 
     * @param categorySelect
     * @param categories
     * @param first
     */
    private void setupCategorySelection(AbstractSelect categorySelect, CategoryContainer categories, boolean first) {
        if (first) {
            first = false;
        }
        categorySelect.setWriteThrough(true);
        categorySelect.setImmediate(true);
        categorySelect.setContainerDataSource(categories);
        categorySelect.setItemCaptionPropertyId("name"); //$NON-NLS-1$
        categorySelect.setNullSelectionAllowed(true);
        categorySelect.setNewItemsAllowed(false);
    }

    /**
     * @param table
     * @param item
     * @param propertyId
     * @param itemId
     * @return
     */
    private Component dynamicCategoryLabel(Table table, final Item item, Object itemId, Object propertyId) {
        final Lifter lifter = getLifter(item);
        // default label.
        final Category category = lifter.getCategory();
        final String unknown = Messages.getString("Common.unknown", app.getLocale()); //$NON-NLS-1$
        final Label categoryLabel = new Label(unknown);//$NON-NLS-1$
        if (category != null) {
            final String name = category.getName();
            categoryLabel.setValue(name);
            Category regCat = lifter.getRegistrationCategory();
            if (!(category.equals(regCat))) {
                categoryLabel.addStyleName("wrong"); //$NON-NLS-1$
            }
        }

        // because we don't want to understand the dependencies between the
        // various properties
        // of a lifter, we let the lifter tell us when it thinks that a
        // dependent property has
        // changed (in this case, the category depends on body weight and
        // gender).
        final Lifter.UpdateEventListener listener = new Lifter.UpdateEventListener() {
            private static final long serialVersionUID = 830792013255211060L;

            @Override
            public void updateEvent(Lifter.UpdateEvent updateEvent) {
                Lifter lifter = (Lifter) updateEvent.getSource();
                logger.debug("received event for "+lifter+" update of "+updateEvent.getPropertyIds());
                final Category category2 = lifter.getCategory();
                synchronized (app) {
                    categoryLabel.removeStyleName("wrong"); //$NON-NLS-1$
                    if (category2 == null) {
                        categoryLabel.setValue(unknown);
                        categoryLabel.addStyleName("wrong"); //$NON-NLS-1$
                    } else {
                        categoryLabel.setValue(category2.getName());
                        Category regCat = lifter.getRegistrationCategory();
                        if (!(category2.equals(regCat))) {
                            // System.err.println("setting flag on category for "+lifter.getLastName()
                            // +" "+System.identityHashCode(this)
                            // +" update of "+updateEvent.getPropertyIds());
                            categoryLabel.addStyleName("wrong"); //$NON-NLS-1$
                        }
                    }
                }
                app.push();
            }
            
        };
        if (categoryLabel.getData() == null) {
            lifter.addListener(listener);
            categoryLabel.setData(listener);
        }

        // done
        return categoryLabel;
    }

    /**
     * @param table
     * @param item
     * @param propertyId
     * @param itemId
     * @return
     */
    private Component categoryLabel(Table table, final Item item, Object itemId, Object propertyId) {
        final Lifter lifter = getLifter(item);
        // default label.
        final Category category = lifter.getCategory();
        final String unknown = Messages.getString("Common.unknown", app.getLocale()); //$NON-NLS-1$
        final Label categoryLabel = new Label(unknown);//$NON-NLS-1$
        // if (!table.isEditable()) {
        if (category != null) {
            final String name = category.getName();
            categoryLabel.setValue(name);
        }
        // }
        return categoryLabel;
    }

    /**
     * Return the lifter hidden behind the item.
     * 
     * @param item
     * @return a Lifter
     */
    private Lifter getLifter(final Item item) {
        return (Lifter) ItemAdapter.getObject(item);
    }

    public String categorySetToString(Set<Long> categoryIds) {
        if (categoryIds == null) return Messages.getString("Common.emptyList", app.getLocale()); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        final String separator = Messages.getString("CommonFieldFactory.listSeparator", app.getLocale()); //$NON-NLS-1$
        for (Long curCategoryId : categoryIds) {
            // Item s = (categories.getItem(curCategoryId));
            Category curCategory = (Category) ItemAdapter.getObject(activeCategories.getItem(curCategoryId));
            sb.append(curCategory.getName());
            sb.append(separator); //$NON-NLS-1$
        }
        if (sb.length() == 0) {
            return Messages.getString("Common.emptyList", app.getLocale()); //$NON-NLS-1$
        } else {
            return sb.substring(0, sb.length() - separator.length()); // hide
                                                                      // last
                                                                      // comma
        }
    }

    private Field adjustDateField(DateField f) {
        f.setDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$
        f.setResolution(DateField.RESOLUTION_MIN);
        return f;
    }

    private HashMap<Object, Component> groupIdToPlatformSelect = new HashMap<Object, Component>();

    public Field getPlatformComboboxFor(Object platformId) {
        ComboBox cb = null;
        if (platformId != null) {
            cb = (ComboBox) groupIdToPlatformSelect.get(platformId);
        }
        if (cb == null) {
            final ComboBox cb2 = new ComboBox();
            cb2.setContainerDataSource(platforms);
            cb2.setItemCaptionPropertyId("name"); //$NON-NLS-1$
            cb2.setNewItemsAllowed(true);

            cb = cb2;
        }
        return cb;
    }

    /**
     * Selection when only one platform is relevant (e.g. when entering
     * registration platform for a lifter)
     * 
     * @param itemId
     * @param prop
     * @return
     */
    @SuppressWarnings("serial")
    public Component getPlatformPopUpFor(Object itemId, final Property prop) {
        ListSelect platformSelect = null;
        if (itemId == null) {
            platformSelect = (ListSelect) groupIdToPlatformSelect.get(itemId);
        }
        if (platformSelect == null) {
            platformSelect = new ListSelect();
            platformSelect.setContainerDataSource(platforms);
            platformSelect.setItemCaptionPropertyId("name"); //$NON-NLS-1$
            platformSelect.setNewItemsAllowed(false);
            platformSelect.setNullSelectionAllowed(true);
            platformSelect.setMultiSelect(false);
            platformSelect.setRows(platforms.size() + 1);
            if (firstCategoryComboBox) {
                firstCategoryComboBox = false;
            }
            platformSelect.setRequiredError(Messages.getString(
                "CommonFieldFactory.youMustSelectAPlatform", app.getLocale())); //$NON-NLS-1$
            platformSelect.setRows(platforms.size() + nbRowsForNullValue(platformSelect));
            groupIdToPlatformSelect.put(itemId, platformSelect);
        }
        platformSelect.setPropertyDataSource(prop);
        platformSelect.setWriteThrough(true);
        platformSelect.setImmediate(true);
        final ListSelect selector = platformSelect;
        PopupView popup = new PopupView(new PopupView.Content() {

            @Override
            public String getMinimizedValueAsHTML() {
                return platformItemDisplayString(prop.getValue());
            }

            @Override
            public Component getPopupComponent() {
                return selector;
            }
        });
        popup.setHideOnMouseOut(true);
        return popup;
    }

    /**
     * @param value
     * @return
     */
    protected String platformItemDisplayString(Object value) {
        Platform curPlatform = null;
        if (value != null) {
            if (value instanceof Platform) {
                curPlatform = (Platform) value;
            } else {
                curPlatform = (Platform) ItemAdapter.getObject(platforms.getItem(value));
            }
        }
        return (curPlatform != null ? curPlatform.getName() : Messages
                .getString("Common.noneSelected", app.getLocale())); //$NON-NLS-1$
    }

    private Map<Object, ListSelect> lifterIdToCategorySelect = new HashMap<Object, ListSelect>();
    private boolean firstCategoryComboBox = true;

    /**
     * Selection when only one category is relevant (e.g. when entering
     * registration category for a lifter)
     * 
     * @param itemId
     * @return
     */
    public Field getCategoryComboboxFor(Object itemId) {
        ListSelect categorySelect = null;
        if (itemId == null) {
            categorySelect = lifterIdToCategorySelect.get(itemId);
        }
        if (categorySelect == null) {
            categorySelect = new ListSelect();
            setupCategorySelection(categorySelect, activeCategories, firstCategoryComboBox);
            if (firstCategoryComboBox) {
                firstCategoryComboBox = false;
            }
            categorySelect.setRequiredError(Messages.getString(
                "CommonFieldFactory.youMustSelectACategory", app.getLocale())); //$NON-NLS-1$
            categorySelect.setWriteThrough(true);
            categorySelect.setImmediate(true);
            lifterIdToCategorySelect.put(itemId, categorySelect);
        }
        return categorySelect;
    }

    /**
     * Selection when only one category is relevant (e.g. when entering
     * registration category for a lifter)
     * 
     * @param itemId
     * @param prop
     * @return
     */
    @SuppressWarnings("serial")
    public Component getCategoryPopUpFor(Object itemId, final Property prop) {
        ListSelect categorySelect = null;
        if (itemId == null) {
            categorySelect = lifterIdToCategorySelect.get(itemId);
        }
        if (categorySelect == null) {
            categorySelect = new ListSelect();
            setupCategorySelection(categorySelect, activeCategories, firstCategoryComboBox);
            if (firstCategoryComboBox) {
                firstCategoryComboBox = false;
            }
            categorySelect.setRequiredError(Messages.getString(
                "CommonFieldFactory.youMustSelectACategory", app.getLocale())); //$NON-NLS-1$
            categorySelect.setRows(activeCategories.size() + nbRowsForNullValue(categorySelect));
            lifterIdToCategorySelect.put(itemId, categorySelect);
        }
        categorySelect.setPropertyDataSource(prop);
        categorySelect.setWriteThrough(true);
        categorySelect.setImmediate(true);
        categorySelect.setNullSelectionAllowed(true);
        final ListSelect selector = categorySelect;
        PopupView popup = new PopupView(new PopupView.Content() {

            @Override
            public String getMinimizedValueAsHTML() {
                return categoryItemDisplayString(prop.getValue());
            }

            @Override
            public Component getPopupComponent() {
                return selector;
            }
        });
        popup.setHideOnMouseOut(true);
        return popup;
    }

    /**
     * @param categorySelect
     * @return
     */
    private int nbRowsForNullValue(ListSelect categorySelect) {
        return (categorySelect.isNullSelectionAllowed() && !categorySelect.isMultiSelect() ? 1 : 0);
    }

    private HashMap<Object, ListSelect> lifterIdToGroupSelect = new HashMap<Object, ListSelect>();
    private boolean firstGroupComboBox = true;

    /**
     * Selection when only one category is relevant (e.g. when entering
     * registration category for a lifter)
     * 
     * @param itemId
     * @return
     */
    public Field getGroupComboboxFor(Object itemId) {
        ListSelect groupSelect = null;
        if (itemId == null) {
            groupSelect = lifterIdToGroupSelect.get(itemId);
        }
        if (groupSelect == null) {
            groupSelect = new ListSelect();
            setupGroupSelection(groupSelect, competitionSessions, firstGroupComboBox);
            if (firstGroupComboBox) {
                firstGroupComboBox = false;
            }
            groupSelect.setRequiredError(Messages.getString("CommonFieldFactory.youMustSelectAGroup", app.getLocale())); //$NON-NLS-1$
            lifterIdToCategorySelect.put(itemId, groupSelect);
        }
        return groupSelect;
    }

    /**
     * Selection when only one category is relevant (e.g. when entering
     * registration category for a lifter)
     * 
     * @param itemId
     * @param prop
     * @return
     */
    @SuppressWarnings("serial")
    public Component getGroupPopUpFor(Object itemId, final Property prop) {
        ListSelect groupSelect = null;
        if (itemId == null) {
            groupSelect = lifterIdToGroupSelect.get(itemId);
        }
        if (groupSelect == null) {
            groupSelect = new ListSelect();
            setupGroupSelection(groupSelect, competitionSessions, firstGroupComboBox);
            if (firstGroupComboBox) {
                firstGroupComboBox = false;
            }
            groupSelect.setRequiredError(Messages.getString("CommonFieldFactory.youMustSelectAGroup", app.getLocale())); //$NON-NLS-1$
            groupSelect.setRows(competitionSessions.size() + nbRowsForNullValue(groupSelect));
            lifterIdToCategorySelect.put(itemId, groupSelect);
        }
        groupSelect.setPropertyDataSource(prop);
        final ListSelect selector = groupSelect;
        groupSelect.setWriteThrough(true);
        groupSelect.setImmediate(true);
        groupSelect.setNullSelectionAllowed(true);
        final PopupView popup = new PopupView(new PopupView.Content() {

            @Override
            public String getMinimizedValueAsHTML() {
                return groupItemDisplayString(prop.getValue());
            }

            @Override
            public Component getPopupComponent() {
                return selector;
            }
        });
        return popup;
    }

    /**
     * Set-up shared behaviour for category combo boxes and multi-selects.
     * 
     * @param groupSelect
     * @param competitionSessions
     * @param first
     */
    private void setupGroupSelection(AbstractSelect groupSelect, HbnContainer<CompetitionSession> competitionSessions, boolean first) {
        if (first) {
            //          groups.addContainerFilter("active", "true", true, false); //$NON-NLS-1$ //$NON-NLS-2$
            // groups.setFilteredGetItemIds(true);
            first = false;
        }
        groupSelect.setWriteThrough(true);
        groupSelect.setImmediate(true);
        groupSelect.setContainerDataSource(competitionSessions);
        groupSelect.setItemCaptionPropertyId("name"); //$NON-NLS-1$
        groupSelect.setNullSelectionAllowed(true);
        groupSelect.setNewItemsAllowed(false);
        groupSelect.setWidth("8ex"); //$NON-NLS-1$
    }

}
