/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.netty.util.ChannelBufferServletOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpResponseServletWrapper implements HttpServletResponse {

	static Logger LOG = LoggerFactory.getLogger(HttpResponseServletWrapper.class);

	private HttpResponse response;
	private String charset;
	private Locale locale;
	private boolean committed = false;
//	private int bufferSize = 65535;
//	private ByteBuf buffer;
	private ChannelBufferServletOutputStream out;
	private Channel channel;
	private HttpRequest request;
	private boolean closeOnComplete = false;
	private Date timestamp;

	private PrintWriter writer;

	public HttpResponseServletWrapper(HttpResponse response, Channel channel, HttpRequest request) {
		this.response = response;
		this.channel = channel;
		this.request = request;
		timestamp = new Date();
		reset();
	}

	public void stamp() {
		timestamp = new Date();
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public boolean isChunked() {
		return !(response instanceof FullHttpResponse);
	}

	public void setChunked(boolean chunked) {
		boolean was = isChunked(); 
		if (chunked != was) {
			HttpResponse old = this.response;
			if (chunked) {
				LOG.info("Switching to streamed response.");
//				if(((DefaultFullHttpResponse)response).content() != null && ((DefaultFullHttpResponse)response).content().readableBytes() > 0)
//					throw new IllegalStateException("Already have content.");
				response = new DefaultHttpResponse(response.protocolVersion(), response.status());
			} else {
				LOG.info("Switching to full response.");
				response = new DefaultFullHttpResponse(response.protocolVersion(), response.status());
			}
			response.headers().add(old.headers());
		}
	}

	public void reset() {
		charset = "ISO-8859-1";
//		buffer = Unpooled.buffer(65535);
//		out = new ChannelBufferServletOutputStream(buffer);
	}

	public HttpResponse getNettyResponse() {
		return response;
	}

	public int getAvailableContentSize() {
		if(isChunked())
			return -1;
		else
			return getContent().readableBytes();
	}

	public ByteBuf getContent() {
		if(isChunked())
			throw new IllegalStateException("Not buffered.");
		return ((FullHttpResponse)response).content();
	}

	@Override
	public String getCharacterEncoding() {
		return charset;
	}

	@Override
	public String getContentType() {
		String contentType = response.headers().get(HttpHeaders.CONTENT_TYPE);
		if (contentType != null && contentType.indexOf("charset=") == -1) {
			return contentType + "; charset=" + charset;
		}
		return contentType;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if(out == null) {
			out = new ChannelBufferServletOutputStream(getContent()) {
				@Override
				public void flush() throws IOException {
					super.flush();
					committed = true;
				}
			};
		}
		return out;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if(writer == null) {
			if(charset == null)
				writer = new PrintWriter(getOutputStream(), false, Charset.forName("ISO-8859-1"));
			else
				writer = new PrintWriter(getOutputStream(), false, Charset.forName(charset));
		}
		return writer;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		this.charset = charset;
	}

	@Override
	public void setContentLength(int len) {
		response.headers().set(HttpHeaders.CONTENT_LENGTH, String.valueOf(len));
	}

	@Override
	public void setContentType(String type) {
		response.headers().set(HttpHeaders.CONTENT_TYPE, type);
	}

	@Override
	public void setBufferSize(int size) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int getBufferSize() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void flushBuffer() throws IOException {
		// TODO can we do anything here?

	}

	@Override
	public void resetBuffer() {
		throw new UnsupportedOperationException("TODO");
//		buffer = Unpooled.buffer(bufferSize);
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
				cookieHeader.append(DateUtils.formatDate(
						new Date(System.currentTimeMillis() + cookie.getMaxAge() * 1000L), DateUtils.PATTERN_RFC1036));
			}
		}

		if (cookie.getSecure()) {
			cookieHeader.append("; Secure");
		}

		/**
		 * Make sure we are not adding duplicate cookies
		 */
		for (Entry<String, String> entry : response.headers()) {
			if (entry.getKey().equals("Set-Cookie") && entry.getValue().equals(cookieHeader.toString())) {
				return;
			}
		}
		addHeader("Set-Cookie", cookieHeader.toString());

	}

	@Override
	public boolean containsHeader(String name) {
		return response.headers().contains(name);
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

	public void sendRedirect(String location, boolean permanent) throws IOException {
		setStatus(permanent ? 301 : 302);
		setHeader(HttpHeaders.LOCATION, location);
	}

	@Override
	public void setDateHeader(String name, long date) {
		setHeader(name, DateUtils.formatDate(new Date(date)));
	}

	@Override
	public void addDateHeader(String name, long date) {
		response.headers().set(name, DateUtils.formatDate(new Date(date)));

	}

	@Override
	public void setHeader(String name, String value) {
		response.headers().set(name, value);
	}

	public void removeHeader(String name) {
		response.headers().remove(name);
	}

	@Override
	public void addHeader(String name, String value) {
		if (name.equalsIgnoreCase("content-type") && response.headers().contains("Content-Type")) {
			setHeader(name, value);
		} else {
			response.headers().add(name, value);
		}
	}

	@Override
	public void setIntHeader(String name, int value) {
		response.headers().set(name, String.valueOf(value));
	}

	@Override
	public void addIntHeader(String name, int value) {
		response.headers().add(name, String.valueOf(value));
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

	@Override
	public int getStatus() {
		return response.status() != null ? response.status().code() : 0;
	}

	@Override
	public String getHeader(String name) {
		return response.headers().get(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return response.headers().getAll(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return response.headers().names();
	}
}
