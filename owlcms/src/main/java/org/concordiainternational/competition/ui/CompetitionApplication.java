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

package org.concordiainternational.competition.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.SocketException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.LocalizedSystemMessages;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.spreadsheet.OutputSheet;
import org.concordiainternational.competition.spreadsheet.OutputSheetStreamSource;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.components.Menu;
import org.concordiainternational.competition.utils.Localized;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.vaadin.artur.icepush.ICEPush;
import org.vaadin.soundplayer.SoundPlayer;

import com.vaadin.Application;
import com.vaadin.data.Buffered;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;
import com.vaadin.event.ListenerMethod;
import com.vaadin.service.ApplicationContext;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.SystemError;
import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.URIHandler;
import com.vaadin.terminal.UserError;
import com.vaadin.terminal.VariableOwner;
import com.vaadin.terminal.gwt.server.ChangeVariablesErrorEvent;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class CompetitionApplication extends Application implements HbnSessionManager, UserActions, Serializable  {
    private static final long serialVersionUID = -1774806616519381075L;

    private static boolean USE_BROWSER_LANGUAGE = true; // ignore preferences
                                                         // received from
                                                         // browser

    private static Logger logger = LoggerFactory.getLogger(CompetitionApplication.class);
    public static XLogger traceLogger = XLoggerFactory.getXLogger("Tracing"); //$NON-NLS-1$

    private static LocalizedSystemMessages localizedMessages;
    private static InheritableThreadLocal<CompetitionApplication> current = new InheritableThreadLocal<CompetitionApplication>();
    
    

    public CompetitionApplication() {
		super();
        current.set(this);
        logger.warn("new application {}",this);
	}
    
	/**
     * @return the current application.
     */
    public static CompetitionApplication getCurrent() {
        return current.get();
    }
    /**
     * @return the current application.
     */
    public static Locale getCurrentLocale() {
        // ignore the preference received from the browser.
        if (current == null) {
            // logger.warn("current locale={}",getDefaultLocale());
            return getDefaultLocale();
        }
        final CompetitionApplication current2 = getCurrent();
        if (current2 == null) {
            // logger.warn("current locale={}",getDefaultLocale());
            return getDefaultLocale();
        }
        // logger.warn("current locale={}",current2.getLocale());
        return current2.getLocale();
    }

    /**
     * return the default locale to whoever needs it.
     */
    public static Locale getDefaultLocale() {
        final String defaultLanguage = Messages.getString("Locale.defaultLanguage", Locale.getDefault()); //$NON-NLS-1$
        final String defaultCountry = Messages.getString("Locale.defaultCountry", Locale.getDefault()); //$NON-NLS-1$
        Locale locale = Locale.getDefault();
        if (defaultCountry == null) {
            locale = new Locale(defaultLanguage);
        } else {
            locale = new Locale(defaultLanguage, defaultCountry);
        }
        return locale;
    }

    /**
     * @param throwable
     * 
     * @return
     */
    public static Throwable getRootCause(Throwable throwable, Locale locale) {
        Throwable cause = throwable.getCause();
        while (cause != null) {
            throwable = cause;
            cause = throwable.getCause();
        }
        if (throwable instanceof Localized) {
            ((Localized) throwable).setLocale(locale);
        }
        return throwable;
    }
    /**
     * Gets the SystemMessages for this application. SystemMessages are used to
     * notify the user of various critical situations that can occur, such as
     * session expiration, client/server out of sync, and internal server error.
     * 
     * Note: this method is static; we need to call
     * {@link LocalizedSystemMessages#setThreadLocale(Locale)} to change the
     * language that will be used for this thread.
     * 
     * @return the LocalizedSystemMessages for this application
     */
    public static SystemMessages getSystemMessages() {
        if (localizedMessages == null) localizedMessages = new LocalizedSystemMessages() {
            private static final long serialVersionUID = -8705748397362824437L;

            @Override
            protected Locale getDefaultSystemMessageLocale() {
                return getDefaultLocale();
            }
        };
        return localizedMessages;
    }

    /**
     * Set current application (used for junit tests).
     * 
     * @param current
     */
    public static void setCurrent(CompetitionApplication current) {
        CompetitionApplication.current.set(current);
    }

    transient private CompetitionSession currentGroup;

    transient public CompetitionApplicationComponents components;

    private UriFragmentUtility uriFragmentUtility = new UriFragmentUtility();

    /*
     * Display views
     */

    private SoundPlayer buzzer = new SoundPlayer();

    private ICEPush pusher;

	private TransactionListener httpRequestListener;

    public void displayRefereeConsole(int refereeIndex) {
        final RefereeConsole view = (RefereeConsole) components
                .getViewByName(CompetitionApplicationComponents.REFEREE_CONSOLE, false);
        view.setRefereeIndex(refereeIndex);
        setMainLayoutContent(view);
        uriFragmentUtility.setFragment(view.getFragment(), false);
    }

    /**
     * @param viewName
     */
    public void doDisplay(String viewName) {
        ApplicationView view = components.getViewByName(viewName, false);
        setMainLayoutContent(view);
        uriFragmentUtility.setFragment(view.getFragment(), false);
    }

    /**
     * @return
     */
    public ICEPush ensurePusher() {
        if (pusher == null) {
            pusher = new ICEPush();
            getMainWindow().addComponent(pusher);
        }
        return pusher;
    }

    /**
     * @return the buzzer
     */
    public SoundPlayer getBuzzer() {
        return buzzer;
    }

    public CompetitionSession getCurrentCompetitionSession() {
        return currentGroup;
    }

    /**
     * Used to get current Hibernate session. Also ensures an open Hibernate
     * transaction.
     */
    public Session getHbnSession() {
        Session currentSession = getCurrentSession();
        if (!currentSession.getTransaction().isActive()) {
            currentSession.beginTransaction();
        }
        return currentSession;
    }

    /**
     * @param platformName
     * @return
     */
    public GroupData getMasterData(String platformName) {
        GroupData masterData = GroupData.getInstance(platformName);
        if (masterData != null) {
            return masterData;
        } else {
            logger.error("Master data for platformName " + platformName + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
    }

    public Platform getPlatform() {
        return components.getPlatform();
    }

    public String getPlatformName() {
        return components.getPlatformName();
    }

    /**
     * Try to load a resource, accounting for the wide variations between run-time environments.
     * Accounts for Tomcat, Eclipse, Jetty.
     * @param path
     * @return
     * @throws IOException
     */
    public InputStream getResourceAsStream(String path) throws IOException {
        InputStream resourceAsStream;
              
        // first, search using class loader
        resourceAsStream = this.getClass().getResourceAsStream("/" + path); //$NON-NLS-1$
        if (resourceAsStream == null) {
        	resourceAsStream = this.getClass().getResourceAsStream("/templates" + path); //$NON-NLS-1$
        }
        
        // if not found, try using the servlet context
        final ServletContext servletContext = getServletContext();
        if (resourceAsStream == null && servletContext != null) {
            resourceAsStream = servletContext.getResourceAsStream("/WEB-INF/classes/templates" + path); //$NON-NLS-1$
        }
        if (resourceAsStream == null && servletContext != null) {
            resourceAsStream = servletContext.getResourceAsStream("/WEB-INF/classes" + path); //$NON-NLS-1$
        }
        if (resourceAsStream == null)
            throw new IOException(
                    Messages.getString("CompetitionApplication.ResourceNotFoundOnClassPath", getLocale()) + path); //$NON-NLS-1$
        return resourceAsStream;
    }

    /**
     * Return the servlet container context to allow various HttpSessions to
     * communicate with one another (e.g. sharing data structures such as the
     * lifting order).
     */
    public ServletContext getServletContext() {
        ApplicationContext ctx = getContext();
        if (ctx == null) return null;
        final ServletContext sCtx = ((WebApplicationContext) ctx).getHttpSession().getServletContext();
        return sCtx;
    }

    /**
     * @return the uriFragmentUtility
     */
    public UriFragmentUtility getUriFragmentUtility() {
        return uriFragmentUtility;
    }

    @Override
    public void init() {    	
        // ignore the preference received from the browser.
        if (!USE_BROWSER_LANGUAGE) this.setLocale(getDefaultLocale());

        if (components == null) {
        	components = new CompetitionApplicationComponents(new Panel(), null, null);
        }

        // set system message language if any are shown while "init" is running.
        LocalizedSystemMessages msg = (LocalizedSystemMessages) getSystemMessages();
        msg.setThreadLocale(this.getLocale());

        // the following defines what will happen before and after each http
        // request.
        if (httpRequestListener == null) {
        	httpRequestListener = attachHttpRequestListener();
        }

        // create the initial look.
        buildMainLayout();
    }

    /**
     * @param streamSource
     * @param filename
     */
    public void openSpreadsheet(OutputSheetStreamSource<? extends OutputSheet> streamSource, final String filename) {
        StreamResource streamResource = new StreamResource(streamSource, filename + ".xls", this); //$NON-NLS-1$
        streamResource.setCacheTime(5000); // no cache (<=0) does not work with
                                           // IE8
        streamResource.setMIMEType("application/x-msexcel"); //$NON-NLS-1$
        this.getMainWindow().open(streamResource, "_top"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.concordiainternational.competition.UserActions#setCurrentGroup(org
     * .concordiainternational.competition.data.Group)
     */
    public void setCurrentCompetitionSession(CompetitionSession value) {
        currentGroup = value;
        final ApplicationView currentView = components.currentView;
        if (currentView != null) {
            if (currentView instanceof EditingView) {
                ((EditingView) currentView).setCurrentGroup(value);
            } else {
                currentView.refresh();
            }
            setMainLayoutContent(currentView);
        }
    }

    public void setMainLayoutContent(ApplicationView c) {
        this.components.currentView = c;
        boolean needsMenu = c.needsMenu();
		this.components.menu.setVisible(needsMenu);
        this.components.mainPanel.setContent(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.concordiainternational.competition.UserActions#setPlatform(java.lang
     * .String)
     */
    public void setPlatformByName(String platformName) {
        components.setPlatformByName(platformName);
    }

    /**
     * <p>
     * Invoked by the terminal on any exception that occurs in application and
     * is thrown by the <code>setVariable</code> to the terminal. The default
     * implementation sets the exceptions as <code>ComponentErrors</code> to the
     * component that initiated the exception and prints stack trace to standard
     * error stream.
     * </p>
     * <p>
     * You can safely override this method in your application in order to
     * direct the errors to some other destination (for example log).
     * </p>
     * 
     * @param event
     *            the change event.
     * @see com.vaadin.terminal.Terminal.ErrorListener#terminalError(com.vaadin.terminal.Terminal.ErrorEvent)
     */
    @Override
    public void terminalError(Terminal.ErrorEvent event) {
        Throwable t = event.getThrowable();
        if (t instanceof SocketException) {
            // Most likely client browser closed socket
            logger.debug(Messages.getString("CompetitionApplication.SocketException", getLocale()) //$NON-NLS-1$
                + Messages.getString("CompetitionApplication.BrowserClosedSession", getLocale())); //$NON-NLS-1$
            return;
        }

        // Finds the original source of the error/exception
        Object owner = null;
        if (event instanceof VariableOwner.ErrorEvent) {
            owner = ((VariableOwner.ErrorEvent) event).getVariableOwner();
        } else if (event instanceof URIHandler.ErrorEvent) {
            owner = ((URIHandler.ErrorEvent) event).getURIHandler();
        } else if (event instanceof ParameterHandler.ErrorEvent) {
            owner = ((ParameterHandler.ErrorEvent) event).getParameterHandler();
        } else if (event instanceof ChangeVariablesErrorEvent) {
            owner = ((ChangeVariablesErrorEvent) event).getComponent();
        }

        Throwable rootCause = CompetitionApplication.getRootCause(t, getLocale());
        logger.debug("rootCause: {} locale={}", rootCause, getLocale()); //$NON-NLS-1$
        String message = rootCause.getMessage();
        if (message == null) message = t.toString();

        logger.debug("t.class: {} locale={}", t.getClass().getName(), getLocale()); //$NON-NLS-1$
        if ((t instanceof Buffered.SourceException || t instanceof ListenerMethod.MethodException || t instanceof RuleViolationException)) {
            // application-level exception, some of which are not worth logging
            // rule violations exceptions are one example.
            if (!(rootCause instanceof RuleViolationException)) {
                // log error cleanly with its stack trace.
                StringWriter sw = new StringWriter();
                rootCause.printStackTrace(new PrintWriter(sw));
                logger.error("root cause \n" + sw.toString()); //$NON-NLS-1$
            }
        } else {
            if (t instanceof SystemError) {
                if (getMainWindow() != null) {
                    getMainWindow().showNotification(Messages.getString("RuleValidation.error", getLocale()), //$NON-NLS-1$
                        message, Notification.TYPE_ERROR_MESSAGE);
                }
            }
            // super.terminalError(event);
            // log error cleanly with its stack trace.
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            logger.error("exception: \n" + sw.toString()); //$NON-NLS-1$
            // return;
        }

        // Shows the error in AbstractComponent
        logger.debug("owner = {}", (owner == null ? "null!" : owner.getClass().toString()
            + System.identityHashCode(owner)));
        if ((owner != null && owner instanceof AbstractComponent && !(owner instanceof AbstractComponentContainer))) {
            logger.debug("case1A: showing component error for {}", message);
            if (rootCause instanceof ErrorMessage) {
                ((AbstractComponent) owner).setComponentError((ErrorMessage) rootCause);
            } else {
                ((AbstractComponent) owner).setComponentError(new UserError(message));
            }
            logger.debug("case1B: notification for {}", message);
            // also as notification
            if (getMainWindow() != null) {
                getMainWindow().showNotification(Messages.getString("RuleValidation.error", getLocale()), //$NON-NLS-1$
                    message, Notification.TYPE_ERROR_MESSAGE);
            }
        } else {
            logger.debug("case2: showing notification for {}", message);
            if (getMainWindow() != null) {
                getMainWindow().showNotification(Messages.getString("RuleValidation.error", getLocale()), //$NON-NLS-1$
                    message, Notification.TYPE_ERROR_MESSAGE);
            }
        }
    }

    /**
     * Vaadin "transaction" equals "http request". This method fires for all
     * servlets in the session.
     */
    private TransactionListener attachHttpRequestListener() {
        TransactionListener listener = new TransactionListener() {
            private static final long serialVersionUID = -2365336986843262181L;

            public void transactionEnd(Application application, Object transactionData) {
                // Transaction listener gets fired for all (Http) sessions
                // of Vaadin applications, checking to be this one.
                if (application == CompetitionApplication.this) {
                    closeHibernateSession();
                }
            }

            public void transactionStart(Application application, Object transactionData) {
                ((LocalizedSystemMessages) getSystemMessages()).setThreadLocale(getLocale());
                current.set(CompetitionApplication.this); // make the
                                                          // application
                                                          // available via
                                                          // ThreadLocal
                HttpServletRequest request = (HttpServletRequest) transactionData;
                checkURI(request.getRequestURI());
                request.getSession(true).setMaxInactiveInterval(3600);
            }
        };
		getContext().addTransactionListener(listener);
		return listener;
    }

    /**
     * Create the main layout.
     */
    private void buildMainLayout() {
        components.mainWindow = new Window(Messages.getString("CompetitionApplication.Title", getLocale())); //$NON-NLS-1$
        setMainWindow(components.mainWindow);

        setTheme("competition"); //$NON-NLS-1$
        VerticalLayout mainLayout = new VerticalLayout();

        // back and bookmark processing -- look at the presence of a fragment in
        // the URL
        mainLayout.addComponent(uriFragmentUtility);
        uriFragmentUtility.setFragment("competitionEditor");
        uriFragmentUtility.addListener(new FragmentChangedListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void fragmentChanged(FragmentChangedEvent source) {
                //logger.warn("fragment {}",source.getUriFragmentUtility().getFragment());
                String frag = source.getUriFragmentUtility().getFragment();
                displayView(frag);
            }

        });


        // include a sound player
        mainLayout.addComponent(buzzer);

        mainLayout.setSizeFull();
        mainLayout.setStyleName("blue"); //$NON-NLS-1$

        components.menu = new Menu();
        mainLayout.addComponent(components.menu);
        components.menu.setVisible(false);
        mainLayout.addComponent(components.mainPanel);
        mainLayout.setExpandRatio(components.mainPanel, 1);
        components.mainPanel.setSizeFull();
        getMainWindow().setContent(mainLayout);
    }

    /**
     * @param uri
     * 
     */
    private void checkURI(String uri) {
        if (!uri.contains("/app/")) {
            logger.error("missing trailing / after app : {}", uri);
            getMainWindow().showNotification(
                Messages.getString("CompetitionApplication.invalidURL", getLocale()) + "<br>", //$NON-NLS-1$
                Messages.getString("CompetitionApplication.invalidURLExplanation", getLocale()),
                Notification.TYPE_ERROR_MESSAGE);
        }
    }

    private void closeHibernateSession() {
        Session sess = getCurrentSession();
        if (sess.getTransaction().isActive()) {
            sess.getTransaction().commit();
            if (sess.isOpen()) sess.flush();
        }
        if (sess.isOpen()) {
            sess.close();
        }
    }

    /**
     * Get a Hibernate session so objects can be stored. If running as a junit
     * test, we call the session factory with parameters that tell it not to
     * persist the database.
     * 
     * @return a Hibernate session
     * @throws HibernateException
     */
    private Session getCurrentSession() throws HibernateException {
        return WebApplicationConfiguration.getSessionFactory().getCurrentSession();
    }

    protected void displayView(String frag) {
        logger.warn("request to display view {}", frag); //$NON-NLS-1$
        ApplicationView view = components.getViewByName(frag, true); // initialize from URI fragment
        setMainLayoutContent(view);
    }
    
}

