/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import org.apache.http.HttpStatus;

import com.hypersocket.netty.forwarding.SocketForwardingWebsocketClient;
import com.hypersocket.server.websocket.WebsocketClient;
import com.hypersocket.server.websocket.WebsocketClientCallback;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

class ClientConnectCallbackImpl implements ChannelFutureListener {

	private WebsocketClientCallback callback;
	private WebsocketClient client;
	
	ClientConnectCallbackImpl(WebsocketClientCallback callback) {
		this.callback = callback;
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {

		if (future.isSuccess()) {
			future.channel().config().setAutoRead(false);
			client = createClient(future.channel());
			callback.websocketAccepted(client);
			future.channel().closeFuture().addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future)
						throws Exception {
					callback.websocketClosed(client);
				}
				
			});
		} else {
			callback.websocketRejected(future.cause(), HttpStatus.SC_NOT_FOUND);
		}
	}
	
	protected WebsocketClient createClient(Channel channel) {
		return new SocketForwardingWebsocketClient(channel);
	}

}
