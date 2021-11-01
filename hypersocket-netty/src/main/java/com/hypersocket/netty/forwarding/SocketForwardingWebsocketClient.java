/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.forwarding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;

public class SocketForwardingWebsocketClient implements NettyWebsocketClient {
	
	public static final AttributeKey<SocketForwardingWebsocketClient> ATTACHMENT = AttributeKey.newInstance(SocketForwardingWebsocketClient.class.getSimpleName());

	static Logger log = LoggerFactory.getLogger(SocketForwardingWebsocketClient.class);
	
	private Channel socketChannel;
	private Channel websocketChannel;
	private long totalBytesOut;
	private long totalBytesIn;
	
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
		websocketChannel.attr(ATTACHMENT).set(this);;
		socketChannel.attr(ATTACHMENT).set(this);
	}
	
	@Override
	public void open() {
		socketChannel.config().setAutoRead(true);
	}
	
	@Override
	public void close() {
		if(socketChannel.isOpen()) {
			socketChannel.close();
		}
		
		if(websocketChannel.isOpen()) {
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
			if (socketChannel.isOpen()) {
				if (log.isDebugEnabled()) {
					log.debug("Forwarding frame to socket "
							+ socketChannel.remoteAddress());
				}

				reportInputBytes(msg.content()
						.readableBytes());
				socketChannel.write(msg.content());
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Forwarding socket is no longer connected for "
							+ socketChannel.remoteAddress());
				}
				close();
			}
		}

		
	}

}
