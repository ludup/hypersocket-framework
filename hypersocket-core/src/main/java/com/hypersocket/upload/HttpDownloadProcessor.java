package com.hypersocket.upload;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.fs.ContentInputStream;
import com.hypersocket.fs.DownloadEventProcessor;
import com.hypersocket.fs.DownloadProcessor;
import com.hypersocket.fs.FileResource;
import com.hypersocket.session.Session;

public class HttpDownloadProcessor implements DownloadProcessor {

	static Logger log = LoggerFactory.getLogger(HttpDownloadProcessor.class);

	public static final String CONTENT_INPUTSTREAM = "ContentInputStream";

	HttpServletRequest request;
	HttpServletResponse response;
	long start;
	long length;
	String protocol;
	Session session;

	static MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

	public HttpDownloadProcessor(HttpServletRequest request,
			HttpServletResponse response, long start, long length, String protocol, Session session) {
		this.request = request;
		this.response = response;
		this.start = start;
		this.length = length;
		this.protocol = protocol;
		this.session = session;
	}

	@Override
	public void startDownload(FileResource resource, String childPath,
			FileObject file, DownloadEventProcessor downloadEventProcessor) {

		try {
			
			long started = downloadEventProcessor.downloadStarted(resource, childPath, file, protocol);
			
			response.setContentType(mimeTypesMap.getContentType(file.getName()
					.getBaseName()));

			if ("true".equals(request.getParameter("forceDownload"))) {
				response.setHeader("Content-disposition",
						"attachment; filename=" + file.getName().getBaseName());
			}

			if (file.getContent().getSize() <= 1024000 && length <= 1024000) {

				InputStream in = new BufferedInputStream(file.getContent().getInputStream());
				if (start > 0) {
					in.skip(start);
				}
				byte[] buf = new byte[65535];
				int r;
				long remaining = length;
				while ((r = in.read(buf, 0,
						(int) Math.min(buf.length, remaining))) > -1
						&& remaining > 0) {
					response.getOutputStream().write(buf, 0, r);
					remaining -= r;
				}

				downloadEventProcessor.downloadComplete(resource, childPath,
						file, length, System.currentTimeMillis() - started, protocol, session);

			} else {
				request.setAttribute(CONTENT_INPUTSTREAM,
						new ContentInputStream(resource, childPath, file,
								start, length, downloadEventProcessor, started, protocol, session));
			}
		} catch (IOException e) {
			downloadEventProcessor.downloadFailed(resource, childPath, file, e, protocol, session);
		}

	}

}
