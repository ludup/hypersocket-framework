package com.hypersocket.servlet.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Request {

	private static ThreadLocal<HttpServletRequest> threadRequests = new ThreadLocal<HttpServletRequest>();
	private static ThreadLocal<HttpServletResponse> threadResponses = new ThreadLocal<HttpServletResponse>();
	
	public static void set(HttpServletRequest request, HttpServletResponse response) {
		threadRequests.set(request);
		threadResponses.set(response);
	}
	
	public static HttpServletRequest get() {
		return threadRequests.get();
	}
	
	public static HttpServletResponse response() {
		return threadResponses.get();
	}
	
	public static boolean isAvailable() {
		return threadRequests.get()!=null;
	}
	
	public static void remove() {
		threadRequests.remove();
		threadResponses.remove();
	}

	public static String generateBaseUrl(HttpServletRequest request) {
		StringBuffer b = new StringBuffer();
		b.append(request.getProtocol());
		b.append("://");
		String host = request.getHeader("X-Forwarded-Host");
		if(host == null || host.length() == 0)
			host = request.getHeader("Host");
		b.append(host);
		return b.toString();
	}
}
