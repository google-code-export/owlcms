/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.util.Locale;

import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.decision.Speakers;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.list.GenericHbnList;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.hbnutil.HbnContainer.EntityItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

public class PlatformList extends GenericHbnList<Platform> implements ApplicationView {

    private static final long serialVersionUID = -6455130090728823622L;
    private String viewName;
    final static org.slf4j.Logger logger = LoggerFactory.getLogger(PlatformList.class);

    public PlatformList(boolean initFromFragment, String viewName) {
        super(CompetitionApplication.getCurrent(), Platform.class, Messages.getString(
                "PlatformList.Platforms", CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view, getLoggingId());

        init();
        registerAsListener();
    }

    private static String[] NATURAL_COL_ORDER = null;
    private static String[] COL_HEADERS = null;

    /**
     * @return Natural property order for Category bean. Used in tables and forms.
     */
    @Override
    protected String[] getColOrder() {
        if (NATURAL_COL_ORDER != null)
            return NATURAL_COL_ORDER;
        NATURAL_COL_ORDER = new String[] { "name", //$NON-NLS-1$
                "showDecisionLights", //$NON-NLS-1$
                "showTimer", //$NON-NLS-1$
                "mixerName", //$NON-NLS-1$
                "actions" //$NON-NLS-1$
        };
        return NATURAL_COL_ORDER;
    }

    /**
     * @return Localized captions for properties in same order as in {@link #getColOrder()}
     */
    @Override
    protected String[] getColHeaders() {
        Locale locale = app.getLocale();
        if (COL_HEADERS != null)
            return COL_HEADERS;
        COL_HEADERS = new String[] {
                Messages.getString("CategoryEditor.name", locale), //$NON-NLS-1$
                Messages.getString("Platform.showDecisionLights", locale), //$NON-NLS-1$
                Messages.getString("Platform.showTimer", locale), //$NON-NLS-1$
                Messages.getString("Platform.speakers", locale), //$NON-NLS-1$
                Messages.getString("Common.actions", locale), //$NON-NLS-1$
        };
        return COL_HEADERS;
    }

    /**
     * Default actions: delete.
     */
    @Override
    protected void addDefaultActions() {
        table.removeGeneratedColumn("actions"); //$NON-NLS-1$
        table.addGeneratedColumn("actions", new ColumnGenerator() { //$NON-NLS-1$
                    private static final long serialVersionUID = 7397136740353981832L;

                    @Override
                    public Component generateCell(Table source, final Object itemId, Object columnId) {
                        HorizontalLayout actions = new HorizontalLayout();
                        Button del = new Button(Messages.getString("Common.delete", app.getLocale())); //$NON-NLS-1$
                        del.addListener(new ClickListener() {
                            private static final long serialVersionUID = 5204920602544644705L;

                            @Override
                            public void buttonClick(ClickEvent event) {
                                try {
                                    deleteItem(itemId);
                                } catch (ConstraintViolationException exception) {
                                    throw new RuntimeException(Messages.getString("PlatformList.MustNotBeInUse", app
                                            .getLocale()));
                                }
                            }
                        });
                        actions.addComponent(del);
                        return actions;
                    }
                });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.concordiainternational.competition.ui.list.GenericHbnList#addGeneratedColumns()
     */
    @Override
    protected void addGeneratedColumns() {
        super.addGeneratedColumns();
        table.removeGeneratedColumn("mixerName"); //$NON-NLS-1$
        table.addGeneratedColumn("mixerName", new ColumnGenerator() { //$NON-NLS-1$
                    private static final long serialVersionUID = 7397136740353981832L;

                    @SuppressWarnings("serial")
                    @Override
                    public Component generateCell(Table source, final Object itemId, Object columnId) {
                        final Item item = table.getItem(itemId);
                        // final Property uiProp = item.getItemProperty(columnId);
                        final Property prop = table.getContainerProperty(itemId, columnId);

                        if (!table.isEditable()) {
                            final Object value = prop.getValue();
                            return new Label((String) value); //$NON-NLS-1$
                        } else {
                            ComboBox ls = new ComboBox("", Speakers.getOutputNames());
                            ls.setNullSelectionAllowed(true);
                            ls.setPropertyDataSource(prop);
                            ls.setImmediate(true);

                            // normally, there is no need for a listener (the setImmediate will
                            // ensure that the setMixerName() method is called right away. But
                            // we would like audio feedback right away if there are multiple audio devices.
                            ls.addListener(new ValueChangeListener() {
                                @SuppressWarnings("rawtypes")
                                @Override
                                public void valueChange(ValueChangeEvent event) {
                                    Platform pl = (Platform) ((EntityItem) item).getPojo();
                                    pl.setMixerName((String) event.getProperty().getValue());
                                    new Speakers().testSound(pl.getMixer());
                                }
                            });
                            return ls;
                        }
                    }
                });
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

    /**
     * @return
     */
    @Override
    public String getFragment() {
        return viewName;
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
