/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.forwarding;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketForwardingWebsocketClient implements NettyWebsocketClient {
	
	static Logger log = LoggerFactory.getLogger(SocketForwardingWebsocketClient.class);
	
	Channel socketChannel;
	Channel websocketChannel;
	long totalBytesOut;
	long totalBytesIn;
	long intervalBytesOut;
	long intervalBytesIn;
	
	public SocketForwardingWebsocketClient(Channel socketChannel) {
		this.socketChannel = socketChannel;
	}
	
	public Channel getSocketChannel() {
		return socketChannel;
	}
	
	public Channel getWebsocketChannel() {
		return websocketChannel;
	}
	
	public void setWebsocketChannel(Channel websocketChannel) {
		this.websocketChannel = websocketChannel;
		websocketChannel.setAttachment(this);
		socketChannel.setAttachment(this);
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

	@Override
	public void frameReceived(WebSocketFrame msg) {
	
		if (socketChannel != null) {
			if (socketChannel.isConnected()) {
				if (log.isDebugEnabled()) {
					log.debug("Forwarding frame to socket "
							+ socketChannel.getRemoteAddress());
				}

				reportInputBytes(msg.getBinaryData()
						.readableBytes());
				socketChannel.write(msg.getBinaryData());
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Forwarding socket is no longer connected for "
							+ socketChannel.getRemoteAddress());
				}
				close();
			}
		}

		
	}

}
