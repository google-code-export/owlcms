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

import java.net.URL;
import java.util.Locale;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.list.GenericHbnList;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

public class CategoryList extends GenericHbnList<Category> implements ApplicationView {

    private static final long serialVersionUID = -6455130090728823622L;
    private String viewName;

    public CategoryList(boolean initFromFragment, String viewName) {
        super(CompetitionApplication.getCurrent(), Category.class, Messages.getString(
            "CategoryList.Categories", CompetitionApplication.getCurrentLocale())); //$NON-NLS-1$
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
    public String[] getColOrder() {
        if (NATURAL_COL_ORDER != null) return NATURAL_COL_ORDER;
        NATURAL_COL_ORDER = new String[] { "name", //$NON-NLS-1$
                "gender", //$NON-NLS-1$
                "minimumWeight", //$NON-NLS-1$
                "maximumWeight", //$NON-NLS-1$
                "active", //$NON-NLS-1$
                "actions" //$NON-NLS-1$
        };
        return NATURAL_COL_ORDER;
    }

    /**
     * @return Localized captions for properties in same order as in
     *         {@link #getColOrder()}
     */
    @Override
    public String[] getColHeaders() {
        Locale locale = app.getLocale();
        if (COL_HEADERS != null) return COL_HEADERS;
        COL_HEADERS = new String[] { Messages.getString("CategoryEditor.name", locale), //$NON-NLS-1$
                Messages.getString("CategoryEditor.gender", locale), //$NON-NLS-1$
                Messages.getString("CategoryEditor.minimumWeight", locale), //$NON-NLS-1$
                Messages.getString("CategoryEditor.maximumWeight", locale), //$NON-NLS-1$
                Messages.getString("CategoryEditor.Active", locale), //$NON-NLS-1$
                Messages.getString("Common.actions", locale), //$NON-NLS-1$
        };
        return COL_HEADERS;
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
    @Override
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

	@Override
	public void registerAsListener() {
		CompetitionApplication.getCurrent().getMainWindow().addListener((CloseListener) this);
	}

	@Override
	public void unregisterAsListener() {
		CompetitionApplication.getCurrent().getMainWindow().addListener((CloseListener) this);
	}
	
	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();	
	}

	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		registerAsListener();
		return null;
	}

}
