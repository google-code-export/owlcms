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
import java.sql.Statement;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
import org.concordiainternational.competition.utils.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.event.def.OverrideMergeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;

/**
 * @author jflamy Called when the application is started to initialize the
 *         database and other global features (serial communication ports)
 */

public class WebApplicationConfiguration implements HbnSessionManager, ServletContextListener {
	private static Logger logger = LoggerFactory.getLogger(WebApplicationConfiguration.class);

	private static EntityManagerFactory sessionFactory = null;
	private static final boolean TEST_MODE = false;

	public static final boolean NECShowsLifterImmediately = true;

	public static final boolean DEFAULT_STICKINESS = true;

	private static AnnotationConfiguration cnf;

	public static NECDisplay necDisplay = null;

	/**
	 * this constructor sets the default values if the full parameterized
	 * constructor has not been called first (which is normally the case). If
	 * the full constructor has been called first, then the existing factory is
	 * returned.
	 * 
	 * @return a Hibernate session factory
	 */
	public static EntityManagerFactory getSessionFactory() {
		// this call sets the default values if the parameterized constructor
		// has not
		// been called first (which is normally the case). If the full
		// constructor
		// has been called first, then the existing factory is returned.
		if (sessionFactory != null) return sessionFactory;
		else throw new RuntimeException("should have called getSessionFactory(testMode,dbPath) first."); //$NON-NLS-1$
	}

