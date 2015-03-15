package com.hypersocket.netty;

import java.io.InputStream;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.stream.ChunkedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.utils.FileUtils;

public class HttpChunkStream implements ChunkedInput {

	static Logger log = LoggerFactory.getLogger(HttpChunkStream.class);
	
	private static final int CHUNK_SIZE = 65535;
	boolean eof = false;

	InputStream data;
	String uri;
	long position;
	
	HttpChunkStream(InputStream data, String uri) {
		this.data = data;
		this.uri = uri;
	}

	@Override
	public Object nextChunk() throws Exception {
	
		byte[] buf = new byte[CHUNK_SIZE];
		
		if (eof)
			return null;
		int b = data.read(buf);
		if (b == -1) {
			if(log.isDebugEnabled()) {
				log.debug("Stream is EOF for " + uri);
			}
			eof = true;
			return new DefaultHttpChunk(ChannelBuffers.EMPTY_BUFFER);
		}
		if(log.isDebugEnabled()) {
			log.debug("Returning chunk of " + b + " bytes at position " + position + " for " + uri);
		}
		position += b;
		DefaultHttpChunk c = new DefaultHttpChunk(ChannelBuffers.wrappedBuffer(
				buf, 0, b));
		return c;
	}

	@Override
	public boolean isEndOfInput() throws Exception {
		return eof;
	}

	@Override
	public boolean hasNextChunk() throws Exception {
		return isEndOfInput() == false;
	}

	@Override
	public void close() throws Exception {
		try {
			if(log.isDebugEnabled()) {
				log.debug("Closing HttpChunkStream for " + uri);
			}
			FileUtils.closeQuietly(data);
		} catch (Exception e) {
		}
	}
}
