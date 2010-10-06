/**
 * 
 */
package org.concordiainternational.competition.publicAddress;

import java.util.ArrayList;
import java.util.List;

import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.SessionData;
import org.concordiainternational.competition.ui.generators.CommonFieldFactory;
import org.concordiainternational.competition.ui.list.GenericList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

/**
 * Editing of group information.
 * 
 * @author jflamy
 */
@SuppressWarnings({ "serial" })
public class PublicAddressForm extends Form  {
	
	final private static Logger logger = LoggerFactory.getLogger(PublicAddressForm.class);
	
	Window window = null;
	GenericList<?> parentList = null;

	private SessionData masterData;

	public PublicAddressForm(SessionData masterData) {
		super();
		this.setFormFieldFactory(new CommonFieldFactory(CompetitionApplication.getCurrent()));
		this.masterData = masterData;
		setItemDataSource(masterData.getPublicAddressItem());
        setWriteThrough(true);
        
        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.addComponent(ok);
        footer.addComponent(cancel);
        footer.setVisible(true);
        
        setFooter(footer);
	}
	
	Button display = new Button(Messages.getString("PublicAddress.display", CompetitionApplication.getCurrentLocale()),new Button.ClickListener() {	
		@Override
		public void buttonClick(ClickEvent event) {
			commit();
			display();
		}
	});
	
	Button clear = new Button(Messages.getString("PublicAddress.clear", CompetitionApplication.getCurrentLocale()),new Button.ClickListener() {	
		@Override
		public void buttonClick(ClickEvent event) {
			commit();
			clearDisplay();
		}
	});
	
	Button ok = new Button(Messages.getString("Common.OK", CompetitionApplication.getCurrentLocale()),new Button.ClickListener() {	
		@Override
		public void buttonClick(ClickEvent event) {
			commit();
			display();
			closeWindow();
		}
	});
	
	Button cancel = new Button(Messages.getString("Common.cancel", CompetitionApplication.getCurrentLocale()),new Button.ClickListener() {	
		@Override
		public void buttonClick(ClickEvent event) {
			discard();
			closeWindow();
		}
	});

	
    @Override
    public void setItemDataSource(Item itemDataSource) {
        if (itemDataSource != null) {
            List<Object> orderedProperties = new ArrayList<Object>();
            orderedProperties.add("title");
            orderedProperties.add("message");
            orderedProperties.add("endHour");
            orderedProperties.add("delay");
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


	public Window getWindow() {
		return window;
	}

	public void setWindow(Window window) {
		this.window = window;
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
		logger.warn("closeWindow {}",parentList);

		if (window != null) {
			Window parent = window.getParent();
			parent.removeWindow(window);
		}
//		if (parentList != null) {
//			// nothing to do in this case
//		}
	}

}
