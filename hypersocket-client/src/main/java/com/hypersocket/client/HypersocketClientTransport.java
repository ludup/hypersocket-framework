/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Map;

public interface HypersocketClientTransport {

	void connect(String host, int port, String path) throws UnknownHostException,
			IOException;

	String get(String uri) throws IOException;

	InputStream getContent(String uri, long timeout) throws IOException;
	
	byte[] getBlob(String uri, long timeout) throws IOException;

	String post(String uri, Map<String, String> params) throws IOException;

	int startLocalForwarding(String listenAddress, int listenPort,
			NetworkResource resource) throws IOException;

	void stopLocalForwarding(String listenAddress, int listenPort);

	boolean isConnected();

	void disconnect(boolean onError);

	void setHeader(String name, String value);

	void removeHeader(String name);

	void shutdown();

	String getHost();

	int getPort();

	void stopAllForwarding();

	String get(String uri, long timeout) throws IOException;

	String post(String uri, Map<String, String> params, long timeout)
			throws IOException;

	String resolveUrl(String string);

}
