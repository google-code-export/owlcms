/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.data;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.concordiainternational.competition.data.lifterSort.LifterSorter.Ranking;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.utils.Coefficients;
import org.concordiainternational.competition.utils.EventHelper;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.concordiainternational.competition.utils.Notifier;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OptimisticLockType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.EventRouter;
import com.vaadin.event.MethodEventSource;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * This class stores all the information related to a particular participant.
 * <p>
 * All persistent properties are managed by Java Persistance annotations.
 * "Field" access mode is used, meaning that it is the values of the fields that
 * are stored, and not the values returned by the getters. Note that it is often
 * necessary to know when a value has been captured or not -- this is why values
 * are stored as Integers or Doubles, so that we can use null to indicate that a
 * value has not been captured.
 * </p>
 * <p>
 * This allows us to use the getters to return the values as they will be
 * displayed by the application
 * </p>
 * <p>
 * Computed fields are defined as final transient properties and marked as
 * @Transient; the only reason for this is so the JavaBeans introspection
 *             mechanisms find them.
 *             </p>
 *             <p>
 *             This class uses events to notify interested user interface
 *             components that fields or computed values have changed. In this
 *             way the user interface does not have to know that the category
 *             field on the screen is dependent on the bodyweight and the gender
 *             -- all the dependency logic is kept at the business object level.
 *             </p>
 * 
 * @author jflamy
 * 
 */
