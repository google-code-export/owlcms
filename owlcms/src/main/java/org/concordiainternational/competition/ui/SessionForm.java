/**
 * 
 */
package org.concordiainternational.competition.ui;

import java.util.ArrayList;
import java.util.List;

import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.generators.CommonFieldFactory;
import org.concordiainternational.competition.ui.list.GenericList;
import org.concordiainternational.competition.utils.ItemAdapter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

/**
 * Editing of Competition Session (aka Group) information.
 * 
 * @author jflamy
 */
@SuppressWarnings({ "serial" })
public class SessionForm extends Form  {
	
	final private static Logger logger = LoggerFactory.getLogger(SessionForm.class);
	
	Window window = null;
	GenericList<?> parentList = null;
	private Item item;

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
			Object object = ItemAdapter.getObject(item);
			Session hbnSession = CompetitionApplication.getCurrent().getHbnSession();
			hbnSession.merge(object);
			hbnSession.flush();
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
    	item = newDataSource;
        if (newDataSource != null) {
            List<Object> orderedProperties = new ArrayList<Object>();
            orderedProperties.add("name");
            orderedProperties.add("weighInTime");
            orderedProperties.add("competitionTime");
            orderedProperties.add("platform");
            orderedProperties.add("categories");
            orderedProperties.add("announcer");
            orderedProperties.add("marshall");
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


	@Override
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
		if (parentList != null) {
			// this could be improved, but little gain as this function is called once per competition session only.
			if (parentList instanceof SessionList) {
				// kludge to force update of editable table;
				// need to investigate why this is happening even though we are passing the table item.
				parentList.toggleEditable();
				parentList.toggleEditable();
			} else {
				parentList.refresh();
			}
		}
	}

}
