package com.hypersocket.server.handlers.impl;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.hypersocket.utils.ITokenResolver;

public interface ContentFilter {

	InputStream getFilterStream(InputStream resourceStream, HttpServletRequest request) throws RedirectException;

	boolean filtersPath(String path);
	
	Integer getWeight();

	List<ITokenResolver> getResolvers(HttpServletRequest request);

	InputStream getFilterStream(InputStream resourceStream, HttpServletRequest request,
			ITokenResolver... additionalResolvers) throws RedirectException;

}
