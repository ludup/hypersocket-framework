package com.hypersocket.client.service;

import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.rmi.CancelledException;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.GUICallback;
import com.hypersocket.extensions.ExtensionDefinition;

/**
 * A facade for the {@link GUIRegistry} instance that should be used 'service
 * side'. It wraps remote callbacks, only calling if the RMI connection hasn't
 * been lost, and sometimes doing to exception handling as well so other service
 * side code does not have to do this.
 */
public class GUIRegistry {

	static Logger log = LoggerFactory.getLogger(GUIRegistry.class);

	private GUICallback gui;
	private final Object lock = new Object();
	private boolean guiAttached;

	public boolean hasGUI() {
		return gui != null;
	}

	public GUICallback getGUI() {
		return gui;
	}

	public void registerGUI(GUICallback gui) throws RemoteException {
		if (this.gui != null)
			throw new IllegalStateException("Already registered " + gui);

		synchronized (lock) {
			this.gui = gui;
			guiAttached = true;
			gui.registered();
			if (log.isInfoEnabled()) {
				log.info("Registered GUI");
			}
		}
	}

	public void unregisterGUI(GUICallback gui) throws RemoteException {
		synchronized (lock) {
			if (gui == null)
				throw new IllegalStateException("Not registered " + gui);
			this.gui = null;
			guiAttached = false;
			gui.unregistered();
			if (log.isInfoEnabled()) {
				log.info("Unregistered GUI");
			}
		}
	}

	public void started(Connection connection) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					log.info("Informing GUI " + connection + " to start");
					gui.started(connection);
				}
			} catch (RemoteException re) {
				log.error("Failed to inform GUI of readyness.", re);
			}
		}
	}

	public void ready(Connection connection) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					log.info("Informing GUI " + connection + " is ready");
					gui.ready(connection);
				}
			} catch (RemoteException re) {
				log.error("Failed to inform GUI of readyness.", re);
			}
		}

	}

	public void loadResources(Connection connection) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					log.info("Informing GUI " + connection + " to load resources");
					gui.loadResources(connection);
				}
			} catch (RemoteException re) {
				log.error("Failed to inform GUI of readyness.", re);
			}
		}

	}

	public void failedToConnect(Connection connection, String reply) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					gui.failedToConnect(connection,
							"Could not connect. " + reply);
				}
			} catch (RemoteException re) {
				log.error("Failed to inform GUI of connection failure.", re);
			}
		}

	}

	public void disconnected(Connection connection, String message) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					gui.disconnected(connection, "Disconnected. "
							+ message);
				}
			} catch (RemoteException re) {
				log.error("Failed to inform GUI of disconnection.", re);
			}
		}

	}

	public void transportConnected(Connection connection) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					gui.transportConnected(connection);
				}
			} catch (RemoteException re) {
				log.error("Failed to inform GUI of transport connection.", re);
			}
		}
	}

	public void notify(String msg, int type) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					gui.notify(msg, type);
				}
			} catch (RemoteException re) {
				log.error("Failed to inform GUI of transport connection.", re);
			}
		}
	}

	public void onExtensionUpdateComplete(String app, ExtensionDefinition def) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					gui.onExtensionUpdateComplete(app, def);
				}
			} catch (RemoteException ex) {
				failed(app, ex);
			}
		}
	}

	public void onUpdateProgress(String app, long sincelastProgress,
			long totalSoFar, long totalBytesExpected) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					gui.onUpdateProgress(app, sincelastProgress, totalSoFar, totalBytesExpected);
				}
			} catch (RemoteException ex) {
				failed(app, ex);
			}
		}
	}

	public void onUpdateStart(String app, long totalBytesExpected) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					gui.onUpdateStart(app, totalBytesExpected);
				}
			} catch (RemoteException ex) {
				failed(app, ex);
			}
		}
	}

	public void onUpdateInit(int apps) throws RemoteException {
		synchronized (lock) {
			if (gui != null && guiAttached) {
				gui.onUpdateInit(apps);
			}
		}
	}

	public void onUpdateComplete(String app, long totalBytesTransfered) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					gui.onUpdateComplete(totalBytesTransfered, app);
				}
			} catch (RemoteException ex) {
				failed(app, ex);
			}
		}
	}

	public void onUpdateFailure(String app, Throwable e) {
		synchronized (lock) {
			try {
				if (gui != null && guiAttached) {
					gui.onUpdateFailure(app,
							e == null ? "Update failed. No reason supplied."
									: e.getMessage());
				}
			} catch (RemoteException ex) {
				failed(app, ex);
			}
		}
	}

	public void onUpdateDone(String failureMessage) throws RemoteException {
		synchronized (lock) {
			if (gui != null && guiAttached) {
				gui.onUpdateDone(failureMessage);
			}
		}
	}

	private void failed(String app, RemoteException ex) {
		if (ex.getCause() instanceof CancelledException) {
			try {
				gui.onUpdateFailure(app, "Cancelled by user.");
			} catch (RemoteException ex2) {
				log.error("Failed to inform GUI of cancelled update.", ex2);
			}
		} else {
			log.error("Failed to inform GUI of update state change.", ex);
		}
		guiAttached = false;
	}

}
