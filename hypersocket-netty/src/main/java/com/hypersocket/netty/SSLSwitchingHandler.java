/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.server.interfaces.http.HTTPProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class SSLSwitchingHandler extends ByteToMessageDecoder {

	static Logger log = LoggerFactory.getLogger(SSLSwitchingHandler.class);

	private NettyServer server;
	
	public SSLSwitchingHandler(NettyServer server) {
		this.server = server;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		super.exceptionCaught(ctx, cause);
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {

		// Will use the first two bytes to detect a protocol.
		if (buffer.readableBytes() < 2) {
			return;
		}

		HTTPInterfaceResource interfaceResource = ctx.channel().parent().attr(NettyServer.INTERFACE_RESOURCE).get();
		if (interfaceResource.getProtocol()==HTTPProtocol.HTTPS) {
			enableSsl(interfaceResource, ctx);
		} else {
			enablePlainHttp(ctx);
		} 

		// Forward the current read buffer as is to the new handlers.
		out.add(buffer.readBytes(buffer.readableBytes()));
	}



	private void enableSsl(HTTPInterfaceResource resource, ChannelHandlerContext ctx) throws IOException {

		if (!(ctx.channel().localAddress() instanceof InetSocketAddress)) {
			throw new IllegalStateException(
					"Cannot perform SSL over SocketAddress of type "
							+ ctx.channel().localAddress().getClass()
									.getName());
		}

		ChannelPipeline p = ctx.pipeline();
		
		p.addLast(
				"ssl",
				new SslHandler(server
						.createSSLEngine(resource,
								(InetSocketAddress) ctx.channel()
								.localAddress(), (InetSocketAddress) ctx
								.channel().remoteAddress())));
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
		p.addLast("encoder", new HttpResponseEncoder());
		p.addLast("deflater", new HttpContentCompressor());
		p.addLast("chunkedWriter", new ChunkedWriteHandler());
		try {
			p.addLast("http", new HttpRequestDispatcherHandler(server));
		} catch (ServletException e) {
			log.error("Servlet error", e);
			ctx.channel().close();
		}
		p.remove(this);
	}

	private void enablePlainHttp(ChannelHandlerContext ctx) {
		ChannelPipeline p = ctx.pipeline();
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
		p.addLast("encoder", new HttpResponseEncoder());
		p.addLast("deflater", new HttpContentCompressor());
		p.addLast("chunkedWriter", new ChunkedWriteHandler());
		try {
			p.addLast("http", new HttpRequestDispatcherHandler(server));
		} catch (ServletException e) {
			log.error("Servlet error", e);
			ctx.channel().close();
		}
		p.remove(this);
	}
}
