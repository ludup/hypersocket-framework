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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class SocketForwardingWebsocketClientHandler extends ChannelInboundHandlerAdapter {

	Logger log = LoggerFactory.getLogger(SocketForwardingWebsocketClientHandler.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx)
			throws Exception {

		Channel ch = ctx.channel();

		if (log.isDebugEnabled()) {
			log.debug("Connected to " + ch.remoteAddress());
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

		Channel ch = ctx.channel();

		if (log.isDebugEnabled()) {
			log.debug("Disconnected from " + ch.remoteAddress());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {

		Channel ch = ctx.channel();
		SocketForwardingWebsocketClient socketClient = ch.attr(SocketForwardingWebsocketClient.ATTACHMENT).get();
		
		if (log.isDebugEnabled()) {
			log.debug("Received exception from " + ch.remoteAddress(),
					cause);
		}
		if(socketClient!=null) {
			socketClient.close();
		}
		
	}

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		Channel ch = ctx.channel();
		SocketForwardingWebsocketClient socketClient = ch.attr(SocketForwardingWebsocketClient.ATTACHMENT).get();

		if (log.isDebugEnabled()) {
			log.debug("Received data from " + ch.remoteAddress());
		}
		
		if (!socketClient.getWebsocketChannel().isOpen()) {

			if (log.isDebugEnabled()) {
				log.debug("Web socket is no longer connected, disconnecting forwarded socket "
						+ ch.remoteAddress());
			}
			ch.close();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Forwarding data to web socket "
						+ socketClient.getWebsocketChannel().remoteAddress());
			}

			ByteBuf buf = (ByteBuf) msg;
			
			socketClient.reportOutputBytes(buf.readableBytes());
			
			socketClient.getWebsocketChannel().write(new BinaryWebSocketFrame(buf));
		}
	}

}
