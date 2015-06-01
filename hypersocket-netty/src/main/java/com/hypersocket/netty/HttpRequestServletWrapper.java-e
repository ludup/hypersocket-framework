/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
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
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.DateUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.hypersocket.netty.util.ChannelBufferServletInputStream;
import com.hypersocket.servlet.HypersocketSession;
import com.hypersocket.servlet.HypersocketSessionFactory;

public class HttpRequestServletWrapper implements HttpServletRequest {

	HttpRequest request;
	Map<String, Object> attributes = new HashMap<String, Object>();
	Map<String,ArrayList<String>> parameters = new HashMap<String,ArrayList<String>>();
	String charset;
	String queryString;
	String pathInfo;
	String url;
	InetSocketAddress localAddress;
	InetSocketAddress remoteAddress;
	Cookie[] cookies;
	boolean secure;
	HttpSession session;
	ServletContext context;

	
	public HttpRequestServletWrapper(HttpRequest request,
			InetSocketAddress localAddress, 
			InetSocketAddress remoteAddress, 
			boolean secure,
			ServletContext context,
			HttpSession session) {
		this.request = request;
		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;
		this.url = (secure ? "https://" : "http://") + request.getHeader(HttpHeaders.HOST) + request.getUri();
		this.secure = secure;
		this.context = context;
		this.session = session;
		
		// Parse the URL
		int doubleSlashPos = url.lastIndexOf("://");
		int startPathPos = url.indexOf("/", doubleSlashPos + 1) + 1;

		int questionMarkPos = url.lastIndexOf('?');
		if (questionMarkPos == url.length() - 1) {
			queryString = "";
			if (startPathPos < questionMarkPos) {
				pathInfo = url.substring(startPathPos, questionMarkPos);
			}
		} else if (questionMarkPos != -1) {
			queryString = url.substring(questionMarkPos + 1);
			if (startPathPos < questionMarkPos) {
				pathInfo = url.substring(startPathPos, questionMarkPos);
			}
			processParameters(queryString);
		} else {
			queryString = null;
			if (startPathPos < url.length()) {
				pathInfo = url.substring(startPathPos);
			}
		}

		String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
		
		String contentTypeCharset = "UTF-8";
		int idx;
		if(contentType!=null) {
			
			if((idx = contentType.indexOf(';')) > -1) {
				String tmp = contentType.substring(idx+1);
				contentType = contentType.substring(0, idx);
				if((idx = tmp.indexOf("charset=")) > -1) {
					contentTypeCharset = tmp.substring(idx+8);
				}
			}
			
			if(contentType!=null && contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
				processParameters(request.getContent().toString(Charset.forName(contentTypeCharset)));
			}
		}
	}
	
	private void processParameters(String params) {
		StringTokenizer paramsParser = new StringTokenizer(params, "&");
		while (paramsParser.hasMoreTokens()) {
			String parameter = paramsParser.nextToken();
			int equalPos = parameter.indexOf('=');
			if (equalPos != -1) {
				try {
					String paramName = URLDecoder.decode(
							parameter.substring(0, equalPos), "UTF-8");
					String paramValue = "";
					if (equalPos != parameter.length() - 1) {
						paramValue = URLDecoder.decode(
								parameter.substring(equalPos + 1), "UTF-8");
					}
					addParameter(paramName, paramValue);
				} catch (UnsupportedEncodingException ex) {
				} catch (IllegalArgumentException iae) {
				}
			}
		}
	}

	void addParameter(String name, String value) {
		if(!parameters.containsKey(name)) {
			parameters.put(name, new ArrayList<String>());
		}
		parameters.get(name).add(value);
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
		} else if (request.getHeader(HttpHeaders.CONTENT_TYPE) == null) {
			return null;
		} else {
			int charsetPos = request.getHeader(HttpHeaders.CONTENT_TYPE)
					.indexOf("charset=");
			if (charsetPos == -1) {
				return "UTF-8";
			} else {
				return request.getHeader(HttpHeaders.CONTENT_TYPE).substring(
						charsetPos + 8);
			}
		}
	}

	@Override
	public void setCharacterEncoding(String charset)
			throws UnsupportedEncodingException {
		this.charset = charset;
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getContentLength() {
		return (int) request.getContentLength();
	}

	@Override
	public String getContentType() {
		return request.getHeader(HttpHeaders.CONTENT_TYPE);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new ChannelBufferServletInputStream(request.getContent());
	}

	public String getParameter(String name) {
		if(parameters.containsKey(name)) {
			return parameters.get(name).get(0);
		} else {
			return null;
		}
	}

	public Enumeration<String> getParameterNames() {
		return new Vector<String>(parameters.keySet()).elements();
	}

	public String[] getParameterValues(String name) {
		if(parameters.containsKey(name)) {
		return parameters.get(name).toArray(new String[0]);
		} else {
			return null;
		}
	}

	public Map<String,String[]> getParameterMap() {
		HashMap<String,String[]> tmp = new HashMap<String,String[]>();
		for(Map.Entry<String, ArrayList<String>> entry : parameters.entrySet()) {
			tmp.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
		}
		return tmp;
	}


	@Override
	public String getProtocol() {
		return url.startsWith("https") ? "https" : "http";
	}

	@Override
	public String getScheme() {
		return secure ? "https" : "http";
	}

	@Override
	public String getServerName() {
		String host = getHeader(HttpHeaders.HOST);
		int idx;
		if((idx = host.indexOf(":")) > -1) {
			host = host.substring(0, idx);
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
		throw new UnsupportedOperationException();
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
	         String tmp = getHeader("COOKIE");
	         StringTokenizer t = new StringTokenizer(tmp, ";");
	         cookies = new Cookie[t.countTokens()];
	         int count = 0;
	         while (t.hasMoreTokens()) {
	            String nextCookie = t.nextToken().trim();
	            int equalsPos = nextCookie.indexOf('=');
	            String cookieName = nextCookie.substring(0, equalsPos);
	            String cookieValue = nextCookie.substring(equalsPos + 1);
	            cookies[count++] = new Cookie(cookieName, cookieValue);
	         }
	      } else if (cookies == null) {
	         cookies = new Cookie[0];
	      }
	      return cookies;
	}

	@Override
	public long getDateHeader(String name) {
		if(getHeader(name)==null) {
			return -1;
		}
		
		return DateUtils.parseDate(getHeader(name)).getTime();
	}

	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return new Vector<String>(request.getHeaders(name)).elements();
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return new Vector<String>(request.getHeaderNames()).elements();
	}

	@Override
	public int getIntHeader(String name) {
		return Integer.parseInt(request.getHeader(name));
	}

	@Override
	public String getMethod() {
		return request.getMethod().getName();
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
		context.getContextPath();
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
		String uri = request.getUri();
		if (url.indexOf('?') == -1) {
	         return uri;
	      } else {
	         return uri.substring(0, uri.indexOf('?'));
	      }
	}

	@Override
	public StringBuffer getRequestURL() {
		if (url.indexOf('?') == -1) {
	         return new StringBuffer(url);
	      } else {
	         return new StringBuffer(url.substring(0, url.indexOf('?')));
	      }
	}

	@Override
	public String getServletPath() {
		return "";
	}

	@Override
	public HttpSession getSession(boolean create) {
		if(session==null && create) {
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

}
