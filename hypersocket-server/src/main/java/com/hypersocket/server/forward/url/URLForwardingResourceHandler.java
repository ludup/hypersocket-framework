package com.hypersocket.server.forward.url;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.server.forward.AbstractForwardingHandler;
import com.hypersocket.server.forward.ForwardingService;
import com.hypersocket.server.forward.url.events.URLForwardingResourceSessionClosed;
import com.hypersocket.server.forward.url.events.URLForwardingResourceSessionOpened;
import com.hypersocket.session.Session;

@Component
public class URLForwardingResourceHandler extends
		AbstractForwardingHandler<URLForwardingResource> {

	@Autowired
	URLForwardingResourceService websiteService;

	@Autowired
	EventService eventService;

	@Autowired
	HypersocketServer server;

	public URLForwardingResourceHandler() {
		super("url");
	}

	@PostConstruct
	private void postConstruct() {
		server.registerWebsocketpHandler(this);
	}

	@Override
	protected ForwardingService<URLForwardingResource> getService() {
		return websiteService;
	}

	@Override
	protected void fireResourceOpenSuccessEvent(Session session,
			URLForwardingResource resource, String hostname, Integer port) {
		eventService.publishEvent(new URLForwardingResourceSessionOpened(this, true,
				resource, session));
	}

	@Override
	protected void fireResourceSessionOpenFailedEvent(Throwable cause,
			Session session, URLForwardingResource resource, String hostname,
			Integer port) {
		eventService.publishEvent(new URLForwardingResourceSessionOpened(this, cause,
				resource, session, hostname));
	}

	@Override
	protected void fireResourceSessionClosedEvent(URLForwardingResource resource,
			Session session, String hostname, Integer port, long totalBytesIn,
			long totalBytesOut) {
		eventService.publishEvent(new URLForwardingResourceSessionClosed(this,
				resource, session, totalBytesIn, totalBytesOut));
	}

}
