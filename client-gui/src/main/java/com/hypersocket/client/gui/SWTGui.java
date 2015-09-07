package com.hypersocket.client.gui;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.jgntp.GntpClient;
import com.google.code.jgntp.GntpNotificationInfo;
import com.google.common.io.Closeables;
import com.hypersocket.client.Prompt;
import com.hypersocket.client.i18n.I18N;
import com.hypersocket.client.rmi.ApplicationLauncher;
import com.hypersocket.client.rmi.ApplicationLauncherTemplate;
import com.hypersocket.client.rmi.CancelledException;
import com.hypersocket.client.rmi.ClientService;
import com.hypersocket.client.rmi.ConfigurationService;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.ConnectionService;
import com.hypersocket.client.rmi.GUICallback;
import com.hypersocket.client.rmi.Resource;
import com.hypersocket.client.rmi.ResourceLauncherThread;
import com.hypersocket.client.rmi.ResourceRealm;
import com.hypersocket.client.rmi.ResourceService;
import com.hypersocket.extensions.ExtensionDefinition;
import com.hypersocket.extensions.ExtensionPlace;

public class SWTGui extends UnicastRemoteObject implements GUICallback {

	static Logger log = LoggerFactory.getLogger(SWTGui.class);

	public static final String APPLICATION_ICON = "/app-icon.png";

	final static int DEFAULT_TIMEOUT = 10000;

	Display display;
	Shell shell;
	int timeout = DEFAULT_TIMEOUT;
	GntpClient client;
	GntpNotificationInfo notif1;
	Label messageLabel;
	int registrations = 0;

	Tray tray;
	Menu trayMenu;
	MenuItem connectOrDisconnectItem;
	MenuItem optionsItem;
	MenuItem connectionsItem;
	// MenuItem resourcesItem;
	// MenuItem launchItem;
	MenuItem exitItem;
	// Menu launchMenu;
	MenuItem resourcesSeparator;
	Image offlineImage;
	Image onlineImage;
	TrayItem trayItem;

	ConnectionService connectionService;
	ConfigurationService configurationService;
	ResourceService resourceService;

	ClientService clientService;
	ConnectionsWindow connectionsWindow;
	ResourcesTree resourcesWindow;

	boolean suspendShellClose = false;
	boolean awaitingServiceStop = false;
	boolean awaitingServiceStart = false;

	private int appsToUpdate = -1;
	private int appsUpdated = 0;
	private UpdateWindow updateWindow;

	protected SWTGui(Display display, Shell shell) throws RemoteException {
		super();
		this.display = display;
		this.shell = shell;

		// try {
		// configureGrowl();
		// } catch (Throwable e) {
		// log.error("Failed to start growl", e);
		// }

		setupSystemTray();

		new RMIConnectThread().start();

	}

	private static final long serialVersionUID = 4078585204004591626L;

	public boolean isFirstRegistration() {
		return registrations == 1;
	}

	public void registered() {
		registrations++;
		setOnlineState(true);
		showPopupMessage("Connected to local Hypersocket service",
				"Hypersocket Client");
	}

	@Override
	public void unregistered() {
		registrations--;
		setOnlineState(false);
		showPopupMessage("Disconnected from local Hypersocket service",
				"Hypersocket Client");
	}

	private void setOnlineState(final boolean online) {
		if (log.isInfoEnabled()) {
			log.info("Setting online state to " + online);
		}
		display.asyncExec(new Runnable() {
			public void run() {
				trayItem.setImage(online ? onlineImage : offlineImage);
				optionsItem.setEnabled(online);
				connectionsItem.setEnabled(online);
				// resourcesItem.setEnabled(online);
			}
		});
	}

