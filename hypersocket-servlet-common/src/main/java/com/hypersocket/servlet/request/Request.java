package com.hypersocket.servlet.request;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
		return threadRequests.get() != null;
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
	
	public static void cleanSessionOnLogin() {
		if (isAvailable()) {
			var session = get().getSession(false);
			if (session != null) {
				var copy = copyCurrentSessionAttributes(session);
				session.invalidate();
				session = get().getSession(true);
				if (copy != null) {
					copy.forEach(session::setAttribute);
				}
			}
		}
	}
	
	private static Map<String, Object> copyCurrentSessionAttributes(HttpSession session) {
		var attributesToCopy = new HashMap<String, Object>();
		var enumeration = session.getAttributeNames();
		while (enumeration.hasMoreElements()) {
			var key = enumeration.nextElement();
			attributesToCopy.put(key, session.getAttribute(key));
		}
		return attributesToCopy;
	}
}
