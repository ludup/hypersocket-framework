/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.hibernate.SessionFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.ProfileLoaderClassPathXmlApplicationContext;
import com.hypersocket.profile.ProfileNameFinder;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.server.MiniHttpServer;
import com.hypersocket.server.MiniHttpServer.DynamicContent;
import com.hypersocket.server.MiniHttpServer.DynamicContentFactory;
import com.hypersocket.upgrade.UpgradeService;

public class Main {

	private static final String STARTUP_TOOK = "startupTook";
	static Logger log = LoggerFactory.getLogger(Main.class);
	static Preferences PREFS = Preferences.userNodeForPackage(Main.class);

	private ApplicationContext applicationContext;
	private HypersocketServer server;
	private Runnable restartCallback;
	private Runnable shutdownCallback;
	private ClassLoader classLoader;
	private File conf;

	private MiniHttpServer miniServer;
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

		LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
		context.setConfigLocation(new File(conf, "log4j.properties").toURI());

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
			if("true".equals(System.getProperty(MiniHttpServer.HYPERSOCKET_BOOT_HTTP_SERVER,MiniHttpServer.HYPERSOCKET_BOOT_HTTP_SERVER_DEFAULT)) && !new File(conf, "no-boot-httpserver").exists()) {
				String username = System.getProperty("user.name");
				miniserverStarted = System.currentTimeMillis();
				File bootKeystore = new File(conf, "boothttp.keystore");
				if (username.equals("root") || username.equals("Administrator"))
					miniServer = new MiniHttpServer(80, 443, bootKeystore);
				else
					miniServer = new MiniHttpServer(8080, 8443, bootKeystore);

				miniServer.addContent(new DynamicContentFactory() {
					@Override
					public DynamicContent get(String path) throws IOException {
						if (path.matches("/.*/api/.*")) {
							throw new IllegalStateException("Booting up and not ready yet, try again shortly.");
						}
						return null;
					}
				});
				miniServer.addContent(new DynamicContentFactory() {
					@Override
					public DynamicContent get(String path) throws IOException {
						if (path.startsWith("/progress")) {
							/* Estimate of 5 minutes for first startup */
							long took = PREFS.getLong(STARTUP_TOOK, TimeUnit.MINUTES.toMillis(5));
							long taken = System.currentTimeMillis() - miniserverStarted;
							return new DynamicContent("text/plain", new ByteArrayInputStream((took == 0 ? "0/0" : taken + "/" + took).getBytes()));
						}
						return null;
					}
				});
				miniServer.addContent(new DynamicContentFactory() {
					@Override
					public DynamicContent get(String path) throws IOException {
						if (path.startsWith("/app") || path.startsWith("/hypersocket")) {
							throw new IllegalArgumentException("/");
						}
						return null;
					}
				});
				miniServer.start();
			}
		} catch (IOException e) {
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
				log.info("There is no restart mechanism available. Shutting down");
			}

			System.exit(0);
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
