package com.hypersocket.client.service.updates;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.client.HypersocketClient;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.service.GUIRegistry;
import com.hypersocket.extensions.AbstractExtensionUpdater;
import com.hypersocket.extensions.ExtensionDefinition;
import com.hypersocket.extensions.ExtensionHelper;
import com.hypersocket.extensions.ExtensionPlace;
import com.hypersocket.utils.TrustModifier;

public class ClientUpdater extends AbstractExtensionUpdater {
	static Logger log = LoggerFactory.getLogger(ClientUpdater.class);

	private GUIRegistry gui;
	private HypersocketClient<Connection> hypersocketClient;
	private ExtensionPlace extensionPlace;
	@SuppressWarnings("unused")
	private Connection connection; // Will probably be used again

	public ClientUpdater(GUIRegistry gui, Connection connection,
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
		// TODO make this configurable or have a way to prompt user to verify
		// certificate
		TrustModifier.relaxHostChecking(con);
		return con.getInputStream();
	}

	@Override
	protected void onUpdateStart(long totalBytesExpected) {
		gui.onUpdateStart(extensionPlace.getApp(), totalBytesExpected);
	}

	@Override
	protected void onUpdateProgress(long sincelastProgress, long totalSoFar, long totalBytesExpected) {
		gui.onUpdateProgress(extensionPlace.getApp(), sincelastProgress,
				totalSoFar,totalBytesExpected);
	}

	@Override
	protected void onUpdateComplete(long totalBytesTransfered) {
		gui.onUpdateComplete(extensionPlace.getApp(), totalBytesTransfered);
	}

	@Override
	protected void onUpdateFailure(Throwable e) {
		gui.onUpdateFailure(extensionPlace.getApp(), e);
	}

	@Override
	protected void onExtensionUpdateComplete(ExtensionDefinition def) {
		gui.onExtensionUpdateComplete(extensionPlace.getApp(), def);
	}

}
