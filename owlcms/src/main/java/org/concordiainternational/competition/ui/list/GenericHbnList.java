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

package org.concordiainternational.competition.ui.list;

import org.concordiainternational.competition.i18n.Messages;

import com.vaadin.Application;
import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;

public abstract class GenericHbnList<T> extends GenericList<T> {

    private static final long serialVersionUID = 8085082497600453476L;

    public GenericHbnList(Application app, Class<T> parameterizedClass, String caption) {
        super(app, parameterizedClass, caption);
    }

    /**
     * Load container content to Table
     */
    @Override
    protected void loadData() {
        // System.err.println("GenericHbnList: loadData()");
        final HbnContainer<T> cont = new HbnContainer<T>(parameterizedClass, (HbnSessionManager) app);
        table.setContainerDataSource(cont);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clearCache() {
        // System.err.println("GenericHbnList: clearCache()");
        ((HbnContainer<T>) table.getContainerDataSource()).clearCache();
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

                public Component generateCell(Table source, final Object itemId, Object columnId) {
                    // HorizontalLayout actions = new HorizontalLayout();
                    Button del = new Button(Messages.getString("Common.delete", app.getLocale())); //$NON-NLS-1$
                    del.addListener(new ClickListener() {
                        private static final long serialVersionUID = 5204920602544644705L;

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
     * @param tableToolbar
     */
    @Override
    protected void createToolbarButtons(HorizontalLayout tableToolbar) {
        toggleEditModeButton = new Button("", this, "toggleEditable"); //$NON-NLS-1$ //$NON-NLS-2$
        tableToolbar.addComponent(toggleEditModeButton);
        addRowButton = new Button(Messages.getString("Common.addRow", app.getLocale()), this, "newItem"); //$NON-NLS-1$ //$NON-NLS-2$
        tableToolbar.addComponent(addRowButton);
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
