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

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.service.ApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class CompetitionEditor extends VerticalLayout implements ApplicationView {

    private static final long serialVersionUID = 5562100583893230718L;

    protected boolean editable = false;

    private CompetitionApplication app;

    Logger logger = LoggerFactory.getLogger(CompetitionEditor.class);

    private TextField displayFN;
    private Select editFN;

    private String viewName;

    public CompetitionEditor(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        
        app = CompetitionApplication.getCurrent();
        final Locale locale = app.getLocale();
        this.setReadOnly(true);

        HbnContainer<Competition> cmp = new HbnContainer<Competition>(Competition.class, app);
        if (cmp.size() == 0) {
            cmp.saveEntity(new Competition());
        }
        final Object firstItemId = cmp.firstItemId();
        HbnContainer<Competition>.EntityItem<Competition> competitionItem = cmp.getItem(firstItemId);

        final FormLayout formLayout = new FormLayout();

        this.addComponent(createTitleBar(locale, formLayout, cmp, competitionItem));
        this.addComponent(createFormLayout(formLayout, locale, cmp, competitionItem));
        this.setSpacing(true);
        this.setMargin(true);
    }

    /**
     * Display the competition information.
     * 
     * TODO: redo this using a form; this would enable us to use a
     * FormFieldFactory and deal more elegantly with the dual nature of the file
     * name field.
     * 
     * @param formLayout
     * @param locale
     * @param cmp
     * @param firstItemId
     * @throws ReadOnlyException
     * @throws ConversionException
     * @throws SourceException
     * @throws InvalidValueException
     */
    private FormLayout createFormLayout(final FormLayout formLayout, final Locale locale,
            HbnContainer<Competition> cmp, final HbnContainer<Competition>.EntityItem<Competition> competitionItem)
            throws ReadOnlyException, ConversionException, SourceException, InvalidValueException {

        addTextField(formLayout, locale, competitionItem, "competitionName", ""); //$NON-NLS-1$ //$NON-NLS-2$
        addDateField(formLayout, locale, competitionItem, "competitionDate", new Date()); //$NON-NLS-1$
        addTextField(formLayout, locale, competitionItem, "competitionSite", ""); //$NON-NLS-1$ //$NON-NLS-2$
        addTextField(formLayout, locale, competitionItem, "competitionCity", ""); //$NON-NLS-1$ //$NON-NLS-2$
        addTextField(formLayout, locale, competitionItem, "competitionOrganizer", ""); //$NON-NLS-1$ //$NON-NLS-2$
        addTextField(formLayout, locale, competitionItem, "invitedIfBornBefore", 0); //$NON-NLS-1$
        addTextField(formLayout, locale, competitionItem, "masters", false); //$NON-NLS-1$
        addTextField(formLayout, locale, competitionItem, "federation", ""); //$NON-NLS-1$ //$NON-NLS-2$
        addTextField(formLayout, locale, competitionItem, "federationAddress", ""); //$NON-NLS-1$ //$NON-NLS-2$
        addTextField(formLayout, locale, competitionItem, "federationWebSite", ""); //$NON-NLS-1$ //$NON-NLS-2$
        addTextField(formLayout, locale, competitionItem, "federationEMail", ""); //$NON-NLS-1$ //$NON-NLS-2$
        displayFN = addTextField(formLayout, locale, competitionItem, "resultTemplateFileName", ""); //$NON-NLS-1$ //$NON-NLS-2$
        editFN = addFileSelector(formLayout, locale, competitionItem, "resultTemplateFileName", ""); //$NON-NLS-1$ //$NON-NLS-2$)

        editable = false;
        setReadOnly(formLayout, true);
        editFN.setVisible(false);
        displayFN.setWidth("120ex");

        return formLayout;
    }

    /**
     * @param formLayout
     * @param locale
     * @param competitionItem
     * @param string
     * @param string2
     */
    private Select addFileSelector(FormLayout formLayout, Locale locale,
            HbnContainer<Competition>.EntityItem<Competition> competitionItem, String fieldName, String initialValue) {
        // File baseDirectory =
        // getApplication().getContext().getBaseDirectory();
        final ApplicationContext context = app.getContext();
        File baseDirectory = context.getBaseDirectory();
        final HbnContainer<Competition>.EntityItem<Competition> ci = competitionItem;
        final Competition competition = (Competition) ci.getPojo();

        final Property itemProperty = competitionItem.getItemProperty(fieldName);
        FilesystemContainer fsContainer = new FilesystemContainer(
                new File(baseDirectory, "/WEB-INF/classes/templates"), "xls", false);
        Select fileSelector = new Select(Messages.getString("Competition." + fieldName, locale), fsContainer);

        fileSelector.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                itemProperty.setValue(event.getProperty());
                logger.warn("property: {}, object field: {}.", itemProperty.getValue(), competition
                        .getResultTemplateFileName());
            }
        });
        fileSelector.setImmediate(true);
        formLayout.addComponent(fileSelector);
        return fileSelector;
    }

    /**
     * @param formLayout
     * @param locale
     * @param competitionItem
     * @param fieldName
     * @return
     * @throws ReadOnlyException
     * @throws ConversionException
     */
    private TextField addTextField(final Layout formLayout, final Locale locale,
            final HbnContainer<Competition>.EntityItem<Competition> competitionItem, final String fieldName,
            final Object initialValue) throws ReadOnlyException, ConversionException {
        final Property itemProperty = competitionItem.getItemProperty(fieldName);
        TextField field = new TextField(Messages.getString("Competition." + fieldName, locale), itemProperty); //$NON-NLS-1$
        field.setWidth("60ex"); //$NON-NLS-1$
        formLayout.addComponent(field);
        if (itemProperty.getValue() == null) itemProperty.setValue(initialValue); //$NON-NLS-1$
        return field;
    }

    /**
     * @param formLayout
     * @param locale
     * @param competitionItem
     * @param fieldName
     * @return
     * @throws ReadOnlyException
     * @throws ConversionException
     */
    private DateField addDateField(final Layout formLayout, final Locale locale,
            final HbnContainer<Competition>.EntityItem<Competition> competitionItem, final String fieldName,
            final Object initialValue) throws ReadOnlyException, ConversionException {
        final Property itemProperty = competitionItem.getItemProperty(fieldName);
        DateField field = new DateField(Messages.getString("Competition." + fieldName, locale), itemProperty); //$NON-NLS-1$
        formLayout.addComponent(field);
        if (itemProperty.getValue() == null) itemProperty.setValue(initialValue); //$NON-NLS-1$
        field.setResolution(DateField.RESOLUTION_DAY);
        return field;
    }

    /**
     * @param locale
     * @param formLayout
     * @param cmp
     */
    private HorizontalLayout createTitleBar(final Locale locale, final FormLayout formLayout,
            final HbnContainer<Competition> cmp, final HbnContainer<Competition>.EntityItem<Competition> competitionItem) {
        final HorizontalLayout hl = new HorizontalLayout();
        final Label cap = new Label(Messages.getString("CompetitionApplication.Competition", locale)); //$NON-NLS-1$
        cap.setWidth("20ex"); //$NON-NLS-1$
        cap.setHeight("1.2em"); //$NON-NLS-1$
        hl.setStyleName("title"); //$NON-NLS-1$
        hl.addComponent(cap);
        hl.setComponentAlignment(cap, Alignment.MIDDLE_LEFT);
        final Button editButton = new Button(Messages.getString("Common.edit", locale)); //$NON-NLS-1$
        hl.addComponent(editButton);
        editButton.addListener(new ClickListener() {
            private static final long serialVersionUID = 7441128401829794738L;

            @Override
            public void buttonClick(ClickEvent event) {
                if (editable) {
                    // layout currently says "OK"
                    event.getButton().setCaption(Messages.getString("Common.edit", locale)); //$NON-NLS-1$
                    CompetitionEditor.this.setReadOnly(formLayout, true);
                    saveItem(competitionItem);
                    editFN.setVisible(false);
                    displayFN.setVisible(true);
                    editable = false;
                } else {
                    // not editable, button currently says "Edit"
                    event.getButton().setCaption(Messages.getString("Common.OK", locale)); //$NON-NLS-1$
                    CompetitionEditor.this.setReadOnly(formLayout, false);
                    editFN.setVisible(true);
                    displayFN.setVisible(false);
                    editable = true;
                }
            }

            private void saveItem(HbnContainer<Competition>.EntityItem<Competition> cmItem) {
                Competition competition = (Competition) cmItem.getPojo();
                System.err
                        .println("saving " + competition.getCompetitionName() + " " + competition.getResultTemplateFileName()); //$NON-NLS-1$
                final Session session = app.getHbnSession();
                session.merge(competition);
                session.flush();
            };
        });
        return hl;
    }

    /**
     * @throws SourceException
     * @throws InvalidValueException
     */
    public void setReadOnly(ComponentContainer cont, boolean b) throws SourceException, InvalidValueException {
        for (Iterator<Component> iterator2 = (Iterator<Component>) cont.getComponentIterator(); iterator2.hasNext();) {
            Component f = (Component) iterator2.next();
            if (f instanceof Field && !(f instanceof Button)) {
                f.setReadOnly(b);
                ((Field) f).setWriteThrough(!b);
            }
        }
    }

    @Override
    public void refresh() {
        // nothing to do.
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
