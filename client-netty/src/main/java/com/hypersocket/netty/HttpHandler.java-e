/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.IOException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHandler extends SimpleChannelUpstreamHandler {

	static Logger log = LoggerFactory.getLogger(HttpHandler.class);

	public HttpHandler() {
	}

	public HttpHandlerResponse sendRequest(Channel channel,
			HttpRequest request, long timeout) throws IOException {

		HttpRequestState state = new HttpRequestState();
		state.future = new DefaultChannelFuture(channel, true);
		channel.setAttachment(state);
		channel.write(request);
		while(!state.future.awaitUninterruptibly(timeout)) {
			if((System.currentTimeMillis() - state.lastActivity) > timeout) {
				if (log.isInfoEnabled()) {
					log.info("Timeout processing http request channelId="
							+ channel.getId());
				}
				channel.setAttachment(null);
				if(state.ex!=null) {
					throw new IOException("Channel exception", state.ex);
				}
				throw new IOException("Timeout processing HTTP request "
						+ request.getMethod() + " " + request.getUri());
			}
		}
		
		channel.setAttachment(null);
		return state.response;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		HttpRequestState state = (HttpRequestState) ctx.getChannel()
				.getAttachment();

		if (state == null) {
			if (log.isWarnEnabled()) {
				log.warn("Received http message but channel does not have a request state channelId="
						+ ctx.getChannel().getId());
			}
			return;
		}
		
		state.lastActivity = System.currentTimeMillis();
		
		if (!state.readingChunks) {
			state.response = new HttpHandlerResponse(
					(HttpResponse) e.getMessage());
			if (!state.response.isChunked()) {
				if (log.isDebugEnabled()) {
					log.debug("Non-chunked message successfully received");
				}
				state.future.setSuccess();
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Chunked message received");
				}
				state.readingChunks = true;
			}
		} else {
			HttpChunk chunk = (HttpChunk) e.getMessage();

			state.response.addChunk(chunk);

			if (log.isDebugEnabled()) {
				log.debug("Read chunk on message receipt (last = " + chunk.isLast());
			}
			
			if (chunk.isLast()) {
				state.readingChunks = false;
				state.future.setSuccess();
			}

		}
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Http connection established channelId="
					+ ctx.getChannel().getId());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {

		if (ctx.getChannel().isConnected()) {
			ctx.getChannel().close();
			if (log.isDebugEnabled()) {
				log.debug("Exception in http channel channelId="
						+ ctx.getChannel().getId(), e.getCause());
			}
		}
		
		HttpRequestState state = (HttpRequestState) ctx.getChannel()
				.getAttachment();

		if (state != null) {
			state.lastActivity = System.currentTimeMillis();
			state.ex = e.getCause();
			state.closed = true;
		}
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Http connection disconnected channelId="
					+ ctx.getChannel().getId());
		}
		HttpRequestState state = (HttpRequestState) ctx.getChannel()
				.getAttachment();
		if(state!=null) {
			state.lastActivity = System.currentTimeMillis();
			state.closed = true;
			state.future.cancel();
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Http connection closed channelId="
					+ ctx.getChannel().getId());
		}
		HttpRequestState state = (HttpRequestState) ctx.getChannel()
				.getAttachment();
		if(state!=null) {
			state.lastActivity = System.currentTimeMillis();
			state.closed = true;
			state.future.cancel();
		}
	}

	class HttpRequestState {

		boolean readingChunks = false;
		ChannelFuture future;
		HttpHandlerResponse response;
		Throwable ex;
		boolean closed;
		long lastActivity = System.currentTimeMillis();
	}
}
