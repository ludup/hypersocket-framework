package com.hypersocket.client.gui.jfx;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	static Logger log = LoggerFactory.getLogger(Main.class);

	Runnable restartCallback;
	Runnable shutdownCallback;
	ClassLoader classLoader;
	static Main instance;

	public Main(Runnable restartCallback, Runnable shutdownCallback) {
		this.restartCallback = restartCallback;
		this.shutdownCallback = shutdownCallback;

		try {
			File dir = new File(System.getProperty("user.home"), ".hypersocket");
			dir.mkdirs();

			PropertyConfigurator.configure("conf" + File.separator
					+ "log4j-gui.properties");

		} catch (Exception e) {
			e.printStackTrace();
			BasicConfigurator.configure();
		}
	}

	public void run() {

		try {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}

			Client.launch();

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed to start client", e);
			System.exit(1);
		}
	}

	public static Main getInstance() {
		return instance;
	}

	public void restart() {
		restartCallback.run();
	}

	public void shutdown() {
		shutdownCallback.run();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		instance = new Main(new DefaultRestartCallback(),
				new DefaultShutdownCallback());

		instance.run();
	}

	public static void runApplication(Runnable restartCallback,
			Runnable shutdownCallback) throws IOException {

		new Main(restartCallback, shutdownCallback).run();

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
