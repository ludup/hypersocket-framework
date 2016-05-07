/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
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
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.DateUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.netty.forwarding.NettyWebsocketClient;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.HttpResponseProcessor;
import com.hypersocket.server.handlers.WebsocketHandler;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.server.interfaces.http.HTTPProtocol;
import com.hypersocket.server.websocket.WebsocketClient;
import com.hypersocket.server.websocket.WebsocketClientCallback;
import com.hypersocket.servlet.HypersocketSession;
import com.hypersocket.utils.ITokenResolver;
import com.hypersocket.utils.TokenReplacementReader;

public class HttpRequestDispatcherHandler extends SimpleChannelUpstreamHandler
		implements HttpResponseProcessor {

	public static final String CONTENT_INPUTSTREAM = "ContentInputStream";
	
	private static Logger log = LoggerFactory
			.getLogger(HttpRequestDispatcherHandler.class);

	private NettyServer server;
	private HttpRequest currentRequest = null;
	private boolean wantsMessage = true;
	
	public HttpRequestDispatcherHandler(NettyServer server)
			throws ServletException {
		this.server = server;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object msg = e.getMessage();

		if (wantsMessage && msg instanceof HttpRequest) {

			HttpRequest nettyRequest = (HttpRequest) msg;

			if (!nettyRequest.isChunked()) {
				processRequest(ctx, nettyRequest);
			} else {
				ctx.getChannel().setAttachment(ChannelBuffers.dynamicBuffer());
				currentRequest = nettyRequest;
				wantsMessage = false;
			}

		} else if (msg instanceof WebSocketFrame) {

			processWebsocketFrame((WebSocketFrame) msg, ctx.getChannel());

		} else if (msg instanceof HttpChunk) {
			
			HttpChunk chunk = (HttpChunk) msg;
			ChannelBuffer buffer = (ChannelBuffer) ctx.getChannel()
					.getAttachment();
			buffer.writeBytes(chunk.getContent());

			if (chunk.isLast()) {
				processRequest(ctx, currentRequest);
				currentRequest = null;
				wantsMessage = true;
			}
		} else {
			if (log.isErrorEnabled()) {
				log.error("Received invalid MessageEvent " + msg.toString());
			}
		}

	}

	private void processRequest(ChannelHandlerContext ctx,
			HttpRequest nettyRequest)
			throws IOException {

		HTTPInterfaceResource interfaceResource = (HTTPInterfaceResource) ctx.getChannel().getParent().getAttachment();
		
		HttpResponseServletWrapper nettyResponse = new HttpResponseServletWrapper(
				new DefaultHttpResponse(HttpVersion.HTTP_1_1,
						HttpResponseStatus.OK),
				ctx.getChannel(), nettyRequest);

		HypersocketSession session = server.setupHttpSession(
				nettyRequest.getHeaders("Cookie"), 
				interfaceResource.getProtocol()==HTTPProtocol.HTTPS,
				nettyResponse);

		InetSocketAddress remoteAddress = (InetSocketAddress) ctx.getChannel().getRemoteAddress();
		if(nettyRequest.containsHeader("X-Forwarded-For")) {
			String[] ips = nettyRequest.getHeader("X-Forwarded-For").split(",");
			remoteAddress = new InetSocketAddress(ips[0], remoteAddress.getPort());
		} else if(nettyRequest.containsHeader("Forwarded")) {
			StringTokenizer t = new StringTokenizer(nettyRequest.getHeader("Forwarded"), ";");
			while(t.hasMoreTokens()) {
				String[] pair = t.nextToken().split("=");
				if(pair.length == 2 && pair[0].equalsIgnoreCase("for")) {
					remoteAddress = new InetSocketAddress(pair[1], remoteAddress.getPort());
				}
			}
		}
		
		HttpRequestServletWrapper servletRequest = new HttpRequestServletWrapper(
				nettyRequest, (InetSocketAddress) ctx.getChannel()
						.getLocalAddress(), remoteAddress, 
						interfaceResource.getProtocol()==HTTPProtocol.HTTPS, 
						server.getServletConfig().getServletContext(), session);

		Request.set(servletRequest);
		
		if (log.isDebugEnabled()) {
			synchronized (log) {
				log.debug("Begin Request <<<<<<<<<");
				log.debug(servletRequest.getMethod() + " "
						+ servletRequest.getRequestURI() + " "
						+ servletRequest.getProtocol());
				Enumeration<String> headerNames = servletRequest
						.getHeaderNames();
				while (headerNames.hasMoreElements()) {
					String header = headerNames.nextElement();
					Enumeration<String> values = servletRequest
							.getHeaders(header);
					while (values.hasMoreElements()) {
						log.debug(header + ": " + values.nextElement());
					}
				}
				log.debug("End Request <<<<<<<<<");
			}
		}

		if (ctx.getChannel().getLocalAddress() instanceof InetSocketAddress) {
			
			if (interfaceResource.getProtocol()==HTTPProtocol.HTTP && interfaceResource.getRedirectHTTPS()) {
				// Redirect the plain port to SSL
				String host = nettyRequest.getHeader(HttpHeaders.HOST);
				if(host==null) {
					nettyResponse.sendError(400, "No Host Header");
				} else {
					int idx;
					if ((idx = host.indexOf(':')) > -1) {
						host = host.substring(0, idx);
					}
					nettyResponse.sendRedirect("https://"
							+ host
							+ (interfaceResource.getRedirectPort() != 443 ? ":"
									+ String.valueOf(interfaceResource.getRedirectPort()) : "")
							+ nettyRequest.getUri());
				}
				sendResponse(servletRequest, nettyResponse, false);
				return;
			}
		}

		if (nettyRequest.containsHeader("Upgrade")) {
			for (WebsocketHandler handler : server.getWebsocketHandlers()) {
				if (handler.handlesRequest(servletRequest)) {
					try {
						handler.acceptWebsocket(servletRequest, nettyResponse,
								new WebsocketConnectCallback(ctx.getChannel(),
										servletRequest, nettyResponse, handler), this);
					} catch (AccessDeniedException ex) {
						log.error("Failed to open tunnel", ex);
						nettyResponse.setStatus(HttpStatus.SC_FORBIDDEN);
						sendResponse(servletRequest, nettyResponse, false);
					} catch (UnauthorizedException ex) {
						log.error("Failed to open tunnel", ex);
						nettyResponse.setStatus(HttpStatus.SC_UNAUTHORIZED);
						sendResponse(servletRequest, nettyResponse, false);
					}
					return;
				}
			}
		} else {
			for (HttpRequestHandler handler : server.getHttpHandlers()) {
				if(log.isDebugEnabled()) {
					log.debug("Checking HTTP handler: " + handler.getName());
				}
				if (handler.handlesRequest(servletRequest.getRequestURI())) {
					if(log.isDebugEnabled()) {
						log.debug(handler.getName() + " is processing HTTP request");
					}
					handler.handleHttpRequest(servletRequest, nettyResponse,
							this);
					return;
				}
			}
		}

		
		send404(servletRequest, nettyResponse);
		sendResponse(servletRequest, nettyResponse, false);

		if (log.isDebugEnabled()) {
			log.debug("Leaving HttpRequestDispatcherHandler processRequest");
		}
	}

	@Override
	public void send404(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
		servletResponse.sendError(HttpStatus.SC_NOT_FOUND);
		URL url = getClass().getResource("/404.html");
		if(servletRequest.getAttribute("404.html")!=null) {
			url = (URL) servletRequest.getAttribute("404.html");
		}
		servletRequest.setAttribute(CONTENT_INPUTSTREAM, url.openStream());
	}
	
	@Override
	public void send500(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse) throws IOException {
		servletResponse.sendError(HttpStatus.SC_NOT_FOUND);
		URL url = getClass().getResource("/500.html");
		if(servletRequest.getAttribute("500.html")!=null) {
			url = (URL) servletRequest.getAttribute("500.html");
		}
		ITokenResolver resolver = new ITokenResolver() {
			
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
					+ channel.getRemoteAddress());
		}
		// Check for closing frame
		if (msg instanceof CloseWebSocketFrame) {
			// handshaker.close(channel, (CloseWebSocketFrame) msg);
			return;
		} else if (!(msg instanceof BinaryWebSocketFrame)) {
			// Close
		}

		NettyWebsocketClient nettyClient = (NettyWebsocketClient) channel.getAttachment();
		nettyClient.frameReceived(msg);
		
	}

	public void sendResponse(final HttpRequestServletWrapper servletRequest,
			final HttpResponseServletWrapper servletResponse, boolean chunked) {

		try {
			
			switch(servletResponse.getNettyResponse().getStatus().getCode()) {
				case HttpStatus.SC_NOT_FOUND:
				{
					send404(servletRequest, servletResponse);
					break;
				}
				case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				{
					send500(servletRequest, servletResponse);
					break;
				}
				default:
				{
				}
			}
			
			addStandardHeaders(servletResponse);

			InputStream content = processContent(
					servletRequest,
					servletResponse,
					servletResponse.getRequest().getHeader(
							HttpHeaders.ACCEPT_ENCODING));

			if (log.isDebugEnabled()) {
				synchronized (log) {
					log.debug("Begin Response >>>>>>");
					log.debug(servletResponse.getNettyResponse().getStatus()
							.toString());
					for (String header : servletResponse.getNettyResponse()
							.getHeaderNames()) {
						for (String value : servletResponse.getNettyResponse()
								.getHeaders(header)) {
							log.debug(header + ": " + value);
						}
					}

				}
			}

			if (content != null) {

				try {
					servletResponse.getChannel().write(
							servletResponse.getNettyResponse());

					if (log.isDebugEnabled()) {
						try {
							log.debug("Sending " + content.available() 
									+ " bytes of HTTP content");
						} catch (IOException e) {
						}
					}

					servletResponse
							.getChannel()
							.write(new HttpChunkStream(content, servletRequest
									.getRequestURI()))
							.addListener(
									new CheckCloseStateListener(servletResponse));
				} catch (Exception e) {
					log.error("Unexpected exception writing content stream", e);
				}
			} else {
				servletResponse
						.getChannel()
						.write(servletResponse.getNettyResponse())
						.addListener(
								new CheckCloseStateListener(servletResponse));
			}
			
		} catch(IOException ex) {
			log.error("IO Error sending HTTP response", ex);
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("End Response >>>>>>");
			}
			
			Request.remove();
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
							+ future.getChannel().getRemoteAddress()
							+ " localAddress="
							+ future.getChannel().getLocalAddress());
				}
				servletResponse.getChannel().close();
			}
		}
	}

	private InputStream processContent(
			HttpRequestServletWrapper servletRequest,
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

			ChannelBuffer buffer = servletResponse.getContent();
			boolean doGzip = false;

			if (servletResponse.getNettyResponse()
					.getHeader("Content-Encoding") == null) {
				if (acceptEncodings != null) {
					doGzip = acceptEncodings.indexOf("gzip") > -1;
				}

				if (doGzip) {
					try {
						ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
						GZIPOutputStream gzip = new GZIPOutputStream(gzipped);
						gzip.write(buffer.array(), 0, buffer.readableBytes());
						gzip.finish();
						buffer = ChannelBuffers.wrappedBuffer(gzipped
								.toByteArray());
						servletResponse.setHeader("Content-Encoding", "gzip");
					} catch (IOException e) {
						log.error("Failed to gzip response", e);
					}
				}
			}

			servletResponse.getNettyResponse().setContent(buffer);
			servletResponse.setHeader("Content-Length",
					String.valueOf(buffer.readableBytes()));
		} else {
			servletResponse.setHeader("Content-Length", "0");
		}

		return null;
	}

	private void addStandardHeaders(HttpResponseServletWrapper servletResponse) {

		servletResponse.setHeader("Server", server.getApplicationName());

		String connection = servletResponse.getRequest().getHeader(
				HttpHeaders.CONNECTION);
		if (connection!=null && connection.equalsIgnoreCase("close")) {
			servletResponse.setHeader("Connection", "close");
			servletResponse.setCloseOnComplete(true);
		}

		servletResponse.setHeader("Date",
				DateUtils.formatDate(new Date(System.currentTimeMillis())));
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		Channel ch = (Channel) ctx.getChannel();

		if (log.isDebugEnabled()) {
			log.debug("Channel disconnected remoteAddress="
					+ ch.getRemoteAddress() + " localAddress="
					+ ch.getLocalAddress());
		}

		if (ch.isOpen()) {
			ch.close();
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Channel ch = (Channel) ctx.getChannel();

		if (log.isDebugEnabled()) {
			log.debug("Channel closed remoteAddress=" + ch.getRemoteAddress()
					+ " localAddress=" + ch.getLocalAddress());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {

		if (log.isErrorEnabled()) {
			log.error("Exception in http request remoteAddress="
					+ e.getChannel().getRemoteAddress() + " localAddress="
					+ e.getChannel().getLocalAddress(), e.getCause());
		}

		ctx.getChannel().close();
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Channel ch = (Channel) ctx.getChannel();

		if (log.isDebugEnabled()) {
			log.debug("Channel open remoteAddress=" + ch.getRemoteAddress()
					+ " localAddress=" + ch.getLocalAddress());
		}
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Channel ch = (Channel) ctx.getChannel();

		if (log.isDebugEnabled()) {
			log.debug("Channel connected remoteAddress="
					+ ch.getRemoteAddress() + " localAddress="
					+ ch.getLocalAddress());
		}
	}

	@Override
	public void sendResponse(HttpServletRequest request,
			HttpServletResponse response, boolean chunked) {
		sendResponse((HttpRequestServletWrapper) request,
				(HttpResponseServletWrapper) response, chunked);
	}

	class WebsocketConnectCallback implements WebsocketClientCallback {

		Channel websocketChannel;
		HttpRequestServletWrapper request;
		HttpResponseServletWrapper response;
		WebsocketHandler handler;
		
		public WebsocketConnectCallback(Channel websocketChannel,
				HttpRequestServletWrapper nettyRequest,
				HttpResponseServletWrapper nettyResponse,
				WebsocketHandler handler) {
			this.websocketChannel = websocketChannel;
			this.request = nettyRequest;
			this.response = nettyResponse;
			this.handler = handler;
		}

		@Override
		public void websocketAccepted(final WebsocketClient client) {

			if (log.isDebugEnabled()) {
				log.debug("Socket connected, completing handshake for "
						+ websocketChannel.getRemoteAddress());
			}

			NettyWebsocketClient websocketClient = (NettyWebsocketClient) client;
			websocketClient.setWebsocketChannel(websocketChannel);

			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
					getWebSocketLocation(request.getNettyRequest()), null,
					true);

			WebSocketServerHandshaker handshaker = wsFactory
					.newHandshaker(request.getNettyRequest());
			if (handshaker == null) {
				wsFactory
						.sendUnsupportedWebSocketVersionResponse(websocketChannel);
			} else {
				handshaker.handshake(websocketChannel,
						request.getNettyRequest()).addListener(
						new ChannelFutureListener() {

							@Override
							public void operationComplete(ChannelFuture future)
									throws Exception {

								if (future.isSuccess()) {
									if (log.isDebugEnabled())
										log.debug("Handshake complete for "
												+ websocketChannel
														.getRemoteAddress());
									client.open();
								} else {
									if (log.isDebugEnabled())
										log.debug("Handshake failed for "
												+ websocketChannel
														.getRemoteAddress());
									client.close();
								}
							}
						});
			}
		}

		@Override
		public void websocketRejected(Throwable cause) {
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			sendResponse(request, response, false);
		}

		@Override
		public void websocketClosed(WebsocketClient client) {
			client.close();
		}

	}

	private static String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.getHeader(HttpHeaders.HOST) + req.getUri();
	}

}
