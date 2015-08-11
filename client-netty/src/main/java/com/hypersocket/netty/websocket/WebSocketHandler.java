/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.websocket;

import java.net.InetSocketAddress;
import java.net.URI;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketHandler extends SimpleChannelUpstreamHandler implements
		WebSocket {

	private static Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

	private ClientBootstrap bootstrap;
	private URI url;
	private WebSocketListener callback;
	private Channel channel;
	private Object attachment;
	private WebSocketClientHandshaker handshaker;

	public WebSocketHandler(ClientBootstrap bootstrap, URI url,
			WebSocketListener callback, WebSocketClientHandshaker handshaker) {
		this.bootstrap = bootstrap;
		this.url = url;
		this.callback = callback;
		this.handshaker = handshaker;
	}
	
	public Integer getId() {
		return channel.getId();
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Web socket connected id=" + ctx.getChannel().getId());
		}
		handshaker.handshake(ctx.getChannel());
		channel = e.getChannel();
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Web socket disconnected id=" + ctx.getChannel().getId());
		}

		callback.onDisconnect(this);
	}

	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Web socket received data id=" + ctx.getChannel().getId());
		}
		if (!handshaker.isHandshakeComplete()) {

			if (log.isDebugEnabled())
				log.debug("Passing data to web socket handshake " + handshaker + " id=" + ctx.getChannel().getId());
			handshaker.finishHandshake(ctx.getChannel(),
					(HttpResponse) e.getMessage());

			if (handshaker.isHandshakeComplete()) {
				if (log.isDebugEnabled())
					log.debug("Web socket handshake complete id=" + ctx.getChannel().getId());
				Channel ch = (Channel) attachment;
				if(ch!=null) {
					ch.setReadable(true);
				}
				callback.onConnect(this);
			} else {
				callback.onError(new Exception("Handshake did not complete id=" + ctx.getChannel().getId()));
			}
			return;
		}

		if (e.getMessage() instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) e.getMessage();
			throw new WebSocketException("Unexpected HttpResponse (status="
					+ response.getStatus() + ", content="
					+ response.getContent().toString(CharsetUtil.UTF_8) + " id="  + ctx.getChannel().getId() + ")");
		}

		WebSocketFrame frame = (WebSocketFrame) e.getMessage();
		callback.onMessage(this, frame);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		if (log.isErrorEnabled()) {
			log.error("Web socket error id="  + ctx.getChannel().getId(), e.getCause());
		}

		final Throwable t = e.getCause();
		callback.onError(t);
		e.getChannel().close();
	}

	public ChannelFuture connect() {
		return bootstrap.connect(new InetSocketAddress(url.getHost(), url
				.getPort()));
	}

	public ChannelFuture disconnect() {
		return channel.close();
	}

	public ChannelFuture send(WebSocketFrame frame) {
		return channel.write(frame);
	}

	public URI getUrl() {
		return url;
	}

	public void setUrl(URI url) {
		this.url = url;
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}
}
