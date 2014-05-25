/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.forwarding;

import org.jboss.netty.channel.Channel;

import com.hypersocket.server.websocket.WebsocketClient;

public class SocketForwardingWebsocketClient implements WebsocketClient {
	
	Channel socketChannel;
	Channel websocketChannel;
	long totalBytesOut;
	long totalBytesIn;
	long intervalBytesOut;
	long intervalBytesIn;
	String resourceKey;
	String resourceBundle;
	public SocketForwardingWebsocketClient(Channel socketChannel, String resourceBundle, String resourceKey) {
		this.socketChannel = socketChannel;
		this.resourceKey = resourceKey;
		this.resourceBundle = resourceBundle;
	}
	
	public Channel getSocketChannel() {
		return socketChannel;
	}
	
	public Channel getWebsocketChannel() {
		return websocketChannel;
	}
	
	public void setWebsocketChannel(Channel websocketChannel) {
		this.websocketChannel = websocketChannel;
	}
	
	@Override
	public void open() {
		socketChannel.setReadable(true);
	}
	
	@Override
	public void close() {
		if(socketChannel.isConnected()) {
			socketChannel.close();
		}
		
		if(websocketChannel.isConnected()) {
			websocketChannel.close();
		}
	}

	public void reportOutputBytes(int count) {
		totalBytesOut += count;
	}
	
	public void reportInputBytes(int count) {
		totalBytesIn += count;
	}

	@Override
	public long getTotalBytesIn() {
		return totalBytesIn;
	}

	@Override
	public long getTotalBytesOut() {
		return totalBytesOut;
	}

}
