package com.hypersocket.netty;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.servlet.ServletInputStream;

import io.netty.handler.codec.http.HttpContent;

public class HttpRequestChunkStream extends ServletInputStream {

	private HttpContent currentChunk;
	private boolean isEOF = false;
	
	@Override
	public int read() throws IOException {
		
		byte[] tmp = new byte[1];
		int r = read(tmp);
		if(r > 0) {
			return tmp[0] & 0xFF;
		} else if(r==0) {
			throw new IOException("Unexpected zero bytes read");
		} else {
			return -1;
		}
	}
	
	@Override
	public synchronized int read(byte[] buf, int off, int len) throws IOException {
		
		long started = System.currentTimeMillis();
		while(!isEOF && currentChunk==null) {
			try {
				wait(1000); // TODO from settings add timeout processing
			} catch (InterruptedException e) {
				throw new InterruptedIOException("Interupted whilst waiting for next HTTP request chunk");
			}
			if((System.currentTimeMillis() - started) > 120000) {
				throw new IOException("Timeout waiting for next HTTP chunk");
			}
		}
		
		if(isEOF) {
			return -1;
		}
		
		int count = Math.min(len, currentChunk.content().readableBytes());
		currentChunk.content().readBytes(buf, off, count);
		
		if(currentChunk.content().readableBytes()==0) {
			currentChunk = null;
			notify();
		}
		
		return count;
	}
	
	synchronized void setCurrentChunk(HttpContent currentChunk) throws InterruptedIOException {
		
		while(!isEOF && this.currentChunk!=null) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				throw new InterruptedIOException("Interupted whilst waiting for next HTTP request chunk");
			}
		}
		if(currentChunk == null || currentChunk.content().readableBytes()==0) {
			isEOF = true;
		} else {
			this.currentChunk = currentChunk;
		}
		
		notify();
	}

}
