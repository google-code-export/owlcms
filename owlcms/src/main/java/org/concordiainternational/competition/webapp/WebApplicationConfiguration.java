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

package org.concordiainternational.competition.webapp;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.concordiainternational.competition.data.Category;
import org.concordiainternational.competition.data.Competition;
import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.nec.NECDisplay;
import org.concordiainternational.competition.utils.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;
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

    private static SessionFactory sessionFactory = null;
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
    public static SessionFactory getSessionFactory() {
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
    public static SessionFactory getSessionFactory(boolean testMode, String dbPath) {
        if (sessionFactory == null) {
            try {
                cnf = new AnnotationConfiguration();
                h2Setup(testMode, dbPath, cnf);
                cnf.setProperty(Environment.USER, "sa"); //$NON-NLS-1$
                cnf.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread"); //$NON-NLS-1$
                cnf.setProperty(Environment.CACHE_PROVIDER, "org.hibernate.cache.EhCacheProvider"); //$NON-NLS-1$
                // cnf.setProperty(Environment.CACHE_PROVIDER,
                // "org.hibernate.cache.HashtableCacheProvider");

                // the following line is necessary because the Lifter class uses
                // the Lift class
                // several times (one for each lift), which would normally force
                // us to override
                // the column names to ensure they are unique. Hibernate
                // supports this with an
                // extension.
                // cnf.setNamingStrategy(DefaultComponentSafeNamingStrategy.INSTANCE);

                // the classes we store in the database.
                cnf.addAnnotatedClass(Lifter.class);
                cnf.addAnnotatedClass(CompetitionSession.class);
                cnf.addAnnotatedClass(Platform.class);
                cnf.addAnnotatedClass(Category.class);
                cnf.addAnnotatedClass(Competition.class);
                cnf.addAnnotatedClass(CompetitionSession.class);

                // listeners
                cnf.setListener("merge", new OverrideMergeEventListener());

                sessionFactory = cnf.buildSessionFactory();
                // create the standard categories, etc.
                // if argument is > 0, create sample data as well.
                Session sess = sessionFactory.getCurrentSession();
                sess.beginTransaction();
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
     * @param cnf
     */
    private static void h2Setup(boolean testMode, String dbPath, AnnotationConfiguration cnf) {
        cnf.setProperty(Environment.DRIVER, "org.h2.Driver"); //$NON-NLS-1$
        if (testMode) {
            cnf.setProperty(Environment.URL, "jdbc:h2:mem:competition"); //$NON-NLS-1$
            cnf.setProperty(Environment.SHOW_SQL, "false"); //$NON-NLS-1$
            cnf.setProperty(Environment.HBM2DDL_AUTO, "create-drop"); //$NON-NLS-1$
        } else {
            cnf.setProperty(Environment.SHOW_SQL, "false"); //$NON-NLS-1$
            cnf.setProperty(Environment.URL, "jdbc:h2:file:" + dbPath); //$NON-NLS-1$
            String ddlMode = "create";
            File file = new File(dbPath + ".h2.db"); //$NON-NLS-1$
            logger.warn("Using Hibernate (file {} exists={}", new Object[] { file.getAbsolutePath(), Boolean.toString(file.exists()) }); //$NON-NLS-1$
            if (file.exists()) {
                ddlMode = "update"; //$NON-NLS-1$
            } else {
            	file = new File(dbPath + ".data.db"); //$NON-NLS-1$
            	logger.warn("Using Hibernate (file {} exists={}", new Object[] { file.getAbsolutePath(), Boolean.toString(file.exists()) }); //$NON-NLS-1$
                if (file.exists()) {
                    ddlMode = "update"; //$NON-NLS-1$
                }
            }
            logger.warn(
                        "Using Hibernate mode {} (file {} exists={}", new Object[] { ddlMode, file.getAbsolutePath(), Boolean.toString(file.exists()) }); //$NON-NLS-1$
            cnf.setProperty(Environment.HBM2DDL_AUTO, ddlMode);
            // throw new
            // ExceptionInInitializerError("Production database configuration not specified");
        }
        cnf.setProperty(Environment.DIALECT, H2Dialect.class.getName());
    }

    /**
     * Insert initial data if the database is empty.
     * 
     * @param liftersToLoad
     * @param sess
     * @param testMode
     */
    public static void insertInitialData(int liftersToLoad, org.hibernate.Session sess, boolean testMode) {
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
            sess.save(competition);

            Platform platform1 = new Platform("Gym 1"); //$NON-NLS-1$
            Platform platform2 = new Platform("Gym 2"); //$NON-NLS-1$
            sess.save(platform1);
            sess.save(platform2);

            Calendar w = Calendar.getInstance();
            w.set(Calendar.MILLISECOND, 0);
            w.set(Calendar.SECOND, 0);
            w.set(Calendar.MINUTE, 0);
            w.set(Calendar.HOUR_OF_DAY, 8);
            Calendar c = (Calendar) w.clone();
            c.add(Calendar.HOUR_OF_DAY, 2);

            CompetitionSession groupA = new CompetitionSession("A", w.getTime(), c.getTime()); //$NON-NLS-1$
            groupA.setPlatform(platform1);
            sess.save(groupA);
            CompetitionSession groupB = new CompetitionSession("B", w.getTime(), c.getTime()); //$NON-NLS-1$
            groupB.setPlatform(platform2);
            sess.save(groupB);
            CompetitionSession groupC = new CompetitionSession("C", w.getTime(), c.getTime()); //$NON-NLS-1$
            groupC.setPlatform(platform1);
            sess.save(groupC);

            if (testMode) insertSampleLifters(liftersToLoad, sess, groupA, groupB, groupC);
        } else {
            // database contains data, leave it alone.
        }

    }

    private static void insertSampleLifters(int liftersToLoad, org.hibernate.Session sess, CompetitionSession groupA, CompetitionSession groupB,
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
            sess.save(p);
            // System.err.println("group A - "+LifterReader.toString(p));
        }
        for (int i = 0; i < liftersToLoad; i++) {
            Lifter p = new Lifter();
            p.setCompetitionSession(groupB);
            p.setFirstName(fnames[r.nextInt(fnames.length)]);
            p.setLastName(lnames[r.nextInt(lnames.length)]);
            sess.save(p);
            // System.err.println("group B - "+LifterReader.toString(p));
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
    public Session getHbnSession() {
        return getSessionFactory().getCurrentSession();
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        WebApplicationConfiguration.getSessionFactory().close(); // Free all
                                                                 // templates,
                                                                 // should free
                                                                 // H2
        h2Shutdown();
        necDisplay.close();
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
            connection.createStatement().execute("SHUTDOWN");
            // logger.warn("orderly shutdown done.");
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
            WebApplicationConfiguration.getSessionFactory(TEST_MODE, dbPath).getCurrentSession();
        } else {
            WebApplicationConfiguration.getSessionFactory(TEST_MODE, "db/" + appName).getCurrentSession(); //$NON-NLS-1$
        }
        if (necDisplay != null) {
            necDisplay.close();
            necDisplay = null;
        }
        final String comPortName = sCtx.getInitParameter("comPort"); //$NON-NLS-1$
        getNecDisplay(comPortName);
        Locale.setDefault(Locale.CANADA_FRENCH);
        logger.info("Default locale: {}", Locale.getDefault());
        logger.debug("contextInitialized() done"); //$NON-NLS-1$
    }

    /**
     * @param comPortName
     * @throws RuntimeException
     */
    public static void getNecDisplay(final String comPortName) throws RuntimeException {
        try {
            necDisplay = new NECDisplay(comPortName);
        } catch (Exception e) {
            // comPortName is likely a USB port which has been disconnected.
            try {
                necDisplay = new NECDisplay("COM1"); //$NON-NLS-1$
            } catch (Exception e1) {
                throw new RuntimeException(e);
            }
        }
    }

}
