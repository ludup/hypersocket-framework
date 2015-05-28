/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.DateUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.hypersocket.netty.util.ChannelBufferServletOutputStream;

public class HttpResponseServletWrapper implements HttpServletResponse {

	HttpResponse response;
	String charset;
	Locale locale;
	boolean committed = false;
	int bufferSize = 65535;
	ChannelBuffer buffer;
	ChannelBufferServletOutputStream out;
	Channel channel;
	HttpRequest request;
	boolean closeOnComplete = false;
	
	public HttpResponseServletWrapper(HttpResponse response, Channel channel, HttpRequest request) {
		this.response = response;
		this.channel = channel;
		this.request = request;
		reset();
	}
	
	public void setChunked(boolean chunked) {
		response.setChunked(chunked);
	}

	public void reset() {
		charset = "ISO-8859-1";
		buffer = ChannelBuffers.dynamicBuffer(65535);
		out = new ChannelBufferServletOutputStream(buffer);
	}
	
	public HttpResponse getNettyResponse() {
		return response;
	}
	
	public ChannelBuffer getContent() {
		return buffer;
	}

	@Override
	public String getCharacterEncoding() {
		return charset;
	}

	@Override
	public String getContentType() {
		String contentType = response.getHeader(HttpHeaders.CONTENT_TYPE);
		if (contentType != null
				&& contentType.indexOf("charset=") == -1) {
			return contentType + "; charset=" + charset;
		}
		return contentType;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(out);
	}

	@Override
	public void setCharacterEncoding(String charset) {
		this.charset = charset;
	}

	@Override
	public void setContentLength(int len) {
		response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(len));
	}

	@Override
	public void setContentType(String type) {
		response.setHeader(HttpHeaders.CONTENT_TYPE, type);
	}

	@Override
	public void setBufferSize(int size) {
		this.bufferSize = size;
	}

	@Override
	public int getBufferSize() {
		return bufferSize;
	}

	@Override
	public void flushBuffer() throws IOException {
		// TODO can we do anything here?

	}

	@Override
	public void resetBuffer() {
		buffer = ChannelBuffers.dynamicBuffer(bufferSize);
	}

	@Override
	public boolean isCommitted() {
		return committed;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public void addCookie(Cookie cookie) {

		StringBuffer cookieHeader = new StringBuffer();

		cookieHeader.append(cookie.getName());
		cookieHeader.append("=");
		cookieHeader.append(cookie.getValue());
		if (cookie.getPath() != null) {
			cookieHeader.append("; Path=");
			cookieHeader.append(cookie.getPath());
		}
		if (cookie.getDomain() != null) {
			cookieHeader.append("; Domain=");
			cookieHeader.append(cookie.getDomain());
		}
		if (cookie.getMaxAge() > 0) {
			cookieHeader.append("; Max-Age=");
			cookieHeader.append(cookie.getMaxAge());
			/**
			 * This breaks IE when date of server and browser do not match
			 */
			cookieHeader.append("; Expires=");
			if (cookie.getMaxAge() == 0) {
				cookieHeader.append(DateUtils.formatDate(new Date(10000), DateUtils.PATTERN_RFC1036));
			} else {
				cookieHeader.append(DateUtils.formatDate(new Date(System
						.currentTimeMillis() + cookie.getMaxAge() * 1000L), DateUtils.PATTERN_RFC1036));
			}
		}
		
		if (cookie.getSecure()) {
			cookieHeader.append("; Secure");
		}

		/**
		 * Make sure we are not adding duplicate cookies
		 */
		for(Entry<String,String> entry : response.getHeaders()) {
			if(entry.getKey().equals("Set-Cookie") && entry.getValue().equals(cookieHeader.toString())) {
				return;
			}
		}
		addHeader("Set-Cookie", cookieHeader.toString());
		
	}

	@Override
	public boolean containsHeader(String name) {
		return response.containsHeader(name);
	}

	@Override
	public String encodeURL(String url) {
		return url;
	}

	@Override
	public String encodeRedirectURL(String url) {
		return url;
	}

	@Override
	public String encodeUrl(String url) {
		return url;
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return url;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		setStatus(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		setStatus(sc);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		setStatus(302);
	    setHeader(HttpHeaders.LOCATION, location);
	}

	@Override
	public void setDateHeader(String name, long date) {
		setHeader(name, DateUtils.formatDate(new Date(date)));
	}

	@Override
	public void addDateHeader(String name, long date) {
		response.setHeader(name, DateUtils.formatDate(new Date(date)));

	}

	@Override
	public void setHeader(String name, String value) {
		response.setHeader(name, value);
	}
	
	public void removeHeader(String name) {
		response.removeHeader(name);
	}

	@Override
	public void addHeader(String name, String value) {
		if(name.equalsIgnoreCase("content-type") && response.containsHeader("Content-Type")) {
			setHeader(name, value);
		} else {
			response.addHeader(name, value);
		}
	}

	@Override
	public void setIntHeader(String name, int value) {
		response.setHeader(name, String.valueOf(value));
	}

	@Override
	public void addIntHeader(String name, int value) {
		response.addHeader(name, String.valueOf(value));
	}

	@Override
	public void setStatus(int sc) {
		response.setStatus(HttpResponseStatus.valueOf(sc));
	}

	@Override
	public void setStatus(int sc, String msg) {
		response.setStatus(HttpResponseStatus.valueOf(sc));
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	public HttpRequest getRequest() {
		return request;
	}

	public void setCloseOnComplete(boolean closeOnComplete) {
		this.closeOnComplete = closeOnComplete;
	}
	
	public boolean isCloseOnComplete() {
		return closeOnComplete;
	}


}
