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
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
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

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.json.RestApi;
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
import com.hypersocket.servlet.request.Request;
import com.hypersocket.utils.ITokenResolver;
import com.hypersocket.utils.TokenAdapter;
import com.hypersocket.utils.TokenReplacementReader;

public class HttpRequestDispatcherHandler extends SimpleChannelUpstreamHandler
		implements HttpResponseProcessor {

	public static final String CONTENT_INPUTSTREAM = "ContentInputStream";
	public static final String BROWSER_URI = "browserRequestUri";
	
	private static Logger log = LoggerFactory
			.getLogger(HttpRequestDispatcherHandler.class);

	private NettyServer server;
	
	public HttpRequestDispatcherHandler(NettyServer server)
			throws ServletException {
		this.server = server;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object msg = e.getMessage();

		if(msg instanceof HttpRequest) {
		
			HttpRequest nettyRequest = (HttpRequest) msg;
			dispatchRequest(ctx, nettyRequest);

		} else if (msg instanceof WebSocketFrame) {

			processWebsocketFrame((WebSocketFrame) msg, ctx.getChannel());

		} else if (msg instanceof HttpChunk) {
	
			HttpChunk chunk = (HttpChunk) msg;
			
			if(log.isDebugEnabled()) {
				log.debug(String.format("Received HTTP chunk of %d bytes", ((HttpChunk) msg).getContent().readableBytes()));
			}
			
			HttpRequestServletWrapper servletRequest = (HttpRequestServletWrapper) ctx.getChannel().getAttachment();
			((HttpRequestChunkStream)servletRequest.getInputStream()).setCurrentChunk(chunk);

		} else {
			if (log.isErrorEnabled()) {
				log.error("Received invalid MessageEvent " + msg.toString());
			}
		}

	}
	
	private void dispatchRequest(ChannelHandlerContext ctx,
			HttpRequest nettyRequest) {
		if(nettyRequest.isChunked()) {
			server.getExecutor().submit(new RequestWorker(ctx, nettyRequest));
		} else {
			new RequestWorker(ctx, nettyRequest).run();
		}
	}

	class RequestWorker implements Runnable {

		ChannelHandlerContext ctx;
		HttpRequest nettyRequest;
		HttpRequestServletWrapper servletRequest;
		HypersocketSession session;
		HttpResponseServletWrapper nettyResponse;
		HTTPInterfaceResource interfaceResource;
		
		RequestWorker(ChannelHandlerContext ctx,
			HttpRequest nettyRequest) {
			this.ctx = ctx;
			this.nettyRequest = nettyRequest;
			
			interfaceResource = (HTTPInterfaceResource) ctx.getChannel().getParent().getAttachment();
			
			nettyResponse = new HttpResponseServletWrapper(
					new DefaultHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.OK),
					ctx.getChannel(), nettyRequest);
	
			session = server.setupHttpSession(
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
			
			servletRequest = new HttpRequestServletWrapper(
					nettyRequest, (InetSocketAddress) ctx.getChannel()
							.getLocalAddress(), remoteAddress, 
							interfaceResource.getProtocol()==HTTPProtocol.HTTPS, 
							server.getServletContext(), session);
			
			if(nettyRequest.isChunked()) {
				ctx.getChannel().setAttachment(servletRequest);
			}
		}
		
		public void run() {
		
			try {

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
					}
				}
					
				String reverseUri = servletRequest.getRequestURI();
				reverseUri = reverseUri.replace(server.getApiPath(), "${apiPath}");
				reverseUri = reverseUri.replace(server.getUiPath(), "${uiPath}");
				reverseUri = reverseUri.replace(server.getBasePath(), "${basePath}");
				
				Map<Pattern,String> rewrites = server.getUrlRewrites();
				for(Pattern regex : rewrites.keySet()) {
					Matcher matcher = regex.matcher(reverseUri);
					if(matcher.matches()) {
						String uri = processReplacements(rewrites.get(regex));
						uri = matcher.replaceAll(uri);
						servletRequest.setAttribute(BROWSER_URI, nettyRequest.getUri());
						servletRequest.parseUri(uri);
						break;
					}
				}
				
				Map<String,String> aliases = server.getAliases();
				if(aliases.containsKey(reverseUri)) {
					String path = processReplacements(aliases.get(reverseUri));
					if(path.startsWith("redirect:")) {
						nettyResponse.sendRedirect(path.substring(9), true);
						sendResponse(servletRequest, nettyResponse, false);
						return;
					} else {
						servletRequest.setAttribute(BROWSER_URI, nettyRequest.getUri());
						servletRequest.parseUri(path);
					}
				}
				
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
												servletRequest, nettyResponse, handler), 
										HttpRequestDispatcherHandler.this);
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
						if (handler.handlesRequest(servletRequest)) {
							if(log.isDebugEnabled()) {
								log.debug(handler.getName() + " is processing HTTP request");
							}
							server.processDefaultResponse(servletRequest, nettyResponse, handler.getDisableCache());
							handler.handleHttpRequest(servletRequest, nettyResponse,
									HttpRequestDispatcherHandler.this);
							return;
						}
					}
				}
		
				server.processDefaultResponse(servletRequest, nettyResponse, true);
				send404(servletRequest, nettyResponse);
				sendResponse(servletRequest, nettyResponse, false);
		
				if (log.isDebugEnabled()) {
					log.debug("Leaving HttpRequestDispatcherHandler processRequest");
				}
			} catch(ClosedChannelException e) { 
				// Ignore unless debug
				if(log.isDebugEnabled()) {
					log.debug("Attempted to perform operation on closed channel", e);
				}
			} catch(IOException ex) { 
				log.error(String.format("I/O error HTTP request worker: %s", ex.getMessage()));
				ctx.getChannel().close();
			} catch(Throwable t) {
				log.error("Exception in HTTP request worker", t);
				ctx.getChannel().close();
			}
		}
	}
	
	protected String processReplacements(String path) {
		path = path.replace("${apiPath}", server.getApiPath());
		path = path.replace("${uiPath}", server.getUiPath());
		return path.replace("${basePath}", server.getBasePath());
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

			if(!StringUtils.equals(RestApi.API_REST, (CharSequence) servletRequest.getAttribute(RestApi.API_REST))) {
				switch (servletResponse.getNettyResponse().getStatus().getCode()) {
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

	private void addStandardHeaders(HttpServletRequest request, HttpResponseServletWrapper servletResponse) {

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

		if (log.isErrorEnabled() && !(e.getCause() instanceof ClosedChannelException)) {
			if(e.getCause() instanceof IOException) {
				log.error(String.format("Exception in HTTP request worker: %s", e.getCause().getMessage()));
			} else {
				log.error("Exception in http request remoteAddress="
						+ e.getChannel().getRemoteAddress() + " localAddress="
						+ e.getChannel().getLocalAddress(), e.getCause());
			}
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
					getWebSocketLocation(request.getNettyRequest()), "binary",
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
									websocketChannel.getCloseFuture().addListener(new ChannelFutureListener() {
										
										@Override
										public void operationComplete(ChannelFuture future) throws Exception {
											client.close();
										}
									});
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
		public void websocketRejected(Throwable cause, int error) {
			response.setStatus(error);
			sendResponse(request, response, false);
		}

		@Override
		public void websocketClosed(WebsocketClient client) {
			client.close();
		}

		@Override
		public InetSocketAddress getRemoteAddress() {
			return new InetSocketAddress(request.getRemoteAddr(), request.getRemotePort());
		}

	}

	private static String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.getHeader(HttpHeaders.HOST) + req.getUri();
	}

}
