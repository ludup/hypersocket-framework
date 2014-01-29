/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.forwarding;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketForwardingWebsocketClientHandler extends SimpleChannelUpstreamHandler {

	Logger log = LoggerFactory.getLogger(SocketForwardingWebsocketClientHandler.class);

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {

		Channel ch = e.getChannel();

		if (log.isDebugEnabled()) {
			log.debug("Connected to " + ch.getRemoteAddress());
		}
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {

		Channel ch = e.getChannel();

		if (log.isDebugEnabled()) {
			log.debug("Disconnected from " + ch.getRemoteAddress());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {

		Channel ch = e.getChannel();
		SocketForwardingWebsocketClient socketClient = (SocketForwardingWebsocketClient) ch.getAttachment();
		
		if (log.isDebugEnabled()) {
			log.debug("Received exception from " + ch.getRemoteAddress(),
					e.getCause());
		}
		
		socketClient.close();
	}

	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		Channel ch = e.getChannel();
		SocketForwardingWebsocketClient socketClient = (SocketForwardingWebsocketClient) ch.getAttachment();

		if (log.isDebugEnabled()) {
			log.debug("Received data from " + ch.getRemoteAddress());
		}
		
		if (!socketClient.getWebsocketChannel().isConnected()) {

			if (log.isDebugEnabled()) {
				log.debug("Web socket is no longer connected, disconnecting forwarded socket "
						+ ch.getRemoteAddress());
			}
			ch.close();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Forwarding data to web socket "
						+ socketClient.getWebsocketChannel().getRemoteAddress());
			}

			ChannelBuffer buf = (ChannelBuffer)  e.getMessage();
			
			socketClient.reportOutputBytes(buf.readableBytes());
			
			socketClient.getWebsocketChannel().write(new BinaryWebSocketFrame(buf));
		}
	}

}
