/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.webapp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.decision.Speakers;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.nec.NECDisplay;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflamy Called when the application is started to initialize the
 *         database and other global features (serial communication ports)
 */

public class WebApplicationConfiguration implements ServletContextListener {
	private static final String COMPETITION_BOOK_TEMPLATE_RESOURCE_PATH = "/templates/competitionBook/CompetitionBook_Total_en.xls";

    private static Logger logger = LoggerFactory.getLogger(WebApplicationConfiguration.class);

	private static EntityManagerFactory entityManagerFactory = null;

	public static final boolean NECShowsLifterImmediately = true;

	public static final boolean DEFAULT_STICKINESS = true;

	public static NECDisplay necDisplay = null;

    private static String persistenceUnit;

	/**
	 * this constructor sets the default values if the full parameterized
	 * constructor has not been called first (which is normally the case). If
	 * the full constructor has been called first, then the existing factory is
	 * returned.
	 * 
	 * @return a Hibernate session factory
	 */
	public static EntityManagerFactory getEntityManagerFactory() {
		// this call sets the default values if the parameterized constructor has not
		// been called first (which is normally the case). If the full
		// constructor has been called first, then the existing factory is returned.
		if (entityManagerFactory != null) return entityManagerFactory;
		else throw new RuntimeException("should have called getEntityManagerFactory(testMode,dbPath) first."); //$NON-NLS-1$
	}


	/**
	 * Insert initial data if the database is empty.
	 * @param sess
	 * @param testMode
	 */
	public static void insertInitialData(EntityManagerFactory emf, boolean testMode) {
	    EntityManager em = emf.createEntityManager();
	    EntityTransaction transaction = em.getTransaction();
	    try {
	        transaction.begin();
	        if (Competition.getAll(em).size() == 0) {
	            // empty database
	            Competition competition = new Competition();
	            competition.setFederation(Messages.getString("Competition.defaultFederation", Locale.getDefault())); //$NON-NLS-1$
	            competition.setFederationAddress(Messages.getString(
	                    "Competition.defaultFederationAddress", Locale.getDefault())); //$NON-NLS-1$
	            competition.setFederationEMail(Messages
	                    .getString("Competition.defaultFederationEMail", Locale.getDefault())); //$NON-NLS-1$
	            competition.setFederationWebSite(Messages.getString(
	                    "Competition.defaultFederationWebSite", Locale.getDefault())); //$NON-NLS-1$

	            Calendar w = Calendar.getInstance();
	            w.set(Calendar.MILLISECOND, 0);
	            w.set(Calendar.SECOND, 0);
	            w.set(Calendar.MINUTE, 0);
	            w.set(Calendar.HOUR_OF_DAY, 8);
	            Calendar c = (Calendar) w.clone();
	            c.add(Calendar.HOUR_OF_DAY, 2);

	            if (testMode) {
	                setupTestData(competition, 5, w, c, em);
	            } else {
	                setupEmptyCompetition(competition, em);	
	            }

	            em.persist(competition);
	        } else {
	            // database contains data, leave it alone.
	        }
	    } finally {
            if (transaction != null && transaction.isActive()) {
                if (transaction.getRollbackOnly()) {
                    transaction.rollback();
                } else {
                    transaction.commit();
                }
            }
            if (em != null) {
                em.close();
            }
	    }

	}

	/**
	 * Create an empty competition.
	 * Set-up the defaults for using the timekeeping and refereeing features.
	 * @param competition 
	 * 
	 * @param em
	 */
	protected static void setupEmptyCompetition(Competition competition, EntityManager em) {
	    Category.insertStandardCategories(em, Locale.getDefault());
		Platform platform1 = new Platform("Platform"); //$NON-NLS-1$
		setDefaultMixerName(platform1);
		platform1.setHasDisplay(false);
		platform1.setShowDecisionLights(true);
		// collar
		platform1.setNbC_2_5(1);
		// small plates
		platform1.setNbS_0_5(1);
		platform1.setNbS_1(1);
		platform1.setNbS_1_5(1);
		platform1.setNbS_2(1);
		platform1.setNbS_2_5(1);
		// large plates, regulation set-up
		platform1.setNbL_2_5(0);
		platform1.setNbL_5(0);
		platform1.setNbL_10(1);
		platform1.setNbL_15(1);
		platform1.setNbL_20(1);
		platform1.setNbL_25(1);
		
		// competition template
		File templateFile;
		URL templateUrl = platform1.getClass().getResource(COMPETITION_BOOK_TEMPLATE_RESOURCE_PATH);
		try {
			templateFile = new File(templateUrl.toURI());
			competition.setResultTemplateFileName(templateFile.getCanonicalPath());
		} catch (URISyntaxException e) {
			templateFile = new File(templateUrl.getPath());
		} catch (IOException e) {
		}

		em.persist(platform1);
		CompetitionSession groupA = new CompetitionSession("A", null, null); //$NON-NLS-1$
		em.persist(groupA);
		CompetitionSession groupB = new CompetitionSession("B", null, null); //$NON-NLS-1$#
		em.persist(groupB);
		CompetitionSession groupC = new CompetitionSession("C", null, null); //$NON-NLS-1$
		em.persist(groupC);
	}

