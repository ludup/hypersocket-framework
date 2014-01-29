package com.hypersocket.server.handlers.impl;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

public interface ContentFilter {

	InputStream getFilterStream(InputStream resourceStream, HttpServletRequest request);

	boolean filtersPath(String path);

}
