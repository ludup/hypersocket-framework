/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.netty.websocket.WebSocket;
import com.hypersocket.netty.websocket.WebSocketListener;

public class LocalForwardingHandler extends SimpleChannelUpstreamHandler
		implements WebSocketListener {

	Logger log = LoggerFactory.getLogger(LocalForwardingHandler.class);

	NettyClientTransport nettyClient;
	Channel channel;
	WebSocket webSocketClient;

	public LocalForwardingHandler(NettyClientTransport nettyClient) {
		this.nettyClient = nettyClient;
	}

	public void childChannelOpen(ChannelHandlerContext ctx,
			ChildChannelStateEvent e) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Received connection on "
					+ e.getChannel().getLocalAddress() + " id="
					+ ctx.getChannel().getId());
		}
	}

	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Accepting connection on "
					+ e.getChannel().getLocalAddress() + " id="
					+ ctx.getChannel().getId());
		}

		channel = e.getChannel();
		channel.setReadable(false);

		webSocketClient = nettyClient.createTunnel(e.getChannel(), this);
		webSocketClient.setAttachment(channel);
		webSocketClient.connect();
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {

		if (log.isDebugEnabled()) {
			log.debug(channel.getRemoteAddress() + ": Socket connected id="
					+ ctx.getChannel().getId());
		}
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {

		if (webSocketClient.isOpen()) {

			if (log.isDebugEnabled()) {
				log.debug(channel.getRemoteAddress() + ": Disconnected id="
						+ ctx.getChannel().getId());
			}

			webSocketClient.disconnect();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Received exception id=" + ctx.getChannel().getId(),
					e.getCause());
		}
	}

	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Received socket data id=" + ctx.getChannel().getId());
		}

		ChannelBuffer buf = (ChannelBuffer) e.getMessage();

		if (log.isTraceEnabled()) {
			log.trace("IN: " + buf.toString(Charset.forName("UTF-8")));
		}

		webSocketClient.send(new BinaryWebSocketFrame(buf));

	}

	public void onConnect(WebSocket client) {

	}

	public void onDisconnect(WebSocket client) {

		if (log.isDebugEnabled()) {
			log.debug("Web socket disconnected id=" + client.getId());
		}

		channel.close();

	}

	public void onMessage(WebSocket client, WebSocketFrame frame) {

		if (log.isDebugEnabled()) {
			log.debug("Received web socket data " + client.getId());
		}

		if (log.isTraceEnabled()) {
			log.trace("OUT:"
					+ frame.getBinaryData().toString(Charset.forName("UTF-8")));
		}

		channel.write(frame.getBinaryData());
	}

	public void onError(Throwable t) {

		if (log.isErrorEnabled()) {
			log.error("Received web socket error", t);
		}

	}

}
