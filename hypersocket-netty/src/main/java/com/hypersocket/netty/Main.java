/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.log4j.PropertyConfigurator;
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
import com.hypersocket.upgrade.UpgradeService;

public class Main {

	static Logger log = LoggerFactory.getLogger(Main.class);
	
	ApplicationContext applicationContext;
	HypersocketServer server;
	Runnable restartCallback;
	Runnable shutdownCallback;
	ClassLoader classLoader;
	File conf;
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
	
	public static void runApplication(Runnable restartCallback,
			Runnable shutdownCallback) throws IOException {

		new Main(restartCallback, shutdownCallback).run();

	}

	public void run() {

		if(instance!=null) {
			throw new IllegalStateException("An attempt has been made to start a second instance of Main");
		}
		Main.instance = this;
		
		if(conf==null) {
			conf = new File("conf");
		}
		
		System.setProperty("hypersocket.conf", conf.getPath());
		
		PropertyConfigurator.configure(new File(conf, "log4j.properties").getAbsolutePath());
		
		classLoader = getClass().getClassLoader();
		if(log.isInfoEnabled()) {
			log.info("Using class loader " + classLoader.getClass().getName());
		}
		
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		try {

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
	
	
	
	protected void runServer() throws AccessDeniedException, ServletException,
			IOException {

		server = (HypersocketServer) applicationContext.getBean("nettyServer");

		server.init(applicationContext);

		try {
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

		if(server!=null) {
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
		
		if(profiles.length == 0){
			applicationContext = new ClassPathXmlApplicationContext(configLocation);
		}else{
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

		TransactionTemplate txnTemplate = new TransactionTemplate(
				transactionManager);

		if (log.isInfoEnabled()) {
			log.info("Calling TransactionTemplate.afterPropertiesSet");
		}

		txnTemplate.afterPropertiesSet();

		if (log.isInfoEnabled()) {
			log.info("Creating transaction for upgrade");
		}

		UpgradeService upgradeService = (UpgradeService) applicationContext
				.getBean("upgradeService");
		
		upgradeService.upgrade(txnTemplate);


	}
	
	
	static class DefaultRestartCallback implements Runnable {

		@Override
		public void run() {
			
			if(log.isInfoEnabled()) {
				log.info("There is no restart mechanism available. Shutting down");
			}
			
			System.exit(0);
		}
		
	}
	
	static class DefaultShutdownCallback implements Runnable {

		@Override
		public void run() {
			
			if(log.isInfoEnabled()) {
				log.info("Shutting down using default shutdown mechanism");
			}
			
			System.exit(0);
		}
		
	}

}
