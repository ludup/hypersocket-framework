package com.hypersocket.servlet.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Request {

	static ThreadLocal<HttpServletRequest> threadRequests = new ThreadLocal<HttpServletRequest>();
	static ThreadLocal<HttpServletResponse> threadResponses = new ThreadLocal<HttpServletResponse>();
	
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
}
