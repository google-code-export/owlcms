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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CompetitionSession implements Serializable {

    private static final long serialVersionUID = -7744027515867237334L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String name;
    Date weighInTime;
    Date competitionTime;

    @ManyToOne
    Platform platform;

    @ManyToMany
    Set<Category> categories;

    // group is the property in Lifter that is the opposite of CompetitionSession.lifters
    @OneToMany(mappedBy = "competitionSession")
    Set<Lifter> lifters;

    public CompetitionSession() {
    }

    public CompetitionSession(String groupName, Date weighin, Date competition) {
        this.name = groupName;
        this.setWeighInTime(weighin);
        this.setCompetitionTime(competition);
    }

    public CompetitionSession(String groupName) {
        this.name = groupName;
        final Date now = Calendar.getInstance().getTime();
        this.setWeighInTime(now);
        this.setCompetitionTime(now);
    }

    /**
     * @return the set of categories for the Group
     */
    public Set<Category> getCategories() {
        return categories;
    }

    /**
     * @return the competition time
     */
    public Date getCompetitionTime() {
        return competitionTime;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the platformName on which group will be lifting
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * @return the weigh-in time (two hours before competition, normally)
     */
    public Date getWeighInTime() {
        return weighInTime;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    /**
     * @param c
     *            the competition time to set
     */
    public void setCompetitionTime(Date c) {
        this.competitionTime = c;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param platformName
     *            the platformName to set
     */
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    /**
     * @param w
     *            the weigh-in time to set
     */
    public void setWeighInTime(Date w) {
        this.weighInTime = w;
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
        CompetitionSession other = (CompetitionSession) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    public Set<Lifter> getLifters() {
        return lifters;
    }

    public void deleteLifters(HbnSessionManager hbnSessionManager) {
        final Session session = hbnSessionManager.getHbnSession();
        for (Lifter curLifter : getLifters()) {
            session.delete(curLifter);
        }
        session.flush();
    }

    public void setLifters(Set<Lifter> lifters) {
        this.lifters = lifters;
    }

    @SuppressWarnings("unchecked")
    static public List<CompetitionSession> getAll() {
        return CompetitionApplication.getCurrent().getHbnSession().createCriteria(CompetitionSession.class).list();
    }

}
