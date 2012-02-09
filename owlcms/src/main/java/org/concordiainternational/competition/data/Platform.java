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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.sound.sampled.Mixer;

import org.concordiainternational.competition.decision.Speakers;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.LiftList;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data on a lifting site.
 * <p>
 * Groups are associated with a lifting platformName.
 * </p>
 * <p>
 * Projectors and officials are associated with a lifting platformName so there
 * is no need to refresh their setup during a competition. The name of the
 * platformName is used as a key in the ServletContext so other sessions and
 * other kinds of pages (such as JSP) can locate the information about that
 * platformName. See in particular the {@link LiftList#updateTable()} method
 * </p>
 * 
 * @author jflamy
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Platform implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(Platform.class);

    private static final long serialVersionUID = -6871042180395572184L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String name;
    
    /**
     * true if the NEC LED display is attached to the platform.
     */
    Boolean hasDisplay = false;
    
    /**
     * true if the referee use this application to give decisions, and
     * decision lights need to be shown on the attempt and result boards.
     */
    Boolean showDecisionLights = false;

    // collar
    Integer nbC_2_5 = 0;

    // small plates
    Integer nbS_0_5 = 0;
    Integer nbS_1 = 0;
    Integer nbS_1_5 = 0;
    Integer nbS_2 = 0;
    Integer nbS_2_5 = 0;
    Integer nbS_5 = 0;

    // large plates
    Integer nbL_2_5 = 0;
    Integer nbL_5 = 0;
    Integer nbL_10 = 0;
    Integer nbL_15 = 0;
    Integer nbL_20 = 0;
    Integer nbL_25 = 0;

    // bar
    Integer officialBar = 0;
    Integer lightBar = 0;

	String mixerName = "";
    transient private Mixer mixer;

    public Platform() {
    }

    public Platform(String name) {
        this.setName(name);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    static public List<Platform> getAll() {
        final List<Platform> list = CompetitionApplication.getCurrent().getHbnSession().createCriteria(Platform.class).addOrder(
            Order.asc("name")) //$NON-NLS-1$
                .list();
        return list;
    }

    @SuppressWarnings("unchecked")
    static public Platform getByName(String name) {
        final List<Platform> list = CompetitionApplication.getCurrent().getHbnSession().createCriteria(Platform.class).add(
            Restrictions.eq("name", name)) //$NON-NLS-1$
                .list();
        if (list.size() == 1) {
            final Platform platform = (Platform) list.get(0);
            platform.setMixerName(platform.mixerName);
			return platform;
        } else {
            return null;
        }
    }

    public Boolean getHasDisplay() {
        return isHasDisplay();
    }

    public Boolean isHasDisplay() {
        if (hasDisplay == null) return false;
        return hasDisplay;
    }

    public void setHasDisplay(Boolean hasDisplay) {
        this.hasDisplay = hasDisplay;
    }

    public static int getSize() {
        return getAll().size();
    }

    @Override
    public String toString() {
        return name + "_" + System.identityHashCode(this); //$NON-NLS-1$
    }

    public Integer getNbC_2_5() {
        if (nbC_2_5 == null) return 0;
        return nbC_2_5;
    }

    public void setNbC_2_5(Integer nbC_2_5) {
        this.nbC_2_5 = nbC_2_5;
    }

    public Integer getNbS_0_5() {
        if (nbS_0_5 == null) return 0;
        return nbS_0_5;
    }

    public void setNbS_0_5(Integer nbS_0_5) {
        this.nbS_0_5 = nbS_0_5;
    }

    public Integer getNbS_1() {
        if (nbS_1 == null) return 0;
        return nbS_1;
    }

    public void setNbS_1(Integer nbS_1) {
        this.nbS_1 = nbS_1;
    }

    public Integer getNbS_1_5() {
        if (nbS_1_5 == null) return 0;
        return nbS_1_5;
    }

    public void setNbS_1_5(Integer nbS_1_5) {
        this.nbS_1_5 = nbS_1_5;
    }

    public Integer getNbS_2() {
        if (nbS_2 == null) return 0;
        return nbS_2;
    }

    public void setNbS_2(Integer nbS_2) {
        this.nbS_2 = nbS_2;
    }

    public Integer getNbS_2_5() {
        if (nbS_2_5 == null) return 0;
        return nbS_2_5;
    }

    public void setNbS_2_5(Integer nbS_2_5) {
        this.nbS_2_5 = nbS_2_5;
    }

    public Integer getNbS_5() {
        if (nbS_5 == null) return 0;
        return nbS_5;
    }

    public void setNbS_5(Integer nbS_5) {
        this.nbS_5 = nbS_5;
    }

    public Integer getNbL_2_5() {
        if (nbL_2_5 == null) return 0;
        return nbL_2_5;
    }

    public void setNbL_2_5(Integer nbL_2_5) {
        this.nbL_2_5 = nbL_2_5;
    }

    public Integer getNbL_5() {
        if (nbL_5 == null) return 0;
        return nbL_5;
    }

    public void setNbL_5(Integer nbL_5) {
        this.nbL_5 = nbL_5;
    }

    public Integer getNbL_10() {
        if (nbL_10 == null) return 0;
        return nbL_10;
    }

    public void setNbL_10(Integer nbL_10) {
        this.nbL_10 = nbL_10;
    }

    public Integer getNbL_15() {
        if (nbL_15 == null) return 0;
        return nbL_15;
    }

    public void setNbL_15(Integer nbL_15) {
        this.nbL_15 = nbL_15;
    }

    public Integer getNbL_20() {
        if (nbL_20 == null) return 0;
        return nbL_20;
    }

    public void setNbL_20(Integer nbL_20) {
        this.nbL_20 = nbL_20;
    }

    public Integer getNbL_25() {
        if (nbL_25 == null) return 0;
        return nbL_25;
    }

    public void setNbL_25(Integer nbL_25) {
        this.nbL_25 = nbL_25;
    }

    public Integer getOfficialBar() {
        if (lightBar == null) return 0;
        return officialBar;
    }

    public void setOfficialBar(Integer officialBar) {
        this.officialBar = officialBar;
    }

    public Integer getLightBar() {
        if (lightBar == null) return 0;
        return lightBar;
    }

    public void setLightBar(Integer lightBar) {
        this.lightBar = lightBar;
    }

	public Boolean getShowDecisionLights() {
		return showDecisionLights == null ? false : showDecisionLights;
	}

	public void setShowDecisionLights(Boolean showDecisionLights) {
		this.showDecisionLights = showDecisionLights;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		Platform other = (Platform) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Mixer getMixer() {
		return mixer;
	}

	public void setMixer(Mixer mixer) {
		this.mixer = mixer;
	}

	/**
	 * @return the mixerName
	 */
	public String getMixerName() {
		return mixerName;
	}

	/**
	 * @param mixerName the mixerName to set
	 */
	public void setMixerName(String mixerName) {
		this.mixerName = mixerName;
		setMixer(null);
		List<Mixer> mixers = Speakers.getOutputs();
		for (Mixer curMixer: mixers) {
			if (curMixer.getMixerInfo().getName().equals(mixerName)) {
				setMixer(curMixer);
				logger.debug("Platform: {}: changing mixer to {}",this.name,mixerName);
				break;
			}
		}
	}
    
}
