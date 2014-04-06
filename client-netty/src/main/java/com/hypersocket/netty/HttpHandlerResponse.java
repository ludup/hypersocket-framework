/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class HttpHandlerResponse {

	HttpResponse response;
	ChannelBuffer content;
	
	public HttpHandlerResponse(HttpResponse response) {
		this.response = response;
	}
	
	public boolean isChunked() {
		return response.isChunked();
	}
	
	public void addChunk(HttpChunk chunk) {
		if(content==null) {
			content = ChannelBuffers.dynamicBuffer();
		}
		content.writeBytes(chunk.getContent());
	}
	
	public ChannelBuffer getContent() {
		if(content==null) {
			return response.getContent();
		}
		return content;
	}

	public String getHeader(String name) {
		return response.getHeader(name);
	}

	public int getStatusCode() {
		return response.getStatus().getCode();
	}
	
	public String getStatusText() {
		return response.getStatus().getReasonPhrase();
	}

	public Set<String> getHeaderNames() {
		return response.getHeaderNames();
	}
	
	public List<Entry<String,String>> getHeaders() {
		return response.getHeaders();
	}
	
}
