/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.servlet.HypersocketServletConfig;
import com.hypersocket.servlet.HypersocketServletContext;
import com.hypersocket.servlet.HypersocketSession;
import com.hypersocket.servlet.HypersocketSessionFactory;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AttributeKey;

public class HttpRequestServletWrapper implements HttpServletRequest {

	public static final AttributeKey<HttpRequestServletWrapper> REQUEST = AttributeKey.newInstance(HttpRequestServletWrapper.class.getSimpleName());

	static Logger log = LoggerFactory.getLogger(HttpRequestServletWrapper.class);
	
	private HttpRequest request;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private Map<String, ArrayList<String>> parameters = new HashMap<String, ArrayList<String>>();
	private String charset;
	private String queryString;
	private String pathInfo;
	private String requestUri;
	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;
	private Cookie[] cookies;
	private boolean secure;
	private HttpSession session;
	private ServletContext context;
//	private HttpRequestChunkStream chunkedInputStream = null;
	private String servletPath;
	private String requestUrl;
	private Date timestamp;

	public HttpRequestServletWrapper(HttpRequest request, InetSocketAddress localAddress,
			InetSocketAddress remoteAddress, boolean secure, ServletContext context, HttpSession session) {
		this.request = request;
		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;
		
		timestamp = new Date();

		this.secure = secure;
		this.context = context;
		this.session = session;

		parseUri(request.getUri());
	}

	void parseUri(String uri) {
		/* There is only one context, so we assume requests coming in for root are part of it */
		if(uri.startsWith("/?")) {
			uri = getContextPath() + uri.substring(1);
		}
		else if(uri.equals("/")) {
			uri = getContextPath();
		}
		
		/* Sanity check */
		/* TODO 404 this? */
//		if (!uri.startsWith(getContextPath()))
//			throw new IllegalArgumentException("Unexpected URI " + uri);

		/*
		 * requestURI is the part of this request's URL from the protocol name up to
		 * the query string in the first line of the HTTP request. The web
		 * container does not decode this String. 
		 * 
		 * For example: First line of
		 * HTTP request Returned Value 
		 * 
		 * POST /some/path.html HTTP/1.1	 	/some/path.html 
		 * GET http://foo.bar/a.html HTTP/1.0 	/a.html 
		 * HEAD /xyz?a=b HTTP/1.1 				/xyz
		 */
		requestUri = uri;
		int idx = requestUri.indexOf('?');
		if(idx != -1) {
			requestUri  = requestUri.substring(0, idx);
		}
		
		/*
		 * The request URL is the URL the client used to make the request. The
		 * returned URL contains a protocol, server name, port number, and
		 * server path, but it does not include query string parameters.
		 * 
		 * If this request has been forwarded using
		 * javax.servlet.RequestDispatcher.forward, the server path in the
		 * reconstructed URL must reflect the path used to obtain the
		 * RequestDispatcher, and not the server path specified by the client.
		 */
		this.requestUrl = (secure ? "https://" : "http://") + request.headers().get(HttpHeaders.HOST) + requestUri;
		
		/* Strip the context path from the working URI */
		uri = uri.equals("/") || !uri.startsWith(getContextPath()) ? uri : uri.substring(getContextPath().length());

		/*
		 * Extract the servlet name. If it is the default servlet, the path will
		 * be empty
		 */
		HypersocketServletConfig servlet = null;

		/*
		 * Find the matching servlet (if any) and the default servlet
		 */
		HypersocketServletConfig defaultServlet = null;
		for (HypersocketServletConfig config : ((HypersocketServletContext) getServletContext()).getServletConfigs()) {
			String name = config.getServletName();
			if (name.equals("default")) {
				defaultServlet = config;
				if (servlet != null)
					break;
			} else {
				name = "/" + name;
				if (uri.equals(name) || uri.startsWith(name + "/") || uri.startsWith(name + "?")) {
					servlet = config;
					if (defaultServlet != null)
						break;
				}
			}
		}

		/*
		 * Determine servlet to use and the path. The path is part of this
		 * request's URL that calls the servlet. This path starts with a "/"
		 * character and includes either the servlet name or a path to the
		 * servlet, but does not include any extra path information or a query
		 * string. Same as the value of the CGI variable SCRIPT_NAME.
		 * 
		 * This path will be an empty string ("") if the servlet used to process
		 * this request was matched using the "/*" pattern.
		 */

		if (servlet == null) {
			servlet = defaultServlet;
			servletPath = "";
		} else {
			servletPath = "/" + servlet.getServletName();
		}

		/*
		 * pathInfo is any extra path information associated with the URL the
		 * client sent when it made this request. The extra path information
		 * follows the servlet path but precedes the query string and will start
		 * with a "/" character.
		 * 
		 * This method returns null if there was no extra path information.
		 * 
		 * We also extract the query string at this point
		 */
		pathInfo = uri.equals("/") || uri.equals("") ? "" : uri.substring(servletPath.length());
		idx = pathInfo.indexOf('?');
		if (idx != -1) {
			queryString = pathInfo.substring(idx + 1);
			pathInfo = pathInfo.substring(0, idx);
		}

		/**
		 * Removed pathInfo.equals("/") from this check
		 */
		if (pathInfo.equals(""))
			pathInfo = null;

		if (queryString != null && queryString.length() > 0) {
			parameters.clear();
			processParameters(queryString);
		}

	}

