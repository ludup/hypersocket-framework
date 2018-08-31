/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;

import com.hypersocket.certificates.CertificateResourceService;
import com.hypersocket.config.ConfigurationValueChangedEvent;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.RealmService;
import com.hypersocket.server.events.ServerStartedEvent;
import com.hypersocket.server.events.ServerStartingEvent;
import com.hypersocket.server.events.ServerStoppedEvent;
import com.hypersocket.server.events.ServerStoppingEvent;
import com.hypersocket.server.events.WebappCreatedEvent;
import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.WebsocketHandler;
import com.hypersocket.server.handlers.impl.APIRequestHandler;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.servlet.HypersocketServletConfig;
import com.hypersocket.servlet.HypersocketServletContext;
import com.hypersocket.servlet.HypersocketSession;
import com.hypersocket.servlet.HypersocketSessionFactory;
import com.hypersocket.session.SessionService;

public abstract class HypersocketServerImpl implements HypersocketServer, 
				ApplicationListener<SystemEvent> {

	private static Logger log = LoggerFactory.getLogger(HypersocketServerImpl.class);

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private String sessionCookieName;

	public static final String API_PATH = "api";

	private AnnotationConfigWebApplicationContext webappContext;
	private DispatcherServlet dispatcherServlet;
	
	private List<String> controllerPackages = new ArrayList<String>();
	
	private Map<HTTPInterfaceResource,SSLContext> sslContexts = new HashMap<HTTPInterfaceResource,SSLContext>();
	private String defaultRedirectPath = null;
	private HomePageResolver homePageResolver = null;
	
	@Autowired
	EventService eventService;
	
	@Autowired
	SessionService sessionService; 
	
	List<HttpRequestHandler> httpHandlers = Collections
			.synchronizedList(new ArrayList<HttpRequestHandler>());

	List<WebsocketHandler> wsHandlers = Collections
			.synchronizedList(new ArrayList<WebsocketHandler>());
	
	List<String> compressablePaths = new ArrayList<String>();
	
	ApplicationContext applicationContext;
	
	@Autowired
	SystemConfigurationService configurationService;
	
	@Autowired 
	RealmService realmService;
	
	@Autowired
	SessionFactory sessionFactory;
	
	String[] enabledCipherSuites;
	String[] enabledProtocols;
	
	boolean stopping = false;
	
	Map<Pattern,String> urlRewrite = new HashMap<Pattern,String>();
	Map<String,String> aliases = new HashMap<String,String>();

	private HypersocketServletContext servletContext;
	
	public HypersocketServerImpl() {
		Security.addProvider(new BouncyCastleProvider());
		

		/* The context is the whole 'webapp'. It's path is the base path, i.e. /product-name */
		controllerPackages.add("com.hypersocket.json.**");
		controllerPackages.add("com.hypersocket.**.json");
	}
	
	@PostConstruct
	private void setup() {
	    servletContext = new HypersocketServletContext(this);
	}
	
	@Override
	public void registerControllerPackage(String controllerPackage) {
		controllerPackages.add(controllerPackage);
	}

	public SSLContext getSSLContext(HTTPInterfaceResource resource, 
			InetSocketAddress localAddress,
			InetSocketAddress remoteAddress) throws FileNotFoundException, IOException {
		return initializeSSL(resource);
	}

	@Override
	public void addUrlRewrite(String regex, String rewrite) {
		urlRewrite.put(Pattern.compile(regex), rewrite);
	}
	
	@Override
	public Map<Pattern,String> getUrlRewrites() {
		return urlRewrite;
	}
	
	@Override
	public void addAlias(String alias, String path) {
		aliases.put(alias, path);
	}
	
	@Override
	public Map<String,String> getAliases() {
		return aliases;
	}
	
	@Override
	public void removeAlias(String alias) {
		aliases.remove(alias);
	}
	
	protected String processReplacements(String str) {
		str = str.replace("${uiPath}", getUiPath());
		str = str.replace("${basePath}", getBasePath());
		return str;
	}
	
	@Override
	public boolean isAliasFor(String alias, String page) {
		
		alias = processReplacements(alias);
		page = processReplacements(page);
		
		for(String a : aliases.keySet()) {
			
			String p = processReplacements(aliases.get(a));
			a = processReplacements(a);
			
			if(alias.equals(a) && page.equals(p)) {
				return true;
			}
		}
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#getHttpHandlers()
	 */
	@Override
	public List<HttpRequestHandler> getHttpHandlers() {
		return httpHandlers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#getWebsocketHandlers()
	 */
	@Override
	public List<WebsocketHandler> getWebsocketHandlers() {
		return wsHandlers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hypersocket.server.HypersocketServer#registerHttpHandler(com.hypersocket
	 * .server.HttpRequestHandler)
	 */
	@Override
	public void registerHttpHandler(HttpRequestHandler handler) {
		handler.setServer(this);
		httpHandlers.add(handler);
		Collections.sort(httpHandlers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#unregisterHttpHandler(com.
	 * hypersocket.server.HttpRequestHandler)
	 */
	@Override
	public void unregisterHttpHandler(HttpRequestHandler handler) {
		httpHandlers.remove(handler);
		Collections.sort(httpHandlers);
	}
	
	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	public void init(ApplicationContext applicationContext)
			throws AccessDeniedException, IOException, ServletException {

		this.applicationContext = applicationContext;
		
		eventService.registerEvent(ServerStartingEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(ServerStartedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(ServerStoppingEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(ServerStoppedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(WebappCreatedEvent.class, RESOURCE_BUNDLE);
		
		eventService.publishEvent(new ServerStartingEvent(this, realmService.getSystemPrincipal().getRealm()));
		
		registerConfiguration();
		
		rebuildEnabledCipherSuites();
		
		createWebappContext();
		
	}


	private void createWebappContext() throws ServletException {
		if (log.isDebugEnabled())
			log.debug("Creating spring webapp context");
		
		HypersocketServletConfig servletConfig = new HypersocketServletConfig("api", servletContext);
		
		webappContext = new AnnotationConfigWebApplicationContext();
		webappContext.setParent(applicationContext);
		webappContext.register(DelegatingWebMvcConfiguration.class);
		webappContext.scan(controllerPackages.toArray(new String[0]));

		webappContext.setServletConfig(servletConfig);
		webappContext.refresh();
		webappContext.start();
		
		// We use a custom implementation of DispatcherServlet so it does not restrict the HTTP methods
		dispatcherServlet = new NonRestrictedDispatcherServlet(webappContext);
		dispatcherServlet.init(servletConfig);

		registerHttpHandler(new APIRequestHandler(dispatcherServlet, 100));

		eventService.publishEvent(new WebappCreatedEvent(this, true, realmService.getSystemRealm()));

	}

	@Override
	public void registerWebsocketpHandler(WebsocketHandler wsHandler) {
		wsHandlers.add(wsHandler);
	}

	protected void registerConfiguration() throws AccessDeniedException, IOException {

		I18NService i18nService = (I18NService) applicationContext
				.getBean("i18NServiceImpl");
		i18nService.registerBundle(RESOURCE_BUNDLE);

	}

	@Override
	public String[] getSSLProtocols() {

		try {
			SSLEngine engine = SSLContext.getDefault().createSSLEngine();
			return engine.getSupportedProtocols();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException();
		}
	}

	@Override
	public String[] getSSLCiphers() {
		
		try {
			SSLEngine engine = SSLContext.getDefault().createSSLEngine();
			return engine.getSupportedCipherSuites();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException();
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hypersocket.server.HypersocketServer#setAttribute(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hypersocket.server.HypersocketServer#getAttribute(java.lang.String,
	 * T)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name, T template) {
		if (attributes.containsKey(name)) {
			return (T) attributes.get(name);
		} else {
			return template;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#getApplicationName()
	 */
	@Override
	public String getApplicationName() {
		return configurationService.getValue("application.name").trim();
	}

	@Override
	public String resolvePath(String path) {
		if(path==null) {
			return getBasePath();
		} else {
			return getBasePath() + (!path.startsWith("/") ? "/" : "") + path;
		}
	}
	
	@Override
	public String getBasePath() {
		return "/" + configurationService.getValue("application.path").trim();
	}
	
	@Override
	public String getUiPath() {
		return resolvePath(getUserInterfacePath());
	}

	@Override
	public String getApiPath() {
		return resolvePath(API_PATH);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#start()
	 */
	@Override
	public void start() throws IOException {

		if (log.isInfoEnabled())
			log.info("Starting server");

		doStart();

		if (log.isInfoEnabled())
			log.info("Server started");
		
		System.setProperty("hypersocket.appPath", getBasePath());
		System.setProperty("hypersocket.uiPath", getUiPath());
		
		eventService.publishEvent(new ServerStartedEvent(this, realmService.getSystemRealm()));

	}

	protected abstract void doStart() throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#stop()
	 */
	@Override
	public void stop() {

		stopping = true;
		
		eventService.publishEvent(new ServerStoppingEvent(this, realmService.getSystemRealm()));
		
		if (log.isInfoEnabled())
			log.info("Stopping server");

		doStop();

		if (log.isInfoEnabled())
			log.info("Server stopped");
		
		eventService.publishEvent(new ServerStoppedEvent(this, realmService.getSystemRealm()));
		
	}
	
	
	public boolean isStopping() {
		return stopping;
	}

	protected abstract void doStop();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#getDispatcherServlet()
	 */
	@Override
	public DispatcherServlet getDispatcherServlet() {
		return dispatcherServlet;
	}

	public HypersocketSession setupHttpSession(List<String> cookies,
			boolean secure,
			HttpServletResponse servletResponse) {

		HypersocketSession session = null;

		for (String header : cookies) {

			StringTokenizer t = new StringTokenizer(header, ";");

			while (t.hasMoreTokens()) {
				String cookie = t.nextToken();
				int idx = cookie.indexOf('=');
				if (idx == -1)
					continue;
				String name = cookie.substring(0, idx).trim();
				if (name.equals(sessionCookieName)) {
					String value = cookie.substring(idx + 1);
					session = HypersocketSessionFactory.getInstance()
							.getSession(value,
									servletContext);
					// Check that the session exists in case we have any old
					// cookies
					if (session != null) {
						break;
					}
				}
			}
		}
		if (session == null) {
			session = HypersocketSessionFactory.getInstance().createSession(
					servletContext);
		}

		Cookie cookie = new Cookie(sessionCookieName, session.getId());
		cookie.setMaxAge(60 * 15);
		cookie.setPath("/");
		if(secure) {
			cookie.setSecure(true);
		} else {
			cookie.setSecure(false);
			cookie.setHttpOnly(true);
		}
		servletResponse.addCookie(cookie);

		return session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#getApplicationPath()
	 */
	@Override
	public String getApplicationPath() {
		return getBasePath();
	}

	public synchronized SSLContext initializeSSL(HTTPInterfaceResource resource) throws FileNotFoundException, IOException {

		if(sslContexts.containsKey(resource)) {
			return sslContexts.get(resource);
		}
		
		CertificateResourceService certificateService = (CertificateResourceService) applicationContext
				.getBean("certificateResourceServiceImpl");
		
		certificateService.setCurrentSession(sessionService.getSystemSession(), Locale.getDefault());
		
		try {

			if (log.isInfoEnabled()) {
				log.info("Initializing SSL contexts");
			}

			KeyStore ks = certificateService.getResourceKeystore(resource.getCertificate());

			// Get the default context
			SSLContext defaultSSLContext = SSLContext.getInstance("TLS");

			// KeyManager's decide which key material to use.
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, "changeit".toCharArray());
//			defaultSSLContext.init(kmf.getKeyManagers(), null, null);

			if (log.isInfoEnabled()) {
				log.info("Completed SSL initialization");
			}


			// Javadoc of SSLContext.init() states the first KeyManager implementing X509ExtendedKeyManager in the array is
			// used. We duplicate this behaviour when picking the KeyManager to wrap around.
			X509ExtendedKeyManager x509KeyManager = null;
			for (KeyManager keyManager : kmf.getKeyManagers()) {
				if (keyManager instanceof X509ExtendedKeyManager) {
					x509KeyManager = (X509ExtendedKeyManager) keyManager;
				}
			}

			if (x509KeyManager == null)
				throw new Exception("KeyManagerFactory did not create an X509ExtendedKeyManager");

			SniKeyManager sniKeyManager = new SniKeyManager(x509KeyManager);

//			context = SSLContext.getInstance("TLS");
			defaultSSLContext.init(new KeyManager[] {
				sniKeyManager
			}, null, null);
			sslContexts.put(resource, defaultSSLContext);
			return defaultSSLContext;
		} catch (Exception ex) {
			log.error("SSL initalization failed", ex);
			throw new IOException("SSL initialization failed: "
					+ ex.getMessage());
		} finally {
			certificateService.clearPrincipalContext();
		}
	}
	
	private void rebuildEnabledCipherSuites() {
		
		try {
			enabledProtocols = configurationService.getValues("ssl.protocols");
		} catch(IllegalStateException ex) {
		}
		
		try {
			String[] ciphers = configurationService.getValues("ssl.ciphers");
			SSLEngine engine = SSLContext.getDefault().createSSLEngine();
			
			if (ciphers!=null && ciphers.length > 0) {
	
				List<String> enabledCSList = new ArrayList<String>(Arrays.asList(engine.getEnabledCipherSuites()));
				List<String> includedCSList = new ArrayList<String>();
				
				boolean hasValid = false;
				for ( String cipherName : ciphers )
				{
					if ( enabledCSList.contains(cipherName) ) {
						includedCSList.add(cipherName);
						hasValid = true;
					}
					else {
						if(log.isInfoEnabled()) {
							log.warn("Disabled cipher: "+ cipherName);
						}
					}
				}
				enabledCipherSuites = (String[]) includedCSList.toArray(new String[includedCSList.size()]);
				
				if(!hasValid) {
					if(log.isWarnEnabled()) {
						log.warn("SSL cipher list did not contain any valid ciphers. Reverting to defaults.");
					}
					enabledCipherSuites = engine.getEnabledCipherSuites();
				} else {
					for(String cipher : enabledCipherSuites) {
						if(log.isInfoEnabled()) {
							log.info("Enabled cipher: " + cipher);
						}
					}
				}
			}
		} catch(Throwable ex) {
			
		}
	}

	public SSLEngine createSSLEngine(
			HTTPInterfaceResource resource,
			InetSocketAddress localAddress,
			InetSocketAddress remoteAddress) throws FileNotFoundException, IOException {
		
		SSLEngine engine = getSSLContext(resource, localAddress, remoteAddress).createSSLEngine();

		engine.setUseClientMode(false);
		engine.setWantClientAuth(false);

		if(enabledCipherSuites!=null && enabledCipherSuites.length > 0) {
			engine.setEnabledCipherSuites(enabledCipherSuites);
		}
		if(enabledProtocols!=null && enabledProtocols.length > 0) {
			engine.setEnabledProtocols(enabledProtocols);
		}
		return engine;
		
	}
	
	@Override
	public void onApplicationEvent(SystemEvent event) {
		
		if(event instanceof WebappCreatedEvent) {
			sessionCookieName = getApplicationName().toUpperCase().replace(' ', '_')
					+ "_HTTP_SESSION";
		}
		if(event instanceof ConfigurationValueChangedEvent) {
			ConfigurationValueChangedEvent configEvent = (ConfigurationValueChangedEvent) event;
			if(configEvent.hasAttribute(ConfigurationValueChangedEvent.ATTR_CONFIG_RESOURCE_KEY)){
				if(configEvent.getAttribute(ConfigurationValueChangedEvent.ATTR_CONFIG_RESOURCE_KEY).equals("ssl.ciphers") 
						|| configEvent.getAttribute(ConfigurationValueChangedEvent.ATTR_CONFIG_RESOURCE_KEY).equals("ssl.protocols")) {
					rebuildEnabledCipherSuites();
				}
				if(configEvent.getAttribute(ConfigurationValueChangedEvent.ATTR_CONFIG_RESOURCE_KEY).equals("application.path")) {
					System.setProperty("hypersocket.appPath", getBasePath());
					if(log.isInfoEnabled()) {
						log.info(String.format("Application path changed to %s", getBasePath()));
					}
				} else if(configEvent.getAttribute(ConfigurationValueChangedEvent.ATTR_CONFIG_RESOURCE_KEY).equals("ui.path")) {
					System.setProperty("hypersocket.uiPath", getUiPath());
					if(log.isInfoEnabled()) {
						log.info(String.format("UI path changed to %s", getUiPath()));
					}
				}
			}
			
		}
		
		processApplicationEvent(event);
	}
	
	protected abstract void processApplicationEvent(SystemEvent event);
	@Override
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	public String getUserInterfacePath() {
		return configurationService.getValue("ui.path");
	}
	
	@Override
	public void addCompressablePath(String path) {
		compressablePaths.add(path);
	}
	
	@Override
	public boolean isCompressablePath(String uri) {
		for(String path : compressablePaths) {
			if(uri.startsWith(path)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDefaultRedirectPath() {
		if(StringUtils.isNotBlank(configurationService.getValue("server.defaultRedirect"))) {
			return configurationService.getValue("server.defaultRedirect");
		}
		
		if(homePageResolver!=null) {
			return homePageResolver.getHomePage().replace("${uiPath}", getUiPath()).replace("${basePath}", getBasePath());
		}
		
		return (defaultRedirectPath==null ? getUiPath() : defaultRedirectPath).replace("${uiPath}", getUiPath()).replace("${basePath}", getBasePath());
	}
	
	@Override
	public void setDefaultRedirectPath(String defaultRedirectPath) {
		this.defaultRedirectPath = defaultRedirectPath;
	}

	@Override
	public HomePageResolver getHomePageResolver() {
		return homePageResolver;
	}

	@Override
	public void setHomePageResolver(HomePageResolver homePageResolver) {
		this.homePageResolver = homePageResolver;
	}

}
