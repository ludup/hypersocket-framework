/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;

import com.hypersocket.certificates.CertificateResourceService;
import com.hypersocket.certificates.events.CertificateResourceCreatedEvent;
import com.hypersocket.certificates.events.CertificateResourceUpdatedEvent;
import com.hypersocket.config.ConfigurationValueChangedEvent;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.events.CoreStartedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.plugins.ExtensionsPluginManager;
import com.hypersocket.realm.RealmService;
import com.hypersocket.server.HomePageResolver.AuthenticationRequirements;
import com.hypersocket.server.events.ServerStartedEvent;
import com.hypersocket.server.events.ServerStartingEvent;
import com.hypersocket.server.events.ServerStoppedEvent;
import com.hypersocket.server.events.ServerStoppingEvent;
import com.hypersocket.server.events.WebappCreatedEvent;
import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.WebsocketHandler;
import com.hypersocket.server.handlers.impl.APIRequestHandler;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceResourceCreatedEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceResourceUpdatedEvent;
import com.hypersocket.servlet.HypersocketServletConfig;
import com.hypersocket.servlet.HypersocketServletContext;
import com.hypersocket.servlet.HypersocketSession;
import com.hypersocket.servlet.HypersocketSessionFactory;
import com.hypersocket.session.Session;