	void processParameters(String params) {
		StringTokenizer paramsParser = new StringTokenizer(params, "&");
		while (paramsParser.hasMoreTokens()) {
			String parameter = paramsParser.nextToken();
			int equalPos = parameter.indexOf('=');
			if (equalPos != -1) {
				try {
					String paramName = URLDecoder.decode(parameter.substring(0, equalPos), "UTF-8");
					String paramValue = "";
					if (equalPos != parameter.length() - 1) {
						paramValue = URLDecoder.decode(parameter.substring(equalPos + 1), "UTF-8");
					}
					addParameter(paramName, paramValue);
				} catch (UnsupportedEncodingException ex) {
				} catch (IllegalArgumentException iae) {
				}
			}
		}
	}

	void addParameter(String name, String value) {
		if (!parameters.containsKey(name)) {
			parameters.put(name, new ArrayList<String>());
		}
		parameters.get(name).add(value);
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public HttpRequest getNettyRequest() {
		return request;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new Vector<String>(attributes.keySet()).elements();
	}

	@Override
	public String getCharacterEncoding() {
		if (charset != null) {
			return charset;
		} else if (request.headers().get(HttpHeaders.CONTENT_TYPE) == null) {
			return null;
		} else {
			int charsetPos = request.headers().get(HttpHeaders.CONTENT_TYPE).indexOf("charset=");
			if (charsetPos == -1) {
				return "UTF-8";
			} else {
				return request.headers().get(HttpHeaders.CONTENT_TYPE).substring(charsetPos + 8);
			}
		}
	}

	@Override
	public void setCharacterEncoding(String charset) throws UnsupportedEncodingException {
		this.charset = charset;
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getContentLength() {
		return (int) HttpUtil.getContentLength(request);
	}

	@Override
	public String getContentType() {
		return request.headers().get(HttpHeaders.CONTENT_TYPE);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
//		if (request.isChunked()) {
//			ChunkedInput ci;
//			if (chunkedInputStream == null) {
//				chunkedInputStream = new HttpRequestChunkStream();
//			}
//			return chunkedInputStream;
//		}
//		return new ChannelBufferServletInputStream(request.getContent());
		throw new UnsupportedOperationException("TODO");
	}

	public String getParameter(String name) {
		if (parameters.containsKey(name)) {
			return parameters.get(name).get(0);
		} else {
			return null;
		}
	}

	public Enumeration<String> getParameterNames() {
		return new Vector<String>(parameters.keySet()).elements();
	}

	public String[] getParameterValues(String name) {
		if (parameters.containsKey(name)) {
			return parameters.get(name).toArray(new String[0]);
		} else {
			return null;
		}
	}

	public Map<String, String[]> getParameterMap() {
		HashMap<String, String[]> tmp = new HashMap<String, String[]>();
		for (Map.Entry<String, ArrayList<String>> entry : parameters.entrySet()) {
			tmp.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
		}
		return tmp;
	}

	@Override
	public String getProtocol() {
		return requestUrl.startsWith("https") ? "https" : "http";
	}

	@Override
	public String getScheme() {
		return secure ? "https" : "http";
	}

	@Override
	public String getServerName() {
		String host = getHeader(HttpHeaders.HOST);
		if(host!=null) {
			int idx;
			if ((idx = host.indexOf(":")) > -1) {
				host = host.substring(0, idx);
			}
		}
		return host;
	}

	@Override
	public int getServerPort() {
		return localAddress.getPort();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	@Override
	public String getRemoteAddr() {
		return remoteAddress.getAddress().getHostAddress();
	}

	@Override
	public String getRemoteHost() {
		return remoteAddress.getHostName();
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}

	@Override
	public Enumeration<Locale> getLocales() {
		final Iterator<Locale> it = Arrays.asList(getLocale()).iterator();
		return new Enumeration<Locale>() {

			@Override
			public boolean hasMoreElements() {
				return it.hasNext();
			}

			@Override
			public Locale nextElement() {
				return it.next();
			}
		};
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRealPath(String path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getRemotePort() {
		return remoteAddress.getPort();
	}

	@Override
	public String getLocalName() {
		return localAddress.getHostName();
	}

	@Override
	public String getLocalAddr() {
		return localAddress.getAddress().getHostAddress();
	}

	@Override
	public int getLocalPort() {
		return localAddress.getPort();
	}

	@Override
	public String getAuthType() {
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		
		if (cookies == null && getHeader("COOKIE") != null) {
			List<Cookie> lst = new ArrayList<Cookie>();
			String tmp = getHeader("COOKIE");
			StringTokenizer t = new StringTokenizer(tmp, ";");
			while (t.hasMoreTokens()) {
				String nextCookie = t.nextToken().trim();
				int equalsPos = nextCookie.indexOf('=');
				String cookieName = nextCookie.substring(0, equalsPos);
				String cookieValue = nextCookie.substring(equalsPos + 1);
				try {
					lst.add(new Cookie(cookieName, cookieValue));
				} catch (IllegalArgumentException e) {
				}
			}
			cookies = lst.toArray(new Cookie[0]);
		} else if (cookies == null) {
			cookies = new Cookie[0];
		}
		return cookies;
	}

	@Override
	public long getDateHeader(String name) {
		if (getHeader(name) == null) {
			return -1;
		}

		return DateUtils.parseDate(getHeader(name)).getTime();
	}

	@Override
	public String getHeader(String name) {
		return request.headers().get(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return new Vector<String>(request.headers().getAll(name)).elements();
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return new Vector<String>(request.headers().names()).elements();
	}

	@Override
	public int getIntHeader(String name) {
		return Integer.parseInt(request.headers().get(name));
	}

	@Override
	public String getMethod() {
		return request.method().name();
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getContextPath() {
		return context.getContextPath();
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return session.getId();
	}

	@Override
	public String getRequestURI() {
		return requestUri;
	}

	@Override
	public StringBuffer getRequestURL() {
		return new StringBuffer(requestUrl);
	}

	@Override
	public String getServletPath() {
		return servletPath;
	}

	@Override
	public HttpSession getSession(boolean create) {
		if (session == null && create) {
			session = HypersocketSessionFactory.getInstance().createSession(context);
		}
		return session;
	}

	@Override
	public HttpSession getSession() {
		return session;
	}

	public void setSession(HypersocketSession session) {
		this.session = session;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {

	}

	@Override
	public void logout() throws ServletException {

	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		return null;
	}
}
