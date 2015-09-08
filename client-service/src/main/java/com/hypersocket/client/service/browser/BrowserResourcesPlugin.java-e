package com.hypersocket.client.service.browser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.client.HypersocketClient;
import com.hypersocket.client.i18n.I18N;
import com.hypersocket.client.rmi.BrowserLauncher;
import com.hypersocket.client.rmi.Resource.Type;
import com.hypersocket.client.rmi.ResourceImpl;
import com.hypersocket.client.rmi.ResourceRealm;
import com.hypersocket.client.rmi.ResourceService;
import com.hypersocket.client.service.AbstractServicePlugin;
import com.hypersocket.client.service.GUIRegistry;

public class BrowserResourcesPlugin extends AbstractServicePlugin {

	static Logger log = LoggerFactory.getLogger(BrowserResourcesPlugin.class);

	List<JsonBrowserResource> browserResources = new ArrayList<JsonBrowserResource>();
	HypersocketClient<?> serviceClient;
	ResourceService resourceService;

	public BrowserResourcesPlugin() {
	}

	@Override
	public boolean start(HypersocketClient<?> serviceClient,
			ResourceService resourceService, GUIRegistry guiRegistry) {

		this.serviceClient = serviceClient;
		this.resourceService = resourceService;

		if (log.isInfoEnabled()) {
			log.info("Starting Browser Resources");
		}

		startBrowserResources();

		return true;
	}

	protected void startBrowserResources() {
		try {
			String json = serviceClient.getTransport().get(
					"browser/myResources");

			ObjectMapper mapper = new ObjectMapper();

			JsonBrowserResourceList list = mapper.readValue(json,
					JsonBrowserResourceList.class);

			Map<String, String> properties = list.getProperties();

			int errors = processBrowserResources(list.getResources(),
					properties.get("authCode"));

			if (errors > 0) {
				// Warn
				serviceClient.showWarning(errors
						+ " websites could not be opened.");
			}

		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("Could not start website resources", e);
			}
		}
	}

	protected int processBrowserResources(JsonBrowserResource[] resources,
			String authCode) throws IOException {

		int errors = 0;

		for (JsonBrowserResource resource : resources) {

			try {

				ResourceRealm resourceRealm = resourceService
						.getResourceRealm(serviceClient.getHost());

				ResourceImpl res = new ResourceImpl(resource.getName() + " - "
						+ I18N.getResource("text.defaultBrowser"));
				res.setLaunchable(true);
				res.setIcon(resource.getLogo());

				if (resource.getType()!=null && resource.getType().equals("FileResource")) {
					res.setType(Type.FILE);
				} else if (resource.getType()!=null && resource.getType().equals("BrowserSSOPlugin")) {
					res.setType(Type.SSO);
				} else {
					res.setType(Type.BROWSER);
					if(res.getIcon() == null || res.getIcon().equals("")) {
						res.setIcon("web-https");
					}
				}

				String sessionId = serviceClient.getSessionId();
				res.setResourceLauncher(new BrowserLauncher(serviceClient
						.getTransport().resolveUrl(
								"attach/"
										+ authCode
										+ "/"
										+ sessionId
										+ "?location="
										+ URLEncoder.encode(
												resource.getLaunchUrl(),
												"UTF-8"))));
				resourceRealm.addResource(res);

			} catch (RemoteException ex) {
				log.error(
						"Received remote exception whilst processing resources",
						ex);
				errors++;
			} catch (UnsupportedEncodingException e) {
				log.error("Looks liek the system does not support UTF-8!", e);
				errors++;
			}

		}

		return errors;
	}

	@Override
	public void stop() {

		if (log.isInfoEnabled()) {
			log.info("Stopping Browser Resources plugin");
		}

		try {
			resourceService.removeResourceRealm(serviceClient.getHost());
		} catch (RemoteException e) {
			log.error(
					"Failed to remove resource realm "
							+ serviceClient.getHost(), e);
		}
	}

	@Override
	public String getName() {
		return "Browser Resources";
	}
}
