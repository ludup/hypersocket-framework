package com.hypersocket.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileDownloadProcessor {

	static Logger log = LoggerFactory.getLogger(FileDownloadProcessor.class);

	public static final String CONTENT_INPUTSTREAM = "ContentInputStream";

	HttpServletRequest request;
	HttpServletResponse response;
	File file;
	String protocol;

	static MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

	public FileDownloadProcessor(HttpServletRequest request,
			HttpServletResponse response, File file,
			String protocol) {
		this.request = request;
		this.response = response;
		this.file = file;
		this.protocol = protocol;
		
		startDownload();
	}

	public void startDownload() {

		try {

			long started = System.currentTimeMillis();

			downloadStarted(file, protocol);

			response.setContentType(mimeTypesMap.getContentType(file.getName()));

			response.setHeader("Content-disposition",
						"attachment; filename=" + file.getName());

			InputStream in = new FileInputStream(file);

			try {
				byte[] buf = new byte[65535];
				int r;
				long remaining = file.length();
				while ((r = in.read(buf, 0,
						(int) Math.min(buf.length, remaining))) > -1
						&& remaining > 0) {
					response.getOutputStream().write(buf, 0, r);
					remaining -= r;
				}

			} finally {
				in.close();
			}
			downloadComplete(file, file.length(),
					System.currentTimeMillis() - started, protocol);

		} catch (IOException e) {
			downloadFailed(file, e, protocol);
		}

	}

	protected abstract void downloadStarted(File file, String protocol);

	protected abstract void downloadComplete(File file, long length,
			long completed, String protocol);

	protected abstract void downloadFailed(File file, Throwable t,
			String protocol);
}
