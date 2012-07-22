/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.SocketException;
import java.net.URL;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.concordiainternational.competition.data.CompetitionSession;
import org.concordiainternational.competition.data.Platform;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.LocalizedSystemMessages;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.mobile.MJuryConsole;
import org.concordiainternational.competition.mobile.MRefereeConsole;
import org.concordiainternational.competition.mobile.MobileMenu;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.concordiainternational.competition.ui.components.Menu;
import org.concordiainternational.competition.ui.components.ResultFrame;
import org.concordiainternational.competition.utils.Localized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.Application;
import com.vaadin.data.Buffered;
import com.vaadin.event.ListenerMethod;
import com.vaadin.service.ApplicationContext;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
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

public class CompetitionApplication extends Application implements UserActions, Serializable  {
    private static final long serialVersionUID = -1774806616519381075L;

    /**
     * if true, use the language set in the browser
     */
    final private static boolean USE_BROWSER_LANGUAGE = true;

    private static Logger logger = LoggerFactory.getLogger(CompetitionApplication.class);
    public static XLogger traceLogger = XLoggerFactory.getXLogger("Tracing"); //$NON-NLS-1$

    private static LocalizedSystemMessages localizedMessages;
    
    private static InheritableThreadLocal<CompetitionApplication> current = new InheritableThreadLocal<CompetitionApplication>();
    
    // these are set for each service() or each testing thread.
    private static InheritableThreadLocal<EntityManager> entityManager = new InheritableThreadLocal<EntityManager>();
    private static InheritableThreadLocal<EntityManagerFactory> entityManagerFactory = new InheritableThreadLocal<EntityManagerFactory>();
    

    public CompetitionApplication() {
		super();
        current.set(this);
        logger.debug("new application {} {}",this, this.getLocale());
	}
    
    public CompetitionApplication(String suffix) {
		super();
        current.set(this);
        setAppSuffix(suffix);
        logger.debug("new application {} {}",this);
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
        final CompetitionApplication current2 = getCurrent();
        if (current2 == null) {
            //logger.warn("*current locale={}",getDefaultLocale());
            return getDefaultLocale();
        }
        //logger.warn("current locale={}",current2.getLocale());
        return current2.getLocale();
    }

