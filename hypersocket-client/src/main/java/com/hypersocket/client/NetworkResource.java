/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client;

import java.io.Serializable;

public class NetworkResource implements Serializable {

	private static final long serialVersionUID = -3525449561878862225L;

	Long id;
	String hostname;
	int port;
	String alias;
	int actualPort;

	public NetworkResource(Long id, String hostname, int port) {
		this.id = id;
		this.hostname = hostname;
		this.port = port;
	}

	public Long getId() {
		return id;
	}

	public String getHostname() {
		return hostname;
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

	public void setLocalInterface(String alias) {
		this.alias = alias;
	}

	public String getLocalInterface() {
		return alias;
	}

}
