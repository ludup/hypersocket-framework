package com.hypersocket.server.json;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.server.HypersocketServer;
import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.HttpResponseProcessor;

@Component
public class DiscoverHandler extends HttpRequestHandler {

	@Autowired
	private HypersocketServer server; 
	
	public DiscoverHandler() {
		super("discover", Integer.MIN_VALUE);

	}

	@PostConstruct
	private void postConstruct() {
		server.registerHttpHandler(this);
	}
	
	@Override
	public boolean handlesRequest(HttpServletRequest request) {
		return request.getRequestURI().equals("/discover") || request.getRequestURI().equals("/discover/");
	}

	@Override
	public boolean getDisableCache() {
		return false;
	}

	@Override
	public void handleHttpRequest(HttpServletRequest request, HttpServletResponse response,
			HttpResponseProcessor responseProcessor) throws IOException {
		response.sendRedirect(server.resolvePath("api/server/discover"));
		responseProcessor.sendResponse(request, response);
	}

}
