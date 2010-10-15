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

import java.util.Locale;

import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.list.GenericHbnList;
import org.hibernate.exception.ConstraintViolationException;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;

public class PlatformList extends GenericHbnList<Platform> implements ApplicationView {

    private static final long serialVersionUID = -6455130090728823622L;
    private String viewName;

    public PlatformList(boolean initFromFragment, String viewName) {
        super(CompetitionApplication.getCurrent(), Platform.class, Messages.getString(
            "PlatformList.Platforms", CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        init();
    }

    private static String[] NATURAL_COL_ORDER = null;
    private static String[] COL_HEADERS = null;

    /**
     * @return Natural property order for Category bean. Used in tables and
     *         forms.
     */
    @Override
    protected String[] getColOrder() {
        if (NATURAL_COL_ORDER != null) return NATURAL_COL_ORDER;
        NATURAL_COL_ORDER = new String[] { "name", //$NON-NLS-1$
                "hasDisplay", //$NON-NLS-1$
                "defaultPlatform", //$NON-NLS-1$
                "actions" //$NON-NLS-1$
        };
        return NATURAL_COL_ORDER;
    }

    /**
     * @return Localized captions for properties in same order as in
     *         {@link #getColOrder()}
     */
    @Override
    protected String[] getColHeaders() {
        Locale locale = app.getLocale();
        if (COL_HEADERS != null) return COL_HEADERS;
        COL_HEADERS = new String[] { Messages.getString("CategoryEditor.name", locale), //$NON-NLS-1$
                Messages.getString("Platform.NECDisplay", locale), //$NON-NLS-1$
                Messages.getString("Platform.defaultPlatform", locale), //$NON-NLS-1$
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

                public Component generateCell(Table source, final Object itemId, Object columnId) {
                    HorizontalLayout actions = new HorizontalLayout();
                    Button del = new Button(Messages.getString("Common.delete", app.getLocale())); //$NON-NLS-1$
                    del.addListener(new ClickListener() {
                        private static final long serialVersionUID = 5204920602544644705L;

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

    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.components.ApplicationView#needsMenu()
     */
    @Override
    public boolean needsMenu() {
        return true;
    }
    /**
     * @return
     */
    public String getFragment() {
        return viewName;
    }
    

    /* (non-Javadoc)
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
}
