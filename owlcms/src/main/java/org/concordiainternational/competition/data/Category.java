/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Category implements Serializable {
    private static final long serialVersionUID = -2877573582040039013L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String name;
    // @Enumerated(EnumType.STRING)
    private String gender;
    Double minimumWeight; // inclusive
    Double maximumWeight; // exclusive
    Boolean active;

    public Boolean getActive() {
        return active;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Category() {
    }

    public Category(String name, Double minimumWeight, Double maximumWeight, String string, Boolean active) {
        this.setName(name);
        this.setMinimumWeight(minimumWeight);
        this.setMaximumWeight(maximumWeight);
        this.setGender(string);
        this.setActive(active);
    }

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the maximumWeight
     */
    public Double getMaximumWeight() {
        return maximumWeight;
    }

    /**
     * @return the minimumWeight
     */
    public Double getMinimumWeight() {
        return minimumWeight;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param string
     *            the gender to set
     */
    public void setGender(String string) {
        this.gender = string;
    }

    /**
     * @param maximumWeight
     *            the maximumWeight to set
     */
    public void setMaximumWeight(Double maximumWeight) {
        this.maximumWeight = maximumWeight;
    }

    /**
     * @param minimumWeight
     *            the minimumWeight to set
     */
    public void setMinimumWeight(Double minimumWeight) {
        this.minimumWeight = minimumWeight;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public static void insertStandardCategories(Session sess, Locale locale) {
        if (sess.createCriteria(Category.class).list().size() == 0) {
            sess.save(new Category(Messages.getString("Category.f40", locale), 0.0, 40.0, Gender.F.toString(), false)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.f44", locale), 0.0, 44.0, Gender.F.toString(), false)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.f48", locale), 0.0, 48.0, Gender.F.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.f53", locale), 48.0, 53.0, Gender.F.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.f58", locale), 53.0, 58.0, Gender.F.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.f63", locale), 58.0, 63.0, Gender.F.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.f69", locale), 63.0, 69.0, Gender.F.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(
                    Messages.getString("Category.fgt69", locale), 69.0, 999.0, Gender.F.toString(), false)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.f75", locale), 69.0, 75.0, Gender.F.toString(), true)); //$NON-NLS-1$
            sess
                    .save(new Category(
                            Messages.getString("Category.fgt75", locale), 75.0, 999.0, Gender.F.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.m46", locale), 0.0, 46.0, Gender.M.toString(), false)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.m51", locale), 0.0, 51.0, Gender.M.toString(), false)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.m56", locale), 0.0, 56.0, Gender.M.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.m62", locale), 56.0, 62.0, Gender.M.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.m69", locale), 62.0, 69.0, Gender.M.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.m77", locale), 69.0, 77.0, Gender.M.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.m85", locale), 77.0, 85.0, Gender.M.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(
                    Messages.getString("Category.mgt85", locale), 85.0, 999.0, Gender.M.toString(), false)); //$NON-NLS-1$
            sess.save(new Category(Messages.getString("Category.m94", locale), 85.0, 94.0, Gender.M.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(
                    Messages.getString("Category.mgt94", locale), 94.0, 999.0, Gender.M.toString(), false)); //$NON-NLS-1$
            sess
                    .save(new Category(
                            Messages.getString("Category.m105", locale), 94.0, 105.0, Gender.M.toString(), true)); //$NON-NLS-1$
            sess.save(new Category(
                    Messages.getString("Category.mgt105", locale), 105.0, 999.0, Gender.M.toString(), true)); //$NON-NLS-1$
        }
    }


    @SuppressWarnings("unchecked")
    static public List<Category> getAll() {
        return CompetitionApplication.getCurrent().getHbnSession().createCriteria(Category.class).list();
    }

	@Override
	public String toString() {
		return name+"_"+hashCode()+"_"+active;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((active == null) ? 0 : active.hashCode());
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result
				+ ((maximumWeight == null) ? 0 : maximumWeight.hashCode());
		result = prime * result
				+ ((minimumWeight == null) ? 0 : minimumWeight.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		if (active == null) {
			if (other.active != null)
				return false;
		} else if (!active.equals(other.active))
			return false;
		if (gender == null) {
			if (other.gender != null)
				return false;
		} else if (!gender.equals(other.gender))
			return false;
		if (maximumWeight == null) {
			if (other.maximumWeight != null)
				return false;
		} else if (!maximumWeight.equals(other.maximumWeight))
			return false;
		if (minimumWeight == null) {
			if (other.minimumWeight != null)
				return false;
		} else if (!minimumWeight.equals(other.minimumWeight))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
    
    
}
