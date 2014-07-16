package com.hypersocket.client.gui;

import java.util.prefs.Preferences;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Shell;

public class AbstractWindow extends ApplicationWindow {
	
	private Preferences prefs;
	
	public AbstractWindow(Shell shell) {
		super(shell);
		prefs = Preferences.userRoot().node(getClass().getName());
	}
	public
	ShellListener getShellListener() {
		return new ShellListener() {

			@Override
			public void shellActivated(ShellEvent evt) {
				getShell().setSize(prefs.getInt("window.width", 300),
						prefs.getInt("window.height", 525));
			}

			@Override
			public void shellClosed(ShellEvent evt) {
				evt.doit = false;
				saveSize();
				getShell().setVisible(false);
			}

			@Override
			public void shellDeactivated(ShellEvent evt) {
				saveSize();
			}

			@Override
			public void shellDeiconified(ShellEvent evt) {
				getShell().setSize(prefs.getInt("window.width", 300),
						prefs.getInt("window.height", 525));
			}

			@Override
			public void shellIconified(ShellEvent evt) {
				saveSize();
			}
			
		};
	}
	
	private void saveSize() {
		prefs.put("window.width", String.valueOf(getShell().getBounds().width));
		prefs.put("window.height", String.valueOf(getShell().getBounds().height));
	}
	
	protected int getWindowWidth(int defaultValue) {
		return prefs.getInt("window.width", defaultValue);
	}
	
	protected int getWindowHeight(int defaultValue) {
		return prefs.getInt("window.height", defaultValue);
	}
	
}