public abstract class HypersocketServerImpl implements HypersocketServer, 
				ApplicationListener<SystemEvent> {

	private static Logger log = LoggerFactory.getLogger(HypersocketServerImpl.class);

	@Autowired
	private EventService eventService;
	
	@Autowired
	private SystemConfigurationService configurationService;
	
	@Autowired 
	private RealmService realmService;

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private String sessionCookieName;

	public static final String API_PATH = "api";

	private AnnotationConfigWebApplicationContext webappContext;
	private DispatcherServlet dispatcherServlet;
	
	private Set<String> controllerPackages = new LinkedHashSet<String>();
	
	private Map<HTTPInterfaceResource,SSLContext> sslContexts = new HashMap<HTTPInterfaceResource,SSLContext>();
	private Map<HTTPInterfaceResource,KeyStore> sslCertificates = new HashMap<HTTPInterfaceResource,KeyStore>();
	private String defaultRedirectPath = null;
	private List<HomePageResolver> homePageResolvers = new ArrayList<>(); 
	
	private List<HttpRequestHandler> httpHandlers = Collections
			.synchronizedList(new ArrayList<HttpRequestHandler>());

	private List<WebsocketHandler> wsHandlers = Collections
			.synchronizedList(new ArrayList<WebsocketHandler>());
	
	private List<String> compressablePaths = new ArrayList<String>();
	
	private ApplicationContext applicationContext;
	
	private String[] enabledCipherSuites;
	private String[] enabledProtocols;
	
	private boolean stopping = false;
	private Map<Pattern,String> urlRewrite = new HashMap<Pattern,String>();
	private Map<String,String> aliases = new HashMap<String,String>();
	private Set<String> protectedPages = new HashSet<String>();
	
	private HypersocketServletContext servletContext;
	
	public HypersocketServerImpl() {
		if("true".equals(System.getProperty("hypersocket.bc", "true")))
			Security.addProvider(new BouncyCastleProvider());
		

		/* The context is the whole 'webapp'. It's path is the base path, i.e. /product-name */
		controllerPackages.add("com.hypersocket.json.**");
		controllerPackages.add("com.hypersocket.**.json");
		
		controllerPackages.add("com.logonbox.json.**");
		controllerPackages.add("com.logonbox.**.json");
	}
	
	@PostConstruct
	private void setup() {
	    servletContext = new HypersocketServletContext(this);
	    
	    String basePath = getBasePath();
	    if(!basePath.equals("/hypersocket")) {
	    	addUrlRewrite("/hypersocket/(.*)", "${basePath}/$1");
	    }
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
	public void removeUrlRewrite(String regex) {
		urlRewrite.remove(Pattern.compile(regex));
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
	
	@Override
	public void protectPage(String page) {
		protectedPages.add(page);
	}
	
	@Override
	public boolean isProtectedPage(String page) {
		return protectedPages.contains(page);
	}
	
	@Override
	public String processReplacements(String str) {
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
	
	@Override
	public ApplicationContext getWebappContext() {
		return webappContext;
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
		
		ExtensionsPluginManager pluginManager = applicationContext.getBean(ExtensionsPluginManager.class);
		pluginManager.startWebContext(webappContext, servletConfig, webappContext.getServletContext());
		
		// We use a custom implementation of DispatcherServlet so it does not restrict the HTTP methods
		dispatcherServlet = new NonRestrictedDispatcherServlet(webappContext, pluginManager);
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
		eventService.publishEvent(new CoreStartedEvent(this, realmService.getSystemRealm()));

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
		
		try {
			eventService.publishEvent(new ServerStoppingEvent(this, realmService.getSystemRealm()));
		}
		catch(Exception e) {
			if(log.isDebugEnabled())
				log.warn("Failed to send stopping event.", e);
			else
				log.warn("Failed to send stopping event.");
		}
		
		if (log.isInfoEnabled())
			log.info("Stopping server");

		doStop();

		if (log.isInfoEnabled())
			log.info("Server stopped");
		
		try {
			eventService.publishEvent(new ServerStoppedEvent(this, realmService.getSystemRealm()));
		}
		catch(Exception e) {
			if(log.isDebugEnabled())
				log.warn("Failed to send stopped event.", e);
			else
				log.warn("Failed to send stopped event.");
		}
		
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
			String domain,
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
		cookie.setSecure(secure);
//		cookie.setHttpOnly(true);
		cookie.setDomain(domain);
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
		
		try(var c = certificateService.tryWithSystemContext()) {

			if (log.isInfoEnabled()) {
				log.info("Initializing SSL contexts");
			}

			if(!sslCertificates.containsKey(resource)) {
				sslCertificates.put(resource, certificateService.getKeystoreWithCertificates(
						resource.getCertificate(),
						resource.getAdditionalCertificates()));
			}
			
			KeyStore ks = sslCertificates.get(resource);
			
			if("true".equals(System.getProperty(MiniHttpServer.HYPERSOCKET_BOOT_HTTP_SERVER,MiniHttpServer.HYPERSOCKET_BOOT_HTTP_SERVER_DEFAULT))) {
				/* Write out this keystore as a file that can be used by the mini-http server
				 * for it's keystore as well. 
				 */
				File file = new File(new File(System.getProperty("hypersocket.conf")), "boothttp.keystore");
				log.info(String.format("Storing certificate keystore for use by Boot HTTP server at %s", file));
				try(OutputStream out = new FileOutputStream(file)) {
					ks.store(out, MiniHttpServer.KEYSTORE_PASSWORD.toCharArray());
					out.flush();
				}
			}

			SSLContext defaultSSLContext = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			
			kmf.init(ks, "changeit".toCharArray());
			
			if (log.isInfoEnabled()) {
				log.info("Completed SSL initialization");
			}

			X509ExtendedKeyManager x509KeyManager = null;
			for (KeyManager keyManager : kmf.getKeyManagers()) {
				if (keyManager instanceof X509ExtendedKeyManager) {
					x509KeyManager = (X509ExtendedKeyManager) keyManager;
				}
			}

			if (x509KeyManager == null)
				throw new Exception("KeyManagerFactory did not create an X509ExtendedKeyManager");

			SniKeyManager sniKeyManager = new SniKeyManager(x509KeyManager);

			defaultSSLContext.init(new KeyManager[] {
				sniKeyManager
			}, null, null);
			sslContexts.put(resource, defaultSSLContext);
			return defaultSSLContext;
		} catch (Exception ex) {
			log.error("SSL initalization failed", ex);
			throw new IOException("SSL initialization failed: "
					+ ex.getMessage());
		}
	}
	
	private void rebuildEnabledCipherSuites() {
		
		List<String> includeProtocols = Arrays.asList(configurationService.getValues("ssl.includeProtocols"));
		List<String> excludeProtocols = Arrays.asList(configurationService.getValues("ssl.excludeProtocols"));
		List<String> includeCiphers = Arrays.asList(configurationService.getValues("ssl.includeCiphers"));
		List<String> excludeCiphers = Arrays.asList(configurationService.getValues("ssl.excludeCiphers"));
		
		List<String> thisEnabledProtocols = new ArrayList<>();
		List<String> thisEnabledCiphers = new ArrayList<>();
		
		try {
			SSLEngine engine = SSLContext.getDefault().createSSLEngine();
			for(String supported : engine.getSupportedProtocols()) {
				if( ( includeProtocols.isEmpty() || includeProtocols.contains(supported)) &&
					!excludeProtocols.contains(supported)) {
					thisEnabledProtocols.add(supported);
					if(log.isInfoEnabled()) {
						log.info("Enabled protocol: " + supported);
					}
				}
				if(excludeProtocols.contains(supported)) {
					if(log.isInfoEnabled()) {
						log.warn("Disabled protocol: "+ supported);
					}
				}
			}
			for(String supported : engine.getSupportedCipherSuites()) {
				if( ( includeCiphers.isEmpty() || includeCiphers.contains(supported)) &&
					!excludeCiphers.contains(supported)) {
					thisEnabledCiphers.add(supported);
					if(log.isInfoEnabled()) {
						log.info("Enabled cipher: " + supported);
					}
				}
				if(excludeCiphers.contains(supported)) {
					if(log.isInfoEnabled()) {
						log.warn("Disabled cipher: "+ supported);
					}
				}
			}
			
			enabledProtocols = thisEnabledProtocols.toArray(new String[0]);
			enabledCipherSuites = thisEnabledCiphers.toArray(new String[0]);
		} catch(Exception ex) {
			throw new IllegalStateException("Failed to adjust SSL configuration.", ex);
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
		
//		log.info("Final SSL configuration");
//		log.info("    Protocols");
//		for(String supported : engine.getSupportedProtocols()) {
//			if(Arrays.asList(engine.getEnabledProtocols()).contains(supported))
//				log.info(String.format("        %s - Supported", supported));
//		}
//		for(String supported : engine.getSupportedProtocols()) {
//			if(!Arrays.asList(engine.getEnabledProtocols()).contains(supported))
//				log.info(String.format("        %s - NOT Supported", supported));
//		}
//		log.info("    Ciphers");
//		for(String supported : engine.getSupportedCipherSuites()) {
//			if(Arrays.asList(engine.getEnabledCipherSuites()).contains(supported))
//				log.info(String.format("        %s - Supported", supported));
//		}
//		for(String supported : engine.getSupportedCipherSuites()) {
//			if(!Arrays.asList(engine.getEnabledCipherSuites()).contains(supported))
//				log.info(String.format("        %s - NOT Supported", supported));
//		}
		
		return engine;
		
	}
	
	@Override
	public void onApplicationEvent(SystemEvent event) {
		
		if(event instanceof WebappCreatedEvent) {
			sessionCookieName = getApplicationName().toUpperCase().replace(' ', '_')
					+ "_HTTP_SESSION";
		}
		if(event instanceof ConfigurationValueChangedEvent && event.isSuccess()) {
			ConfigurationValueChangedEvent configEvent = (ConfigurationValueChangedEvent) event;
			if(configEvent.getConfigResourceKey().equals("ssl.includeCiphers") 
					|| configEvent.getConfigResourceKey().equals("ssl.excludeCiphers")
					|| configEvent.getConfigResourceKey().equals("ssl.includeProtocols")
					|| configEvent.getConfigResourceKey().equals("ssl.excludeProtocols")) {
				rebuildEnabledCipherSuites();
			}
			if(configEvent.getConfigResourceKey().equals("application.path")) {
				System.setProperty("hypersocket.appPath", getBasePath());
				if(log.isInfoEnabled()) {
					log.info(String.format("Application path changed to %s", getBasePath()));
				}
			} else if(configEvent.getConfigResourceKey().equals("ui.path")) {
				System.setProperty("hypersocket.uiPath", getUiPath());
				if(log.isInfoEnabled()) {
					log.info(String.format("UI path changed to %s", getUiPath()));
				}
			}
			
		} else if(event instanceof CertificateResourceUpdatedEvent || event instanceof CertificateResourceCreatedEvent
				|| event instanceof HTTPInterfaceResourceUpdatedEvent || event instanceof HTTPInterfaceResourceCreatedEvent) {
			
			synchronized(this) {
				
				if(log.isInfoEnabled()) {
					log.info("Detected change in certificates or HTTP intefaces so removing SSL certificate/context cache");
				}
				sslCertificates.clear();
				sslContexts.clear();
			}
		}
		
		processApplicationEvent(event);
	}
	
	protected abstract void processApplicationEvent(SystemEvent event);
	
	protected synchronized void clearSSLContexts(HTTPInterfaceResource interfaceResource) {
		sslContexts.remove(interfaceResource);
		sslCertificates.remove(interfaceResource);
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

	@Override
	public String getDefaultRedirectPath(HttpServletRequest request, HttpServletResponse response) {
		if(StringUtils.isNotBlank(configurationService.getValue("server.defaultRedirect"))) {
			return configurationService.getValue("server.defaultRedirect");
		}
		
		HttpSession session = request.getSession(false);
		Session state = session == null ? null : (Session)session.getAttribute("authenticatedSession");
		boolean authenticated = state != null && !state.isClosed();
		
		/* First look for the most recently added specific resolver for this authentication state */
		for(int i = homePageResolvers.size() - 1 ; i >= 0; i--) {
			HomePageResolver r = homePageResolvers.get(i);
			if( (r.getAuthenticationRequirements() == AuthenticationRequirements.AUTHENTICATED && authenticated) ||
				(r.getAuthenticationRequirements() == AuthenticationRequirements.NOT_AUTHENTICATED && !authenticated)) {
				return r.getHomePage().replace("${uiPath}", getUiPath()).replace("${basePath}", getBasePath());
			}
		}
		
		/* Now look for the most recently added resolver that applies to any authentication state */
		for(int i = homePageResolvers.size() - 1 ; i >= 0; i--) {
			HomePageResolver r = homePageResolvers.get(i);
			if(r.getAuthenticationRequirements() == AuthenticationRequirements.ANY) {
				return r.getHomePage().replace("${uiPath}", getUiPath()).replace("${basePath}", getBasePath());
			}
		}
		
		return (defaultRedirectPath==null ? getUiPath() : defaultRedirectPath).replace("${uiPath}", getUiPath()).replace("${basePath}", getBasePath());
	}
	
	@Override
	public void setDefaultRedirectPath(String defaultRedirectPath) {
		this.defaultRedirectPath = defaultRedirectPath;
	}

	@Override
	public List<HomePageResolver> getHomePageResolvers() {
		return homePageResolvers;
	}

	@Override
	public void addHomePageResolver(HomePageResolver homePageResolver) {
		homePageResolvers.add(homePageResolver);
	}

	@Override
	public void removeHomePageResolver(HomePageResolver homePageResolver) {
		homePageResolvers.remove(homePageResolver);
	}

}
