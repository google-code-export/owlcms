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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Category other = (Category) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    static public List<Category> getAll() {
        return CompetitionApplication.getCurrent().getHbnSession().createCriteria(Category.class).list();
    }
}
