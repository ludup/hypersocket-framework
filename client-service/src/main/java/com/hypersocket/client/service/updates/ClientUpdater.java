package com.hypersocket.client.service.updates;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.client.HypersocketClient;
import com.hypersocket.client.rmi.CancelledException;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.GUICallback;
import com.hypersocket.extensions.AbstractExtensionUpdater;
import com.hypersocket.extensions.ExtensionDefinition;
import com.hypersocket.extensions.ExtensionHelper;
import com.hypersocket.extensions.ExtensionPlace;
import com.hypersocket.utils.TrustModifier;

public class ClientUpdater extends AbstractExtensionUpdater {
	static Logger log = LoggerFactory.getLogger(ClientUpdater.class);

	private GUICallback gui;
	private boolean guiAttached = true;
	private HypersocketClient<Connection> hypersocketClient;
	private ExtensionPlace extensionPlace;
	private Connection connection;

	public ClientUpdater(GUICallback gui, Connection connection,
			HypersocketClient<Connection> hypersocketClient,
			ExtensionPlace extensionPlace) {
		super();
		this.connection = connection;
		this.gui = gui;
		this.hypersocketClient = hypersocketClient;
		this.extensionPlace = extensionPlace;
	}

	@Override
	protected Map<ExtensionPlace, List<ExtensionDefinition>> onResolveExtensions()
			throws IOException {

		log.info(String.format("Resolving extensions for %s in %s",
				extensionPlace.getApp(), extensionPlace.getDir()));

		// Client service first
		String reply = hypersocketClient.getTransport().get(
				"clientUpdates/resolveExtensions/" + extensionPlace.getApp());
		ObjectMapper mapper = new ObjectMapper();
		JsonExtensionResourceList json = mapper.readValue(reply,
				JsonExtensionResourceList.class);
		if (json.isSuccess()) {
			// Compare extensions returned by server with those we have
			// installed locally in the client service
			Map<String, ExtensionDefinition> defMap = new HashMap<>();
			for (ExtensionDefinition def : json.getResources()) {
				defMap.put(def.getId(), def);
			}

			ArrayList<ExtensionDefinition> extensionList = new ArrayList<ExtensionDefinition>(
					ExtensionHelper.processLocalExtensions(defMap,
							extensionPlace).values());
			Map<ExtensionPlace, List<ExtensionDefinition>> map = new HashMap<ExtensionPlace, List<ExtensionDefinition>>();
			map.put(extensionPlace, extensionList);
			return map;
		}
		throw new IOException(json.getError());
	}

	@Override
	protected InputStream downloadFromUrl(URL url) throws IOException {
		URLConnection con = url.openConnection();
		// TODO make this configurable or have a way to prompt user to verify certificate
		TrustModifier.relaxHostChecking(con);
		return con.getInputStream();
	}

	@Override
	protected void onUpdateStart(long totalBytesExpected) {
		try {
			gui.onUpdateStart(extensionPlace.getApp(), totalBytesExpected);
			guiAttached = true;
		} catch (RemoteException e) {
			if (e.getCause() instanceof CancelledException) {
				try {
					gui.onUpdateFailure(null, "Cancelled by user.");
				} catch (RemoteException e2) {
				}
			} else {
				log.error(
						"Failed to inform client of starting update. No further attempts will be made.",
						e);
				guiAttached = false;
			}
		}
	}

	@Override
	protected void onUpdateProgress(long sincelastProgress, long totalSoFar) {
		if (guiAttached) {
			try {
				gui.onUpdateProgress(extensionPlace.getApp(),
						sincelastProgress, totalSoFar);
			} catch (RemoteException e) {
				if (e.getCause() instanceof CancelledException) {
					try {
						gui.onUpdateFailure(null, "Cancelled by user.");
					} catch (RemoteException e2) {
					}
				} else {
					log.error(
							"Failed to inform client of update progress. No further attempts will be made.",
							e);
					guiAttached = false;
				}
			}
		}
	}

	@Override
	protected void onUpdateComplete(long totalBytesTransfered) {
		if (guiAttached) {
			try {
				gui.onUpdateComplete(totalBytesTransfered,
						extensionPlace.getApp());
			} catch (RemoteException e) {
				if (e.getCause() instanceof CancelledException) {
					try {
						gui.onUpdateFailure(null, "Cancelled by user.");
					} catch (RemoteException e2) {
					}
				} else {
					log.error(
							"Failed to inform client of update completion. No further attempts will be made.",
							e);
					guiAttached = false;
				}
			}
		}
	}

	@Override
	protected void onUpdateFailure(Throwable e) {
		if (guiAttached) {
			try {
				gui.onUpdateFailure(
						extensionPlace.getApp(),
						e == null ? "Update failed. No reason supplied." : e
								.getMessage());
			} catch (RemoteException ex) {
				log.error(
						"Failed to inform client of update failure. No further attempts will be made.",
						ex);
				guiAttached = false;
			}
		}
	}

	@Override
	protected void onExtensionUpdateComplete(ExtensionDefinition def) {
		if (guiAttached) {
			try {
				gui.onExtensionUpdateComplete(extensionPlace.getApp(), def);
			} catch (RemoteException ex) {
				if (ex.getCause() instanceof CancelledException) {
					try {
						gui.onUpdateFailure(null, "Cancelled by user.");
					} catch (RemoteException e) {
					}
				} else {
					log.error(
							"Failed to inform client of extension update completion. No further attempts will be made.",
							ex);
					guiAttached = false;
				}
			}
		}
	}

}
