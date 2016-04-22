/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.hypersocket.netty.forwarding.SocketForwardingWebsocketClient;
import com.hypersocket.server.websocket.WebsocketClient;
import com.hypersocket.server.websocket.WebsocketClientCallback;

class ClientConnectCallbackImpl implements ChannelFutureListener {

	WebsocketClientCallback callback;
	WebsocketClient client;
	ClientConnectCallbackImpl(WebsocketClientCallback callback) {
		this.callback = callback;
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {

		if (future.isSuccess()) {
			future.getChannel().setReadable(false);
			client = createClient(future.getChannel());
			callback.websocketAccepted(client);
			future.getChannel().getCloseFuture().addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future)
						throws Exception {
					callback.websocketClosed(client);
				}
				
			});
		} else {
			callback.websocketRejected(future.getCause());
		}
	}
	
	protected WebsocketClient createClient(Channel channel) {
		return new SocketForwardingWebsocketClient(channel);
	}

}
