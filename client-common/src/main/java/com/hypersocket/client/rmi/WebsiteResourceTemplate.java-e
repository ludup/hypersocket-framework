package com.hypersocket.client.rmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hypersocket.client.NetworkResource;
import com.hypersocket.client.i18n.I18N;

public class WebsiteResourceTemplate implements Serializable {

	private static final long serialVersionUID = -7338245578258667006L;

	Long id;
	String name;
	String launchUrl;
	String[] additionalUrls;
	List<NetworkResource> liveResources = new ArrayList<NetworkResource>();
	
	public WebsiteResourceTemplate(Long id, String name, String launchUrl, String[] additionalUrls) {
		this.id = id;
		this.name = name;
		this.launchUrl = launchUrl;
		this.additionalUrls = additionalUrls;
	}

	public String getName() {
		return name;
	}
	
	public String getLaunchUrl() {
		return launchUrl;
	}

	public String[] getAdditionalUrls() {
		return additionalUrls;
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
