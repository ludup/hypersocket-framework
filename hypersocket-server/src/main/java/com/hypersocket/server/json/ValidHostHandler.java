package com.hypersocket.server.json;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.HttpResponseProcessor;

@Component
public class ValidHostHandler extends HttpRequestHandler {

	@Autowired
	HypersocketServer server; 
	
	@Autowired
	RealmService realmService; 
	
	@Autowired
	SystemConfigurationService systemConfigurationService; 
	
	public ValidHostHandler() {
		super("host", Integer.MIN_VALUE);

	}

	@PostConstruct
	private void postConstruct() {
		server.registerHttpHandler(this);
	}
	
	@Override
	public boolean handlesRequest(HttpServletRequest request) {
		Boolean result = (Boolean) request.getSession().getAttribute("validatedHost");
		if(result == null 
				&& systemConfigurationService.getBooleanValue("auth.validateHost") 
				&& !(request.getServerName().equals("localhost") || request.getServerName().equals("127.0.0.1"))) {
			result = realmService.getRealmByHost(request.getServerName(), null) == null;
			request.getSession().setAttribute("validatedHost", result);
			return result;
		} else {
			return result==null ? Boolean.FALSE : result;
		}
	}

	@Override
	public boolean getDisableCache() {
		return false;
	}

	@Override
	public void handleHttpRequest(HttpServletRequest request, HttpServletResponse response,
			HttpResponseProcessor responseProcessor) throws IOException {
		response.setStatus(HttpStatus.SC_NOT_FOUND);
		responseProcessor.sendResponse(request, response, false);
	}

}