	/**
	 * Full constructor, normally invoked first.
	 * 
	 * @param testMode
	 *            true if the database runs in memory, false is there is a
	 *            physical database
	 * @param dbPath
	 * @return
	 */
	public static EntityManagerFactory getSessionFactory(boolean testMode, String dbPath) {
		if (sessionFactory == null) {
			try {
				cnf = new AnnotationConfiguration();
				//derbySetup(testMode, dbPath, cnf);
				h2Setup(testMode, dbPath, cnf);
				cnf.setProperty(Environment.USER, "sa"); //$NON-NLS-1$
				cnf.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread"); //$NON-NLS-1$

				// the classes we store in the database.
				cnf.addAnnotatedClass(Lifter.class);
				cnf.addAnnotatedClass(CompetitionSession.class);
				cnf.addAnnotatedClass(Platform.class);
				cnf.addAnnotatedClass(Category.class);
				cnf.addAnnotatedClass(Competition.class);
				cnf.addAnnotatedClass(CompetitionSession.class);

				//                cnf.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.EhCacheProvider"); //$NON-NLS-1$
				cnf.setProperty("hibernate.cache.region.factory_class", "net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory"); //$NON-NLS-1$ //$NON-NLS-2$
				cnf.setProperty("hibernate.cache.use_second_level_cache", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				cnf.setProperty("hibernate.cache.use_query_cache", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				// cnf.setProperty(Environment.CACHE_PROVIDER,"org.hibernate.cache.HashtableCacheProvider");

				// the following line is necessary because the Lifter class uses
				// the Lift class several times (one for each lift), which would normally force
				// us to override the column names to ensure they are unique. Hibernate
				// supports this with an extension.
				// cnf.setNamingStrategy(DefaultComponentSafeNamingStrategy.INSTANCE);

				// listeners
				cnf.setListener("merge", new OverrideMergeEventListener()); //$NON-NLS-1$

				sessionFactory = (EntityManagerFactory) cnf.buildSessionFactory();
				// create the standard categories, etc.
				// if argument is > 0, create sample data as well.
				EntityManager sess = sessionFactory.createEntityManager();
				sess.joinTransaction();
				Category.insertStandardCategories(sess, Locale.getDefault());
				insertInitialData(5, sess, testMode);
				sess.flush();
				sess.close();
			} catch (Throwable ex) {
				// Make sure you log the exception, as it might be swallowed
				ex.printStackTrace(System.err);
				throw new ExceptionInInitializerError(ex);
			}
		}

		return sessionFactory;
	}

	/**
	 * @param testMode
	 * @param dbPath
	 * @param cnf1
	 * @throws IOException 
	 */
	private static void h2Setup(boolean testMode, String dbPath, AnnotationConfiguration cnf1) throws IOException {
		cnf1.setProperty(Environment.DRIVER, "org.h2.Driver"); //$NON-NLS-1$
		if (testMode) {
			cnf1.setProperty(Environment.URL, "jdbc:h2:mem:competition"); //$NON-NLS-1$
			cnf1.setProperty(Environment.SHOW_SQL, "false"); //$NON-NLS-1$
			cnf1.setProperty(Environment.HBM2DDL_AUTO, "create-drop"); //$NON-NLS-1$
		} else {
			File file = new File(dbPath).getParentFile(); //$NON-NLS-1$
			if (!file.exists()) {
				boolean status = file.mkdirs();
				if (! status) {
					throw new RuntimeException("could not create directories for "+file.getCanonicalPath());
				}
			}

			cnf1.setProperty(Environment.SHOW_SQL, "false"); //$NON-NLS-1$
			cnf1.setProperty(Environment.URL, "jdbc:h2:file:" + dbPath); //$NON-NLS-1$
			String ddlMode = "create"; //$NON-NLS-1$
			file = new File(dbPath + ".h2.db"); //$NON-NLS-1$            
			if (file.exists()) {
				ddlMode = "update"; //$NON-NLS-1$
			} else {
				file = new File(dbPath + ".data.db"); //$NON-NLS-1$
				if (file.exists()) {
					ddlMode = "update"; //$NON-NLS-1$
				}
			}
			logger.info("Using Hibernate mode {} (file {} exists={}", new Object[] { ddlMode, file.getAbsolutePath(), Boolean.toString(file.exists()) }); //$NON-NLS-1$
			cnf1.setProperty(Environment.HBM2DDL_AUTO, ddlMode);
			// throw new
			// ExceptionInInitializerError("Production database configuration not specified");
		}
		cnf1.setProperty(Environment.DIALECT, H2Dialect.class.getName());
	}

	/**
	 * @param testMode
	 * @param dbPath
	 * @param cnf1
	 */
	@SuppressWarnings("unused")
	private static void derbySetup(boolean testMode, String dbPath, AnnotationConfiguration cnf1) {
		if (testMode) {
			cnf1.setProperty(Environment.DRIVER, "org.h2.Driver"); //$NON-NLS-1$
			cnf1.setProperty(Environment.URL, "jdbc:h2:mem:competition"); //$NON-NLS-1$
			cnf1.setProperty(Environment.SHOW_SQL, "false"); //$NON-NLS-1$
			cnf1.setProperty(Environment.HBM2DDL_AUTO, "create-drop"); //$NON-NLS-1$
		} else {
			cnf1.setProperty(Environment.DRIVER, "org.apache.derby.jdbc.EmbeddedDriver"); //$NON-NLS-1$
			cnf1.setProperty(Environment.SHOW_SQL, "false"); //$NON-NLS-1$

			String ddlMode = "create"; //$NON-NLS-1$
			String suffix=";create=true";

			File file = new File(dbPath); //$NON-NLS-1$
			if (file.exists()) {
				ddlMode = "update"; //$NON-NLS-1$
			} else {
				file = new File(dbPath); //$NON-NLS-1$
				if (file.exists()) {
					ddlMode = "update"; //$NON-NLS-1$
				}
			}
			logger.info(
					"Using Hibernate mode {} (file {} exists={}",
					new Object[] { ddlMode, file.getAbsolutePath(), Boolean.toString(file.exists()) }); //$NON-NLS-1$
			cnf1.setProperty(Environment.HBM2DDL_AUTO, ddlMode);
			cnf1.setProperty(Environment.URL, "jdbc:derby:" + dbPath + suffix); //$NON-NLS-1$
			// throw new ExceptionInInitializerError("Production database configuration not specified");
		}
		cnf1.setProperty(Environment.DIALECT, DerbyDialect.class.getName());
	}

	/**
	 * Insert initial data if the database is empty.
	 * 
	 * @param liftersToLoad
	 * @param sess
	 * @param testMode
	 */
	public static void insertInitialData(int liftersToLoad, EntityManager sess, boolean testMode) {
		if (sess.createCriteria(CompetitionSession.class).list().size() == 0) {
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
				setupTestData(competition, liftersToLoad, sess, w, c);
			} else {
				setupEmptyCompetition(competition, sess);	
			}
			
			sess.persist(competition);
		} else {
			// database contains data, leave it alone.
		}

	}

	/**
	 * Create an empty competition.
	 * Set-up the defaults for using the timekeeping and refereeing features.
	 * @param competition 
	 * 
	 * @param sess
	 */
	protected static void setupEmptyCompetition(Competition competition, EntityManager sess) {
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
		URL templateUrl = platform1.getClass().getResource("/templates/teamResults/TeamResultSheetTemplate_Standard.xls");
		try {
			templateFile = new File(templateUrl.toURI());
			competition.setResultTemplateFileName(templateFile.getCanonicalPath());
		} catch (URISyntaxException e) {
			templateFile = new File(templateUrl.getPath());
		} catch (IOException e) {
		}

		sess.persist(platform1);
		CompetitionSession groupA = new CompetitionSession("A", null, null); //$NON-NLS-1$
		sess.persist(groupA);
		CompetitionSession groupB = new CompetitionSession("B", null, null); //$NON-NLS-1$#
		sess.persist(groupB);
		CompetitionSession groupC = new CompetitionSession("C", null, null); //$NON-NLS-1$
		sess.persist(groupC);
	}

	/**
	 * @param competition 
	 * @param liftersToLoad
	 * @param sess
	 * @param w
	 * @param c
	 */
	protected static void setupTestData(Competition competition, int liftersToLoad,
			EntityManager sess, Calendar w, Calendar c) {
		Platform platform1 = new Platform("Gym 1"); //$NON-NLS-1$
		sess.persist(platform1);
		Platform platform2 = new Platform("Gym 2"); //$NON-NLS-1$
		sess.persist(platform1);
		sess.persist(platform2);
		CompetitionSession groupA = new CompetitionSession("A", w.getTime(), c.getTime()); //$NON-NLS-1$
		groupA.setPlatform(platform1);
		CompetitionSession groupB = new CompetitionSession("B", w.getTime(), c.getTime()); //$NON-NLS-1$
		groupB.setPlatform(platform2);
		CompetitionSession groupC = new CompetitionSession("C", w.getTime(), c.getTime()); //$NON-NLS-1$
		groupC.setPlatform(platform1);
		sess.persist(groupA);
		sess.persist(groupB);
		sess.persist(groupC);
		insertSampleLifters(liftersToLoad, sess, groupA, groupB, groupC);
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

	/*
	 * We implement HbnSessionManager as a convenience; when a domain class
	 * needs access to persistance, and we don't want to pass in another
	 * HbnSessionManager such as the application, we use this one. (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager#getHbnSession()
	 */
	@Override
	public EntityManager getHbnSession() {
		return getSessionFactory().createEntityManager();
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		WebApplicationConfiguration.getSessionFactory().close(); // Free all
		// templates,
		// should free
		// H2
		h2Shutdown();
		if (necDisplay != null) {
			necDisplay.close();
		}
		necDisplay = null;
		logger.debug("contextDestroyed() done"); //$NON-NLS-1$
	}

	/**
	 * Try to shutdown H2 cleanly.
	 */
	private void h2Shutdown() {
		Connection connection = null;
		try {
			connection = cnf.buildSettings().getConnectionProvider().getConnection();
			Statement stmt = connection.createStatement();
			try {
				stmt.execute("SHUTDOWN"); //$NON-NLS-1$
			} finally {
				stmt.close();
			}
		} catch (HibernateException e) {
			LoggerUtils.logException(logger, e);
		} catch (SQLException e) {
			LoggerUtils.logException(logger, e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					LoggerUtils.logException(logger, e);
				}
			}
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		ServletContext sCtx = arg0.getServletContext();
		String dbPath = sCtx.getInitParameter("dbPath"); //$NON-NLS-1$
		String appName = sCtx.getServletContextName();
		if (dbPath != null) {
			WebApplicationConfiguration.getSessionFactory(TEST_MODE, dbPath).createEntityManager();
		} else {
			WebApplicationConfiguration.getSessionFactory(TEST_MODE, "db/" + appName).createEntityManager(); //$NON-NLS-1$
		}
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

}
