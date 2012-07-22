/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui.list;

import javax.persistence.EntityManager;

import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.vaadin.addons.criteriacontainer.CriteriaContainer;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

public abstract class GenericPersistedList<T> extends GenericList<T> {

    private static final long serialVersionUID = 8085082497600453476L;

    public GenericPersistedList(CompetitionApplication app, Class<T> parameterizedClass, String caption) {
        super(app, parameterizedClass, caption);
    }

    /**
     * Load container content to Table.
     * Underlying container has detached entities and entity manager is closed to prevent leaks.
     * The container's refresh method will therefore fail if invoked.
     */
    @Override
    protected void loadData() {
        // System.err.println("GenericHbnList: loadData()");
        EntityManager em = null;
        try {
            em = CompetitionApplication.getNewGlobalEntityManager();
            final CriteriaContainer<T> cont = new CriteriaContainer<T>(em, true, true, parameterizedClass, 100);
            table.setContainerDataSource(cont);
        } finally {
            if (em != null) em.close();
        }

    }

    /**
     * By default, add an action column with "delete" button.
     */
    @Override
    protected void addGeneratedColumns() {
        // action buttons on each row
        addDefaultActions();
    }

    /**
     * Default actions: delete.
     */
    protected void addDefaultActions() {
        table.removeGeneratedColumn("actions"); //$NON-NLS-1$
        table.addGeneratedColumn("actions", new ColumnGenerator() { //$NON-NLS-1$
                private static final long serialVersionUID = 7397136740353981832L;

                @Override
				public Component generateCell(Table source, final Object itemId, Object columnId) {
                    // HorizontalLayout actions = new HorizontalLayout();
                    Button del = new Button(Messages.getString("Common.delete", app.getLocale())); //$NON-NLS-1$
                    del.addListener(new ClickListener() {
                        private static final long serialVersionUID = 5204920602544644705L;

                        @Override
						public void buttonClick(ClickEvent event) {
                            deleteItem(itemId);
                        }
                    });
                    // actions.addComponent(del);
                    // return actions;
                    return del;
                }
            });
    }

    /**
     * @param tableToolbar1
     */
    @Override
    protected void createToolbarButtons(HorizontalLayout tableToolbar1) {
        toggleEditModeButton = new Button("", this, "toggleEditable"); //$NON-NLS-1$ //$NON-NLS-2$
        tableToolbar1.addComponent(toggleEditModeButton);
        addRowButton = new Button(Messages.getString("Common.addRow", app.getLocale()), this, "newItem"); //$NON-NLS-1$ //$NON-NLS-2$
        tableToolbar1.addComponent(addRowButton);
        setButtonVisibility();
        setDefaultActions();
    }

    /**
     * This method is used in response to a button click.
     */
    @Override
    public void toggleEditable() {
        super.toggleEditable();
        setDefaultActions();
    }

    /**
     * Make sure that if the table is editable its actions are visible.
     */
    protected void setDefaultActions() {
        if (table != null) {
            if (!table.isEditable()) {
                table.removeGeneratedColumn("actions"); //$NON-NLS-1$
                table.setSizeFull();
            } else {
                this.addDefaultActions();
            }
        }
    }

    @Override
    protected void setButtonVisibility() {
        if (table == null) {
            addRowButton.setVisible(false);
            toggleEditModeButton.setVisible(false);
            return;
        }
        if (table.isEditable()) {
            toggleEditModeButton.setCaption(Messages.getString("Common.done", app.getLocale())); //$NON-NLS-1$
            toggleEditModeButton.setVisible(true);
            addRowButton.setVisible(true);
        } else {
            toggleEditModeButton.setCaption(Messages.getString("Common.edit", app.getLocale())); //$NON-NLS-1$
            toggleEditModeButton.setVisible(true);
            addRowButton.setVisible(false);
        }
    }

    @Override
    protected String[] getColHeaders() {
        return null;
    }

    @Override
    protected String[] getColOrder() {
        return null;
    }

    @Override
    protected void positionTable() {
        super.positionTable();
        table.setSelectable(false);
    }
}
