package com.hypersocket.client.gui;

import java.io.File;
import java.rmi.RMISecurityManager;
import java.util.prefs.Preferences;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	static Logger log = LoggerFactory.getLogger(Main.class);

	SWTGui swtGui;

	Display display;
	Shell shell;
	
	
	public void run(String[] args) {

		try {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new RMISecurityManager());
			}

			display = new Display();
			shell = new Shell(display);
			
			shell.addListener(SWT.CLOSE, new Listener() {
				public void handleEvent(Event event) {
					event.doit = false;
				}
			});
			
			swtGui = new SWTGui(display, shell);
			
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.dispose();

		} catch (Exception e) {
			log.error("Failed to start client", e);
			System.exit(1);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			File dir = new File(System.getProperty("user.home"), ".hypersocket");
			dir.mkdirs();
			
			PropertyConfigurator.configure("conf" + File.separator + "log4j-gui.properties");
			BasicConfigurator.configure();
		} catch (Exception e) {
			BasicConfigurator.configure();
		}
		new Main().run(args);
	}

}