    /**
     * @return the current application.
     */
    public static Locale getCurrentSupportedLocale() {
        final CompetitionApplication current2 = getCurrent();
        if (current2 == null) {
            return getDefaultLocale();
        }
        final Locale currentLocale =  current2.getLocale();
        
        // compare the current language with code retrieved from bundle. If the
        // code is different, then the language is not directly translated.
		final String languageCode = Messages.getString("Locale.languageCode", currentLocale); //$NON-NLS-1$
		logger.info("Locale.languageCode({})={}",currentLocale,languageCode);
        if (currentLocale.getLanguage().equals(languageCode)) {
        	return currentLocale; 
        } else {
        	return getDefaultLocale();
        }
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

    protected UriFragmentUtility uriFragmentUtility = new UriFragmentUtility();

    /*
     * Display views
     */

//    private SoundPlayer buzzer = new SoundPlayer();

    protected ICEPush pusher = null;

	protected TransactionListener httpRequestListener;

	protected boolean pusherDisabled = false;

	private String appSuffix = "/app/";

	private Panel mobilePanel;

	private boolean layoutCreated;

	private MobileMenu mobileMenu;

	protected String contextURI;

	private VerticalLayout mainLayout;

	private ApplicationView mainLayoutContent;

    public void displayRefereeConsole(int refereeIndex) {
        final ORefereeConsole view = (ORefereeConsole) components
                .getViewByName(CompetitionApplicationComponents.OREFEREE_CONSOLE, false);
        view.setIndex(refereeIndex);
        setMainLayoutContent(view);	
        uriFragmentUtility.setFragment(view.getFragment(), false);
    }
    
    public void displayOJuryConsole(int refereeIndex) {
        final ORefereeConsole view = (ORefereeConsole) components
                .getViewByName(CompetitionApplicationComponents.OJURY_CONSOLE, false);
        view.setIndex(refereeIndex);
        setMainLayoutContent(view);	
        uriFragmentUtility.setFragment(view.getFragment(), false);
    }
    
    public void displayMRefereeConsole(int refereeIndex) {
        final MRefereeConsole view = (MRefereeConsole) components
                .getViewByName(CompetitionApplicationComponents.MREFEREE_CONSOLE, false);
        view.setIndex(refereeIndex);
        setMainLayoutContent(view);	
        uriFragmentUtility.setFragment(view.getFragment(), false);
    }
    
    public void displayMJuryConsole(int refereeIndex) {
        final MJuryConsole view = (MJuryConsole) components
                .getViewByName(CompetitionApplicationComponents.MJURY_CONSOLE, false);
        view.setIndex(refereeIndex);
        setMainLayoutContent(view);	
        uriFragmentUtility.setFragment(view.getFragment(), false);
    }

    /**
     * @param viewName
     */
    public void doDisplay(String viewName) {
//    	logger.debug("doDisplay {}",viewName);

        ApplicationView view = components.getViewByName(viewName, false);
        // remove all listeners on current view.
        getMainLayoutContent().unregisterAsListener();
        setMainLayoutContent(view);
        uriFragmentUtility.setFragment(view.getFragment(), false);
    }
    
    /**
     * @param viewName
     */
    public void displayProjector(String viewName, String stylesheet) {
//    	logger.debug("doDisplay {}",viewName);
        ResultFrame view = (ResultFrame) components.getViewByName(viewName, false);
        setMainLayoutContent(view);
        view.setStylesheet(stylesheet);
        view.refresh();
        uriFragmentUtility.setFragment(view.getFragment(), false);
    }

    /**
     * @return
     */
    synchronized protected ICEPush ensurePusher() {
        if (pusher == null) {
            pusher = new ICEPush();
            getMainWindow().addComponent(pusher);
        }
        return pusher;
    }

    synchronized public void push() {
    	pusher = this.ensurePusher();
    	if (!pusherDisabled) {
    		logger.debug("pushing with {} on window {}",pusher,getMainWindow());
    		pusher.push();
    	}
    }
    
    public void setPusherDisabled(boolean disabled) {
    	pusherDisabled = disabled;
    }
    
    public boolean getPusherDisabled() {
    	return pusherDisabled;
    }
    
//    /**
//     * @return the buzzer
//     */
//    public SoundPlayer getBuzzer() {
//        return buzzer;
//    }

    public CompetitionSession getCurrentCompetitionSession() {
    	if (currentGroup != null) {
			final String name = currentGroup.getName();
			MDC.put("currentGroup", name);
    	} else {
    		MDC.put("currentGroup", "-");
    	}
        return currentGroup;
    }

    /**
     * Used to get current Hibernate session. Also ensures an open Hibernate
     * transaction.
     */
	public EntityManager startTransaction() {
        EntityManager currentSession = getEntityManager();
        if (!currentSession.getTransaction().isActive()) {
            currentSession.getTransaction().begin();
        }
        return currentSession;
    }
    
    private void endTransaction() {
        EntityManager sess = getEntityManager();
        if (sess.getTransaction().isActive()) {
            sess.getTransaction().commit();
            if (sess.isOpen()) sess.flush();
        }
        if (sess.isOpen()) {
            sess.close();
        }
    }

    
    /**
     * @param platformName
     * @return
     */
    public SessionData getMasterData() {
    	
    	String platformName = getPlatformName();
    	if (platformName == null) {
    		platformName = CompetitionApplicationComponents.initPlatformName();
    	}
		return getMasterData(platformName);
    }
    
    /**
     * @param platformName
     * @return
     */
    public SessionData getMasterData(String platformName) {
        SessionData masterData = SessionData.getSingletonForPlatform(platformName);
        if (masterData != null) {
        	masterData.getCurrentSession();
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
//        logger.debug("classpath: {}",System.getProperty("java.class.path"));
              
        // first, search using class loader
        resourceAsStream = this.getClass().getResourceAsStream(path); //$NON-NLS-1$
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
        sharedInit();

        // create the initial look.
        buildMainLayout();
    }

	/**
	 * 
	 */
	public void sharedInit() {
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
	}

    /**
     * @param streamSource
     * @param filename
     */
    @Override
	public void openSpreadsheet(StreamResource.StreamSource streamSource, final String filename) {
        StreamResource streamResource = new StreamResource(streamSource, filename + ".xls", this); //$NON-NLS-1$
        streamResource.setCacheTime(5000); // no cache (<=0) does not work with IE8
        streamResource.setMIMEType("application/x-msexcel"); //$NON-NLS-1$
//        WebApplicationContext webAppContext = (WebApplicationContext)this.getContext();
//        if (webAppContext.getBrowser().isChrome()) {
//            this.getMainWindow().open(streamResource, "_blank"); //$NON-NLS-1$
//        } else {
            this.getMainWindow().open(streamResource, "_top"); //$NON-NLS-1$        	
//    }

    }
    
	public void openPdf(StreamSource streamSource, final String filename) {
        StreamResource streamResource = new StreamResource(streamSource, filename + ".pdf", this); //$NON-NLS-1$
        streamResource.setCacheTime(5000); // no cache (<=0) does not work with IE8
        streamResource.setMIMEType("application/pdf"); //$NON-NLS-1$
        this.getMainWindow().open(streamResource, "_blank"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.concordiainternational.competition.UserActions#setCurrentGroup(org
     * .concordiainternational.competition.data.Group)
     */
    @Override
	public void setCurrentCompetitionSession(CompetitionSession newSession) {
        if (newSession != null) {      	
            final String name = newSession.getName();
			MDC.put("currentGroup", "*"+name);
        }
    	final CompetitionSession oldSession = currentGroup;
        currentGroup = newSession;
        final ApplicationView currentView = components.currentView;
        if (currentView != null) {
            if (currentView instanceof EditingView && oldSession != newSession) {
                ((EditingView) currentView).setCurrentSession(newSession);
            } else {
                currentView.refresh();
            }
            setMainLayoutContent(currentView);
        }

    }



    /*
     * (non-Javadoc)
     * 
     * @see
     * org.concordiainternational.competition.UserActions#setPlatform(java.lang
     * .String)
     */
    @Override
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
    protected TransactionListener attachHttpRequestListener() {
        TransactionListener listener = new TransactionListener() {
            private static final long serialVersionUID = -2365336986843262181L;

            @Override
			public void transactionEnd(Application application, Object transactionData) {
                // Transaction listener gets fired for all (Http) sessions
                // of Vaadin applications, checking to be this one.
                if (application == CompetitionApplication.this) {
                    endTransaction();
                }
                current.remove();
            }

            @Override
			public void transactionStart(Application application, Object transactionData) {
                ((LocalizedSystemMessages) getSystemMessages()).setThreadLocale(getLocale());
                current.set(CompetitionApplication.this); // make the application available via ThreadLocal
                HttpServletRequest request = (HttpServletRequest) transactionData;
                final String requestURI = request.getRequestURI();
				checkURI(requestURI);
                if (requestURI.contains("juryDisplay")) {
                	request.getSession(true).setMaxInactiveInterval(-1);
                } else {
                	request.getSession(true).setMaxInactiveInterval(3600);
                }
                getCurrentCompetitionSession(); // sets up the MDC
            }
        };
		getContext().addTransactionListener(listener);
		return listener;
    }

    /**
     * Create the main layout.
     */
    @SuppressWarnings("serial")
	private void buildMainLayout() {
        components.mainWindow = new Window(Messages.getString("CompetitionApplication.Title", getLocale())); //$NON-NLS-1$
        setMainWindow(components.mainWindow);
        mainLayout = new VerticalLayout();

        // back and bookmark processing -- look at the presence of a fragment in
        // the URL
        mainLayout.addComponent(uriFragmentUtility);

        uriFragmentUtility.addListener(new FragmentChangedListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void fragmentChanged(FragmentChangedEvent source) {
                logger.trace("fragmentChanged {}",source.getUriFragmentUtility().getFragment());
                String frag = source.getUriFragmentUtility().getFragment();
                displayView(frag);
            }
        });
        
        components.mainWindow.addURIHandler(new URIHandler() {
			@Override
			public DownloadStream handleURI(URL context, String relativeUri) {
				final String externalForm = context.toExternalForm();
				contextURI = externalForm;
//				logger.debug("handleURI uris: {} {}",externalForm,contextURI);
				if (layoutCreated) {
//					logger.debug("layout exists, skipping");
					return null; // already created layout
				} else {
//					logger.debug("creating layout");
				}

				if (contextURI.endsWith("/app/")){
//					LoggerUtils.logException(logger, new Exception("creating app layout !"+externalForm+" "+contextURI));
//					logger.debug("creating app layout");
					createAppLayout(mainLayout);
					if (relativeUri.isEmpty()) {
						setMainLayoutContent(components.getViewByName("competitionEditor", false));
					}
				} else if (contextURI.endsWith("/m/")) {
//					LoggerUtils.logException(logger, new Exception("creating mobile layout !"+externalForm+" "+contextURI));
					createMobileLayout(mainLayout);
					if (relativeUri.isEmpty()) {
						setMainLayoutContent(components.getViewByName("", false));
					}
				} else {
					throw new RuntimeException(Messages.getString("CompetitionApplication.invalidURL", getLocale()));
				}
				layoutCreated = true;
				return null;
			}});


    }



	/**
	 * @param mainLayout1
	 */
	private void createAppLayout(VerticalLayout mainLayout1) {
		mobilePanel = null;
        setTheme("competition"); //$NON-NLS-1$
        
//		// include a sound player
//        mainLayout1.addComponent(buzzer);

        mainLayout1.setSizeFull();
        //mainLayout1.setStyleName("blue"); //$NON-NLS-1$

        components.menu = new Menu();
        mainLayout1.addComponent(components.menu);
        components.menu.setVisible(false);
        
        mainLayout1.addComponent(components.mainPanel);
        mainLayout1.setExpandRatio(components.mainPanel, 1);
        components.mainPanel.setSizeFull();
        
        getMainWindow().setContent(mainLayout1);
	}
	
	/**
	 * @param mainLayout1
	 */
	protected void createMobileLayout(VerticalLayout mainLayout1) {
		components.mainPanel = null;
        setTheme("m");
        
        mainLayout1.setMargin(false,false,false,false);
        mainLayout1.setSizeFull();
        
        setMobileMenu(new MobileMenu());
        getMobileMenu().setSizeFull();
        mainLayout1.addComponent(getMobileMenu());

        mobilePanel = new Panel();
        mainLayout1.addComponent(mobilePanel);
        mobilePanel.setSizeFull();
        mobilePanel.setVisible(false);
        
        getMainWindow().setContent(mainLayout1);		
	}
	
    public void setMainLayoutContent(ApplicationView c) {
    	mainLayoutContent = c;
        boolean needsMenu = c.needsMenu();
    	//logger.debug(">>>>> setting app view for {} -- view {}",this,c);
        if (this.mobilePanel == null) {
            this.components.currentView = c;
    		final Menu menu = this.components.menu;
    		if (menu != null) menu.setVisible(needsMenu);
            this.components.mainPanel.setContent(c);
        } else {
        	getMobileMenu().setVisible(needsMenu);
        	getMobileMenu().setSizeUndefined();
        	mobilePanel.setVisible(true);
        	mobilePanel.setContent(c);
        	mainLayout.setExpandRatio(getMobileMenu(),0);
        	mainLayout.setExpandRatio(mobilePanel,100);
        }
    }
    
    public ApplicationView getMainLayoutContent() {
    	return mainLayoutContent;
    }
    
    /**
     * @param uri
     * 
     */
    private void checkURI(String uri) {
		if (uri.endsWith("/app") || uri.endsWith("/m")) {
            logger.error("missing trailing / after app : {}", uri);
            getMainWindow().showNotification(
                Messages.getString("CompetitionApplication.invalidURL", getLocale()) + "<br>", //$NON-NLS-1$
                Messages.getString("CompetitionApplication.invalidURLExplanation", getLocale()),
                Notification.TYPE_ERROR_MESSAGE);
        }
    }

    protected void displayView(String frag) {
        logger.debug("request to display view {}", frag); //$NON-NLS-1$
        ApplicationView view = components.getViewByName(frag, true); // initialize from URI fragment
        setMainLayoutContent(view);
    }

	public String getAppSuffix() {
		return appSuffix;
	}

	public void setAppSuffix(String appSuffix) {
		this.appSuffix = appSuffix;
	}

	public void setPusher(ICEPush pusher) {
		this.pusher = pusher;
	}

	public ICEPush getPusher() {
		return pusher;
	}

	public void setMobileMenu(MobileMenu mobileMenu) {
		this.mobileMenu = mobileMenu;
	}

	public MobileMenu getMobileMenu() {
		return mobileMenu;
	}


    /**
     * @return a thread-local entity manager
     */
    public static EntityManager getEntityManager() {
        return entityManager.get();
    }

    /**
     * Set a thread-local entity manager.
     * The entity manager is set for each request (or each test case)
     * @param em
     */
    public static void setEntityManager(EntityManager em) {
        entityManager.set(em);
    }

    public static void setEntityManagerFactory(EntityManagerFactory emf) {
        entityManagerFactory.set(emf);
    }
    
    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory.get();
    }
    
    public static void removeThreadLocals() {
        entityManager.remove();
        entityManagerFactory.remove();
        current.remove();
    }

    public static void setThreadLocals(EntityManagerFactory emf, EntityManager em) {
        setEntityManager(em);
        setEntityManagerFactory(emf);
        // note that current is set in initializer.
    }

    /**
     * Create a new entity manager, to be shared amongst several threads.
     * Such managers are not closed/flushed for each request, and must be managed
     * explicitly.  Use with care to avoid creating memory leaks.
     * 
     * @return a new entity manager
     */
    public static EntityManager getNewGlobalEntityManager() {
        //FIXME: create a list for cleanup in the webapp context.
        return getEntityManagerFactory().createEntityManager();
    }


}

