/**
 * 
 */
package org.concordiainternational.competition.ui;

import java.util.ArrayList;
import java.util.List;

import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.generators.CommonFieldFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;

/**
 * Editing of group information.
 * 
 * @author jflamy
 */
@SuppressWarnings({ "serial" })
public class SessionForm extends Form  {

	public SessionForm() {
		super();
		this.setFormFieldFactory(new CommonFieldFactory(CompetitionApplication.getCurrent()));
		
        setWriteThrough(false);
        
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
		}
	});
	
	Button cancel = new Button(Messages.getString("Common.cancel", CompetitionApplication.getCurrentLocale()),new Button.ClickListener() {	
		@Override
		public void buttonClick(ClickEvent event) {
			discard();
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
            setReadOnly(true);
            getFooter().setVisible(true);
        } else {
            super.setItemDataSource(null);
            getFooter().setVisible(false);
        }
    }



}
