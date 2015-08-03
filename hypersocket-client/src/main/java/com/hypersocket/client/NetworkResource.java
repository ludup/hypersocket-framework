/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client;

import java.io.Serializable;

import com.hypersocket.utils.IPAddressValidator;

public class NetworkResource implements Serializable,ServiceResource {

	private static final long serialVersionUID = -3525449561878862225L;

	Long id;
	String hostname;
	String desintationHostname;
	int port;
	String alias;
	int actualPort;
	String uri;
	Status serviceStatus = Status.UNKNOWN;

	public NetworkResource(Long id, String hostname, String destinationHostname, int port, String uri) {
		this.id = id;
		this.hostname = IPAddressValidator.getInstance().getGuaranteedHostname(
				hostname);
		this.desintationHostname = destinationHostname;
		this.port = port;
		this.uri = uri;
	}

	public Long getId() {
		return id;
	}

	public String getHostname() {
		return hostname;
	}

	public String getDestinationHostname() {
		return desintationHostname;
	}
	
	public int getPort() {
		return port;
	}

	public void setLocalPort(int actualPort) {
		this.actualPort = actualPort;
	}

	public int getLocalPort() {
		return actualPort;
	}

	public void setAliasInterface(String alias) {
		this.alias = alias;
	}

	public String getAliasInterface() {
		return alias;
	}

	public String getUri() {
		return uri;
	}

	@Override
	public Status getServiceStatus() {
		return serviceStatus;
	}

	public void setServiceStatus(Status serviceStatus) {
		this.serviceStatus = serviceStatus;
	}

	@Override
	public String getServiceDescription() {
		return getDestinationHostname() + ":" + getPort();
	}

}
