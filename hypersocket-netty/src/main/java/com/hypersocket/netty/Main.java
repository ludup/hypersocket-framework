/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.prefs.Preferences;

import javax.net.ssl.KeyManagerFactory;
import javax.servlet.ServletException;

import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.hibernate.SessionFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.hypersocket.certs.X509CertificateUtils;
import com.hypersocket.netty.log.XYamlConfigurationFactory;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.ProfileLoaderClassPathXmlApplicationContext;
import com.hypersocket.profile.ProfileNameFinder;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.upgrade.UpgradeService;
import com.sshtools.uhttpd.UHTTPD;
import com.sshtools.uhttpd.UHTTPD.RootContext;
import com.sshtools.uhttpd.UHTTPD.Status;

public class Main {
	
	static {
		PluginManager.addPackage(XYamlConfigurationFactory.class.getPackageName());

		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	private static final String STARTUP_TOOK = "startupTook";
	
	static Logger log = LoggerFactory.getLogger(Main.class);
	static Preferences PREFS = Preferences.userNodeForPackage(Main.class);

	private ApplicationContext applicationContext;
	private HypersocketServer server;
	private Runnable restartCallback;
	private Runnable shutdownCallback;
	private ClassLoader classLoader;
	private File conf;

	private RootContext miniServer;
	private long miniserverStarted;
	
	static Main instance;

	public Main(Runnable restartCallback, Runnable shutdownCallback) {
		this.restartCallback = restartCallback;
		this.shutdownCallback = shutdownCallback;
	}

	public void setConfigurationDir(File conf) {
		this.conf = conf;
	}

	public File getConfigurationDir() {
		return conf;
	}

	public HypersocketServer getServer() {
		return server;
	}

	public static void main(String[] args) {
		try {
			runApplication(new DefaultRestartCallback(), new DefaultShutdownCallback());
		} catch (IOException e) {
			log.error("Failed to run application", e);
		}
	}

	public static void runApplication(Runnable restartCallback, Runnable shutdownCallback) throws IOException {

		new Main(restartCallback, shutdownCallback).run();

	}

	public void run() {

		if (instance != null) {
			throw new IllegalStateException("An attempt has been made to start a second instance of Main");
		}
		Main.instance = this;

		if (conf == null) {
			conf = new File("conf");
		}

		System.setProperty("hypersocket.conf", conf.getPath());

		classLoader = getClass().getClassLoader();
		if (log.isInfoEnabled()) {
			log.info("Using class loader " + classLoader.getClass().getName());
		}

		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		try {
			createMiniServer();
			createApplicationContext();

			runServer();
		} catch (Throwable e) {
			log.error("Failed to run application", e);
			System.exit(1);
		}
	}

	public static Main getInstance() {
		return instance;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	protected void createMiniServer() {
		try {
			/*
			 * This lightweight HTTP/HTTPS server is started to provide the user with some kind 
			 * of UI while all other subsystems are initializing. 
			 * 
			 * It also provides log output (via a custom log appender), and startup progress
			 * based on the last known time taken.
			 */
			if("true".equals(System.getProperty(HypersocketServer.HYPERSOCKET_BOOT_HTTP_SERVER, HypersocketServer.HYPERSOCKET_BOOT_HTTP_SERVER_DEFAULT)) && !new File(conf, "no-boot-httpserver").exists()) {
				String username = System.getProperty("user.name");
				var admin = username.equals("root") || username.equals("Administrator");
				miniserverStarted = System.currentTimeMillis();
				var bootKeystore = new File(conf, "boothttp.keystore").toPath();
				var kspassword = "changeit".toCharArray();
				
				if (!Files.exists(bootKeystore)) {
					log.info(String.format("Generating keystore"));
					KeyPair kp = X509CertificateUtils.generatePrivateKey("RSA", 2048);
					X509Certificate cert = X509CertificateUtils.generateSelfSignedCertificate(
							InetAddress.getLocalHost().getHostName(), "", "", "", "", "", kp, "SHA1WithRSAEncryption", new String[0]);
					var ks = X509CertificateUtils.createPKCS12Keystore(kp, new X509Certificate[] { cert }, "server",
							kspassword);
					log.info(String.format("Writing temporary keystore to %s", bootKeystore));
					try (OutputStream fout = Files.newOutputStream(bootKeystore)) {
						ks.store(fout, kspassword);
					}
					var kmf = KeyManagerFactory.getInstance("SunX509");
					kmf.init(ks, kspassword);
				}
				var took = PREFS.getLong(STARTUP_TOOK, TimeUnit.MINUTES.toMillis(5));
				miniServer = UHTTPD.server().
						withHttpAddress("0.0.0.0").
						withHttpsAddress("0.0.0.0").
						withHttp(Integer.parseInt(System.getProperty("hypersocket.http.port", admin ? "80" : "8080"))).
						withHttps(Integer.parseInt(System.getProperty("hypersocket.https.port", admin ? "443" : "8443"))).
						withKeyStoreFile(bootKeystore).
						withKeyStorePassword(kspassword).
						withoutCache().
						get("/random", 		tx -> tx.response(Math.random())).
						get("/.*/api/.*", 	tx -> tx.responseCode(Status.SERVICE_UNAVAILABLE)).
						get("/app/.*|/hypersocket/.*", tx -> tx.redirect(Status.MOVED_TEMPORARILY, "/")).
						get("/", UHTTPD.classpathResource("boothttp/index.html")).
						get("/progress", 	tx -> tx.response("text/plain", took == 0 ? "0/0" : (System.currentTimeMillis() - miniserverStarted) + "/" + took)).
						classpathResources("(.*)", "boothttp").
						build();
				miniServer.start();
			}
		} catch (Exception e) {
			log.error(
					"Failed to setup bootstrap HTTP server, no web requests will be served until the app is fully started.",
					e);
		}
	}

	protected void runServer() throws AccessDeniedException, ServletException, IOException {

		server = (HypersocketServer) applicationContext.getBean("nettyServer");

		server.init(applicationContext);

		try {
			if (miniServer != null) {
				miniServer.close();
				PREFS.putLong(STARTUP_TOOK, System.currentTimeMillis() - miniserverStarted);
			}
			server.start();

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					if (!server.isStopping()) {
						server.stop();
					}
				}
			});
		} catch (Throwable t) {
			log.error("Failed to start server", t);
			System.exit(1);
		}

	}

	/**
	 * Restart the current Java application
	 * 
	 * @throws IOException
	 */
	public void restartServer() throws IOException {

		if (server != null) {
			server.stop();
		}
		restartCallback.run();

	}

	public void shutdownServer() {

		server.stop();
		shutdownCallback.run();

	}

	protected void createApplicationContext() {

		if (log.isInfoEnabled()) {
			log.info(String.format("Creating spring application context with version %s", SpringVersion.getVersion()));
		}

		String[] profiles = ProfileNameFinder.findProfiles();
		String configLocation = "classpath*:/applicationContext.xml";

		if (profiles.length == 0) {
			applicationContext = new ClassPathXmlApplicationContext(configLocation);
		} else {
			applicationContext = new ProfileLoaderClassPathXmlApplicationContext(configLocation, profiles);
		}

		if (log.isInfoEnabled()) {
			log.info("Obtaining platform transaction manager");
		}

		PlatformTransactionManager transactionManager = (PlatformTransactionManager) applicationContext
				.getBean("transactionManager");

		if (log.isInfoEnabled()) {
			log.info("Creating transaction template");
		}

		TransactionTemplate txnTemplate = new TransactionTemplate(transactionManager);

		if (log.isInfoEnabled()) {
			log.info("Calling TransactionTemplate.afterPropertiesSet");
		}

		txnTemplate.afterPropertiesSet();

		if (log.isInfoEnabled()) {
			log.info("Creating transaction for upgrade");
		}

		UpgradeService upgradeService = (UpgradeService) applicationContext.getBean("upgradeService");
		upgradeService.upgrade((SessionFactory) applicationContext.getBean("sessionFactory"), txnTemplate);

	}

	static class DefaultRestartCallback implements Runnable {

		@Override
		public void run() {

			if (log.isInfoEnabled()) {
				log.info("Shutting down with forker restart code.");
			}

			System.exit(99);
		}

	}

	static class DefaultShutdownCallback implements Runnable {

		@Override
		public void run() {

			if (log.isInfoEnabled()) {
				log.info("Shutting down using default shutdown mechanism");
			}

			System.exit(0);
		}

	}

}