	public void notify(String msg, int type) {

		switch (type) {
		case NOTIFY_CONNECT:
		case NOTIFY_DISCONNECT:

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (connectionsWindow != null) {
						connectionsWindow.updateActions();
					}
					rebuildLaunchMenu();
				}
			});

			break;
		default:
			break;
		}
		showPopupMessage(msg, "Hypersocket Client");
	}

	private void showPopupMessage(final String message, final String title) {

	}

	private void rebuildLaunchMenu() {

		Integer previousCount = (Integer) trayMenu.getData();
		if (previousCount != null) {
			List<MenuItem> toDispose = new ArrayList<MenuItem>();
			for (int i = 0; i < previousCount; i++) {
				toDispose.add(trayMenu.getItem(i));
			}
			for (MenuItem i : toDispose) {
				i.dispose();
			}
		}

		if (resourcesSeparator != null) {
			resourcesSeparator.dispose();
			resourcesSeparator = null;
		}

		int idx = 0;
		try {
			for (ResourceRealm realm : resourceService.getResourceRealms()) {

				if (realm.getResources().size() > 0) {
					MenuItem realmItem = new MenuItem(trayMenu, SWT.CASCADE,
							idx++);
					realmItem.setText(realm.getName());
					realmItem.setEnabled(true);

					Menu realmMenu = new Menu(trayMenu);
					realmItem.setMenu(realmMenu);

					for (Resource res : realm.getResources()) {
						if (res.isLaunchable()) {
							MenuItem resourceItem = new MenuItem(realmMenu,
									SWT.PUSH);
							resourceItem.setText(res.getName());
							resourceItem.setData(res);
							resourceItem.addListener(SWT.Selection,
									new Listener() {
										public void handleEvent(Event e) {
											Resource res = (Resource) e.widget
													.getData();
											new ResourceLauncherThread(res.getResourceLauncher());
										}
									});
						} 
					}
				}
			}

			if (idx > 0) {
				resourcesSeparator = new MenuItem(trayMenu, SWT.SEPARATOR, idx);
			}

			trayMenu.setData(new Integer(idx));
		} catch (RemoteException e) {
			log.error("Failed to communicate with service", e);
		}
	}

	private void connectToService() throws RemoteException, NotBoundException {

		Properties properties = new Properties();
		FileInputStream in;
		try {
			if (Boolean.getBoolean("hypersocket.development")) {
				in = new FileInputStream(System.getProperty("user.home")
						+ File.separator + ".hypersocket" + File.separator
						+ "conf" + File.separator + "rmi.properties");
			} else {
				in = new FileInputStream("conf" + File.separator
						+ "rmi.properties");
			}

			try {
				properties.load(in);
			} finally {
				in.close();
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		int port = Integer.parseInt(properties.getProperty("port", "50000"));

		try {

			if (log.isDebugEnabled()) {
				log.debug("Connecting to local service on port " + port);
			}
			Registry registry = LocateRegistry.getRegistry(port);

			connectionService = (ConnectionService) registry
					.lookup("connectionService");

			configurationService = (ConfigurationService) registry
					.lookup("configurationService");

			resourceService = (ResourceService) registry
					.lookup("resourceService");

			clientService = (ClientService) registry.lookup("clientService");

			clientService.registerGUI(this);

			if (isFirstRegistration()) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						try {
							clientService.unregisterGUI(SWTGui.this);
						} catch (RemoteException e) {
						}
					}
				});
			}

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (connectionsWindow != null) {
						connectionsWindow.updateActions();
					}
					rebuildLaunchMenu();
				}
			});

			new RMIStatusThread().start();
		} catch (Throwable e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to connect to local service on port " + port);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			connectToService();
		}

	}

	protected RenderedImage getImage(String name) throws IOException {
		InputStream is = getClass().getResourceAsStream(name);
		try {
			return ImageIO.read(is);
		} finally {
			Closeables.closeQuietly(is);
		}
	}

	private void setupSystemTray() {

		tray = display.getSystemTray();
		if (tray == null) {
			throw new IllegalStateException("System tray is not supported in "
					+ System.getProperty("os.name"));
		}

		InputStream in = Main.class
				.getClassLoader()
				.getResourceAsStream(
						System.getProperty("os.name").startsWith("Mac") ? "tray-offline.png"
								: "tray-offline-white.png");
		offlineImage = new Image(display, in);

		in = Main.class.getClassLoader().getResourceAsStream("tray.png");
		onlineImage = new Image(display, in);

		trayItem = new TrayItem(tray, SWT.NONE);
		trayItem.setImage(offlineImage);

		trayMenu = new Menu(shell, SWT.POP_UP);

		optionsItem = new MenuItem(trayMenu, SWT.PUSH);
		optionsItem.setText(I18N.getResource("client.menu.options"));
		optionsItem.setEnabled(false);
		optionsItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				optionsItem.setEnabled(false);
				OptionsDialog options = new OptionsDialog(SWTGui.this);
				if (options.open() == Dialog.OK) {
					try {
						options.saveOptions();
						changeLocale(configurationService.getValue("ui.locale",
								"en"));
						resetMenuText();
					} catch (IOException ex) {
						log.error("", ex);
					}
				}
				optionsItem.setEnabled(true);
			}
		});

		connectionsItem = new MenuItem(trayMenu, SWT.PUSH);
		connectionsItem.setText(I18N.getResource("connections.text"));
		connectionsItem.setEnabled(false);
		connectionsItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {

				if (connectionsWindow == null) {
					connectionsWindow = new ConnectionsWindow(SWTGui.this,
							clientService);
					shell.pack();
					connectionsWindow.open();
				}
				connectionsWindow.getShell().setVisible(true);
				connectionsWindow.getShell().setActive();
			}
		});

		new MenuItem(trayMenu, SWT.SEPARATOR);

		exitItem = new MenuItem(trayMenu, SWT.PUSH);
		exitItem.setText(I18N.getResource("client.menu.exit"));
		exitItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				System.exit(0);
			}
		});

		shell.addShellListener(new ShellListener() {
			public void shellIconified(ShellEvent e) {
			}

			public void shellDeiconified(ShellEvent e) {
			}

			public void shellDeactivated(ShellEvent e) {
			}

			public void shellClosed(ShellEvent e) {
				shell.setVisible(false);
				// e.doit = false;
			}

			public void shellActivated(ShellEvent e) {
			}
		});

		trayItem.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				trayMenu.setVisible(true);
			}
		});
	}

	protected void resetMenuText() {
		exitItem.setText(I18N.getResource("client.menu.exit"));
		optionsItem.setText(I18N.getResource("client.menu.options"));
	}

	protected void changeLocale(String locale) {

	}

	class RMIConnectThread extends Thread {
		public void run() {
			try {
				connectToService();

				if (awaitingServiceStart) {
					log.info("Service has started again after an update, now restarting the UI");
					Main.getInstance().restart();
				}
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to connect to service", e);
				}
			}
		}
	}

	class RMIStatusThread extends Thread {
		public void run() {
			try {
				boolean running = true;
				while (running) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
					if (clientService != null) {
						try {
							clientService.ping();
							display.asyncExec(new Runnable() {
								@Override
								public void run() {
									if (connectionsWindow != null) {
										connectionsWindow.refresh();
									}
								}
							});
						} catch (Exception e) {
							running = false;
							log.error("Failed to get local service status", e);
						}
					}
				}
			} finally {
				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						closeConnectionsWindow();
						if (awaitingServiceStop) {
							awaitingServiceStop = false;
							awaitingServiceStart = true;
							updateWindow.awaitingNewService();

							// Wait for service to start again

							display.timerExec(30000, new Runnable() {
								@Override
								public void run() {
									if (awaitingServiceStart)
										updateWindow.failure(
												null,
												I18N.getResource("client.update.serviceDidNotStartInTime"));
								}
							});
						} else {
							closeUpdateWindow();
						}
					}
				});
				unregistered();
				new RMIConnectThread().start();
			}
		}
	}

	public Shell getShell() {
		return shell;
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	@Override
	public Map<String, String> showPrompts(final List<Prompt> prompts, int attempts, boolean success) {
		final Map<String, String> results = new HashMap<String, String>();

		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				shell.forceFocus();
				LogonDialog logonDialog = new LogonDialog(shell, prompts);
				logonDialog.open();
				Map<String, String> res = logonDialog.getResults();
				if (res != null) {
					results.putAll(res);
				}
			}
		});

		if (results.size() > 0) {
			return results;
		} else {
			return null;
		}
	}

	@Override
	public int executeAsUser(ApplicationLauncherTemplate launcherTemplate,
			String clientUsername, String connectedHostname)
			throws RemoteException {

		ApplicationLauncher launcher = new ApplicationLauncher(clientUsername,
				connectedHostname, launcherTemplate);
		return launcher.launch();
	}

	private void closeConnectionsWindow() {
		if (connectionsWindow != null) {
			log.info("Closing connections window");
			connectionsWindow.close();
			connectionsWindow = null;
		}
	}

	private void closeUpdateWindow() {
		if (updateWindow != null) {
			log.info("Closing updates window");
			updateWindow.close();
			updateWindow = null;
		}
	}

	@Override
	public void disconnected(Connection connection, String errorMessage)
			throws RemoteException {
		// TODO use these instead of polling in SWT client too
	}

	@Override
	public void failedToConnect(Connection connection, String errorMessage)
			throws RemoteException {
		// TODO use these instead of polling in SWT client too
	}

	@Override
	public void transportConnected(Connection connection)
			throws RemoteException {
		// TODO use these instead of polling in SWT client too
	}

	@Override
	public void loadResources(Connection connection) throws RemoteException {
	}

	@Override
	public void ready(Connection connection) throws RemoteException {
		// TODO use these instead of polling in SWT client too
	}

	@Override
	public void onUpdateStart(final String app, final long totalBytesExpected)
			throws RemoteException {
		if (isUpdateCancelled()) {
			throw new CancelledException();
		}
		shell.getDisplay().syncExec(new Runnable() {

			public void run() {
				updateWindow.start(app, totalBytesExpected);
			}
		});
	}

	@Override
	public void onUpdateProgress(final String app,
			final long sincelastProgress, final long totalSoFar, long totalBytesExpected)
			throws RemoteException {
		if (isUpdateCancelled()) {
			throw new CancelledException();
		}
		shell.getDisplay().syncExec(new Runnable() {

			public void run() {
				updateWindow.progress(app, sincelastProgress, totalSoFar);
			}
		});
	}

	@Override
	public void onUpdateComplete(final long totalBytesTransfered,
			final String app) throws RemoteException {
		if (isUpdateCancelled()) {
			throw new RemoteException("Cancelled by user.");
		}
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				updateWindow.complete(app);
			}
		});
		appsUpdated++;
		log.info(String.format(
				"Update of %s complete, have now updated %d of %d apps", app,
				appsUpdated, appsToUpdate));

	}

	@Override
	public void onUpdateFailure(final String app, final String message) {
		if (updateWindow != null) {
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					updateWindow.failure(app, message);
				}
			});
			appsUpdated++;
		}
	}

	@Override
	public void onExtensionUpdateComplete(String app, ExtensionDefinition def) {
	}

	@Override
	public void onUpdateInit(int expectedApps) throws RemoteException {
		appsToUpdate = expectedApps;
		appsUpdated = 0;
		shell.getDisplay().syncExec(new Runnable() {

			public void run() {
				updateWindow = new UpdateWindow(shell) {
					@Override
					public boolean close() {
						try {
							return super.close();
						} finally {
							updateWindow = null;
							appsToUpdate = 0;
							appsUpdated = 0;
							trayMenu.setEnabled(true);
							if (connectionsWindow != null) {
								connectionsWindow.updateActions();
							}
						}
					}
				};
				updateWindow.show();
				trayMenu.setEnabled(false);
				if (connectionsWindow != null) {
					connectionsWindow.updateActions();
				}
			}
		});
	}

	@Override
	public ExtensionPlace getExtensionPlace() throws RemoteException {
		return ExtensionPlace.getDefault();
	}

	@Override
	public void onUpdateDone(final String errorMessage) throws RemoteException {
		if (isUpdateCancelled()) {
			throw new CancelledException();
		}
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (errorMessage == null) {
					log.info(String
							.format("All apps updated, starting restart process"));
					updateWindow.done();
					awaitingServiceStop = true;
					display.timerExec(30000, new Runnable() {
						@Override
						public void run() {
							if (awaitingServiceStop)
								updateWindow.failure(
										null,
										I18N.getResource("client.update.serviceDidNotStopInTime"));
						}
					});
				} else {
					updateWindow.failure(null, errorMessage);
				}
			}
		});
	}

	public boolean isUpdating() {
		return appsToUpdate > 0;
	}

	private boolean isUpdateCancelled() {
		return updateWindow == null || updateWindow.isCancelled();
	}

	@Override
	public void started(Connection connection) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
