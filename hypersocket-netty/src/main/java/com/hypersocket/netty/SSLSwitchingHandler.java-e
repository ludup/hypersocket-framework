/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

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

public class SSLSwitchingHandler extends FrameDecoder {

//	static OrderedMemoryAwareThreadPoolExecutor executor = new OrderedMemoryAwareThreadPoolExecutor(
//			200, 1048576, 1073741824, 100, TimeUnit.MILLISECONDS,
//			Executors.defaultThreadFactory());

	static Logger log = LoggerFactory.getLogger(SSLSwitchingHandler.class);

	NettyServer server;
	int httpPort;
	int httpsPort;

	public SSLSwitchingHandler(NettyServer server, int httpPort, int httpsPort) {
		this.server = server;
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
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

		InetSocketAddress sockAddr = (InetSocketAddress) channel.getLocalAddress();
		if (sockAddr.getPort() == httpsPort) {
			enableSsl(ctx);
		} else {
			enablePlainHttp(ctx);
		} 

		// Forward the current read buffer as is to the new handlers.
		return buffer.readBytes(buffer.readableBytes());
	}

//	private boolean isSsl(int magic1) {
//		switch (magic1) {
//		case 20:
//		case 21:
//		case 22:
//		case 23:
//		case 255:
//			return true;
//		default:
//			return magic1 >= 128;
//		}
//	}

//	private boolean isHttp(int magic1, int magic2) {
//		return magic1 == 'G' && magic2 == 'E' || // GET
//				magic1 == 'P' && magic2 == 'O' || // POST / POLL
//				magic1 == 'P' && magic2 == 'U' || // PUT
//				magic1 == 'H' && magic2 == 'E' || // HEAD
//				magic1 == 'O' && magic2 == 'P' || // OPTIONS
//				magic1 == 'P' && magic2 == 'A' || // PATCH
//				magic1 == 'D' && magic2 == 'E' || // DELETE
//				magic1 == 'T' && magic2 == 'R' || // TRACE
//				magic1 == 'P' && magic2 == 'R' || // PROPFIND / PROPPATCH
//				magic1 == 'M' && magic2 == 'K' || // MKCOL
//				magic1 == 'M' && magic2 == 'O' || // MOVE
//				magic1 == 'C' && magic2 == 'O' || // COPY / CONNECT
//				magic1 == 'L' && magic2 == 'O' || // LOCK
//				magic1 == 'U' && magic2 == 'N' || // UNLOCK / UNSUBSCRIBE
//				magic1 == 'B' && magic2 == 'M' || // BMOVE
//				magic1 == 'B' && magic2 == 'R' || // BRPOPFIND / BPROPATCH
//				magic1 == 'N' && magic2 == 'O' || // NOTIFY
//				magic1 == 'S' && magic2 == 'E' || // SEARCH
//				magic1 == 'S' && magic2 == 'U' || // SUBSCRIIBE
//				magic1 == 'X' && magic2 == '-'; // X-MS-ENUMATTS
//	}

	private void enableSsl(ChannelHandlerContext ctx) {

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
						.createSSLEngine((InetSocketAddress) ctx.getChannel()
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
