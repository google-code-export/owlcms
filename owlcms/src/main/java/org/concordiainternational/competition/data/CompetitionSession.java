/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CompetitionSession implements Serializable, Comparable<Object> {

    private static final long serialVersionUID = -7744027515867237334L;
    // @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(CompetitionSession.class);

    @SuppressWarnings("unchecked")
    static public List<CompetitionSession> getAll() {
        return CompetitionApplication.getCurrent().getHbnSession().createCriteria(CompetitionSession.class).list();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String name;
    Date weighInTime;
    Date competitionTime;

    String announcer;
    String marshall;
    String timeKeeper;
    String technicalController;
    String referee1;
    String referee2;
    String referee3;
    String jury;

    @Transient
    final transient String competitionShortDateTime = "";
    @Transient
    final transient String weighInShortDateTime = "";

    @ManyToOne(optional = true)
    Platform platform;

    @ManyToMany(fetch = FetchType.EAGER)
    Set<Category> categories;
    // group is the property in Lifter that is the opposite of CompetitionSession.lifters
    @OneToMany(mappedBy = "competitionSession")
    // ,fetch=FetchType.EAGER)
    Set<Lifter> lifters;

    private static final SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public CompetitionSession() {
    }

    public CompetitionSession(String groupName) {
        this.name = groupName;
        final Date now = Calendar.getInstance().getTime();
        this.setWeighInTime(now);
        this.setCompetitionTime(now);
    }

    public CompetitionSession(String groupName, Date weighin, Date competition) {
        this.name = groupName;
        this.setWeighInTime(weighin);
        this.setCompetitionTime(competition);
    }

    @Override
    public int compareTo(Object arg0) {
        CompetitionSession other = (CompetitionSession) arg0;
        if (this.weighInTime == null && other.weighInTime == null)
            return 0;
        if (this.weighInTime == null)
            return -1;
        if (other.weighInTime == null)
            return 1;

        if (this.name == null && other.name == null)
            return 0;
        if (this.name == null)
            return -1;
        if (other.name == null)
            return 1;
        return this.name.compareTo(other.name);
    }

    public void deleteLifters(HbnSessionManager hbnSessionManager) {
        final Session session = hbnSessionManager.getHbnSession();
        for (Lifter curLifter : getLifters()) {
            session.delete(curLifter);
        }
        session.flush();
    }

    /*
     * (non-Javadoc)
     * 
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
        CompetitionSession other = (CompetitionSession) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    public String getAnnouncer() {
        return announcer;
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
    public String getCompetitionShortDateTime() {
        String formatted = "";
        try {
            formatted = sFormat.format(competitionTime);
        } catch (Exception e) {
            LoggerUtils.errorException(logger, e);
        }
        return formatted;
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

    public String getJury() {
        return jury;
    }

    public Set<Lifter> getLifters() {
        return lifters;
    }

    public String getMarshall() {
        return marshall;
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

    public String getReferee1() {
        return referee1;
    }

    public String getReferee2() {
        return referee2;
    }

    public String getReferee3() {
        return referee3;
    }

    public String getTechnicalController() {
        return technicalController;
    }

    public String getTimeKeeper() {
        return timeKeeper;
    }

    /**
     * @return the weigh-in time (two hours before competition, normally)
     */
    public String getWeighInShortDateTime() {
        String formatted = "";
        try {
            formatted = sFormat.format(weighInTime);
        } catch (Exception e) {
            LoggerUtils.errorException(logger, e);
        }
        return formatted;
    }

    /**
     * @return the weigh-in time (two hours before competition, normally)
     */
    public Date getWeighInTime() {
        return weighInTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    public void setAnnouncer(String announcer) {
        this.announcer = announcer;
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

    public void setJury(String jury) {
        this.jury = jury;
    }

    public void setLifters(Set<Lifter> lifters) {
        this.lifters = lifters;
    }

    public void setMarshall(String announcer) {
        this.marshall = announcer;
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

    public void setReferee1(String referee1) {
        this.referee1 = referee1;
    }

    public void setReferee2(String referee2) {
        this.referee2 = referee2;
    }

    public void setReferee3(String referee3) {
        this.referee3 = referee3;
    }

    public void setTechnicalController(String technicalController) {
        this.technicalController = technicalController;
    }

    public void setTimeKeeper(String timeKeeper) {
        this.timeKeeper = timeKeeper;
    }

    /**
     * @param w
     *            the weigh-in time to set
     */
    public void setWeighInTime(Date w) {
        this.weighInTime = w;
    }

}
