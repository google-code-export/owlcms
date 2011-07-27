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
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Set;

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.generators.CommonColumnGenerator;
import org.concordiainternational.competition.ui.list.GenericHbnList;
import org.concordiainternational.competition.utils.ItemAdapter;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Window;

public class SessionList extends GenericHbnList<CompetitionSession> implements ApplicationView {

    private static final long serialVersionUID = -6455130090728823622L;
    private String viewName;
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(SessionList.class);

    public SessionList(boolean initFromFragment, String viewName) {
        super(CompetitionApplication.getCurrent(), CompetitionSession.class, Messages.getString(
            "GroupList.Groups", CompetitionApplication.getCurrent().getLocale())); //$NON-NLS-1$
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
        NATURAL_COL_ORDER = new String[] { 
        		"name", //$NON-NLS-1$
                "weighInTime", //$NON-NLS-1$
                "competitionTime", //$NON-NLS-1$
                "platform", //$NON-NLS-1$
                "categories", //$NON-NLS-1$
                "actions", //$NON-NLS-1$
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
        COL_HEADERS = new String[] { Messages.getString("Group.name", locale), //$NON-NLS-1$
                Messages.getString("Group.weighInTime", locale), //$NON-NLS-1$
                Messages.getString("Group.competitionTime", locale), //$NON-NLS-1$
                Messages.getString("Group.platform", locale), //$NON-NLS-1$
                Messages.getString("Group.categories", locale), //$NON-NLS-1$
                Messages.getString("Common.actions", locale), //$NON-NLS-1$
        };
        return COL_HEADERS;
    }

    /**
     * Computed columnts
     */
    @Override
    protected void addGeneratedColumns() {
        super.addGeneratedColumns();
        table.removeGeneratedColumn("categories"); //$NON-NLS-1$
        table.addGeneratedColumn("categories", new CommonColumnGenerator(app)); //$NON-NLS-1$
//        table.removeGeneratedColumn("weighInTime"); //$NON-NLS-1$
//        table.addGeneratedColumn("weighInTime", new CommonColumnGenerator(app)); //$NON-NLS-1$
//        table.removeGeneratedColumn("competitionTime"); //$NON-NLS-1$
//        table.addGeneratedColumn("competitionTime", new CommonColumnGenerator(app)); //$NON-NLS-1$
        table.removeGeneratedColumn("platform"); //$NON-NLS-1$
        table.addGeneratedColumn("platform", new CommonColumnGenerator(app)); //$NON-NLS-1$
        
        setExpandRatios();
        table.setColumnExpandRatio("name", 0.3F);
        table.setColumnExpandRatio("actions", 1.2F);
        
    }

    /**
     * Default actions: delete lifters, edit.
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
                                throw new RuntimeException(Messages.getString("GroupList.GroupMustBeEmpty", app
                                        .getLocale()));
                            }
                        }
                    });
                    actions.addComponent(del);

                    Button clear = new Button(Messages.getString("GroupList.clear", app.getLocale())); //$NON-NLS-1$
                    clear.addListener(new ClickListener() {
                        private static final long serialVersionUID = 5204920602544644705L;

                        @Override
						public void buttonClick(ClickEvent event) {
                            clearCompetitionSession((Long) itemId);
                        }
                    });
                    actions.addComponent(clear);
                    
                    Button edit = new Button(Messages.getString("Common.edit", app.getLocale())); //$NON-NLS-1$
                    edit.addListener(new ClickListener() {
                        private static final long serialVersionUID = 5204920602544644705L;

                        @Override
						public void buttonClick(ClickEvent event) {
                        	editCompetitionSession((Long) itemId, table.getItem(itemId));
                        }
                    });
                    actions.addComponent(edit);
                    return actions;
                }
            });
    }

    /**
     * Remove all lifters in the CompetitionSession
     * @param itemId
     */
    private void clearCompetitionSession(Long itemId) {
        Item item = table.getContainerDataSource().getItem(itemId);
        CompetitionSession competitionSession = (CompetitionSession) ItemAdapter.getObject(item);
        int nbLifters = 0;
        Set<Lifter> lifters = competitionSession.getLifters();
        if (lifters != null && lifters.size() > 0) {
            nbLifters = lifters.size();
            competitionSession.deleteLifters((CompetitionApplication) app);
        }
        Locale locale = CompetitionApplication.getCurrentLocale();
		String messageTemplate = Messages.getString("GroupList.erased", locale); //$NON-NLS-1$
		app.getMainWindow().showNotification(MessageFormat.format(messageTemplate,nbLifters)); 
    }
    
    /**
     * @param itemId
     * @param item2 
     */
    private void editCompetitionSession(Long itemId, Item item2) {
        CompetitionSession competitionSession = (CompetitionSession) ItemAdapter.getObject(item2);
        SessionForm form = new SessionForm();
        form.setItemDataSource(item2);
        form.setReadOnly(false);

        Window editingWindow = new Window(competitionSession.getName());
        form.setWindow(editingWindow);
        form.setParentList(this);
        editingWindow.getContent().addComponent(form);
        app.getMainWindow().addWindow(editingWindow);
        editingWindow.setHeight("90%");
        editingWindow.setWidth("40em");
        editingWindow.center();
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
		app.getMainWindow().addListener((CloseListener) this);
	}

	@Override
	public void unregisterAsListener() {
		app.getMainWindow().addListener((CloseListener) this);
	}
	
	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();	
	}
	
	/* Called on refresh.
	 * @see com.vaadin.terminal.URIHandler#handleURI(java.net.URL, java.lang.String)
	 */
	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		registerAsListener();
		return null;
	}

}
