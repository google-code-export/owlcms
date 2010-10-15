package org.concordiainternational.competition.data;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.vaadin.data.hbnutil.HbnContainer;

@SuppressWarnings("serial")
public class CategoryContainer extends HbnContainer<Category> {

	private boolean activeOnly = false;

	/**
     * Default constructor, shows all athletes.
     * 
     * @param application
     */
    public CategoryContainer(HbnSessionManager sessMgr) {
        super(Category.class, sessMgr);
    }
    
    /**
     * Alternate constructor that shows only athletes that have weighed-in.
     * 
     * @param application
     * @param excludeNotWeighed
     */
    public CategoryContainer(HbnSessionManager sessMgr, boolean activeOnly) {
    	this(sessMgr);
        this.activeOnly  = activeOnly;
    }

    /*
     * This class adds filtering criteria other than simple textual filtering as
     * implemented by the Filterable interface. (non-Javadoc)
     * 
     * @see
     * com.vaadin.data.hbnutil.HbnContainer#addSearchCriteria(org.hibernate.
     * Criteria)
     */
    @Override
    public Criteria addSearchCriteria(Criteria criteria) {
    	if (activeOnly) {
    		criteria.add(Restrictions.eq("active", Boolean.TRUE));
    	}
		return criteria;
    }
}
