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
    	logger.warn("getResultTemplateFileName {}",resultTemplateFileName);
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
}
