package com.hypersocket.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.HttpResponseProcessor;
import com.hypersocket.server.handlers.WebsocketHandler;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.server.interfaces.http.HTTPProtocol;
import com.hypersocket.servlet.HypersocketSession;
import com.hypersocket.servlet.request.Request;
import com.hypersocket.session.json.SessionUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

public class RequestWorker implements Runnable {

	static Logger log = LoggerFactory
			.getLogger(RequestWorker.class);
	/**
	 * 
	 */
//	private final HttpRequestDispatcherHandler httpRequestDispatcherHandler;
	private ChannelHandlerContext ctx;
	private HttpRequest nettyRequest;
	private HttpRequestServletWrapper servletRequest;
	private HypersocketSession session;
	private HttpResponseServletWrapper servletResponse;
	private HTTPInterfaceResource interfaceResource;
	private NettyServer nettyServer;
	private HttpResponseProcessor responseProcessor;
	private String contentType;
	private String contentTypeCharset;

	public RequestWorker(ChannelHandlerContext ctx,
			HttpRequest nettyRequest, NettyServer nettyServer, HttpResponseProcessor responseProcessor) {
		this.nettyServer = nettyServer;
		this.ctx = ctx;
		this.nettyRequest = nettyRequest;
		this.responseProcessor = responseProcessor;

		interfaceResource = ctx.channel().parent().attr(NettyServer.INTERFACE_RESOURCE).get();

		servletResponse = new HttpResponseServletWrapper(
				new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK), ctx.channel(), nettyRequest);

		InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
		io.netty.handler.codec.http.HttpHeaders headers = nettyRequest.headers();
		if (headers.contains("X-Forwarded-For")) {
			String[] ips = headers.get("X-Forwarded-For").split(",");

			/*
			 * Some proxies might send a port number. It seems a bit ambiguous if this is in
			 * the spec or not, depending on where you look.
			 * 
			 * Let's support it anyway
			 */
			String[] ipAndPort = ips[0].split(":");

			remoteAddress = new InetSocketAddress(ips[0],
					ipAndPort.length > 1 ? Integer.parseInt(ipAndPort[1]) : remoteAddress.getPort());
		} else if (headers.contains("Forwarded")) {
			StringTokenizer t = new StringTokenizer(headers.get("Forwarded"), ";");
			while (t.hasMoreTokens()) {
				String[] pair = t.nextToken().split("=");
				if (pair.length == 2 && pair[0].equalsIgnoreCase("for")) {
					remoteAddress = new InetSocketAddress(pair[1], remoteAddress.getPort());
				}
			}
		}

		session = this.nettyServer.setupHttpSession(headers.getAll("Cookie"),
				interfaceResource.getProtocol() == HTTPProtocol.HTTPS,
				StringUtils.substringBefore(headers.get("Host"), ":"), servletResponse);

		servletRequest = new HttpRequestServletWrapper(nettyRequest, (InetSocketAddress) ctx.channel().localAddress(),
				remoteAddress, interfaceResource.getProtocol() == HTTPProtocol.HTTPS,
				this.nettyServer.getServletContext(), session);
		


		contentType = nettyRequest.headers().get(HttpHeaders.CONTENT_TYPE);

