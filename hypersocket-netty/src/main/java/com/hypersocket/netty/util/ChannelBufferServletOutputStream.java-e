/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.util;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;

public class ChannelBufferServletOutputStream extends ServletOutputStream {

	ChannelBufferOutputStream out;
	
	public ChannelBufferServletOutputStream(ChannelBuffer buffer) {
		out = new ChannelBufferOutputStream(buffer);
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}
	
	public void write(byte[] buf, int off, int len) throws IOException {
		out.write(buf, off, len);
	}

}
