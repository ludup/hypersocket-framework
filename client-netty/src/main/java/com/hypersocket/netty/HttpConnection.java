/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.IOException;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;


public class HttpConnection {

	Channel channel;
	HttpHandler httpHandler;
	HttpConnectionPool pool;
	
	NioClientSocketChannelFactory clientSocketFactory = null;
	ClientBootstrap httpBootstrap;
	
	HttpConnection(Channel channel, HttpHandler httpHandler, HttpConnectionPool pool) {
		this.channel = channel;
		this.httpHandler = httpHandler;
		this.pool = pool;
	}
	
	public HttpHandlerResponse sendRequest(HttpRequest request, long timeout) throws IOException {
		return httpHandler.sendRequest(channel, request, timeout);
	}

	public boolean isConnected() {
		return channel.isConnected();
	}
	
	public void disconnect() {
		channel.close();
	}
	
	public void cleanup() {
		if(channel.isConnected()) {
			disconnect();
		}
	}

	public int getId() {
		return channel.getId();
	}
	
	
}