		int idx;
		if (contentType != null) {
			contentTypeCharset = "UTF-8";
			if ((idx = contentType.indexOf(';')) > -1) {
				String tmp = contentType.substring(idx + 1);
				contentType = contentType.substring(0, idx);
				if ((idx = tmp.indexOf("charset=")) > -1) {
					contentTypeCharset = tmp.substring(idx + 8);
				}
			}
		}

//			if(HttpUtil.isTransferEncodingChunked(nettyRequest)) {
		ctx.channel().attr(HttpRequestServletWrapper.REQUEST).set(servletRequest);
//			}
	}

	protected String processReplacements(String path) {
		path = path.replace("${apiPath}", nettyServer.getApiPath());
		path = path.replace("${uiPath}", nettyServer.getUiPath());
		return path.replace("${basePath}", nettyServer.getBasePath());
	}
	
	public void run() {

		try {
			HttpRequestDispatcherHandler.requestLocal.set(servletRequest);

			if (contentType != null && contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
				String content;
				if (HttpUtil.isTransferEncodingChunked(nettyRequest)) {
					content = IOUtils.toString(servletRequest.getInputStream(), contentTypeCharset);
				} else {
					content = servletRequest.getRequestContent().toString(Charset.forName(contentTypeCharset));
				}
				servletRequest.processParameters(content);
			}

			if (log.isDebugEnabled()) {
				synchronized (log) {
					log.debug("Begin Request <<<<<<<<<");
					log.debug(servletRequest.getMethod() + " "
							+ servletRequest.getRequestURI() + " " + servletRequest.getProtocol());
					Enumeration<String> headerNames = servletRequest.getHeaderNames();
					while (headerNames.hasMoreElements()) {
						String header = headerNames.nextElement();
						Enumeration<String> values = servletRequest.getHeaders(header);
						while (values.hasMoreElements()) {
							log.debug(header + ": " + values.nextElement());
						}
					}
				}
			}

			String reverseUri = servletRequest.getRequestURI();
			reverseUri = reverseUri.replace(this.nettyServer.getApiPath(), "${apiPath}");
			reverseUri = reverseUri.replace(this.nettyServer.getUiPath(), "${uiPath}");
			reverseUri = reverseUri.replace(this.nettyServer.getBasePath(), "${basePath}");

			if (this.nettyServer.isProtectedPage(reverseUri)) {
				if (!ApplicationContextServiceImpl.getInstance().getBean(SessionUtils.class)
						.hasActiveSession(servletRequest)) {
					servletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
					responseProcessor.sendResponse(servletRequest, servletResponse, false);
					return;
				}
			}

			Map<Pattern, String> rewrites = this.nettyServer.getUrlRewrites();
			for (Pattern regex : rewrites.keySet()) {
				Matcher matcher = regex.matcher(reverseUri);
				if (matcher.matches()) {
					String uri = processReplacements(rewrites.get(regex));
					uri = matcher.replaceAll(uri);
					servletRequest.setAttribute(HttpRequestDispatcherHandler.BROWSER_URI, nettyRequest.uri());
					servletRequest.parseUri(uri);
					reverseUri = servletRequest.getRequestURI();
					reverseUri = reverseUri.replace(this.nettyServer.getApiPath(),
							"${apiPath}");
					reverseUri = reverseUri.replace(this.nettyServer.getUiPath(), "${uiPath}");
					reverseUri = reverseUri.replace(this.nettyServer.getBasePath(),
							"${basePath}");
					break;
				}
			}

			Map<String, String> aliases = this.nettyServer.getAliases();
			while (aliases.containsKey(reverseUri)) {
				String path = processReplacements(aliases.get(reverseUri));
				if (path.startsWith("redirect:")) {
					String redirPath = path.substring(9);
					if (StringUtils.isNotBlank(servletRequest.getQueryString())) {
						redirPath += "?" + servletRequest.getQueryString();
					}
					if (log.isDebugEnabled()) {
						log.debug("Redirecting to " + redirPath + " for " + reverseUri);
					}
					servletResponse.sendRedirect(redirPath,
							false /* Don't use a permanent redirection as the alias target may change */);
					responseProcessor.sendResponse(servletRequest, servletResponse, false);
					return;
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Using alias " + path + " for path " + reverseUri);
					}
					servletRequest.setAttribute(HttpRequestDispatcherHandler.BROWSER_URI, nettyRequest.uri());
					servletRequest.parseUri(path);
					reverseUri = path;
					reverseUri = reverseUri.replace(this.nettyServer.getApiPath(),
							"${apiPath}");
					reverseUri = reverseUri.replace(this.nettyServer.getUiPath(), "${uiPath}");
					reverseUri = reverseUri.replace(this.nettyServer.getBasePath(),
							"${basePath}");
				}
			}

			Request.set(servletRequest, servletResponse);

			if (log.isDebugEnabled()) {
				synchronized (log) {
					log.debug("Begin Request <<<<<<<<<");
					log.debug(servletRequest.getMethod() + " "
							+ servletRequest.getRequestURI() + " " + servletRequest.getProtocol());
					Enumeration<String> headerNames = servletRequest.getHeaderNames();
					while (headerNames.hasMoreElements()) {
						String header = headerNames.nextElement();
						Enumeration<String> values = servletRequest.getHeaders(header);
						while (values.hasMoreElements()) {
							log.debug(header + ": " + values.nextElement());
						}
					}
					log.debug("End Request <<<<<<<<<");
				}
			}

			if (ctx.channel().localAddress() instanceof InetSocketAddress) {

				if (interfaceResource.getProtocol() == HTTPProtocol.HTTP
						&& this.nettyServer.isRedirectable(nettyRequest.uri())
						&& interfaceResource.getRedirectHTTPS()) {

					if (nettyRequest.uri().equals("/health-check")) {
						servletResponse.setStatus(HttpStatus.SC_OK);
						responseProcessor.sendResponse(servletRequest, servletResponse, false);
						return;
					} else {
						// Redirect the plain port to SSL
						String host = nettyRequest.headers().get(HttpHeaders.HOST);
						if (host == null) {
							servletResponse.sendError(400, "No Host Header");
						} else {
							int idx = 0;
							if ((idx = host.indexOf(':')) > -1) {
								host = host.substring(0, idx);
							}
							servletResponse.sendRedirect("https://" + host
									+ (interfaceResource.getRedirectPort() != 443
											? ":" + String.valueOf(interfaceResource.getRedirectPort())
											: "")
									+ nettyRequest.uri());
						}
						responseProcessor.sendResponse(servletRequest, servletResponse, false);
						return;
					}
				}
			}

			if (nettyRequest.headers().contains("Upgrade")) {
				for (WebsocketHandler handler : this.nettyServer.getWebsocketHandlers()) {
					if (handler.handlesRequest(servletRequest)) {
						try {
							handler.acceptWebsocket(servletRequest, servletResponse,
									new WebsocketConnectCallback(ctx.channel(),
											servletRequest, servletResponse, handler, responseProcessor),
									responseProcessor);
						} catch (AccessDeniedException ex) {
							log.error("Failed to open tunnel", ex);
							servletResponse.setStatus(HttpStatus.SC_FORBIDDEN);
							responseProcessor.sendResponse(servletRequest, servletResponse, false);
						} catch (UnauthorizedException ex) {
							log.error("Failed to open tunnel", ex);
							servletResponse.setStatus(HttpStatus.SC_UNAUTHORIZED);
							responseProcessor.sendResponse(servletRequest, servletResponse, false);
						}
						return;
					}
				}
			} else {

				for (HttpRequestHandler handler : this.nettyServer.getHttpHandlers()) {
					if (log.isDebugEnabled()) {
						log.debug("Checking HTTP handler: " + handler.getName());
					}
					if (handler.handlesRequest(servletRequest)) {
						if (log.isDebugEnabled()) {
							log.debug(String.format("%s is processing HTTP request for %s",
									handler.getName(), reverseUri));
						}
						this.nettyServer.processDefaultResponse(servletRequest, servletResponse,
								handler.getDisableCache());
						handler.handleHttpRequest(servletRequest, servletResponse, responseProcessor);
						return;
					}
				}
			}

			this.nettyServer.processDefaultResponse(servletRequest, servletResponse, true);
			responseProcessor.send404(servletRequest, servletResponse);
			responseProcessor.sendResponse(servletRequest, servletResponse, false);

			if (log.isDebugEnabled()) {
				log.debug("Leaving HttpRequestDispatcherHandler processRequest");
			}
		} catch (ClosedChannelException e) {
			// Ignore unless debug
			if (log.isDebugEnabled()) {
				log.debug("Attempted to perform operation on closed channel", e);
			}
		} catch (IOException ex) {
			log.error(String.format("I/O error HTTP request worker: %s", ex.getMessage()),
					ex);
			ctx.channel().close();
		} catch (Throwable t) {
			log.error("Exception in HTTP request worker", t);
			ctx.channel().close();
		} finally {
			HttpRequestDispatcherHandler.requestLocal.remove();
		}
	}
}