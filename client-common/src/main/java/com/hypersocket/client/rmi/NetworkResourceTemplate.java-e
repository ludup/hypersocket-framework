/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client.rmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hypersocket.client.NetworkResource;
import com.hypersocket.client.i18n.I18N;
import com.hypersocket.utils.IPAddressValidator;

public class NetworkResourceTemplate implements Serializable {

	private static final long serialVersionUID = -4065939088547544167L;

	String name;
	String hostname;
	String destinationHostname;
	String protocol;
	int startPort;
	int endPort;
	String transport;
	String status;
	List<NetworkResource> liveResources = new ArrayList<NetworkResource>();

	public NetworkResourceTemplate(String name, String hostname,
			String destinationHostname, String protocol, String transport,
			int startPort, int endPort) {
		this.name = name;
		this.hostname = IPAddressValidator.getInstance().getGuaranteedHostname(
				hostname);
		this.destinationHostname = destinationHostname;
		this.protocol = protocol;
		this.startPort = startPort;
		this.endPort = endPort;
		this.transport = transport;
	}

	public String getName() {
		return name;
	}

	public String getHostname() {
		return hostname;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getDestinationHostname() {
		return destinationHostname;
	}

	public int getStartPort() {
		return startPort;
	}

	public int getEndPort() {
		return endPort;
	}

	public String getTransport() {
		return transport;
	}

	public String getStatus() {
		if (liveResources.size() > 0) {
			StringBuffer buf = new StringBuffer();
			for (NetworkResource r : liveResources) {
				if (buf.length() > 0) {
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

}
