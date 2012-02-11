/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class Competition implements Serializable {
    private static final long serialVersionUID = -2817516132425565754L;
    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Competition.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String competitionName;
    String competitionSite;
    Date competitionDate;
    String competitionCity;
    String competitionOrganizer;
    String resultTemplateFileName;

    Integer invitedIfBornBefore;
    Boolean masters;

    String federation;
    String federationAddress;
    String federationWebSite;
    String federationEMail;

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionSite() {
        return competitionSite;
    }

    public void setCompetitionSite(String competitionSite) {
        this.competitionSite = competitionSite;
    }

    public Date getCompetitionDate() {
        return competitionDate;
    }

    public void setCompetitionDate(Date competitionDate) {
        this.competitionDate = competitionDate;
    }

    public String getCompetitionCity() {
        return competitionCity;
    }

    public void setCompetitionCity(String competitionCity) {
        this.competitionCity = competitionCity;
    }

    public Boolean getMasters() {
        return masters;
    }

    public void setMasters(Boolean masters) {
        this.masters = masters;
    }

    public String getFederation() {
        return federation;
    }

    public void setFederation(String federation) {
        this.federation = federation;
    }

    public String getFederationAddress() {
        return federationAddress;
    }

    public void setFederationAddress(String federationAddress) {
        this.federationAddress = federationAddress;
    }

    public String getFederationWebSite() {
        return federationWebSite;
    }

    public void setFederationWebSite(String federationWebSite) {
        this.federationWebSite = federationWebSite;
    }

    public String getFederationEMail() {
        return federationEMail;
    }

    public void setFederationEMail(String federationEMail) {
        this.federationEMail = federationEMail;
    }

    public Long getId() {
        return id;
    }

    public Integer getInvitedIfBornBefore() {
        if (invitedIfBornBefore == null) return 0;
        return invitedIfBornBefore;
    }

    public void setInvitedIfBornBefore(Integer invitedIfBornBefore) {
        this.invitedIfBornBefore = invitedIfBornBefore;
    }

    public String getCompetitionOrganizer() {
        return competitionOrganizer;
    }

    public void setCompetitionOrganizer(String competitionOrganizer) {
        this.competitionOrganizer = competitionOrganizer;
    }

    public String getResultTemplateFileName() {
    	//logger.debug("getResultTemplateFileName {}",resultTemplateFileName);
        if (resultTemplateFileName != null && new File(resultTemplateFileName).exists()) {
            return resultTemplateFileName;
        } else {
            return CompetitionApplication.getCurrent().getContext().getBaseDirectory()
                + "/WEB-INF/classes/templates/team/TeamResultSheetTemplate.xls";
        }
    }

    public void setResultTemplateFileName(String resultTemplateFileName) {
        this.resultTemplateFileName = resultTemplateFileName;
    }

    static Integer invitedThreshold = null;

    @SuppressWarnings("unchecked")
    public static int invitedIfBornBefore() {
        if (invitedThreshold != null) return invitedThreshold;
        final CompetitionApplication currentApp = CompetitionApplication.getCurrent();
        final Session hbnSession = currentApp.getHbnSession();
        List<Competition> competitions = hbnSession.createCriteria(Competition.class).list();
        if (competitions.size() > 0) {
            final Competition competition = competitions.get(0);
            invitedThreshold = competition.getInvitedIfBornBefore();
        }
        return invitedThreshold;
    }

    @SuppressWarnings("unchecked")
    static public List<Competition> getAll() {
        final List<Competition> list = CompetitionApplication.getCurrent().getHbnSession().createCriteria(Competition.class).list();
        return list;
    }

    static Boolean isMasters = null;

    @SuppressWarnings("unchecked")
    public static boolean isMasters() {
        if (isMasters != null) return isMasters;
        final CompetitionApplication currentApp = CompetitionApplication.getCurrent();
        final Session hbnSession = currentApp.getHbnSession();
        List<Competition> competitions = hbnSession.createCriteria(Competition.class).list();
        if (competitions.size() > 0) {
            final Competition competition = competitions.get(0);
            isMasters = competition.getMasters();
            if (isMasters == null) return false; // junit database does not have
                                                 // this attribute set.
        }
        return isMasters;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((competitionName == null) ? 0 : competitionName.hashCode());
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
		Competition other = (Competition) obj;
		if (competitionName == null) {
			if (other.competitionName != null)
				return false;
		} else if (!competitionName.equals(other.competitionName))
			return false;
		return true;
	}
    
    
}
