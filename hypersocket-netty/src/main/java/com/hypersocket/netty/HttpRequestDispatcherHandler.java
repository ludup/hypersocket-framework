/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.json.RestApi;
import com.hypersocket.netty.forwarding.NettyWebsocketClient;
import com.hypersocket.server.handlers.HttpResponseProcessor;
import com.hypersocket.servlet.request.Request;
import com.hypersocket.utils.ITokenResolver;
import com.hypersocket.utils.TokenAdapter;
import com.hypersocket.utils.TokenReplacementReader;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.stream.ChunkedStream;

public class HttpRequestDispatcherHandler extends ChannelInboundHandlerAdapter
		implements HttpResponseProcessor {

	public static final String CONTENT_INPUTSTREAM = "ContentInputStream";
	public static final String BROWSER_URI = "browserRequestUri";
	
	static Logger log = LoggerFactory
			.getLogger(HttpRequestDispatcherHandler.class);

	NettyServer server;
	
	public HttpRequestDispatcherHandler(NettyServer server)
			throws ServletException {
		this.server = server;
	}

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.info("Channel read: " + msg.getClass().getName());
		if (msg instanceof WebSocketFrame) {
			processWebsocketFrame((WebSocketFrame) msg, ctx.channel());
		}
	    else if (msg instanceof HttpRequest) {
	    	HttpRequest chunk = (HttpRequest) msg;
			dispatchRequest(ctx, chunk);
	    }
	    else if (msg instanceof HttpContent) {
	      HttpContent chunk = (HttpContent) msg;
		  if(log.isDebugEnabled()) {
			  log.debug(String.format("Received HTTP chunk of %d bytes", chunk.content().readableBytes())); 
		  }
		  if(msg instanceof LastHttpContent) {
		    	// End of content for chunked encoding
		    	/* TODO anything? */
//				dispatchRequest(ctx, msg);
		  }
		  else {
			  HttpRequestServletWrapper servletRequest = ctx.channel().attr(HttpRequestServletWrapper.REQUEST).get();
			  HttpRequestChunkStream in = (HttpRequestChunkStream)servletRequest.getInputStream();
			  in.setCurrentChunk(chunk);
		  }
	  	}
	    else {
			if (log.isErrorEnabled()) {
				log.error("Received invalid MessageEvent " + msg.toString());
			}
		}

	}
	
	private void dispatchRequest(ChannelHandlerContext ctx,
			HttpRequest nettyRequest) {
		if(HttpUtil.isTransferEncodingChunked(nettyRequest)) {
			server.getExecutor().submit(new RequestWorker(ctx, nettyRequest, server, this));
		} else {
			new RequestWorker(ctx, nettyRequest, server, this).run();
		}
	}
	
	static ThreadLocal<HttpServletRequest> requestLocal = new ThreadLocal<>();
	
	public static HttpServletRequest getRequest() {
		return requestLocal.get();
	}

	@Override
	public void send404(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
		servletResponse.sendError(HttpStatus.SC_NOT_FOUND);
		URL url = getClass().getResource("/404.html");
		if(servletRequest.getAttribute("404.html")!=null) {
			url = (URL) servletRequest.getAttribute("404.html");
		}
		servletResponse.setContentType("text/html");
		servletRequest.setAttribute(CONTENT_INPUTSTREAM, url.openStream());
	}
	
	@Override
	public void send401(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
		servletResponse.sendError(HttpStatus.SC_UNAUTHORIZED);
	}
	
	@Override
	public void send500(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse) throws IOException {
		servletResponse.sendError(HttpStatus.SC_NOT_FOUND);
		URL url = getClass().getResource("/500.html");
		if(servletRequest.getAttribute("500.html")!=null) {
			url = (URL) servletRequest.getAttribute("500.html");
		}
		ITokenResolver resolver = new TokenAdapter() {
			
			@Override
			public String resolveToken(String tokenName) {
				return (String)servletRequest.getAttribute(tokenName);
			}
		};
		
		TokenReplacementReader reader = new TokenReplacementReader(
				new InputStreamReader(url.openStream()), Arrays.asList(resolver));
		servletRequest.setAttribute(CONTENT_INPUTSTREAM, 
				new ReaderInputStream(reader, Charset.forName("UTF-8")));
	}
	
	private void processWebsocketFrame(WebSocketFrame msg, Channel channel) {

		if (log.isDebugEnabled()) {
			log.debug("Received websocket frame from "
					+ channel.remoteAddress());
		}
		// Check for closing frame
		if (msg instanceof CloseWebSocketFrame) {
			// handshaker.close(channel, (CloseWebSocketFrame) msg);
			return;
		} else if (!(msg instanceof BinaryWebSocketFrame)) {
			// Close
		}

		NettyWebsocketClient nettyClient = channel.attr(NettyWebsocketClient.WEBSOCKET_CLIENT).get();
		nettyClient.frameReceived(msg);
		
	}

	@Override
	public void sendResponse(HttpServletRequest request,
			HttpServletResponse response, boolean chunked) {
		sendResponse((HttpRequestServletWrapper)request,
				(HttpResponseServletWrapper) response, chunked);
	}

	public void sendResponse(final HttpRequestServletWrapper servletRequest,
			final HttpResponseServletWrapper servletResponse, boolean chunked) {

		try {
			HttpResponse nettyResponse = ((HttpResponseServletWrapper)servletResponse).getNettyResponse();
			if(!StringUtils.equals(RestApi.API_REST, (CharSequence) servletRequest.getAttribute(RestApi.API_REST))) {
				switch (nettyResponse.status().code()) {
					case HttpStatus.SC_NOT_FOUND: {
						send404(servletRequest, servletResponse);
						break;
					}
					case HttpStatus.SC_INTERNAL_SERVER_ERROR: {
						send500(servletRequest, servletResponse);
						break;
					}
					default: {
						
					}
				}
			}
			
			addStandardHeaders(servletRequest, servletResponse);

			Object contentObj = processContent(
					servletRequest,
					servletResponse,
					servletResponse.getRequest().headers().get(
							HttpHeaders.ACCEPT_ENCODING));

			if (nettyResponse != null && (log.isDebugEnabled() || isLoggableStatus(nettyResponse.status()))) {
				synchronized (log) {
					log.info("Begin Response >>>>>> " + servletRequest.getRequestURI());
					log.info(nettyResponse.status().toString());
					for (String header : nettyResponse
							.headers().names()) {
						for (String value : nettyResponse
								.headers().getAll(header)) {
							log.debug(header + ": " + value);
						}
					}
				}
			}

			if (contentObj != null) {
				try {
					servletResponse.getChannel().write(
							nettyResponse);
					
					if(contentObj instanceof ByteBuf) {
						ByteBuf content = (ByteBuf)contentObj;
						if (log.isDebugEnabled()) {
							log.debug("Sending " + content.capacity() + " " +
									( servletResponse.isChunked() ? "chunked" : "non-chunked") + " bytes of buffered HTTP content");
						}
						if(servletResponse.isChunked()) {
							servletResponse
									.getChannel()
									.writeAndFlush(content)
									.addListener(
											new CheckCloseStateListener(servletResponse));
						}
						else {
							servletResponse.getChannel().writeAndFlush(content);
						}
					}
					else {
						InputStream content = (InputStream)contentObj;
						if (log.isDebugEnabled()) {
							try {
								log.debug("Sending " + content.available() + " " +
										( servletResponse.isChunked() ? "chunked" : "non-chunked") + " bytes of streamed HTTP content");
							} catch (IOException e) {
							}
						}
						if(servletResponse.isChunked()) {
							servletResponse
									.getChannel()
									.writeAndFlush(new ChunkedStream(content))
									.addListener(
											new CheckCloseStateListener(servletResponse));
						}
						else {
							servletResponse.getChannel().writeAndFlush(content);
						}
					}
				} catch (Exception e) {
					log.error("Unexpected exception writing content stream", e);
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Sending " + ( servletResponse.isChunked() ? "chunked" : "non-chunked") + " non-content response");
				}
				if(servletResponse.isChunked()) {
					servletResponse
							.getChannel()
							.writeAndFlush(nettyResponse)
							.addListener(
									new CheckCloseStateListener(servletResponse));
				}
				else {
					servletResponse.getChannel().writeAndFlush(nettyResponse);
				}
			}
			
		} catch(IOException ex) {
			log.error("IO Error sending HTTP response", ex);
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("End Response >>>>>>");
			}
			servletResponse.stamp();
			server.getRequestLog().log(servletRequest, servletResponse);

			Request.remove();
		}

	}
	
	private boolean isLoggableStatus(HttpResponseStatus status) {
		switch(status.code()) {
		case 400:
		case 405:
		case 406:
		case 415:	
			return true;
		default:
			return false;
		}
	}

	class CheckCloseStateListener implements ChannelFutureListener {

		HttpResponseServletWrapper servletResponse;

		CheckCloseStateListener(HttpResponseServletWrapper servletResponse) {
			this.servletResponse = servletResponse;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (servletResponse.isCloseOnComplete() && future.isDone()) {
				if (log.isDebugEnabled()) {
					log.debug("Closing HTTP connection remoteAddress="
							+ future.channel().remoteAddress()
							+ " localAddress="
							+ future.channel().localAddress());
				}
				servletResponse.getChannel().close();
			}
		}
	}

	private Object processContent(
			HttpServletRequest servletRequest,
			HttpResponseServletWrapper servletResponse, String acceptEncodings) {

		InputStream writer = (InputStream) servletRequest
				.getAttribute(CONTENT_INPUTSTREAM);

		if (writer != null) {
			if (log.isDebugEnabled()) {
				log.debug("Response for " + servletRequest.getRequestURI()
						+ " will be chunked");
			}
			servletResponse.setChunked(true);
			servletResponse.removeHeader(HttpHeaders.CONTENT_LENGTH);
			servletResponse.setHeader(HttpHeaders.TRANSFER_ENCODING, "chunked");
			return writer;
		}

		if (servletResponse.getContent().readableBytes() > 0) {

			ByteBuf buffer = servletResponse.getContent();
			boolean doGzip = false;

			if (servletResponse.getHeader("Content-Encoding") == null) {
				if (acceptEncodings != null) {
					doGzip = acceptEncodings.indexOf("gzip") > -1;
				}

				if (doGzip) {
					try {
						ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
						GZIPOutputStream gzip = new GZIPOutputStream(gzipped);
						gzip.write(buffer.array(), 0, buffer.readableBytes());
						gzip.finish();
						buffer = Unpooled.wrappedBuffer(gzipped
								.toByteArray());
						servletResponse.setHeader("Content-Encoding", "gzip");
					} catch (IOException e) {
						log.error("Failed to gzip response", e);
					}
				}
			}

			servletResponse.setHeader("Content-Length",
					String.valueOf(buffer.readableBytes()));
			return buffer;
		} else {
			servletResponse.setHeader("Content-Length", "0");
		}

		return null;
	}

	private void addStandardHeaders(HttpServletRequest request, HttpResponseServletWrapper servletResponse) {

		servletResponse.setHeader("Server", server.getApplicationName());

		String connection = servletResponse.getRequest().headers().get(
				HttpHeaders.CONNECTION);
		if (connection!=null && connection.equalsIgnoreCase("close")) {
			servletResponse.setHeader("Connection", "close");
			servletResponse.setCloseOnComplete(true);
		}

		servletResponse.setHeader("Date",
				DateUtils.formatDate(new Date(System.currentTimeMillis())));
		
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel ch = (Channel) ctx.channel();

		if (log.isDebugEnabled()) {
			log.debug("Channel disconnected remoteAddress="
					+ ch.remoteAddress() + " localAddress="
					+ ch.localAddress());
		}

		if (ch.isOpen()) {
			ch.close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {

		if (log.isDebugEnabled() && !(cause instanceof ClosedChannelException)) {
			if(cause instanceof IOException) {
				log.debug(String.format("Exception in HTTP request worker: %s", cause.getMessage()));
			} else {
				log.error("Exception in http request remoteAddress="
						+ ctx.channel().remoteAddress() + " localAddress="
						+ ctx.channel().localAddress(), cause);
			}
		}

		ctx.channel().close();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx)
			throws Exception {
		Channel ch = (Channel) ctx.channel();

		if (log.isDebugEnabled()) {
			log.debug("Channel open remoteAddress=" + ch.remoteAddress()
					+ " localAddress=" + ch.localAddress());
		}
	}

	static String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.headers().get(HttpHeaders.HOST) + req.uri();
	}

}
