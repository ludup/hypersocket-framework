package com.hypersocket.client.service.browser;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.hypersocket.client.NetworkResource;
import com.hypersocket.client.i18n.I18N;
import com.hypersocket.utils.IPAddressValidator;

public class BrowserResourceTemplate implements Serializable {

	private static final long serialVersionUID = -7338245578258667006L;

	Long id;
	String name;
	String launchUrl;
	
	List<NetworkResource> liveResources = new ArrayList<NetworkResource>();
	
	public BrowserResourceTemplate(Long id, String name, String launchUrl) throws MalformedURLException {
		this.id = id;
		this.name = name;
		this.launchUrl = sanitizeURL(launchUrl).toExternalForm();
	}

	private URL sanitizeURL(String url) throws MalformedURLException {
		
		URL u = new URL(url);
		String hostname = IPAddressValidator.getInstance().getGuaranteedHostname(u.getHost());
		
		return new URL(u.getProtocol(), hostname, u.getPort(), u.getFile());
	}
	
	public String getName() {
		return name;
	}
	
	public String getLaunchUrl() {
		return launchUrl;
	}

	public String getStatus() {
		if(liveResources.size() > 0) {
			StringBuffer buf = new StringBuffer();
			for(NetworkResource r : liveResources) {
				if(buf.length() > 0) {
					buf.append(",");
				}
				buf.append(r.getLocalPort());
			}
			return I18N.getResource("status.active", buf.toString());
		} else {
			return I18N.getResource("status.inactive");
		}
	}
	
	public void addLiveResource(NetworkResource resource) {
		liveResources.add(resource);
	}

	public List<NetworkResource> getLiveResources() {
		return liveResources;
	}

	public Long getId() {
		return id;
	}
}