	/**
	 * @param competition 
	 * @param liftersToLoad
	 * @param w
	 * @param c
	 * @param em
	 */
	protected static void setupTestData(Competition competition, int liftersToLoad,
			Calendar w, Calendar c, EntityManager em) {
		Platform platform1 = new Platform("Gym 1"); //$NON-NLS-1$
		em.persist(platform1);
		Platform platform2 = new Platform("Gym 2"); //$NON-NLS-1$
		em.persist(platform1);
		em.persist(platform2);
		CompetitionSession groupA = new CompetitionSession("A", w.getTime(), c.getTime()); //$NON-NLS-1$
		groupA.setPlatform(platform1);
		CompetitionSession groupB = new CompetitionSession("B", w.getTime(), c.getTime()); //$NON-NLS-1$
		groupB.setPlatform(platform2);
		CompetitionSession groupC = new CompetitionSession("C", w.getTime(), c.getTime()); //$NON-NLS-1$
		groupC.setPlatform(platform1);
		em.persist(groupA);
		em.persist(groupB);
		em.persist(groupC);
		insertSampleLifters(liftersToLoad, em, groupA, groupB, groupC);
	}



	private static void insertSampleLifters(int liftersToLoad, EntityManager sess, CompetitionSession groupA, CompetitionSession groupB,
			CompetitionSession groupC) {
		final String[] fnames = { "Peter", "Albert", "Joshua", "Mike", "Oliver", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"Paul", "Alex", "Richard", "Dan", "Umberto", "Henrik", "Rene", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"Fred", "Donald" }; //$NON-NLS-1$ //$NON-NLS-2$
		final String[] lnames = { "Smith", "Gordon", "Simpson", "Brown", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"Clavel", "Simons", "Verne", "Scott", "Allison", "Gates", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"Rowling", "Barks", "Ross", "Schneider", "Tate" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		Random r = new Random(0);

		for (int i = 0; i < liftersToLoad; i++) {
			Lifter p = new Lifter();
			p.setCompetitionSession(groupA);
			p.setFirstName(fnames[r.nextInt(fnames.length)]);
			p.setLastName(lnames[r.nextInt(lnames.length)]);
			sess.persist(p);
			// System.err.println("group A - "+InputSheetHelper.toString(p));
		}
		for (int i = 0; i < liftersToLoad; i++) {
			Lifter p = new Lifter();
			p.setCompetitionSession(groupB);
			p.setFirstName(fnames[r.nextInt(fnames.length)]);
			p.setLastName(lnames[r.nextInt(lnames.length)]);
			sess.persist(p);
			// System.err.println("group B - "+InputSheetHelper.toString(p));
		}
		sess.flush();
	}

    /**
     * @param platform1
     */
    protected static void setDefaultMixerName(Platform platform1) {
        String mixerName = null;    
        try {
            mixerName = Speakers.getOutputNames().get(0);
            platform1.setMixerName(mixerName);
        } catch (Exception e) {
            // leave mixerName null
        }
    }
    
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		if (entityManagerFactory != null) {
		    entityManagerFactory.close();
		}
	    derbyShutdown();
		if (necDisplay != null) {
			necDisplay.close();
		}
		necDisplay = null;
		logger.debug("contextDestroyed() done"); //$NON-NLS-1$
	}

	
	/**
	 * Attempt to shutdown embedded Derby cleanly.
	 */
	private void derbyShutdown() {
	    EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit+"-shutdown");
	    EntityManager em = emf.createEntityManager();
	    
	    Session session = (Session) em.getDelegate();
	    session.doWork(new Work(){

            @Override
            public void execute(Connection connection) throws SQLException {
                connection.getMetaData();
                // nothing, all we wanted was an open connection.
            }       
	    });
	    em.close();
	    emf.close();
	}
	

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		ServletContext sCtx = arg0.getServletContext();

		if (necDisplay != null) {
			necDisplay.close();
			necDisplay = null;
		}
		final String comPortName = sCtx.getInitParameter("comPort"); //$NON-NLS-1$
		getNecDisplay(comPortName);
        final String defaultLanguage = Messages.getString("Locale.defaultLanguage", Locale.getDefault()); //$NON-NLS-1$
        final String defaultCountry = Messages.getString("Locale.defaultCountry", Locale.getDefault()); //$NON-NLS-1$
		Locale.setDefault(new Locale(defaultLanguage,defaultCountry));
		logger.info("Default JVM Locale set to: {}", Locale.getDefault()); //$NON-NLS-1$
		logger.debug("contextInitialized() done"); //$NON-NLS-1$
	}

	/**
	 * @param comPortName
	 * @throws RuntimeException
	 */
	public static void getNecDisplay(final String comPortName) throws RuntimeException {
		try {
			if (comPortName != null && !comPortName.isEmpty()) {
				necDisplay = new NECDisplay();
				necDisplay.setComPortName(comPortName);
			}
		} catch (Exception e) {
			logger.warn("Could not open port {} {}",comPortName,e.getMessage());
		}
	}

    public static EntityManagerFactory getPersistentEntityManagerFactory() {
        if (entityManagerFactory == null) {
            persistenceUnit = "owlcms-embedded";
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);
        }
        
        return entityManagerFactory;
    }
    
    public static EntityManagerFactory getTestEntityManagerFactory() {
        if (entityManagerFactory == null) {
            persistenceUnit = "owlcms-inmemory";
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);
        }
        return entityManagerFactory;
    }



}
