/**
 * 
 */
package org.concordiainternational.competition.ui;

import java.util.ArrayList;
import java.util.List;

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.generators.CommonFieldFactory;
import org.concordiainternational.competition.ui.list.GenericList;

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
public class SessionForm extends Form  {
	
	Window window = null;
	GenericList<CompetitionSession> parentList = null;


	public SessionForm() {
		super();
		this.setFormFieldFactory(new CommonFieldFactory(CompetitionApplication.getCurrent()));
		
        setWriteThrough(true);
        
        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.addComponent(ok);
        footer.addComponent(cancel);
        footer.setVisible(true);
        
        setFooter(footer);
	}
	
	Button ok = new Button(Messages.getString("Common.OK", CompetitionApplication.getCurrentLocale()),new Button.ClickListener() {	
		@Override
		public void buttonClick(ClickEvent event) {
			commit();
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
    public void setItemDataSource(Item newDataSource) {
        if (newDataSource != null) {
            List<Object> orderedProperties = new ArrayList<Object>();
            orderedProperties.add("name");
            orderedProperties.add("weighInTime");
            orderedProperties.add("competitionTime");
            orderedProperties.add("platform");
            orderedProperties.add("categories");
            orderedProperties.add("announcer");
            orderedProperties.add("timeKeeper");
            orderedProperties.add("technicalController");
            orderedProperties.add("referee1");
            orderedProperties.add("referee2");
            orderedProperties.add("referee3");
            orderedProperties.add("jury");
            super.setItemDataSource(newDataSource, orderedProperties);
            getFooter().setVisible(true);
        } else {
            super.setItemDataSource(null);
            getFooter().setVisible(false);
        }
    }


	public Window getWindow() {
		return window;
	}

	public void setWindow(Window window) {
		this.window = window;
	}


	public GenericList<CompetitionSession> getParentList() {
		return parentList;
	}


	public void setParentList(GenericList<CompetitionSession> parentList) {
		this.parentList = parentList;
	}


	/**
	 * 
	 */
	private void closeWindow() {
		if (window != null) {
			Window parent = window.getParent();
			parent.removeWindow(window);
		}
		if (parentList != null) {
			parentList.toggleEditable();
			parentList.toggleEditable();
		}
	}

}
