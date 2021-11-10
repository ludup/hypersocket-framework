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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.auth.json.UnauthorizedException;
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
import com.hypersocket.session.json.SessionUtils;
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
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.stream.ChunkedStream;

public class HttpRequestDispatcherHandler extends ChannelInboundHandlerAdapter
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

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
		  HttpRequestServletWrapper servletRequest = ctx.channel().attr(HttpRequestServletWrapper.REQUEST).get();
		  ((HttpRequestChunkStream)servletRequest.getInputStream()).setCurrentChunk(chunk);
			  
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
			server.getExecutor().submit(new RequestWorker(ctx, nettyRequest));
		} else {
			new RequestWorker(ctx, nettyRequest).run();
		}
	}
	
	private static ThreadLocal<HttpServletRequest> requestLocal = new ThreadLocal<>();
	
	public static HttpServletRequest getRequest() {
		return requestLocal.get();
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
			
			interfaceResource = ctx.channel().parent().attr(NettyServer.INTERFACE_RESOURCE).get();
			
			nettyResponse = new HttpResponseServletWrapper(
					new DefaultHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.OK),
					ctx.channel(), nettyRequest);
	
			InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			if(nettyRequest.headers().contains("X-Forwarded-For")) {
				String[] ips = nettyRequest.headers().get("X-Forwarded-For").split(",");
				
				/* Some proxies might senda port number. It seems a bit ambiguous if
				 * this is in the spec or not, depending on where you look. 
				 * 
				 * Let's support it anyway
				 */
				String[] ipAndPort = ips[0].split(":");
				
				remoteAddress = new InetSocketAddress(ips[0], ipAndPort.length > 1 ? Integer.parseInt(ipAndPort[1]) : remoteAddress.getPort());
			} else if(nettyRequest.headers().contains("Forwarded")) {
				StringTokenizer t = new StringTokenizer(nettyRequest.headers().get("Forwarded"), ";");
				while(t.hasMoreTokens()) {
					String[] pair = t.nextToken().split("=");
					if(pair.length == 2 && pair[0].equalsIgnoreCase("for")) {
						remoteAddress = new InetSocketAddress(pair[1], remoteAddress.getPort());
					}
				}
			}
			
			session = server.setupHttpSession(
					nettyRequest.headers().getAll("Cookie"), 
					interfaceResource.getProtocol()==HTTPProtocol.HTTPS,
					StringUtils.substringBefore(nettyRequest.headers().get("Host"), ":"),
					nettyResponse);
			
			servletRequest = new HttpRequestServletWrapper(
					nettyRequest, (InetSocketAddress) ctx.channel()
							.localAddress(), remoteAddress, 
							interfaceResource.getProtocol()==HTTPProtocol.HTTPS, 
							server.getServletContext(), session);
			
			if(HttpUtil.isTransferEncodingChunked(nettyRequest)) {
				ctx.channel().attr(HttpRequestServletWrapper.REQUEST).set(servletRequest);
			}
		}
		
		public void run() {

			try {
				requestLocal.set(servletRequest);

				String contentType = nettyRequest.headers().get(HttpHeaders.CONTENT_TYPE);
				
				int idx;
				if (contentType != null) {
					String contentTypeCharset = "UTF-8";
					if ((idx = contentType.indexOf(';')) > -1) {
						String tmp = contentType.substring(idx + 1);
						contentType = contentType.substring(0, idx);
						if ((idx = tmp.indexOf("charset=")) > -1) {
							contentTypeCharset = tmp.substring(idx + 8);
						}
					}
					if (contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
						String content; 
						if(HttpUtil.isTransferEncodingChunked(nettyRequest)) {
							content = IOUtils.toString(servletRequest.getInputStream(), contentTypeCharset);
						} else {
							content = servletRequest.getRequestContent().toString(Charset.forName(contentTypeCharset));
						}
						servletRequest.processParameters(content);
					}
				}
				
				
				
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
				
				if(server.isProtectedPage(reverseUri)) {
					if(!ApplicationContextServiceImpl.getInstance().getBean(SessionUtils.class).hasActiveSession(servletRequest)) {
						nettyResponse.setStatus(HttpStatus.SC_NOT_FOUND);
						sendResponse(servletRequest, nettyResponse, false);
						return;
					}
				}
				
				Map<Pattern,String> rewrites = server.getUrlRewrites();
				for(Pattern regex : rewrites.keySet()) {
					Matcher matcher = regex.matcher(reverseUri);
					if(matcher.matches()) {
						String uri = processReplacements(rewrites.get(regex));
						uri = matcher.replaceAll(uri);
						servletRequest.setAttribute(BROWSER_URI, nettyRequest.uri());
						servletRequest.parseUri(uri);
						reverseUri = servletRequest.getRequestURI();
						reverseUri = reverseUri.replace(server.getApiPath(), "${apiPath}");
						reverseUri = reverseUri.replace(server.getUiPath(), "${uiPath}");
						reverseUri = reverseUri.replace(server.getBasePath(), "${basePath}");
						break;
					}
				}
				
				Map<String,String> aliases = server.getAliases();
				while(aliases.containsKey(reverseUri)) {
					String path = processReplacements(aliases.get(reverseUri));
					if(path.startsWith("redirect:")) {
						String redirPath = path.substring(9);
						if(StringUtils.isNotBlank(servletRequest.getQueryString())) {
							redirPath += "?" + servletRequest.getQueryString();
						}
						if(log.isDebugEnabled()) {
							log.debug("Redirecting to " + redirPath + " for " + reverseUri);
						}
						nettyResponse.sendRedirect(redirPath, false /* Don't use a permanent redirection as the alias target may change */);
						sendResponse(servletRequest, nettyResponse, false);
						return;
					} else {
						if(log.isDebugEnabled()) {
							log.debug("Using alias " + path + " for path " + reverseUri);
						}
						servletRequest.setAttribute(BROWSER_URI, nettyRequest.uri());
						servletRequest.parseUri(path);
						reverseUri = path;
						reverseUri = reverseUri.replace(server.getApiPath(), "${apiPath}");
						reverseUri = reverseUri.replace(server.getUiPath(), "${uiPath}");
						reverseUri = reverseUri.replace(server.getBasePath(), "${basePath}");
					}
				}
				
				Request.set(servletRequest, nettyResponse);
				
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
		
				if (ctx.channel().localAddress() instanceof InetSocketAddress) {
					
					if (interfaceResource.getProtocol()==HTTPProtocol.HTTP && server.isRedirectable(nettyRequest.uri()) && interfaceResource.getRedirectHTTPS()) {
						
						if(nettyRequest.uri().equals("/health-check")) {
							nettyResponse.setStatus(HttpStatus.SC_OK);
							sendResponse(servletRequest, nettyResponse, false);
							return;
						} else {
							// Redirect the plain port to SSL
							String host = nettyRequest.headers().get(HttpHeaders.HOST);
							if(host==null) {
								nettyResponse.sendError(400, "No Host Header");
							} else {
								if ((idx = host.indexOf(':')) > -1) {
									host = host.substring(0, idx);
								}
								nettyResponse.sendRedirect("https://"
										+ host
										+ (interfaceResource.getRedirectPort() != 443 ? ":"
												+ String.valueOf(interfaceResource.getRedirectPort()) : "")
										+ nettyRequest.uri());
							}
							sendResponse(servletRequest, nettyResponse, false);
							return;
						}
					}
				}
		
				if (nettyRequest.headers().contains("Upgrade")) {
					for (WebsocketHandler handler : server.getWebsocketHandlers()) {
						if (handler.handlesRequest(servletRequest)) {
							try {
								handler.acceptWebsocket(servletRequest, nettyResponse,
										new WebsocketConnectCallback(ctx.channel(),
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
								log.debug(String.format("%s is processing HTTP request for %s", handler.getName(), reverseUri));
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
				log.error(String.format("I/O error HTTP request worker: %s", ex.getMessage()), ex);
				ctx.channel().close();
			} catch(Throwable t) {
				log.error("Exception in HTTP request worker", t);
				ctx.channel().close();
			}
			finally {
				requestLocal.remove();
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
						+ websocketChannel.remoteAddress());
			}

			NettyWebsocketClient websocketClient = (NettyWebsocketClient) client;
			websocketClient.setWebsocketChannel(websocketChannel);

			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
					getWebSocketLocation(request.getNettyRequest()), "binary",
					true);

			WebSocketServerHandshaker handshaker = wsFactory
					.newHandshaker(request.getNettyRequest());
			if (handshaker == null) {
				WebSocketServerHandshakerFactory
						.sendUnsupportedVersionResponse(websocketChannel);
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
														.remoteAddress());
									client.open();
									websocketChannel.closeFuture().addListener(new ChannelFutureListener() {
										
										@Override
										public void operationComplete(ChannelFuture future) throws Exception {
											client.close();
										}
									});
								} else {
									if (log.isDebugEnabled())
										log.debug("Handshake failed for "
												+ websocketChannel
														.remoteAddress());
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
		return "ws://" + req.headers().get(HttpHeaders.HOST) + req.uri();
	}

}
