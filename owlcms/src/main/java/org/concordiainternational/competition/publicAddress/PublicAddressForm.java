/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.publicAddress;

import java.util.ArrayList;
import java.util.List;

import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.generators.CommonFieldFactory;
import org.concordiainternational.competition.ui.list.GenericList;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

/**
 * Editing of group information.
 * 
 * @author jflamy
 */
@SuppressWarnings({ "serial" })
public class PublicAddressForm extends Form implements Window.CloseListener {

    public static void editPublicAddress(GenericList<?> list,
            SessionData masterData) {
        /* Create an empty "Message" item */
        if (masterData.getPublicAddressItem() == null) {
            PropertysetItem item = new PropertysetItem();
            item.addItemProperty("title", new ObjectProperty<String>("", String.class));
            item.addItemProperty("message", new ObjectProperty<String>("", String.class));
            item.addItemProperty("remainingSeconds", new ObjectProperty<IntermissionTimer>(null, IntermissionTimer.class));
            masterData.setPublicAddressItem(item);
        }

        PublicAddressForm form = new PublicAddressForm(masterData);
        form.setReadOnly(false);

        CompetitionApplication app = CompetitionApplication.getCurrent();
        Window editingWindow = new Window(Messages.getString("LiftList.publicAddress", app.getLocale()));
        form.setWindow(editingWindow);
        form.setParentList(list);
        editingWindow.getContent().addComponent(form);
        app.getMainWindow().addWindow(editingWindow);
        editingWindow.setWidth("60em");
        editingWindow.center();
    }

    private CountdownField countdownField;

    public class PublicAddressFormFieldFactory implements FormFieldFactory {

        @Override
        public Field createField(Item item, Object propertyId, Component uiContext) {
            if (propertyId.equals("remainingSeconds")) {
                countdownField = new CountdownField();
                countdownField.setCaption(Messages.getString("Field.CountdownField.caption", CompetitionApplication.getCurrentLocale()));
                masterData.addBlackBoardListener(countdownField);
                return countdownField;
            } else if (propertyId.equals("title")) {
                TextField titleField = new TextField();
                titleField.setCaption(Messages.getString("FieldName.title", CompetitionApplication.getCurrentLocale()));
                titleField.setColumns(20);
                return titleField;
            } else if (propertyId.equals("message")) {
                TextArea messageField = new TextArea();
                messageField.setCaption(Messages.getString("FieldName.message", CompetitionApplication.getCurrentLocale()));
                messageField.setRows(5);
                messageField.setColumns(20); // yields about 30 chars monospaced.
                messageField.addStyleName("fixedFont"); //$NON-NLS-1$
                return messageField;
            } else {
                return null;
            }

        }

    }

    final private static Logger logger = LoggerFactory.getLogger(PublicAddressForm.class);
    CommonFieldFactory commonFactory = new CommonFieldFactory(CompetitionApplication.getCurrent());
    Window window = null;
    GenericList<?> parentList = null;

    private SessionData masterData;

    public PublicAddressForm(SessionData masterData) {
        super();
        this.setFormFieldFactory(new PublicAddressFormFieldFactory());
        this.masterData = masterData;
        setItemDataSource(masterData.getPublicAddressItem());

        setImmediate(true);
        setWriteThrough(true);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);

        Label spacer = new Label("");
        footer.addComponent(spacer);
        // footer.addComponent(ok);
        footer.addComponent(cancel);
        footer.addComponent(display);
        footer.addComponent(clear);

        footer.setVisible(true);
        footer.setWidth("100%");
        footer.setExpandRatio(spacer, 100);

        setFooter(footer);
    }

    Button display = new Button(
            Messages.getString("PublicAddress.display", CompetitionApplication.getCurrentLocale()), new Button.ClickListener() { //$NON-NLS-1$
                @Override
                public void buttonClick(ClickEvent event) {
                    LoggerUtils.buttonSetup();
                    commit();
                    display();
                }
            });

    Button clear = new Button(
            Messages.getString("PublicAddress.clear", CompetitionApplication.getCurrentLocale()), new Button.ClickListener() { //$NON-NLS-1$
                @Override
                public void buttonClick(ClickEvent event) {
                    LoggerUtils.buttonSetup();
                    commit();
                    clearDisplay();
                }
            });

    //	Button ok = new Button(Messages.getString("Common.OK", CompetitionApplication.getCurrentLocale()),new Button.ClickListener() {	 //$NON-NLS-1$
    // @Override
    // public void buttonClick(ClickEvent event) {
    // commit();
    // display();
    // closeWindow();
    // }
    // });

    Button cancel = new Button(Messages.getString("Common.cancel", CompetitionApplication.getCurrentLocale()), new Button.ClickListener() { //$NON-NLS-1$
                @Override
                public void buttonClick(ClickEvent event) {
                    LoggerUtils.buttonSetup();
                    discard();
                    closeWindow();
                }
            });

    @Override
    public void setItemDataSource(Item itemDataSource) {
        if (itemDataSource != null) {
            List<Object> orderedProperties = new ArrayList<Object>();
            orderedProperties.add("remainingSeconds"); //$NON-NLS-1$

            orderedProperties.add("title"); //$NON-NLS-1$
            orderedProperties.add("message"); //$NON-NLS-1$

            super.setItemDataSource(itemDataSource, orderedProperties);
            getFooter().setVisible(true);
        } else {
            super.setItemDataSource(null);
            getFooter().setVisible(false);
        }
    }

    protected void clearDisplay() {
        masterData.clearPublicAddressDisplay();
    }

    protected void display() {
        masterData.displayPublicAddress();
    }

    @Override
    public Window getWindow() {
        return window;
    }

    public void setWindow(Window window) {
        this.window = window;
        window.addListener(this);
    }

    public GenericList<?> getParentList() {
        return parentList;
    }

    public void setParentList(GenericList<?> parentList) {
        this.parentList = parentList;
    }

    /**
	 * 
	 */
    private void closeWindow() {
        logger.debug("closeWindow {}", parentList); //$NON-NLS-1$
        masterData.removeBlackBoardListener(countdownField);

        if (window != null) {
            Window parent = window.getParent();
            parent.removeWindow(window);
        }
        // if (parentList != null) {
        // // nothing to do in this case
        // }
    }

    @Override
    public void windowClose(CloseEvent e) {
        logger.debug("windowClose {}", parentList); //$NON-NLS-1$
        masterData.removeBlackBoardListener(countdownField);
    }

}
