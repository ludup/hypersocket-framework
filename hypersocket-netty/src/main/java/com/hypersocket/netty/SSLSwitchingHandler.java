/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.servlet.ServletException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.server.interfaces.http.HTTPProtocol;

public class SSLSwitchingHandler extends FrameDecoder {


	static Logger log = LoggerFactory.getLogger(SSLSwitchingHandler.class);

	NettyServer server;
	
	public SSLSwitchingHandler(NettyServer server) {
		this.server = server;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		super.exceptionCaught(ctx, e);
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {

		// Will use the first two bytes to detect a protocol.
		if (buffer.readableBytes() < 2) {
			return null;
		}

		HTTPInterfaceResource interfaceResource = (HTTPInterfaceResource) channel.getParent().getAttachment();
		if (interfaceResource.getProtocol()==HTTPProtocol.HTTPS) {
			enableSsl(interfaceResource, ctx);
		} else {
			enablePlainHttp(ctx);
		} 

		// Forward the current read buffer as is to the new handlers.
		return buffer.readBytes(buffer.readableBytes());
	}



	private void enableSsl(HTTPInterfaceResource resource, ChannelHandlerContext ctx) throws IOException {

		if (!(ctx.getChannel().getLocalAddress() instanceof InetSocketAddress)) {
			throw new IllegalStateException(
					"Cannot perform SSL over SocketAddress of type "
							+ ctx.getChannel().getLocalAddress().getClass()
									.getName());
		}

		ChannelPipeline p = ctx.getPipeline();

		p.addLast(
				"ssl",
				new SslHandler(server
						.createSSLEngine(resource,
								(InetSocketAddress) ctx.getChannel()
								.getLocalAddress(), (InetSocketAddress) ctx
								.getChannel().getRemoteAddress())));
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("aggregator", new HttpChunkAggregator(Integer.MAX_VALUE));
		p.addLast("encoder", new HttpResponseEncoder());
		p.addLast("chunkedWriter", new ChunkedWriteHandler());
		try {
			p.addLast("http", new HttpRequestDispatcherHandler(server));
		} catch (ServletException e) {
			log.error("Servlet error", e);
			ctx.getChannel().close();
		}
		p.remove(this);
	}

	private void enablePlainHttp(ChannelHandlerContext ctx) {
		ChannelPipeline p = ctx.getPipeline();
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("aggregator", new HttpChunkAggregator(Integer.MAX_VALUE));
		p.addLast("encoder", new HttpResponseEncoder());
		p.addLast("chunkedWriter", new ChunkedWriteHandler());
		try {
			p.addLast("http", new HttpRequestDispatcherHandler(server));
		} catch (ServletException e) {
			log.error("Servlet error", e);
			ctx.getChannel().close();
		}
		p.remove(this);
	}
}
