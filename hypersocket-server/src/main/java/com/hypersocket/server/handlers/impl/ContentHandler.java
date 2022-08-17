package com.hypersocket.server.handlers.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ContentHandler {

	boolean handlesRequest(HttpServletRequest request);

	void handleHttpRequest(HttpServletRequest request, HttpServletResponse response) throws IOException;

	String getResourceName();

	InputStream getResourceStream(String path) throws FileNotFoundException;

	long getResourceLength(String path) throws FileNotFoundException;

	long getLastModified(String path) throws FileNotFoundException;

	int getResourceStatus(String path) throws RedirectException;

	void addAlias(String alias, String path);

	void addFilter(ContentFilter filter);

	void removeAlias(String string);

	void addDynamicPage(String path);

	void removeDynamicPage(String path);

	boolean hasAlias(String alias);

}
