/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.net.URL;
import java.util.Locale;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.list.GenericHbnList;
import org.concordiainternational.competition.utils.LoggerUtils;

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
        LoggerUtils.mdcPut(LoggerUtils.LoggingKeys.view,getLoggingId());
        init();
    }

    private static String[] NATURAL_COL_ORDER = null;
    private static String[] COL_HEADERS = null;

    /**
     * @return Natural property order for Category bean. Used in tables and forms.
     */
    @Override
    public String[] getColOrder() {
        if (NATURAL_COL_ORDER != null)
            return NATURAL_COL_ORDER;
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
     * @return Localized captions for properties in same order as in {@link #getColOrder()}
     */
    @Override
    public String[] getColHeaders() {
        Locale locale = app.getLocale();
        if (COL_HEADERS != null)
            return COL_HEADERS;
        COL_HEADERS = new String[] { Messages.getString("CategoryEditor.name", locale), //$NON-NLS-1$
                Messages.getString("CategoryEditor.gender", locale), //$NON-NLS-1$
                Messages.getString("CategoryEditor.minimumWeight", locale), //$NON-NLS-1$
                Messages.getString("CategoryEditor.maximumWeight", locale), //$NON-NLS-1$
                Messages.getString("CategoryEditor.Active", locale), //$NON-NLS-1$
                Messages.getString("Common.actions", locale), //$NON-NLS-1$
        };
        return COL_HEADERS;
    }

    /**
     * This method is used in response to a button click.
     */
    @Override
    public void toggleEditable() {
        super.toggleEditable();
        if (!table.isEditable()) {
            CategoryLookup.getSharedInstance().reload();
        }
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
