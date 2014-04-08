/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
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
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

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

import com.hypersocket.certs.CertificateService;
import com.hypersocket.config.ConfigurationChangedEvent;
import com.hypersocket.config.ConfigurationService;
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
import com.hypersocket.servlet.HypersocketServletConfig;
import com.hypersocket.servlet.HypersocketSession;
import com.hypersocket.servlet.HypersocketSessionFactory;

public abstract class HypersocketServerImpl implements HypersocketServer, 
				ApplicationListener<SystemEvent> {

	private static Logger log = LoggerFactory.getLogger(HypersocketServerImpl.class);

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private String sessionCookieName;

	public static final String API_PATH = "api";

	private AnnotationConfigWebApplicationContext webappContext;
	private DispatcherServlet dispatcherServlet;
	private HypersocketServletConfig servletConfig;
	
	@Autowired
	EventService eventService;
	
	List<HttpRequestHandler> httpHandlers = Collections
			.synchronizedList(new ArrayList<HttpRequestHandler>());

	List<WebsocketHandler> wsHandlers = Collections
			.synchronizedList(new ArrayList<WebsocketHandler>());
	
	List<String> compressablePaths = new ArrayList<String>();
	
	ApplicationContext applicationContext;
	
	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
	SessionFactory sessionFactory;
	
	SSLContext defaultSSLContext;
	String[] enabledCipherSuites;
	String[] enabledProtocols;
	
	boolean stopping = false;
	
	public HypersocketServerImpl() {

	}
	
	@Override
	public boolean isPlainPort(InetSocketAddress localAddress) {
		if(Boolean.getBoolean("hypersocket.development")) {
			return localAddress.getPort() == 8080;
		} else {
			return localAddress.getPort() == configurationService.getIntValue("http.port");
		}
	}

	@Override
	public boolean isSSLPort(InetSocketAddress localAddress) {
		if(Boolean.getBoolean("hypersocket.development")) {
			return localAddress.getPort() == 8443;
		} else {
			return localAddress.getPort() == configurationService.getIntValue("https.port");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#getHttpPort()
	 */
	@Override
	public int getHttpPort() {
		return Boolean.getBoolean("hypersocket.development") ? 8080 : configurationService.getIntValue("http.port");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#getHttpsPort()
	 */
	@Override
	public int getHttpsPort() {
		return Boolean.getBoolean("hypersocket.development") ? 8443 : configurationService.getIntValue("https.port");
	}

	public SSLContext getSSLContext(InetSocketAddress localAddress,
			InetSocketAddress remoteAddress) {
		return defaultSSLContext;
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
	}
	
	public void init(ApplicationContext applicationContext)
			throws AccessDeniedException, IOException, ServletException {

		this.applicationContext = applicationContext;
		
		eventService.registerEvent(ServerStartingEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(ServerStartedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(ServerStoppingEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(ServerStoppedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(WebappCreatedEvent.class, RESOURCE_BUNDLE);
		
		eventService.publishEvent(new ServerStartingEvent(this));
		
		Security.addProvider(new BouncyCastleProvider());

		initializeSSL();
		
		registerConfiguration();
		
		rebuildEnabledCipherSuites();
		
		createWebappContext();
		
	}

	private void createWebappContext() throws ServletException {
		if (log.isDebugEnabled())
			log.debug("Creating spring webapp context");

		servletConfig = new HypersocketServletConfig("default",
				resolvePath(API_PATH));
		
		webappContext = new AnnotationConfigWebApplicationContext();
		webappContext.setParent(applicationContext);
		webappContext.register(DelegatingWebMvcConfiguration.class);
		webappContext.scan("com.hypersocket.json.**",
				"com.hypersocket.**.json");

		webappContext.setServletConfig(servletConfig);
		webappContext.refresh();

		// We use a custom implementation of DispatcherServlet so it does not restrict the HTTP methods
		dispatcherServlet = new NonRestrictedDispatcherServlet(webappContext);
		dispatcherServlet.init(servletConfig);

		registerHttpHandler(new APIRequestHandler(dispatcherServlet, this, 100));

		eventService.publishEvent(new WebappCreatedEvent(this, true));

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
	public boolean isHttpsRequired() {
		return configurationService.getBooleanValue("require.https");
	}

	@Override
	public String[] getSSLProtocols() {
		SSLEngine engine = defaultSSLContext.createSSLEngine();
		return engine.getSupportedProtocols();
	}

	@Override
	public String[] getSSLCiphers() {
		SSLEngine engine = defaultSSLContext.createSSLEngine();
		return engine.getSupportedCipherSuites();
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
		return getBasePath() + (!path.startsWith("/") ? "/" : "") + path;
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
		
		eventService.publishEvent(new ServerStartedEvent(this));

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
		
		eventService.publishEvent(new ServerStoppingEvent(this));
		
		if (log.isInfoEnabled())
			log.info("Stopping server");

		doStop();

		if (log.isInfoEnabled())
			log.info("Server stopped");
		
		eventService.publishEvent(new ServerStoppedEvent(this));
		
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hypersocket.server.HypersocketServer#getServletConfig()
	 */
	@Override
	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	public HypersocketSession setupHttpSession(List<String> cookies,
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
									servletConfig.getServletContext());
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
					servletConfig.getServletContext());
		}

		Cookie cookie = new Cookie(sessionCookieName, session.getId());
		cookie.setSecure(false);
		cookie.setMaxAge(60 * 15);
		cookie.setPath("/");
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

	public void initializeSSL() throws FileNotFoundException, IOException {

		CertificateService certificateService = (CertificateService) applicationContext
				.getBean("certificateServiceImpl");
		RealmService realmService = (RealmService) applicationContext
				.getBean("realmServiceImpl");

		try {

			if (log.isInfoEnabled()) {
				log.info("Initializing SSL contexts");
			}

			certificateService.setCurrentPrincipal(realmService
					.getSystemPrincipal(), Locale.getDefault(),
					realmService.getSystemPrincipal().getRealm());

			KeyStore ks = certificateService.getDefaultCertificate();

			// Get the default context
			defaultSSLContext = SSLContext.getInstance("TLS");

			// KeyManager's decide which key material to use.
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, "changeit".toCharArray());
			defaultSSLContext.init(kmf.getKeyManagers(), null, null);

			if (log.isInfoEnabled()) {
				log.info("Completed SSL initialization");
			}
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
			SSLEngine engine = defaultSSLContext.createSSLEngine();
			
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
		} catch(IllegalStateException ex) {
			
		}
	}

	public SSLEngine createSSLEngine(InetSocketAddress localAddress,
			InetSocketAddress remoteAddress) {
		
		SSLEngine engine = getSSLContext(localAddress, remoteAddress).createSSLEngine();

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
		if(event.getResourceKey().equals(ConfigurationChangedEvent.EVENT_RESOURCE_KEY)) {
			String resourceKey = (String) event.getAttribute(ConfigurationChangedEvent.ATTR_CONFIG_RESOURCE_KEY);
			if(resourceKey.equals("ssl.ciphers") || resourceKey.equals("ssl.protocols")) {
				rebuildEnabledCipherSuites();
			}
		}
		if(event instanceof ConfigurationChangedEvent) {
			ConfigurationChangedEvent configEvent = (ConfigurationChangedEvent) event;
			if(configEvent.getResourceKey().equals("application.path")) {
				System.setProperty("hypersocket.appPath", getBasePath());
			} else if(configEvent.getResourceKey().equals("ui.path")) {
				System.setProperty("hypersocket.uiPath", getUiPath());
			}
			
		}
	}
	
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

}