@Entity
@org.hibernate.annotations.Entity(optimisticLock = OptimisticLockType.VERSION)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Lifter implements MethodEventSource, Notifier {

    /**
     * Lifter events all derive from this.
     */
    public class UpdateEvent extends EventObject {
        private static final long serialVersionUID = -126644150054472005L;
        private List<String> propertyIds;

        /**
         * Constructs a new event with a specified source component.
         * 
         * @param source
         *            the source component of the event.
         * @param propertyIds
         *            that have been updated.
         */
        public UpdateEvent(Lifter source, String... propertyIds) {
            super(source);
            this.propertyIds = Arrays.asList(propertyIds);
        }

        public List<String> getPropertyIds() {
            return propertyIds;
        }

    }

    /**
     * Listener interface for receiving <code>Lifter.UpdateEvent</code>s.
     */
    public interface UpdateEventListener extends java.util.EventListener {

        /**
         * This method will be invoked when a Lifter.UpdateEvent is fired.
         * 
         * @param updateEvent
         *            the event that has occured.
         */
        public void updateEvent(Lifter.UpdateEvent updateEvent);
    }

    private static final long serialVersionUID = -7046330458901785754L;

    private static final Logger logger = LoggerFactory.getLogger(Lifter.class);

    static CategoryLookup categoryLookup = null;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long version;

    Integer lotNumber = null;
    Integer startNumber = null;
    String firstName = ""; //$NON-NLS-1$
    String lastName = ""; //$NON-NLS-1$
    String club = ""; //$NON-NLS-1$

    String gender = ""; //$NON-NLS-1$
    Integer ageGroup = 0;
    Integer birthDate = 1900;
    Double bodyWeight = null;

    String membership = ""; //$NON-NLS-1$
    @ManyToOne
    CompetitionSession competitionSession;

    // This is brute force, but having embedded classes does not bring much
    // and we don't want joins or other such logic for the lifter card.
    // Since the lifter card is 6 x 4 items, we take the simple route.

    // Note: we use Strings because we need to distinguish actually entered
    // values (such as 0)
    // from empty cells. Using Integers or Doubles would work as well, but many
    // people want to type
    // "-" or other things in the cells, so Strings are actually easier.

    @ManyToOne
    Category registrationCategory = null;
    String snatch1Declaration;
    String snatch1Change1;
    String snatch1Change2;
    String snatch1ActualLift;
    Date snatch1LiftTime;

    String snatch2Declaration;
    String snatch2Change1;
    String snatch2Change2;
    String snatch2ActualLift;
    Date snatch2LiftTime;

    String snatch3Declaration;
    String snatch3Change1;
    String snatch3Change2;
    String snatch3ActualLift;
    Date snatch3LiftTime;

    String cleanJerk1Declaration;
    String cleanJerk1Change1;
    String cleanJerk1Change2;
    String cleanJerk1ActualLift;
    Date cleanJerk1LiftTime;

    String cleanJerk2Declaration;
    String cleanJerk2Change1;
    String cleanJerk2Change2;
    String cleanJerk2ActualLift;
    Date cleanJerk2LiftTime;

    String cleanJerk3Declaration;
    String cleanJerk3Change1;
    String cleanJerk3Change2;
    String cleanJerk3ActualLift;
    Date cleanJerk3LiftTime;

    Integer snatchRank;
    Integer cleanJerkRank;
    Integer totalRank;
    Integer sinclairRank;
    Integer customRank;

    Float snatchPoints;
    Float cleanJerkPoints;
    Float totalPoints; // points based on totalRank
    Float sinclairPoints;
    Float customPoints;

    Integer teamSinclairRank;
    Integer teamSnatchRank;
    Integer teamCleanJerkRank;

    Integer teamTotalRank;
    Integer teamCombinedRank;
    
    Boolean teamMember = true; // false if substitute; note that we consider null to be true.;
    Integer qualifyingTotal = 0;
    
    /*
     * Computed properties. We create them here because we want the
     * corresponding accessors to be discovered by introspection. Setters are
     * not defined (the fields are final). Getters perform the required
     * computation.
     * 
     * BEWARE: the variables defined here must NOT be used -- you must be able
     * to comment them out and get no compilation errors. All the code should
     * use the getters only.
     */
    @Transient
    final transient String snatch1AutomaticProgression = ""; //$NON-NLS-1$

    @Transient
    final transient String snatch2AutomaticProgression = ""; //$NON-NLS-1$
    @Transient
    final transient String snatch3AutomaticProgression = ""; //$NON-NLS-1$

    @Transient
    final transient String cleanJerk1AutomaticProgression = ""; //$NON-NLS-1$
    @Transient
    final transient String cleanJerk2AutomaticProgression = ""; //$NON-NLS-1$

    @Transient
    final transient String cleanJerk3AutomaticProgression = ""; //$NON-NLS-1$
    @Transient
    final transient Integer bestSnatch = 0;
    @Transient
    final transient Integer bestCleanJerk = 0;

    @Transient
    final transient Integer total = 0;
    @Transient
    final transient Integer attemptsDone = 0;

    @Transient
    final transient Integer snatchAttemptsDone = 0;
    @Transient
    final transient Integer cleanJerkAttemptsDone = 0;
    @Transient
    Date lastLiftTime = null;
    @Transient
    final transient Integer nextAttemptRequestedWeight = 0;
    /*
     * Non-persistent properties. These properties are used during computations,
     * but need not be stored in the database
     */
    @Transient
    Integer liftOrderRank = 0;
    @Transient
    Integer resultOrderRank = 0;
    @Transient
    boolean currentLifter = false;
    @Transient
    boolean forcedAsCurrent = false;
    /*
     * Transient fields that have no relevance to the persistent state of a
     * lifter All framework-related and pattern-related constructs go here.
     */
    @Transient
    private EventRouter eventRouter;

	private Double customScore;

    /**
     * This method is the Java object for the method in the Listener interface.
     * It allows the framework to know how to pass the event information.
     */
    private static final Method LIFTER_EVENT_METHOD = EventHelper.findMethod(UpdateEvent.class, // when
        // receiving
        // this
        // type
        // of
        // event
        UpdateEventListener.class, // an object implementing this interface...
        "updateEvent"); // ... will be called with this method. //$NON-NLS-1$;

    @SuppressWarnings("unchecked")
    static public List<Lifter> getAll() {
        return CompetitionApplication.getCurrent().getHbnSession().createCriteria(Lifter.class).list();
    }

    public static boolean isEmpty(String value) {
        return (value == null) || value.trim().isEmpty();
    }

    public static int zeroIfInvalid(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public Lifter() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.MethodEventSource#addListener(java.lang.Class,
     * java.lang.Object, java.lang.reflect.Method)
     */
    @Override
	@SuppressWarnings({ "rawtypes" })
    public void addListener(Class eventType, Object object, Method method) {
        getEventRouter().addListener(eventType, object, method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.MethodEventSource#addListener(java.lang.Class,
     * java.lang.Object, java.lang.String)
     */
    @Override
	@SuppressWarnings({ "rawtypes" })
    public void addListener(Class eventType, Object object, String methodName) {
        getEventRouter().addListener(eventType, object, methodName);
    }

    /**
     * Register a new Lifter.Listener object with a Lifter in order to be
     * informed of updates.
     * 
     * @param listener
     */
    @Override
	public void addListener(EventListener listener) {
        getEventRouter().addListener(UpdateEvent.class, listener, LIFTER_EVENT_METHOD);
    }

 

    public void failedLift() {
        logger.debug("{}", this); //$NON-NLS-1$
        final String weight = Integer.toString(-getNextAttemptRequestedWeight());
        doLift(weight);
    }

    /**
     * @return the ageGroup
     */
    public Integer getAgeGroup() {
        if (this.birthDate == null || this.gender == null || this.gender.trim().isEmpty()) {
            return null;
        }
        int year1 = Calendar.getInstance().get(Calendar.YEAR);
        final int age = year1 - this.birthDate;
        if (age < 30) {
            return null;
        }
        int ageGroup1 = (int) (Math.ceil(age / 5) * 5);
        if (this.getGender().equals("F") && ageGroup1 >= 65) { //$NON-NLS-1$
            return 65;
        }
        if (this.getGender().equals("M") && ageGroup1 >= 80) { //$NON-NLS-1$
            return 80;
        }
        // normal case
        return ageGroup1;
    }

    /**
     * @return the ageGroup
     */
    public String getMastersAgeGroupInterval() {
        Integer ageGroup1 = getAgeGroup();
        if (this.getGender().equals("F") && ageGroup1 >= 65) { //$NON-NLS-1$
            return "65+";
        }
        if (this.getGender().equals("M") && ageGroup1 >= 80) { //$NON-NLS-1$
            return "80+";
        }
        return ageGroup1 + "-" + (ageGroup1 + 4);
    }

    public String getMastersGenderAgeGroupInterval() {
        Integer ageGroup1 = getAgeGroup();
        if (this.getGender().equals("F") && ageGroup1 >= 65) { //$NON-NLS-1$
            return "F65+";
        }
        if (this.getGender().equals("M") && ageGroup1 >= 80) { //$NON-NLS-1$
            return "M80+";
        }
        return getGender().toUpperCase() + ageGroup1 + "-" + (ageGroup1 + 4);
    }

    /**
     * @return the attemptsDone
     */
    public Integer getAttemptsDone() {
        return getSnatchAttemptsDone() + getCleanJerkAttemptsDone();
    }

    /**
     * @return the bestCleanJerk
     */
    public Integer getBestCleanJerk() {
        final int cj1 = zeroIfInvalid(cleanJerk1ActualLift);
        final int cj2 = zeroIfInvalid(cleanJerk2ActualLift);
        final int cj3 = zeroIfInvalid(cleanJerk3ActualLift);
        return max(0, cj1, cj2, cj3);
    }

    public int getBestCleanJerkAttemptNumber() {
        int referenceValue = getBestCleanJerk();
        if (referenceValue > 0) {
            if (zeroIfInvalid(cleanJerk3ActualLift) == referenceValue) return 6;
            if (zeroIfInvalid(cleanJerk2ActualLift) == referenceValue) return 5;
            if (zeroIfInvalid(cleanJerk1ActualLift) == referenceValue) return 4;
        }
        return 0; // no match - bomb-out.
    }

    public int getBestResultAttemptNumber() {
        int referenceValue = getBestCleanJerk();
        if (referenceValue > 0) {
            if (zeroIfInvalid(cleanJerk3ActualLift) == referenceValue) return 6;
            if (zeroIfInvalid(cleanJerk2ActualLift) == referenceValue) return 5;
            if (zeroIfInvalid(cleanJerk1ActualLift) == referenceValue) return 4;
        } else {
            if (referenceValue > 0) {
                referenceValue = getBestSnatch();
                if (zeroIfInvalid(snatch3ActualLift) == referenceValue) return 3;
                if (zeroIfInvalid(snatch2ActualLift) == referenceValue) return 2;
                if (zeroIfInvalid(snatch1ActualLift) == referenceValue) return 1;
            }
        }
        return 0; // no match - bomb-out.
    }

    /**
     * @return the bestSnatch
     */
    public Integer getBestSnatch() {
        final int sn1 = zeroIfInvalid(snatch1ActualLift);
        final int sn2 = zeroIfInvalid(snatch2ActualLift);
        final int sn3 = zeroIfInvalid(snatch3ActualLift);
        return max(0, sn1, sn2, sn3);
    }

    public int getBestSnatchAttemptNumber() {
        int referenceValue = getBestSnatch();
        if (referenceValue > 0) {
            if (zeroIfInvalid(snatch3ActualLift) == referenceValue) return 3;
            if (zeroIfInvalid(snatch2ActualLift) == referenceValue) return 2;
            if (zeroIfInvalid(snatch1ActualLift) == referenceValue) return 1;
        }
        return 0; // no match - bomb-out.
    }

    /* *****************************************************************************************
     * Actual persisted properties.
     */
    
    
    /**
     * @return the birthDate
     */
    public Integer getBirthDate() {
        return birthDate;
    };


    /**
     * @return the bodyWeight
     */
    public Double getBodyWeight() {
        return bodyWeight;
    }

    /**
     * @return the category
     */
    public Category getCategory() {
        final CategoryLookup categoryLookup1 = CategoryLookup.getSharedInstance();
        final Category lookup = categoryLookup1.lookup(this.gender, this.bodyWeight);
        // System.err.println("getCategory: "+this.gender+","+this.bodyWeight+" = "+(lookup
        // == null? null: lookup.getName()));
        return lookup;
    };

    /**
     * Compute the body weight at the maximum weight of the lifter's category.
     * Note: for the purpose of this computation, only "official" categories are
     * used as the purpose is to totalRank athletes according to their
     * competition potential.
     * 
     * @return
     */
    public Double getCategorySinclair() {
        Category category = getCategory();
        if (category == null) return 0.0;
        Double categoryWeight = category.getMaximumWeight();
        final Integer total1 = getTotal();
        if (total1 == null || total1 < 0.1) return 0.0;
        if (getGender().equalsIgnoreCase("M")) { //$NON-NLS-1$
            if (categoryWeight < 56.0) {
                categoryWeight = 56.0;
            } else if (categoryWeight > Coefficients.menMaxWeight()) {
                categoryWeight = Coefficients.menMaxWeight();
            }
        } else {
            if (categoryWeight < 48.0) {
                categoryWeight = 48.0;
            } else if (categoryWeight > Coefficients.menMaxWeight()) {
                categoryWeight = Coefficients.womenMaxWeight();
            }
        }
        return getSinclair(categoryWeight);
    };

    public String getCleanJerk1ActualLift() {
        return emptyIfNull(cleanJerk1ActualLift);
    };
    
    public Integer getCleanJerk1AsInteger() {
    	return asInteger(cleanJerk1ActualLift);
    }

	/**
	 * @return
	 */
	protected Integer asInteger(String stringValue) {
		if (stringValue == null) return null;
        try {
        	return Integer.parseInt(stringValue);
        } catch (NumberFormatException nfe) {
        	return null;
        }
	};

    public String getCleanJerk1AutomaticProgression() {
        return "-"; // there is no such thing. //$NON-NLS-1$
    };

    public String getCleanJerk1Change1() {
        return emptyIfNull(cleanJerk1Change1);
    };

    public String getCleanJerk1Change2() {
        return emptyIfNull(cleanJerk1Change2);
    };

    public String getCleanJerk1Declaration() {
        return emptyIfNull(cleanJerk1Declaration);
    };

    public Date getCleanJerk1LiftTime() {
        return cleanJerk1LiftTime;
    }

    public String getCleanJerk2ActualLift() {
        return emptyIfNull(cleanJerk2ActualLift);
    }
    
    public Integer getCleanJerk2AsInteger() {
    	return asInteger(cleanJerk2ActualLift);
    }

    public String getCleanJerk2AutomaticProgression() {
        final int prevVal = zeroIfInvalid(cleanJerk1ActualLift);
        return doAutomaticProgression(prevVal);
    }

    public String getCleanJerk2Change1() {
        return emptyIfNull(cleanJerk2Change1);
    }

    public String getCleanJerk2Change2() {
        return emptyIfNull(cleanJerk2Change2);
    }

    public String getCleanJerk2Declaration() {
        return emptyIfNull(cleanJerk2Declaration);
    }

    public Date getCleanJerk2LiftTime() {
        return cleanJerk2LiftTime;
    }

    public String getCleanJerk3ActualLift() {
        return emptyIfNull(cleanJerk3ActualLift);
    }
    
    public Integer getCleanJerk3AsInteger() {
    	return asInteger(cleanJerk3ActualLift);
    }

    public String getCleanJerk3AutomaticProgression() {
        final int prevVal = zeroIfInvalid(cleanJerk2ActualLift);
        return doAutomaticProgression(prevVal);
    }

    public String getCleanJerk3Change1() {
        return emptyIfNull(cleanJerk3Change1);
    }

    public String getCleanJerk3Change2() {
        return emptyIfNull(cleanJerk3Change2);
    }

    public String getCleanJerk3Declaration() {
        return emptyIfNull(cleanJerk3Declaration);
    }

    public Date getCleanJerk3LiftTime() {
        return cleanJerk3LiftTime;
    }

    /**
     * @return the cleanJerkAttemptsDone
     */
    public Integer getCleanJerkAttemptsDone() {
        // if lifter signals he wont take his remaining tries, a zero is entered
        // further lifts are not counted.
        int attempts = 0;
        if (!isEmpty(cleanJerk1ActualLift)) {
            attempts++;
        } else {
            return attempts;
        }
        if (!isEmpty(cleanJerk2ActualLift)) {
            attempts++;
        } else {
            return attempts;
        }
        if (!isEmpty(cleanJerk3ActualLift)) {
            attempts++;
        } else {
            return attempts;
        }
        return attempts;
    }

    public Float getCleanJerkPoints() {
        if (cleanJerkPoints == null) return 0.0F;
        return cleanJerkPoints;
    }

    public Integer getCleanJerkRank() {
        return cleanJerkRank;
    }

    /**
     * @return total for clean and jerk
     */
    public int getCleanJerkTotal() {
        final int cleanJerkTotal = max(0, zeroIfInvalid(cleanJerk1ActualLift), zeroIfInvalid(cleanJerk2ActualLift),
            zeroIfInvalid(cleanJerk3ActualLift));
        return cleanJerkTotal;
    }

    /**
     * @return the club
     */
    public String getClub() {
        return club;
    }

    public Float getCombinedPoints() {
        return getSnatchPoints() + getCleanJerkPoints() + getTotalPoints();
    }

    public boolean getCurrentLifter() {
        return currentLifter;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    public boolean getForcedAsCurrent() {
        return forcedAsCurrent;
    }

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @return the group
     */
    public CompetitionSession getCompetitionSession() {
        return competitionSession;
    }



    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Compute the time of last lift for lifter. Times are only compared within
     * the same lift type (if a lifter is at the first attempt of clean and
     * jerk, then the last lift occurred forever ago.)
     * 
     * @param lifter1
     * @return null if lifter has not lifted
     */
    public Date getLastLiftTime() {
        Date max = null; // long ago

        if (getAttemptsDone() <= 3) {
            final Date sn1 = snatch1LiftTime;
            if (sn1 != null) {
                max = sn1;
            } else {
                return max;
            }
            final Date sn2 = snatch2LiftTime;
            if (sn2 != null) {
                max = (max.after(sn2) ? max : sn2);
            } else {
                return max;
            }
            final Date sn3 = snatch3LiftTime;
            if (sn3 != null) {
                max = (max.after(sn3) ? max : sn3);
            } else {
                return max;
            }
        } else {
            final Date cj1 = cleanJerk1LiftTime;
            if (cj1 != null) {
                max = cj1;
            } else {
                return max;
            }
            final Date cj2 = cleanJerk2LiftTime;
            if (cj2 != null) {
                max = (max.after(cj2) ? max : cj2);
            } else {
                return max;
            }
            final Date cj3 = cleanJerk3LiftTime;
            if (cj3 != null) {
                max = (max.after(cj3) ? max : cj3);
            } else {
                return max;
            }
        }

        return max;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    public Date getLastSuccessfulLiftTime() {
        if (zeroIfInvalid(cleanJerk3ActualLift) > 0) return getCleanJerk3LiftTime();
        if (zeroIfInvalid(cleanJerk2ActualLift) > 0) return getCleanJerk2LiftTime();
        if (zeroIfInvalid(cleanJerk1ActualLift) > 0) return getCleanJerk1LiftTime();
        if (zeroIfInvalid(snatch3ActualLift) > 0) return getCleanJerk3LiftTime();
        if (zeroIfInvalid(snatch2ActualLift) > 0) return getCleanJerk2LiftTime();
        if (zeroIfInvalid(snatch1ActualLift) > 0) return getCleanJerk1LiftTime();
        return new Date(0L); // long ago
    }

    public Integer getLiftOrderRank() {
        return liftOrderRank;
    }

    /**
     * @return the lotNumber
     */
    public Integer getLotNumber() {
        return (lotNumber == null ? 0 : lotNumber);
    }

    public String getMembership() {
        return membership;
    }

    /**
     * @return the nextAttemptRequestedWeight
     */
    public Integer getNextAttemptRequestedWeight() {
        int attempt = getAttemptsDone() + 1;
        switch (attempt) {
        case 1:
            return (Integer) last(zeroIfInvalid(getSnatch1AutomaticProgression()), zeroIfInvalid(snatch1Declaration),
                zeroIfInvalid(snatch1Change1), zeroIfInvalid(snatch1Change2));
        case 2:
            return (Integer) last(zeroIfInvalid(getSnatch2AutomaticProgression()), zeroIfInvalid(snatch2Declaration),
                zeroIfInvalid(snatch2Change1), zeroIfInvalid(snatch2Change2));
        case 3:
            return (Integer) last(zeroIfInvalid(getSnatch3AutomaticProgression()), zeroIfInvalid(snatch3Declaration),
                zeroIfInvalid(snatch3Change1), zeroIfInvalid(snatch3Change2));
        case 4:
            return (Integer) last(zeroIfInvalid(getCleanJerk1AutomaticProgression()),
                zeroIfInvalid(cleanJerk1Declaration), zeroIfInvalid(cleanJerk1Change1),
                zeroIfInvalid(cleanJerk1Change2));
        case 5:
            return (Integer) last(zeroIfInvalid(getCleanJerk2AutomaticProgression()),
                zeroIfInvalid(cleanJerk2Declaration), zeroIfInvalid(cleanJerk2Change1),
                zeroIfInvalid(cleanJerk2Change2));
        case 6:
            return (Integer) last(zeroIfInvalid(getCleanJerk3AutomaticProgression()),
                zeroIfInvalid(cleanJerk3Declaration), zeroIfInvalid(cleanJerk3Change1),
                zeroIfInvalid(cleanJerk3Change2));
        }
        return 0;
    }

    public Integer getQualifyingTotal() {
        if (qualifyingTotal == null) return 0;
        return qualifyingTotal;
    }

    public Integer getRank() {
        return totalRank;
    }

    public Category getRegistrationCategory() {
        return registrationCategory;
    }

    public Integer getResultOrderRank() {
        return resultOrderRank;
    }

    /**
     * Compute the Sinclair total for the lifter, that is, the total multiplied
     * by a value that depends on the lifter's body weight. This value
     * extrapolates what the lifter would have lifted if he/she had the bodymass
     * of a maximum-weight lifter.
     * 
     * @return the sinclair-adjusted value for the lifter
     */
    public Double getSinclair() {
        final Double bodyWeight1 = getBodyWeight();
        if (bodyWeight1 == null) return 0.0;
        return getSinclair(bodyWeight1);
    }

    public Double getSinclair(Double bodyWeight1) {
        Integer total1 = getTotal();
        if (total1 == null || total1 < 0.1) return 0.0;
        if (gender == null) return 0.0;
        if (gender.equalsIgnoreCase("M")) { //$NON-NLS-1$
            return total1 * sinclairFactor(bodyWeight1, Coefficients.menCoefficient(), Coefficients.menMaxWeight());
        } else {
            return total1 * sinclairFactor(bodyWeight1, Coefficients.womenCoefficient(), Coefficients.womenMaxWeight());
        }
    }

    public Integer getSinclairRank() {
        return sinclairRank;
    }

    static int year = Calendar.getInstance().get(Calendar.YEAR);

    public Double getSMM() {
        try {
            final Integer birthDate1 = getBirthDate();
            if (birthDate1 == null) return 0.0;
            return getSinclair() * Coefficients.getSMMCoefficient(year - birthDate1);
        } catch (IOException e) {
            LoggerUtils.logException(logger, e);
            return getSinclair();
        }
    }

    public String getSnatch1ActualLift() {
        return emptyIfNull(snatch1ActualLift);
    }
    
    public Integer getSnatch1AsInteger() {
        return asInteger(snatch1ActualLift);
    }

    public String getSnatch1AutomaticProgression() {
        return "-"; //no such thing. //$NON-NLS-1$
    }

    public String getSnatch1Change1() {
        return emptyIfNull(snatch1Change1);
    }

    public String getSnatch1Change2() {
        return emptyIfNull(snatch1Change2);
    }

    public String getSnatch1Declaration() {
        return emptyIfNull(snatch1Declaration);
    }

    public Date getSnatch1LiftTime() {
        return snatch1LiftTime;
    }

    public String getSnatch2ActualLift() {
        return emptyIfNull(snatch2ActualLift);
    }
    
    public Integer getSnatch2AsInteger() {
        return asInteger(snatch2ActualLift);
    }

    public String getSnatch2AutomaticProgression() {
        final int prevVal = zeroIfInvalid(snatch1ActualLift);
        return doAutomaticProgression(prevVal);
    }

    public String getSnatch2Change1() {
        return emptyIfNull(snatch2Change1);
    }

    public String getSnatch2Change2() {
        return emptyIfNull(snatch2Change2);
    }

    public String getSnatch2Declaration() {
        return emptyIfNull(snatch2Declaration);
    }

    public Date getSnatch2LiftTime() {
        return snatch2LiftTime;
    }

    public String getSnatch3ActualLift() {
        return emptyIfNull(snatch3ActualLift);
    }
    
    public Integer getSnatch3AsInteger() {
        return asInteger(snatch3ActualLift);
    }
    
    public String getSnatch3AutomaticProgression() {
        final int prevVal = zeroIfInvalid(snatch2ActualLift);
        return doAutomaticProgression(prevVal);
    }

    public String getSnatch3Change1() {
        return emptyIfNull(snatch3Change1);
    }

    public String getSnatch3Change2() {
        return emptyIfNull(snatch3Change2);
    }

    public String getSnatch3Declaration() {
        return emptyIfNull(snatch3Declaration);
    }

    public Date getSnatch3LiftTime() {
        return snatch3LiftTime;
    }

    /**
     * @return how many snatch attempts have been performed
     */
    public Integer getSnatchAttemptsDone() {
        // lifter signals he wont take his remaining tries, a zero is entered
        // further lifts are not counted.
        int attempts = 0;
        if (!isEmpty(snatch1ActualLift)) {
            attempts++;
        } else {
            return attempts;
        }
        if (!isEmpty(snatch2ActualLift)) {
            attempts++;
        } else {
            return attempts;
        }
        if (!isEmpty(snatch3ActualLift)) {
            attempts++;
        } else {
            return attempts;
        }
        return attempts;
    }

    public Float getSnatchPoints() {
        if (snatchPoints == null) return 0.0F;
        return snatchPoints;
    }

    public Integer getSnatchRank() {
        return snatchRank;
    }

    /**
     * @return total for snatch.
     */
    public int getSnatchTotal() {
        final int snatchTotal = max(0, zeroIfInvalid(snatch1ActualLift), zeroIfInvalid(snatch2ActualLift),
            zeroIfInvalid(snatch3ActualLift));
        return snatchTotal;
    }

    public Integer getTeamCleanJerkRank() {
        return teamCleanJerkRank;
    }

    public Boolean getTeamMember() {
        if (teamMember == null) return true;
        return teamMember;
    }

    public Integer getTeamSnatchRank() {
        return teamSnatchRank;
    }

    public Integer getTeamTotalRank() {
        return teamTotalRank;
    }

    /**
     * Total is zero if all three snatches or all three clean&jerks are failed.
     * Failed lifts are indicated as negative amounts. Total is the sum of all
     * good lifts otherwise. Null entries indicate that no data has been
     * captured, and are counted as zero.
     * 
     * @return the total
     */
    public Integer getTotal() {
        final int snatchTotal = getSnatchTotal();
        if (snatchTotal == 0) return 0;
        final int cleanJerkTotal = getCleanJerkTotal();
        if (cleanJerkTotal == 0) return 0;
        return snatchTotal + cleanJerkTotal;
    }
    
    public Float getTotalPoints() {
        if (totalPoints == null) return 0.0F;
        return totalPoints;
    }

    public Integer getTotalRank() {
        return totalRank;
    }


    public boolean isCurrentLifter() {
        return currentLifter;
    }

    public boolean isForcedAsCurrent() {
        return forcedAsCurrent;
    }

    public boolean isInvited() {
        final Locale locale = CompetitionApplication.getCurrentLocale();
        int threshold = Competition.invitedIfBornBefore();
        return (getBirthDate() < threshold)
            || membership.equalsIgnoreCase(Messages.getString("Lifter.InvitedAbbreviated", locale)) //$NON-NLS-1$
        // || !getTeamMember()
        ;
    }

    public void removeAllListeners() {
        if (eventRouter != null) {
            eventRouter.removeAllListeners();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.MethodEventSource#removeListener(java.lang.Class,
     * java.lang.Object)
     */
    @Override
	@SuppressWarnings({ "rawtypes" })
    public void removeListener(Class eventType, Object target) {
        if (eventRouter != null) {
            eventRouter.removeListener(eventType, target);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.MethodEventSource#removeListener(java.lang.Class,
     * java.lang.Object, java.lang.reflect.Method)
     */
    @Override
	@SuppressWarnings({ "rawtypes" })
    public void removeListener(Class eventType, Object target, Method method) {
        if (eventRouter != null) {
            eventRouter.removeListener(eventType, target, method);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.MethodEventSource#removeListener(java.lang.Class,
     * java.lang.Object, java.lang.String)
     */
    @Override
	@SuppressWarnings({ "rawtypes" })
    public void removeListener(Class eventType, Object target, String methodName) {
        if (eventRouter != null) {
            eventRouter.removeListener(eventType, target, methodName);
        }
    }

    /**
     * Remove a specific Lifter.Listener object
     * 
     * @param listener
     */
    @Override
	public void removeListener(EventListener listener) {
        if (eventRouter != null) {
            eventRouter.removeListener(UpdateEvent.class, listener, LIFTER_EVENT_METHOD);
        }
    }

    public void resetForcedAsCurrent() {
        this.forcedAsCurrent = false;
    }

    public void setAgeGroup(Integer ageGroup) {
        this.ageGroup = ageGroup;
    }

    public void setAsCurrentLifter(Boolean currentLifter) {
        // if (currentLifter)
        // System.err.println("Lifter.setAsCurrentLifter(): current lifter is now "+getLastName()+" "+getFirstName());
        this.currentLifter = currentLifter;
        if (currentLifter) {
            logger.info("{} is current lifter", this);
        }
    }

    public void setAttemptsDone(Integer i) {
    }

    public void setBestCleanJerk(Integer i) {
    }

    public void setBestSnatch(Integer i) {
    }

    /**
     * @param birthDate
     *            the birthDate to set
     */
    public void setBirthDate(Integer birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * @param bodyWeight
     *            the bodyWeight to set
     */
    public void setBodyWeight(Double bodyWeight) {
        this.bodyWeight = bodyWeight;
        fireEvent(new UpdateEvent(this, "category", "bodyWeight")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @param category
     *            the category to set
     */
    // public void setCategory(Category category) {
    // this.category = category;
    // }

    public void setCleanJerk1ActualLift(String cleanJerk1ActualLift) {
        validateActualLift(1, getCleanJerk1AutomaticProgression(), cleanJerk1Declaration, cleanJerk1Change1,
            cleanJerk1Change2, cleanJerk1ActualLift);
        this.cleanJerk1ActualLift = cleanJerk1ActualLift;
        if (zeroIfInvalid(cleanJerk1ActualLift) == 0) this.cleanJerk1LiftTime = (null);
        else this.cleanJerk1LiftTime = (Calendar.getInstance().getTime());
        logger.info("{} cleanJerk1ActualLift={}", this, cleanJerk1ActualLift);
        fireEvent(new UpdateEvent(this, "cleanJerk1ActualLift", "cleanJerk1LiftTime", "total")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void setCleanJerk1AutomaticProgression(String s) {
    }

    public void setCleanJerk1Change1(String cleanJerk1Change1) {
        if ("0".equals(cleanJerk1Change1)) {
            this.cleanJerk1Change1 = cleanJerk1Change1;
            logger.info("{} cleanJerk1Change1={}", this, cleanJerk1Change1);
            setCleanJerk1ActualLift("0");
            return;
        }
        validateChange1(1, getCleanJerk1AutomaticProgression(), cleanJerk1Declaration, cleanJerk1Change1,
            cleanJerk1Change2, cleanJerk1ActualLift, false);
        this.cleanJerk1Change1 = cleanJerk1Change1;
        logger.info("{} cleanJerk1Change1={}", this, cleanJerk1Change1);
        fireEvent(new UpdateEvent(this, "cleanJerk1Change1")); //$NON-NLS-1$
    }

    public void setCleanJerk1Change2(String cleanJerk1Change2) {
        if ("0".equals(cleanJerk1Change2)) {
            this.cleanJerk1Change2 = cleanJerk1Change2;
            logger.info("{} cleanJerk1Change2={}", this, cleanJerk1Change2);
            setCleanJerk1ActualLift("0");
            return;
        }
        validateChange2(1, getCleanJerk1AutomaticProgression(), cleanJerk1Declaration, cleanJerk1Change1,
            cleanJerk1Change2, cleanJerk1ActualLift, false);
        this.cleanJerk1Change2 = cleanJerk1Change2;
        logger.info("{} cleanJerk1Change2={}", this, cleanJerk1Change2);
        fireEvent(new UpdateEvent(this, "cleanJerk1Change2")); //$NON-NLS-1$
    }

    public void setCleanJerk1Declaration(String cleanJerk1Declaration) {
        if ("0".equals(cleanJerk1Declaration)) {
            this.cleanJerk1Declaration = cleanJerk1Declaration;
            logger.info("{} cleanJerk1Declaration={}", this, cleanJerk1Declaration);
            setCleanJerk1ActualLift("0");
            return;
        }
        validateDeclaration(1, getCleanJerk1AutomaticProgression(), cleanJerk1Declaration, cleanJerk1Change1,
            cleanJerk1Change2, cleanJerk1ActualLift, false);
        this.cleanJerk1Declaration = cleanJerk1Declaration;
        logger.info("{} cleanJerk1Declaration={}", this, cleanJerk1Declaration);
        fireEvent(new UpdateEvent(this, "cleanJerk1Declaration")); //$NON-NLS-1$
    }

    public void setCleanJerk1LiftTime(java.util.Date date) {
    }

    public void setCleanJerk2ActualLift(String cleanJerk2ActualLift) {
        validateActualLift(2, getCleanJerk2AutomaticProgression(), cleanJerk2Declaration, cleanJerk2Change1,
            cleanJerk2Change2, cleanJerk2ActualLift);
        this.cleanJerk2ActualLift = cleanJerk2ActualLift;
        if (zeroIfInvalid(cleanJerk2ActualLift) == 0) this.cleanJerk2LiftTime = (null);
        else this.cleanJerk2LiftTime = (Calendar.getInstance().getTime());
        logger.info("{} cleanJerk2ActualLift={}", this, cleanJerk2ActualLift);
        fireEvent(new UpdateEvent(this, "cleanJerk2ActualLift", "cleanJerk2LiftTime", "total")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void setCleanJerk2AutomaticProgression(String s) {
    }

    public void setCleanJerk2Change1(String cleanJerk2Change1) throws RuleViolationException {
        if ("0".equals(cleanJerk2Change1)) {
            this.cleanJerk2Change1 = cleanJerk2Change1;
            logger.info("{} cleanJerk2Change1={}", this, cleanJerk2Change1);
            setCleanJerk2ActualLift("0");
            return;
        }
        validateChange1(2, getCleanJerk2AutomaticProgression(), cleanJerk2Declaration, cleanJerk2Change1,
            cleanJerk2Change2, cleanJerk2ActualLift, false);
        this.cleanJerk2Change1 = cleanJerk2Change1;
        logger.info("{} cleanJerk2Change1={}", this, cleanJerk2Change1);
        fireEvent(new UpdateEvent(this, "cleanJerk2Change1")); //$NON-NLS-1$
    }

    public void setCleanJerk2Change2(String cleanJerk2Change2) {
        if ("0".equals(cleanJerk2Change2)) {
            this.cleanJerk2Change2 = cleanJerk2Change2;
            logger.info("{} cleanJerk2Change2={}", this, cleanJerk2Change2);
            setCleanJerk2ActualLift("0");
            return;
        }
        validateChange2(2, getCleanJerk2AutomaticProgression(), cleanJerk2Declaration, cleanJerk2Change1,
            cleanJerk2Change2, cleanJerk2ActualLift, false);
        this.cleanJerk2Change2 = cleanJerk2Change2;
        logger.info("{} cleanJerk2Change2={}", this, cleanJerk2Change2);
        fireEvent(new UpdateEvent(this, "cleanJerk2Change2")); //$NON-NLS-1$
    }

    public void setCleanJerk2Declaration(String cleanJerk2Declaration) {
        if ("0".equals(cleanJerk2Declaration)) {
            this.cleanJerk2Declaration = cleanJerk2Declaration;
            logger.info("{} cleanJerk2Declaration={}", this, cleanJerk2Declaration);
            setCleanJerk2ActualLift("0");
            return;
        }
        validateDeclaration(2, getCleanJerk2AutomaticProgression(), cleanJerk2Declaration, cleanJerk2Change1,
            cleanJerk2Change2, cleanJerk2ActualLift, false);
        this.cleanJerk2Declaration = cleanJerk2Declaration;
        logger.info("{} cleanJerk2Declaration={}", this, cleanJerk2Declaration);
        fireEvent(new UpdateEvent(this, "cleanJerk2Declaration")); //$NON-NLS-1$
    }

    public void setCleanJerk2LiftTime(Date cleanJerk2LiftTime) {
    }

    public void setCleanJerk3ActualLift(String cleanJerk3ActualLift) {
        validateActualLift(3, getCleanJerk3AutomaticProgression(), cleanJerk3Declaration, cleanJerk3Change1,
            cleanJerk3Change2, cleanJerk3ActualLift);
        this.cleanJerk3ActualLift = cleanJerk3ActualLift;
        if (zeroIfInvalid(cleanJerk3ActualLift) == 0) this.cleanJerk3LiftTime = (null);
        else this.cleanJerk3LiftTime = (Calendar.getInstance().getTime());
        logger.info("{} cleanJerk3ActualLift={}", this, cleanJerk3ActualLift);
        fireEvent(new UpdateEvent(this, "cleanJerk3ActualLift", "cleanJerk3LiftTime", "total")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void setCleanJerk3AutomaticProgression(String s) {
    }

    public void setCleanJerk3Change1(String cleanJerk3Change1) throws RuleViolationException {
        if ("0".equals(cleanJerk3Change1)) {
            this.cleanJerk3Change1 = cleanJerk3Change1;
            logger.info("{} cleanJerk3Change1={}", this, cleanJerk3Change1);
            setCleanJerk3ActualLift("0");
            return;
        }
        validateChange1(3, getCleanJerk3AutomaticProgression(), cleanJerk3Declaration, cleanJerk3Change1,
            cleanJerk3Change2, cleanJerk3ActualLift, false);
        this.cleanJerk3Change1 = cleanJerk3Change1;
        logger.info("{} cleanJerk3Change1={}", this, cleanJerk3Change1);
        fireEvent(new UpdateEvent(this, "cleanJerk3Change1")); //$NON-NLS-1$
    }

    public void setCleanJerk3Change2(String cleanJerk3Change2) {
        if ("0".equals(cleanJerk3Change2)) {
            this.cleanJerk3Change2 = cleanJerk3Change2;
            logger.info("{} cleanJerk3Change2={}", this, cleanJerk3Change2);
            setCleanJerk3ActualLift("0");
            return;
        }
        validateChange2(3, getCleanJerk3AutomaticProgression(), cleanJerk3Declaration, cleanJerk3Change1,
            cleanJerk3Change2, cleanJerk3ActualLift, false);
        this.cleanJerk3Change2 = cleanJerk3Change2;
        logger.info("{} cleanJerk3Change2={}", this, cleanJerk3Change2);
        fireEvent(new UpdateEvent(this, "cleanJerk3Change2")); //$NON-NLS-1$
    }

    public void setCleanJerk3Declaration(String cleanJerk3Declaration) {
        if ("0".equals(cleanJerk3Declaration)) {
            this.cleanJerk3Declaration = cleanJerk3Declaration;
            logger.info("{} cleanJerk3Declaration={}", this, cleanJerk3Declaration);
            setCleanJerk3ActualLift("0");
            return;
        }
        validateDeclaration(3, getCleanJerk3AutomaticProgression(), cleanJerk3Declaration, cleanJerk3Change1,
            cleanJerk3Change2, cleanJerk3ActualLift, false);
        this.cleanJerk3Declaration = cleanJerk3Declaration;
        logger.info("{} cleanJerk3Declaration={}", this, cleanJerk3Declaration);
        fireEvent(new UpdateEvent(this, "cleanJerk3Declaration")); //$NON-NLS-1$
    }

    public void setCleanJerk3LiftTime(Date cleanJerk3LiftTime) {
    }

    public void setCleanJerkAttemptsDone(Integer i) {
    }

    public void setCleanJerkPoints(Float cleanJerkPoints) {
        this.cleanJerkPoints = cleanJerkPoints;
    }

    public void setCleanJerkRank(Integer cleanJerkRank) {
        this.cleanJerkRank = cleanJerkRank;
    }

    /**
     * @param club
     *            the club to set
     */
    public void setClub(String club) {
        this.club = club;
    }

    public void setCurrentLifter(boolean currentLifter) {
        this.currentLifter = currentLifter;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @param forcedAsCurrent
     */
    public void setForcedAsCurrent(boolean forcedAsCurrent) {
        logger.debug("setForcedAsCurrent({})", forcedAsCurrent); //$NON-NLS-1$
        this.forcedAsCurrent = forcedAsCurrent;
        fireEvent(new UpdateEvent(this, "_forceAsCurrent")); // no visible property update //$NON-NLS-1$
    }

    /**
     * @param string
     *            the gender to set
     */
    public void setGender(String string) {
        if (string != null) {
            this.gender = string.toUpperCase();
        } else {
            this.gender = string;
        }
        fireEvent(new UpdateEvent(this, "category", "gender")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @param competitionSession
     *            the group to set
     */
    public void setCompetitionSession(CompetitionSession competitionSession) {
        this.competitionSession = competitionSession;
    }

    public void setLastLiftTime(Date d) {
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLiftOrderRank(Integer liftOrder) {
        this.liftOrderRank = liftOrder;
    }

    /**
     * @param lotNumber
     *            the lotNumber to set
     */
    public void setLotNumber(Integer lotNumber) {
        this.lotNumber = lotNumber;
    }

    public Integer getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(Integer startNumber) {
        this.startNumber = startNumber;
    }

    public void setMembership(String membership) {
        this.membership = membership;
    }

    public void setNextAttemptRequestedWeight(Integer i) {
    }

    public void setQualifyingTotal(Integer qualifyingTotal) {
        this.qualifyingTotal = qualifyingTotal;
    }

    public void setRank(Integer i) {
        this.totalRank = i;
    }

    public void setRegistrationCategory(Category registrationCategory) {
        this.registrationCategory = registrationCategory;
        // category may need to be flagged as different from registration if
        // this
        // value is changed.
        fireEvent(new UpdateEvent(this, "registrationCategory", "category")); //$NON-NLS-1$//$NON-NLS-2$
    }

    public void setResultOrderRank(Integer resultOrderRank, Ranking rankingType) {
        this.resultOrderRank = resultOrderRank;
    }

    public void setSinclairRank(Integer sinclairRank) {
        this.sinclairRank = sinclairRank;
    }

    public void setSnatch1ActualLift(String snatch1ActualLift) {
        validateActualLift(1, getSnatch1AutomaticProgression(), snatch1Declaration, snatch1Change1, snatch1Change2,
            snatch1ActualLift);
        this.snatch1ActualLift = snatch1ActualLift;
        if (zeroIfInvalid(snatch1ActualLift) == 0) this.snatch1LiftTime = null;
        else this.snatch1LiftTime = (Calendar.getInstance().getTime());
        logger.info("{} snatch1ActualLift={}", this, snatch1ActualLift);
        fireEvent(new UpdateEvent(this, "snatch1ActualLift", "snatch1LiftTime", "total")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void setSnatch1AutomaticProgression(String s) {
    }

    public void setSnatch1Change1(String snatch1Change1) throws RuleViolationException {
        if ("0".equals(snatch1Change1)) {
            this.snatch1Change1 = snatch1Change1;
            logger.info("{} snatch1Change1={}", this, snatch1Change1);
            setSnatch1ActualLift("0");
            return;
        }
        validateChange1(1, getSnatch1AutomaticProgression(), snatch1Declaration, snatch1Change1, snatch1Change2,
            snatch1ActualLift, true);
        this.snatch1Change1 = snatch1Change1;
        logger.info("{} snatch1Change1={}", this, snatch1Change1);
        fireEvent(new UpdateEvent(this, "snatch1Change1")); //$NON-NLS-1$
    }

    public void setSnatch1Change2(String snatch1Change2) {
        if ("0".equals(snatch1Change2)) {
            this.snatch1Change2 = snatch1Change2;
            logger.info("{} snatch1Change2={}", this, snatch1Change2);
            setSnatch1ActualLift("0");
            return;
        }
        validateChange2(1, getSnatch1AutomaticProgression(), snatch1Declaration, snatch1Change1, snatch1Change2,
            snatch1ActualLift, true);
        this.snatch1Change2 = snatch1Change2;
        logger.info("{} snatch1Change2={}", this, snatch1Change2);
        fireEvent(new UpdateEvent(this, "snatch1Change2")); //$NON-NLS-1$
    }

    public void setSnatch1Declaration(String snatch1Declaration) {
        if ("0".equals(snatch1Declaration)) {
            this.snatch1Declaration = snatch1Declaration;
            logger.info("{} snatch1Declaration={}", this, snatch1Declaration);
            setSnatch1ActualLift("0");
            return;
        }
        validateDeclaration(1, getSnatch1AutomaticProgression(), snatch1Declaration, snatch1Change1, snatch1Change2,
            snatch1ActualLift, true);
        this.snatch1Declaration = snatch1Declaration;
        logger.info("{} snatch1Declaration={}", this, snatch1Declaration);
        fireEvent(new UpdateEvent(this, "snatch1Declaration")); //$NON-NLS-1$
    }

    public void setSnatch1LiftTime(Date snatch1LiftTime) {
    }

    public void setSnatch2ActualLift(String snatch2ActualLift) {
        validateActualLift(2, getSnatch2AutomaticProgression(), snatch2Declaration, snatch2Change1, snatch2Change2,
            snatch2ActualLift);
        this.snatch2ActualLift = snatch2ActualLift;
        if (zeroIfInvalid(snatch2ActualLift) == 0) this.snatch2LiftTime = (null);
        else this.snatch2LiftTime = (Calendar.getInstance().getTime());
        logger.info("{} snatch2ActualLift={}", this, snatch2ActualLift);
        fireEvent(new UpdateEvent(this, "snatch2ActualLift", "snatch2LiftTime", "total")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void setSnatch2AutomaticProgression(String s) {
    }

    public void setSnatch2Change1(String snatch2Change1) throws RuleViolationException {
        if ("0".equals(snatch2Change1)) {
            this.snatch2Change1 = snatch2Change1;
            logger.info("{} snatch2Change1={}", this, snatch2Change1);
            setSnatch2ActualLift("0");
            return;
        }
        validateChange1(2, getSnatch2AutomaticProgression(), snatch2Declaration, snatch2Change1, snatch2Change2,
            snatch2ActualLift, true);
        this.snatch2Change1 = snatch2Change1;
        logger.info("{} snatch2Change1={}", this, snatch2Change1);
        fireEvent(new UpdateEvent(this, "snatch2Change1")); //$NON-NLS-1$
    }

    /* *********************************************************************************
     * UpdateEvent framework.
     */

    public void setSnatch2Change2(String snatch2Change2) {
        if ("0".equals(snatch2Change2)) {
            this.snatch2Change2 = snatch2Change2;
            logger.info("{} snatch2Change2={}", this, snatch2Change2);
            setSnatch2ActualLift("0");
            return;
        }
        validateChange2(2, getSnatch2AutomaticProgression(), snatch2Declaration, snatch2Change1, snatch2Change2,
            snatch2ActualLift, true);
        this.snatch2Change2 = snatch2Change2;
        logger.info("{} snatch2Change2={}", this, snatch2Change2);
        fireEvent(new UpdateEvent(this, "snatch2Change2")); //$NON-NLS-1$
    }

    public void setSnatch2Declaration(String snatch2Declaration) {
        if ("0".equals(snatch2Declaration)) {
            this.snatch2Declaration = snatch2Declaration;
            logger.info("{} snatch2Declaration={}", this, snatch2Declaration);
            setSnatch2ActualLift("0");
            return;
        }
        validateDeclaration(2, getSnatch2AutomaticProgression(), snatch2Declaration, snatch2Change1, snatch2Change2,
            snatch2ActualLift, true);
        this.snatch2Declaration = snatch2Declaration;
        logger.info("{} snatch2Declaration={}", this, snatch2Declaration);
        fireEvent(new UpdateEvent(this, "snatch2Declaration")); //$NON-NLS-1$
    }

    public void setSnatch2LiftTime(Date snatch2LiftTime) {
    }

    public void setSnatch3ActualLift(String snatch3ActualLift) {
        validateActualLift(3, getSnatch3AutomaticProgression(), snatch3Declaration, snatch3Change1, snatch3Change2,
            snatch3ActualLift);
        this.snatch3ActualLift = snatch3ActualLift;
        if (zeroIfInvalid(snatch3ActualLift) == 0) this.snatch3LiftTime = (null);
        else this.snatch3LiftTime = (Calendar.getInstance().getTime());
        logger.info("{} snatch3ActualLift={}", this, snatch3ActualLift);
        fireEvent(new UpdateEvent(this, "snatch2ActualLift", "snatch2LiftTime", "total")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void setSnatch3AutomaticProgression(String s) {
    }

    public void setSnatch3Change1(String snatch3Change1) throws RuleViolationException {
        if ("0".equals(snatch3Change1)) {
            this.snatch3Change1 = snatch3Change1;
            logger.info("{} snatch3Change1={}", this, snatch3Change1);
            setSnatch3ActualLift("0");
            return;
        }
        validateChange1(3, getSnatch3AutomaticProgression(), snatch3Declaration, snatch3Change1, snatch3Change2,
            snatch3ActualLift, true);
        this.snatch3Change1 = snatch3Change1;
        logger.info("{} snatch3Change1={}", this, snatch3Change1);
        fireEvent(new UpdateEvent(this, "snatch3Change1")); //$NON-NLS-1$
    }

    /*
     * General event framework: we implement the
     * com.vaadin.event.MethodEventSource interface which defines how a notifier
     * can call a method on a listener to signal that an event has occurred, and
     * how the listener can register/unregister itself.
     */

    public void setSnatch3Change2(String snatch3Change2) {
        if ("0".equals(snatch3Change2)) {
            this.snatch3Change2 = snatch3Change2;
            logger.info("{} snatch3Change2={}", this, snatch3Change2);
            setSnatch3ActualLift("0");
            return;
        }
        validateChange2(3, getSnatch3AutomaticProgression(), snatch3Declaration, snatch3Change1, snatch3Change2,
            snatch3ActualLift, true);
        this.snatch3Change2 = snatch3Change2;
        logger.info("{} snatch3Change2={}", this, snatch3Change2);
        fireEvent(new UpdateEvent(this, "snatch3Change2")); //$NON-NLS-1$
    }

    public void setSnatch3Declaration(String snatch3Declaration) {
        if ("0".equals(snatch3Declaration)) {
            this.snatch3Declaration = snatch3Declaration;
            logger.info("{} snatch3Declaration={}", this, snatch3Declaration);
            setSnatch3ActualLift("0");
            return;
        }
        validateDeclaration(3, getSnatch3AutomaticProgression(), snatch3Declaration, snatch3Change1, snatch3Change2,
            snatch3ActualLift, true);
        this.snatch3Declaration = snatch3Declaration;
        logger.info("{} snatch3Declaration={}", this, snatch3Declaration);
        fireEvent(new UpdateEvent(this, "snatch3Declaration")); //$NON-NLS-1$
    }

    public void setSnatch3LiftTime(Date snatch3LiftTime) {
    }

    public void setSnatchAttemptsDone(Integer i) {
    }

    public void setSnatchPoints(float snatchPoints) {
        this.snatchPoints = snatchPoints;
    }

    public void setSnatchRank(Integer snatchRank) {
        this.snatchRank = snatchRank;
    }

    public void setTeamCleanJerkRank(Integer teamCJRank) {
        this.teamCleanJerkRank = teamCJRank;
    }

    public void setTeamCombinedRank(Integer teamCombinedRank) {
        this.teamCombinedRank = teamCombinedRank;
    }

    public void setTeamMember(Boolean teamMember) {
        this.teamMember = teamMember;
    }

    public void setTeamSnatchRank(Integer teamSnatchRank) {
        this.teamSnatchRank = teamSnatchRank;
    }

    public void setTeamTotalRank(Integer teamTotalRank) {
        this.teamTotalRank = teamTotalRank;
    }

    public void setTeamSinclairRank(Integer teamSinclairRank) {
        this.teamSinclairRank = teamSinclairRank;
    }

    public void setTotal(Integer i) {
    }

    public void setTotalPoints(float totalPoints) {
        this.totalPoints = totalPoints;
    }

    public void setTotalRank(Integer totalRank) {
        this.totalRank = totalRank;
    }
    
	public Double getCustomScore() {
		if (customScore == null || customScore < 0.01) return new Double(getTotal());
		return customScore;
	}
    
	public void setCustomScore(Double customScore) {
		this.customScore = customScore;
	}

	public void setCustomRank(Integer customRank) {
        this.customRank = customRank;
    }
    
	public Integer getCustomRank() {
		return this.customRank;
	}

	public void setCustomPoints(float customPoints) {
        this.customPoints = customPoints;
    }

    public void successfulLift() {
        logger.debug("good lift for {}, listeners={}", this); //, getEventRouter().dumpListeners(this)); //$NON-NLS-1$
        final String weight = Integer.toString(getNextAttemptRequestedWeight());
        doLift(weight);
    }

    @Override
    public String toString() {
        return getLastName() + "_" + getFirstName() + "_" + System.identityHashCode(this); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void withdraw() {
        if (snatch1ActualLift != null && snatch1ActualLift.trim().isEmpty()) {
            setSnatch1ActualLift("0");
        }
        if (snatch2ActualLift != null && snatch2ActualLift.trim().isEmpty()) {
            setSnatch2ActualLift("0");
        }
        if (snatch3ActualLift != null && snatch3ActualLift.trim().isEmpty()) {
            setSnatch3ActualLift("0");
        }
        if (cleanJerk1ActualLift != null && cleanJerk1ActualLift.trim().isEmpty()) {
            setCleanJerk1ActualLift("0");
        }
        if (cleanJerk2ActualLift != null && cleanJerk2ActualLift.trim().isEmpty()) {
            setCleanJerk2ActualLift("0");
        }
        if (cleanJerk3ActualLift != null && cleanJerk3ActualLift.trim().isEmpty()) {
            setCleanJerk3ActualLift("0");
        }
    }



    /**
     * @param prevVal
     * @return
     */
    private String doAutomaticProgression(final int prevVal) {
        if (prevVal > 0) {
            return Integer.toString(prevVal + 1);
        } else {
            return Integer.toString(Math.abs(prevVal));
        }
    }

    /**
     * @param lifter
     * @param lifters
     * @param weight
     */
    private void doLift(final String weight) {
        switch (this.getAttemptsDone() + 1) {
        case 1:
            this.setSnatch1ActualLift(weight);
            break;
        case 2:
            this.setSnatch2ActualLift(weight);
            break;
        case 3:
            this.setSnatch3ActualLift(weight);
            break;
        case 4:
            this.setCleanJerk1ActualLift(weight);
            break;
        case 5:
            this.setCleanJerk2ActualLift(weight);
            break;
        case 6:
            this.setCleanJerk3ActualLift(weight);
            break;
        }
    }

    private String emptyIfNull(String value) {
        return (value == null ? "" : value); //$NON-NLS-1$
    }

    /**
     * @return the object's event router.
     */
    private EventRouter getEventRouter() {
        if (eventRouter == null) {
            // eventRouter = new EventRouter(this);
            eventRouter = new EventRouter();
            logger.trace("new event router for lifter {}", this); //$NON-NLS-1$
        }
        return eventRouter;
    }

    private Integer last(Integer... items) {
        int lastIndex = items.length - 1;
        while (lastIndex >= 0) {
            if (items[lastIndex] > 0) {
                return items[lastIndex];
            }
            lastIndex--;
        }
        return 0;
    }

    private Integer max(Integer... items) {
        List<Integer> itemList = Arrays.asList(items);
        final Integer max = Collections.max(itemList);
        return max;
    }

    @SuppressWarnings("unused")
    private Integer max(String... items) {
        List<String> itemList = Arrays.asList(items);
        List<Integer> intItemList = new ArrayList<Integer>(itemList.size());
        for (String curString : itemList) {
            intItemList.add(zeroIfInvalid(curString));
        }
        final Integer max = Collections.max(intItemList);
        return max;
    }

    /**
     * Compute the sinclair formula given its parameters.
     * 
     * @param coefficient
     * @param maxWeight
     */
    // ($R7*
    // SI($E7="m",
    // SI($G7<173.961,
    // 10^(0.784780654*((LOG10($G7/173.961))^2)),
    // 1),
    // SI($G7<125.441,
    // 10^(1.056683941*((LOG10($G7/125.441))^2)),1)
    // )
    // )
    private Double sinclairFactor(Double bodyWeight1, Double coefficient, Double maxWeight) {
        if (bodyWeight1 == null) return 0.0;
        if (bodyWeight1 >= maxWeight) {
            return 1.0;
        } else {
            return Math.pow(10.0, coefficient * (Math.pow(Math.log10(bodyWeight1 / maxWeight), 2)));
        }
    }

    /**
     * @param curLift
     * @param actualLift
     */
    private void validateActualLift(int curLift, String automaticProgression, String declaration, String change1,
            String change2, String actualLift) {
        if (actualLift == null || actualLift.trim().length() == 0) return; // allow
        // reset
        // of
        // field.
        final int declaredChanges = last(zeroIfInvalid(declaration), zeroIfInvalid(change1), zeroIfInvalid(change2));
        final int iAutomaticProgression = zeroIfInvalid(automaticProgression);
        final int liftedWeight = zeroIfInvalid(actualLift);
        logger.debug("declaredChanges={} automaticProgression={} liftedWeight={}", //$NON-NLS-1$
            new Object[] { declaredChanges, automaticProgression, liftedWeight });
        if (liftedWeight == 0) {
            // lifter is not taking try; always ok no matter what was declared.
            return;
        }
        if (declaredChanges == 0 && iAutomaticProgression > 0) {
            // assume data entry is being done without reference to
            // declarations, check if > progression
            if (Math.abs(liftedWeight) >= iAutomaticProgression) {
                return;
            } else {
                throw RuleViolation.liftValueBelowProgression(curLift, actualLift, iAutomaticProgression);
            }
        } else {
            // declarations are being captured, lift must match declared
            // changes.
            final boolean declaredChangesOk = declaredChanges >= iAutomaticProgression;
            final boolean liftedWeightOk = Math.abs(liftedWeight) == declaredChanges;
            if (liftedWeightOk && declaredChangesOk) {
                return;
            } else {
            	if (declaredChanges == 0)
            		return;
                if (!declaredChangesOk)
                    throw RuleViolation.declaredChangesNotOk(curLift, declaredChanges, iAutomaticProgression,
                        iAutomaticProgression + 1);
                if (!liftedWeightOk)
                    throw RuleViolation
                            .liftValueNotWhatWasRequested(curLift, actualLift, declaredChanges, liftedWeight);
            }
        }
    }

    /**
     * @param curLift
     * @param actualLift
     */
    private void validateChange1(int curLift, String automaticProgression, String declaration, String change1,
            String change2, String actualLift, boolean isSnatch) {
        if (change1 == null || change1.trim().length() == 0) return; // allow reset of field.
        int newVal = zeroIfInvalid(change1);
        int prevVal = zeroIfInvalid(automaticProgression);
        if (newVal < prevVal) throw RuleViolation.declaredChangesNotOk(curLift, newVal, prevVal);
        check15_20kiloRule(newVal, isSnatch);
    }

    /**
     * @param curLift
     * @param actualLift
     */
    private void validateChange2(int curLift, String automaticProgression, String declaration, String change1,
            String change2, String actualLift, boolean isSnatch) {
        if (change2 == null || change2.trim().length() == 0) return; // allow reset of field.
        int newVal = zeroIfInvalid(change2);
        int prevVal = zeroIfInvalid(automaticProgression);
        if (newVal < prevVal) throw RuleViolation.declaredChangesNotOk(curLift, newVal, prevVal);
        check15_20kiloRule(newVal, isSnatch);
    }

    /**
     * @param curLift
     * @param actualLift
     */
    private void validateDeclaration(int curLift, String automaticProgression, String declaration, String change1,
            String change2, String actualLift, boolean isSnatch) {
        if (declaration == null || declaration.trim().length() == 0) return; // allow reset of field.
        int newVal = zeroIfInvalid(declaration);
        int iAutomaticProgression = zeroIfInvalid(automaticProgression);
        // allow null declaration for reloading old results.
        if (iAutomaticProgression > 0 && newVal < iAutomaticProgression) throw RuleViolation.declarationValueTooSmall(curLift, newVal, iAutomaticProgression);
        check15_20kiloRule(newVal, isSnatch);
    }

    
    public void check15_20kiloRule() throws RuleViolationException {
        int qualTotal = getQualifyingTotal();
        boolean enforce15_20rule = Competition.isEnforce15_20rule();
        if (qualTotal == 0  || !enforce15_20rule) {
            return;
        }

        int currentAttempt = getAttemptsDone() + 1;
        int curStartingTotal = 0;
        int snatchRequest = 0;
        int cleanJerkRequest = 0;
        
        if (currentAttempt <= 3) {
            cleanJerkRequest = last(zeroIfInvalid(getCleanJerk1AutomaticProgression()),
                    zeroIfInvalid(cleanJerk1Declaration), zeroIfInvalid(cleanJerk1Change1),
                    zeroIfInvalid(cleanJerk1Change2));
            snatchRequest = getNextAttemptRequestedWeight();
        } else if (currentAttempt <= 4) {
            cleanJerkRequest = last(zeroIfInvalid(getCleanJerk1AutomaticProgression()),
                    zeroIfInvalid(cleanJerk1Declaration), zeroIfInvalid(cleanJerk1Change1),
                    zeroIfInvalid(cleanJerk1Change2));
            // get first snatch that was actually lifted
            snatchRequest = zeroIfInvalid(getSnatch1ActualLift());
            if (snatchRequest < 0) {
                snatchRequest = zeroIfInvalid(getSnatch2ActualLift());
            }
            if (snatchRequest < 0) {
                snatchRequest = zeroIfInvalid(getSnatch3ActualLift());
            }
            if (snatchRequest == 0) {
                return; // did not succeed.
            }
            cleanJerkRequest = getNextAttemptRequestedWeight();
        } else {
            // once 1st c&j is over, rule no longer needs checking
            return;
        }
        
        curStartingTotal  = snatchRequest + cleanJerkRequest;
        int delta = qualTotal - curStartingTotal;
        String message = null;
        Locale locale = CompetitionApplication.getCurrentLocale();
        if (getGender().equals("M") && (delta > 20)) {
            message = RuleViolation.rule15_20Violated(snatchRequest,cleanJerkRequest, delta-20, qualTotal).getLocalizedMessage(locale);
        } else if (getGender().equals("F") && (delta > 15)) {
            message = RuleViolation.rule15_20Violated(snatchRequest, cleanJerkRequest, delta-15, qualTotal).getLocalizedMessage(locale);
        } 
        if (message != null) {
            Window mainWindow = CompetitionApplication.getCurrent().getMainWindow();
            Notification notif = new Notification(message,Notification.TYPE_ERROR_MESSAGE);
            notif.setDelayMsec(0);
            mainWindow.showNotification(notif);
        }
    }
    
    
    public void check15_20kiloRule(int newVal, boolean isSnatch) throws RuleViolationException {
        int qualTotal = getQualifyingTotal();
        
        boolean enforce15_20rule = Competition.isEnforce15_20rule();
        if (qualTotal == 0  || !enforce15_20rule) {
            return;
        }

        int currentAttempt = getAttemptsDone() + 1;
        int curStartingTotal = 0;
        int snatchRequest = 0;
        int cleanJerkRequest = 0;
        
        if (currentAttempt == 1) {
            // weigh-in
            if (isSnatch) {
                cleanJerkRequest = last(zeroIfInvalid(getCleanJerk1AutomaticProgression()),
                        zeroIfInvalid(cleanJerk1Declaration), zeroIfInvalid(cleanJerk1Change1),
                        zeroIfInvalid(cleanJerk1Change2));
                if (cleanJerkRequest == 0) return;
                snatchRequest = newVal;
            } else {
                snatchRequest = last(zeroIfInvalid(getSnatch1AutomaticProgression()),
                    zeroIfInvalid(snatch1Declaration), zeroIfInvalid(snatch1Change1),
                    zeroIfInvalid(snatch1Change2));
                if (snatchRequest == 0) return;
                cleanJerkRequest = newVal;
            }
        } else if (currentAttempt <= 3) {
            assert isSnatch == true;
            cleanJerkRequest = last(zeroIfInvalid(getCleanJerk1AutomaticProgression()),
                    zeroIfInvalid(cleanJerk1Declaration), zeroIfInvalid(cleanJerk1Change1),
                    zeroIfInvalid(cleanJerk1Change2));
            snatchRequest = newVal;
        } else if (currentAttempt <= 4) { // 1st c&j
            assert isSnatch == false;
            // get first snatch that was actually lifted
            snatchRequest = zeroIfInvalid(getSnatch1ActualLift());
            if (snatchRequest < 0) {
                snatchRequest = zeroIfInvalid(getSnatch2ActualLift());
            }
            if (snatchRequest < 0) {
                snatchRequest = zeroIfInvalid(getSnatch3ActualLift());
            }
            if (snatchRequest == 0) {
                return; // did not succeed.
            }
            cleanJerkRequest = newVal;
        } else {
            // once lifter has declared sufficient weight on first c&j, no way bar is going down
            return;
        }
        
        curStartingTotal  = snatchRequest + cleanJerkRequest;
        int delta = qualTotal - curStartingTotal;
        String message = null;
        Locale locale = CompetitionApplication.getCurrentLocale();
        
        // signal violation except if lifter is withdrawing (request = 0)
        if (getGender().equals("M") && (delta > 20) && snatchRequest > 0 && cleanJerkRequest > 0) {
            message = RuleViolation.rule15_20Violated(snatchRequest,cleanJerkRequest, delta-20, qualTotal).getLocalizedMessage(locale);
        } else if (getGender().equals("F") && (delta > 15) && snatchRequest > 0 && cleanJerkRequest > 0) {
            message = RuleViolation.rule15_20Violated(snatchRequest, cleanJerkRequest, delta-15, qualTotal).getLocalizedMessage(locale);
        } 
        if (message != null) {
            Window mainWindow = CompetitionApplication.getCurrent().getMainWindow();
            mainWindow.showNotification(message,Notification.TYPE_ERROR_MESSAGE);
        }
    }
    
    /**
     * Broadcast a Lifter.event to all registered listeners
     * 
     * @param updateEvent
     *            contains the source (ourself) and the list of properties to be
     *            refreshed.
     */
    protected void fireEvent(UpdateEvent updateEvent) {
        logger
                .debug("Lifter: firing event from " + System.identityHashCode(this) + " " + lastName + " " + firstName + " " + updateEvent.getPropertyIds()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        if (eventRouter != null) {
            eventRouter.fireEvent(updateEvent);
        }

    }

    /**
     * @return
     */
    public String getMastersLongCategory() {
        String catString;
        String gender1 = getGender().toUpperCase();
        final String mastersAgeCategory = getMastersAgeGroup(gender1);
        final String shortCategory = getShortCategory(gender1);
        catString = mastersAgeCategory + " " + shortCategory;
        return catString;
    }

    /**
     * @param gender
     * @return
     */
    public String getMastersAgeGroup() {
        String gender1 = getGender().toUpperCase();
        return getMastersAgeGroup(gender1);
    }

    /**
     * @param gender1
     * @return
     */
    private String getMastersAgeGroup(String gender1) {
        Integer ageGroup1;
        ageGroup1 = getAgeGroup();
        final String agePlus = (("M".equals(gender1) && ageGroup1 == 80) || ("F".equals(gender1) && ageGroup1 == 65) ? "+"
                : "");
        final String mastersAgeCategory = gender1 + ageGroup1 + agePlus;
        return mastersAgeCategory;
    }

    /**
     * @param gender
     * @return
     */
    public String getShortCategory() {
        String gender1 = getGender();
        return getShortCategory(gender1);
    }

    /**
     * @param gender1
     * @return
     */
    public String getShortCategory(String gender1) {
//        final Double catMin = getCategory().getMinimumWeight();
//        Double catMax = getCategory().getMaximumWeight();
//        final String weightPlus = (("M".equals(gender1) && catMin > 104.999) || ("F".equals(gender1) && catMin > 74.999) ? ">"
//                : "");
//        if (catMax > 125) {
//            catMax = catMin;
//        }
//        final String shortCategory = weightPlus + catMax.intValue();
//        return shortCategory;
        final Category category = getCategory();
        if (category == null) return "";
        
        String shortCategory = category.getName();
        int gtPos = shortCategory.indexOf(">");
        if (gtPos > 0) {
        	return shortCategory.substring(gtPos);
        } else {
        	return shortCategory.substring(1);
        }
    }

    /**
     * @return
     */
    public String getDisplayCategory() {
        final Category category = getCategory();
        if (category != null && Competition.isMasters()) {
            return getShortCategory();
        } else {
            return category != null ? category.getName() : "";
        }
    }

    /**
     * @return
     */
    public String getLongCategory() {
        final Category category = getCategory();
        if (category != null && Competition.isMasters()) {
            return getMastersLongCategory();
        } else {
            return getDisplayCategory();
        }
    }
    
    @Version
    public Long getVersion() {
        return version;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((club == null) ? 0 : club.hashCode());
		result = prime * result
				+ ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result
				+ ((lastName == null) ? 0 : lastName.hashCode());
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
		Lifter other = (Lifter) obj;
		if (club == null) {
			if (other.club != null)
				return false;
		} else if (!club.equals(other.club))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (gender == null) {
			if (other.gender != null)
				return false;
		} else if (!gender.equals(other.gender))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		return true;
	}


}
