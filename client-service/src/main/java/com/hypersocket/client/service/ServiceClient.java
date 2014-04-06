package com.hypersocket.client.service;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.HypersocketClient;
import com.hypersocket.client.HypersocketClientTransport;
import com.hypersocket.client.Prompt;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.GUICallback;

public class ServiceClient extends HypersocketClient<Connection> {

	static Logger log = LoggerFactory.getLogger(ServiceClient.class);
	
	ClientServiceImpl service;
	List<ServicePlugin> plugins = new ArrayList<ServicePlugin>();
	
	protected ServiceClient(HypersocketClientTransport transport,
			Locale currentLocale, ClientServiceImpl service, Connection connection) throws IOException {
		super(transport, currentLocale, service);
		this.service = service;
		setAttachment(connection);
	}

	@Override
	protected void onDisconnect() {
		// Do nothing cause the listener now handles this
	}

	// @Override
	protected Map<String, String> showLogin(List<Prompt> prompts) {
		if(service.getGUI()!=null) {
			try {
				return service.getGUI().showPrompts(prompts);
			} catch(RemoteException e) {
				log.error("Failed to show prompts", e);
			}
		}
		return null;
	}
	
	protected void onConnected() {
		loadPlugins();
	}

	private void loadPlugins() {
		
		try {
			Enumeration<URL> urls = getClass().getClassLoader().getResources("service-plugin.properties");
			
			while(urls.hasMoreElements()) {
				URL pluginUrl = urls.nextElement();
				try {
					Properties props = new Properties();
					props.load(pluginUrl.openStream());
					
					if(log.isInfoEnabled()) {
						log.info("Starting service plugin " + props.getProperty("plugin.name"));
					}
					ServicePlugin plugin = (ServicePlugin) Class.forName(props.getProperty("plugin.class")).newInstance();
					if(plugin.start(this)) {
						plugins.add(plugin);
					}
				} catch (Throwable e) {
					log.error("Failed to load plugin " + pluginUrl.toString(), e);
				}
				
			}
		} catch(Throwable e) {
			log.error("Failed to load plugins", e);
		}
		
	}

	@Override
	public void showWarning(String msg) {
		if (service.getGUI() != null) {
			try {
				service.getGUI().notify(msg, GUICallback.NOTIFY_WARNING);
			} catch (RemoteException e) {
				log.error("Failed to show warning", e);
			}
		}
	}

	@Override
	public void showError(String msg) {
		if (service.getGUI() != null) {
			try {
				service.getGUI().notify(msg, GUICallback.NOTIFY_ERROR);
			} catch (RemoteException e) {
				log.error("Failed to show error", e);
			}
		}
	}

	@Override
	protected void onDisconnecting() {
		
		for(ServicePlugin plugin : plugins) {
			try {
				plugin.stop();
			} catch (Throwable e) {
				log.error("Failed to stop plugin " + plugin.getName(), e);
			}
		}
		
	}

}
