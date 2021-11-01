/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.util;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

public class ChannelBufferServletOutputStream extends ServletOutputStream {

	private ByteBufOutputStream out;
	
	public ChannelBufferServletOutputStream(ByteBuf buffer) {
		out = new ByteBufOutputStream(buffer);
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}
	
	public void write(byte[] buf, int off, int len) throws IOException {
		out.write(buf, off, len);
	}

}
