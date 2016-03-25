package com.hypersocket.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PipedOperationOutputStream extends OutputStream implements Runnable {

static Logger log = LoggerFactory.getLogger(PipedOperationOutputStream.class);
	
	PipedOutputStream out = new PipedOutputStream();
	PipedInputStream in = new PipedInputStream(out, 1024000);
	OutputStream source;
	boolean operationComplete = false;
	boolean started = false;
	
	public PipedOperationOutputStream(OutputStream source) throws IOException {
		this.source = source;
	}

	protected InputStream getInputStream() {
		return in;
	}
	
	protected OutputStream getOutputStream() {
		return source;
	}
	
	@Override
	public void write(int b) throws IOException {
		if(!started) {
			startProcessing();
		}
		out.write(b);
	}
	
	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		if(!started) {
			startProcessing();
		}
		out.write(buf, off, len);
	}

	@Override
	public void close() throws IOException {
		waitForClose();
	}
	
	public abstract void processInput() throws IOException;
	
	public synchronized void run() {
		
		/**
		 * IMPORTANT.. NEEDS REFACTORING TO VIRTUAL FILE SYSTEM
		 */
		try {
			processInput();
		} catch(IOException ex) {
			log.error("Excepton during piped input operation", ex);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(source);
		}
		
		operationComplete = true;
		notify();
	}
	
	protected synchronized void startProcessing() {
		Thread t = new Thread(this, "ProcessingStream");
		started = true;
		t.start();
	}
	
	private synchronized void waitForClose() {
		
		try {
			while(!operationComplete) {
				wait(500);
			}
		} catch (InterruptedException e) {
			log.error("Interrupted whilst waiting for processing operation to complete");
		}
	}
}
