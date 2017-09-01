package com.hypersocket.servlet.request;

import javax.servlet.http.HttpServletRequest;

public class Request {

	static ThreadLocal<HttpServletRequest> threadRequests = new ThreadLocal<HttpServletRequest>();
	
	public static void set(HttpServletRequest request) {
		threadRequests.set(request);
	}
	
	public static HttpServletRequest get() {
		return threadRequests.get();
	}
	
	public static boolean isAvailable() {
		return threadRequests.get()!=null;
	}
	
	public static void remove() {
		threadRequests.remove();
	}
}
