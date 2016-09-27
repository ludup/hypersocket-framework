package com.hypersocket.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Close a stream when it reaches EOF.
 * @author lee
 *
 */
public class CloseOnEOFInputStream extends InputStream {

	InputStream in;
	
	
	public CloseOnEOFInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		int r = in.read();
		checkClose(r);
		return r;
	}
	
	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		int r = in.read(buf, off, len);
		checkClose(r);
		return r;
	}
	
	private void checkClose(int r) throws IOException {
		if(r==-1) {
			in.close();
		}
	}

}
