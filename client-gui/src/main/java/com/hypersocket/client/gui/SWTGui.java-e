package com.hypersocket.client.gui;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

import com.google.code.jgntp.Gntp;
import com.google.code.jgntp.GntpApplicationInfo;
import com.google.code.jgntp.GntpClient;
import com.google.code.jgntp.GntpErrorStatus;
import com.google.code.jgntp.GntpListener;
import com.google.code.jgntp.GntpNotification;
import com.google.code.jgntp.GntpNotificationInfo;
import com.google.common.io.Closeables;
import com.hypersocket.client.Prompt;
import com.hypersocket.client.i18n.I18N;
import com.hypersocket.client.rmi.ClientService;
import com.hypersocket.client.rmi.ConfigurationService;
import com.hypersocket.client.rmi.ConnectionService;
import com.hypersocket.client.rmi.GUICallback;

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
	MenuItem exitItem;

	Image offlineImage;
	Image onlineImage;
	TrayItem trayItem;

	ConnectionService connectionService;
	ConfigurationService configurationService;
	ClientService clientService;
	ConnectionsWindow connectionsWindow;
	
	int rmiPort;

	protected SWTGui(Display display, Shell shell, int rmiPort)
			throws RemoteException {
		super();
		this.display = display;
		this.shell = shell;
		this.rmiPort = rmiPort;

		try {
			configureGrowl();
		} catch (Throwable e) {
			log.error("Failed to start growl", e);
		}

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
	public void unregistered() throws RemoteException {
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
			}
		});
	}

	public void notify(String msg, int type) {
		switch(type) {
		case NOTIFY_CONNECT:
		case NOTIFY_DISCONNECT:
			Display.getDefault().asyncExec(new Runnable() {
			    public void run() {
			    	if(connectionsWindow!=null) {
						connectionsWindow.updateActions();
					}
			    }
			});
			
			break;
		default:
			break;
		}
		showPopupMessage(msg, "Hypersocket Client");
	}

	private void showPopupMessage(final String message, final String title) {
		try {
			client.notify(
					Gntp.notification(notif1, title)
							.text(message)
							.context(12345)
							.header(Gntp.APP_SPECIFIC_HEADER_PREFIX
									+ "Filename", "file.txt").build(), 5,
					TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void connectToService(int port) throws RemoteException,
			NotBoundException {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Connecting to local service on port " + port);
			}
			Registry registry = LocateRegistry.getRegistry(port);

			connectionService = (ConnectionService) registry
					.lookup("connectionService");

			configurationService = (ConfigurationService) registry
					.lookup("configurationService");

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

			new RMIStatusThread().start();
		} catch (Throwable e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to connect to local service on port " + port);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			connectToService(port);
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

	private void configureGrowl() throws IOException {
		GntpApplicationInfo info = Gntp.appInfo("Test")
				.icon(getImage(APPLICATION_ICON)).build();
		notif1 = Gntp.notificationInfo(info, "Notify 1")
				.icon(getImage(APPLICATION_ICON)).build();

		client = Gntp.client(info).listener(new GntpListener() {
			@Override
			public void onRegistrationSuccess() {
				log.info("Registered");
			}

			@Override
			public void onNotificationSuccess(GntpNotification notification) {
				log.info("Notification success: " + notification);
			}

			@Override
			public void onClickCallback(GntpNotification notification) {
				log.info("Click callback: " + notification.getContext());
			}

			@Override
			public void onCloseCallback(GntpNotification notification) {
				log.info("Close callback: " + notification.getContext());
			}

			@Override
			public void onTimeoutCallback(GntpNotification notification) {
				log.info("Timeout callback: " + notification.getContext());
			}

			@Override
			public void onRegistrationError(GntpErrorStatus status,
					String description) {
				log.info("Registration Error: " + status + " - desc: "
						+ description);
			}

			@Override
			public void onNotificationError(GntpNotification notification,
					GntpErrorStatus status, String description) {
				log.info("Notification Error: " + status + " - desc: "
						+ description);
			}

			@Override
			public void onCommunicationError(Throwable t) {
				log.error("Communication error", t);
			}
		}).build();

		client.register();

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
		connectionsItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {

				if(connectionsWindow==null) {
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
                e.doit = false;
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
				connectToService(rmiPort);
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
						} catch (Exception e) {
							running = false;
							log.error("Failed to get local service status", e);
						}
					}
				}
			} finally {
				setOnlineState(false);
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
	public Map<String, String> showPrompts(final List<Prompt> prompts) {
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

		return results;
	}
}
