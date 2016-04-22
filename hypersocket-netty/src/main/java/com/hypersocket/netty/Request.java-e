package com.hypersocket.netty;

import javax.servlet.http.HttpServletRequest;

public class Request {

	static ThreadLocal<HttpServletRequest> threadRequests = new ThreadLocal<HttpServletRequest>();
	
	static void set(HttpServletRequest request) {
		threadRequests.set(request);
	}
	
	public static HttpServletRequest get() {
		return threadRequests.get();
	}
	
	static void remove() {
		threadRequests.remove();
	}
}
