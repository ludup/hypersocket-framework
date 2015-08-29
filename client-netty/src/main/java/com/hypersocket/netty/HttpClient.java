/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.HypersocketVersion;
import com.hypersocket.netty.websocket.WebSocket;
import com.hypersocket.netty.websocket.WebSocketHandler;
import com.hypersocket.netty.websocket.WebSocketListener;

public class HttpClient {

	static Logger log = LoggerFactory.getLogger(HttpClient.class);

	String host;
	int port;
	boolean secure;
	boolean disconnected = false;
	int minimumConnections = 0;
	Map<String,CookieHolder> cookies = new HashMap<String,CookieHolder>();
	Map<String, String> staticHeaders = new HashMap<String, String>();

	HttpConnectionPool connectionPool;
	HttpHandler httpHandler;
	WebSocketHandler websocketHandler;

	public HttpClient(String host, int port, boolean secure) {
		this.host = host;
		this.port = port;
		this.secure = secure;
	}

	public void connect(ExecutorService bossExecutor, ExecutorService workerExecutor) throws IOException {
		this.connectionPool = new HttpConnectionPool(this,bossExecutor, workerExecutor,
				httpHandler = new HttpHandler());
	}

	public HttpHandlerResponse sendRequest(HttpRequest request, long timeout)
			throws IOException {
		HttpConnection con = connectionPool.checkout();

		debugRequest(processRequest(request));

		try {
			HttpHandlerResponse res = con.sendRequest(request, timeout);
			if(res==null) {
				throw new IOException("Failed to complete request, no response received!");
			}
			return processResponse(res);
		} finally {
			connectionPool.checkin(con);
		}
	}

	private void debugRequest(HttpRequest request) {

		if (log.isDebugEnabled()) {
			log.debug(request.getMethod() + " " + request.getUri());

			for (Entry<String, String> header : request.getHeaders()) {
				log.debug(header.getKey() + ": " + header.getValue());
			}

			log.debug(request.getContent().toString(Charset.forName("UTF-8")));
		}
	}

	private void debugResponse(HttpHandlerResponse response) {

		if (log.isDebugEnabled()) {

			log.debug(response.getStatusCode() + " " + response.getStatusText()
					+ " chunked=" + response.isChunked());

			for (Entry<String, String> header : response.getHeaders()) {
				log.debug(header.getKey() + ": " + header.getValue());
			}

			String content = response.getContent().toString(
					Charset.forName("UTF-8"));

			log.debug(content);
		}
	}

	private HttpRequest processRequest(HttpRequest request) {
		request.setHeader("Host", getHost() + ((isSecure() && getPort()==443) || (!isSecure() && getPort()==80) ? "" : ":" + getPort()));
		request.setHeader("Connection", "keep-alive");
		request.setHeader("User-Agent", "Hypersocket-Client; " + HypersocketVersion.getVersion() + ";" 
							+ System.getProperty("os.name") + ";" + System.getProperty("os.version"));
		if(!StringUtils.isBlank(getCookies())) {
			request.setHeader("Cookie", getCookies());
		}
		for (String name : staticHeaders.keySet()) {
			request.setHeader(name, staticHeaders.get(name));
		}

		return request;
	}

	private HttpHandlerResponse processResponse(HttpHandlerResponse response) {

		for (Entry<String, String> header : response.getHeaders()) {
			if (header.getKey().equalsIgnoreCase("Set-Cookie")) {
				Set<Cookie> cs = new CookieDecoder().decode(header.getValue());
				for (Cookie c : cs) {
					if(cookies.containsKey(c.getName())) {
						cookies.remove(c.getName());
					}
					cookies.put(c.getName(), new CookieHolder(c));
				}
			}
		}

		debugResponse(response);
		return response;
	}

	public WebSocket createWebsocket(URI uri, WebSocketListener callback)
			throws IOException {
		return connectionPool.createWebsocketConnection(uri, callback);
	}

	String getCookies() {

		StringBuffer buf = new StringBuffer();

		Set<CookieHolder> toRemove = new HashSet<CookieHolder>();
		for (CookieHolder c : cookies.values()) {
			if (c.hasExpired()) {
				if(log.isDebugEnabled()) {
					log.debug("Cookie " + c.getCookie().getName() + " has expired");
				}
				toRemove.add(c);
			} else {
				buf.append(c.getCookie().getName());
				buf.append('=');
				buf.append(c.getCookie().getValue());
				buf.append(';');
			}
		}

		for(CookieHolder c : toRemove) {
			cookies.remove(c.getCookie().getName());
		}

		return buf.toString();
	}

	public void disconnect() {
		disconnected = true;
		if(connectionPool != null) {
			connectionPool.disconnectAll();
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isSecure() {
		return secure;
	}

	public int getMinimumConnections() {
		return minimumConnections;
	}

	public boolean isDisconnected() {
		return disconnected;
	}

	public void addStaticHeader(String name, String value) {
		staticHeaders.put(name, value);
	}

	public void removeStaticHeader(String name) {
		staticHeaders.remove(name);
	}

}
