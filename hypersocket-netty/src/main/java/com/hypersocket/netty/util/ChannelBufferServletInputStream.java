/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.util;

import java.io.IOException;

import javax.servlet.ServletInputStream;

import org.jboss.netty.buffer.ChannelBuffer;

public class ChannelBufferServletInputStream extends ServletInputStream {

	ChannelBuffer content;
	
	public ChannelBufferServletInputStream(ChannelBuffer content) {
		this.content = content;
	}
	
	@Override
	public int read() throws IOException {
		if(content.readableBytes() > 0) {
			return (int)content.readByte();
		} else {
			return -1;
		}
	}
	
	public int read(byte[] buf, int off, int len) throws IOException {
		int readableBytes = content.readableBytes();
		int count = Math.min(readableBytes, len);
		if(count > 0) {
			content.readBytes(buf, off, count);
			return count;
		} else {
			return -1;
		}
	}
	
}
