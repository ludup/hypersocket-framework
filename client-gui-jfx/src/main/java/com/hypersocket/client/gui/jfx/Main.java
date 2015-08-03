package com.hypersocket.client.gui.jfx;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
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
		instance = this;

		this.restartCallback = restartCallback;
		this.shutdownCallback = shutdownCallback;

		// http://stackoverflow.com/questions/24159825/changing-application-dock-icon-javafx-programatically
		try {
			if (SystemUtils.IS_OS_MAC_OSX) {
				Class<?> appClazz = Class.forName("com.apple.eawt.Application");
				Object app = appClazz.getMethod("getApplication").invoke(null);
				appClazz.getMethod("setDockIconImage", Image.class)
						.invoke(app,
								java.awt.Toolkit
										.getDefaultToolkit()
										.getImage(
												Main.class
														.getResource("hypersocket-icon128x128.png")));
			}
		} catch (Exception e) {
			// Won't work on Windows or Linux.
		}

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

	/*
	 * NOTE: LauncherImpl has to be used, as Application.launch() tests where
	 * the main() method was invoked from by examining the stack (stupid stupid
	 * stupid technique!). Because we are launched from BoostrapMain, this is
	 * what it detects. To work around this LauncherImpl.launchApplication() is
	 * used directly, which is an internal API.
	 */
	@SuppressWarnings("restriction")
	public void run() {

		try {
			// :(
			com.sun.javafx.application.LauncherImpl.launchApplication(
					Client.class, null, new String[0]);
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

		new Main(new DefaultRestartCallback(), new DefaultShutdownCallback())
				.run();

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
