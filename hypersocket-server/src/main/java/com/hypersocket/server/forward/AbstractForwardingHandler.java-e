/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.forward;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.UserVariableReplacement;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.server.handlers.HttpResponseProcessor;
import com.hypersocket.server.handlers.WebsocketHandler;
import com.hypersocket.server.websocket.TCPForwardingClientCallback;
import com.hypersocket.server.websocket.WebsocketClient;
import com.hypersocket.server.websocket.WebsocketClientCallback;
import com.hypersocket.session.ResourceSession;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;
import com.hypersocket.session.json.SessionUtils;

public abstract class AbstractForwardingHandler<T extends ForwardingResource> implements WebsocketHandler {

	static Logger log = LoggerFactory.getLogger(AbstractForwardingHandler.class);

	@Autowired
	SessionService sessionService;

	@Autowired
	SessionUtils sessionUtils;

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	HypersocketServer server;

	@Autowired
	EventService eventService;

	@Autowired
	UserVariableReplacement userVariableReplacement;
	
	String path;
	
	public AbstractForwardingHandler(String path) {
		this.path = path;
	}

	@Override
	public boolean handlesRequest(HttpServletRequest request) {
		return request.getRequestURI().startsWith(server.resolvePath(path));
	}
	
	protected abstract ForwardingService<T> getService();

	@Override
	public void acceptWebsocket(HttpServletRequest request,
			HttpServletResponse response, WebsocketClientCallback callback,
			HttpResponseProcessor processor) throws UnauthorizedException,
			AccessDeniedException {

		if (!sessionService.isLoggedOn(sessionUtils.getActiveSession(request),
				true)) {
			throw new UnauthorizedException();
		}

		Session session = sessionUtils.getActiveSession(request);

		getService().setCurrentSession(session,
				sessionUtils.getLocale(request));

		try {
			Long resourceId = Long
					.parseLong(request.getParameter("resourceId"));

			Integer port = Integer.parseInt(request.getParameter("port"));

			T resource = getService().getResourceById(resourceId);
			
			String hostname = resource.getDestinationHostname();
			if(StringUtils.isBlank(hostname)) {
				hostname = resource.getHostname();
			}
		
			hostname = userVariableReplacement.replaceVariables(session.getCurrentPrincipal(), hostname);
			
			getService().verifyResourceSession(
					resource, hostname, port, ForwardingTransport.TCP, session);

			server.connect(new TCPForwardingHandlerCallback(callback, session,
					resource, hostname, port));

		} catch(AccessDeniedException ex) { 
			// TODO Log event
			log.error("Cannot accept tunnel", ex);
			throw ex;
		} catch (ResourceNotFoundException e) {
			// TODO Log event
			log.error("Cannot find resource", e);
			throw new AccessDeniedException("Resource not found");
		} finally {
			getService().clearPrincipalContext();
		}
	}
	
	protected abstract void fireResourceOpenSuccessEvent(Session session, T resource,
			String hostname, Integer port);

	protected abstract void fireResourceSessionOpenFailedEvent(Throwable cause,
			Session session, T resource, String hostname, Integer port);
	
	protected abstract void fireResourceSessionClosedEvent(T resource, Session session,
			String hostname, Integer port, long totalBytesIn, long totalBytesOut);
	
	class TCPForwardingHandlerCallback implements TCPForwardingClientCallback {

		WebsocketClientCallback callback;
		T resource;
		String hostname;
		Session session;
		Integer port;
		ResourceSession<T> resourceSession;
		
		TCPForwardingHandlerCallback(WebsocketClientCallback callback,
				Session session, T resource, String hostname, Integer port) {
			this.callback = callback;
			this.resource = resource;
			this.hostname = hostname;
			this.session = session;
			this.port = port;
		}

		@Override
		public void websocketAccepted(final WebsocketClient client) {

			callback.websocketAccepted(client);

			if(!sessionService.hasResourceSession(session, resource)) {
				fireResourceOpenSuccessEvent(session, resource, hostname, port);
			}
		
			resourceSession = new ResourceSession<T>() {
				@Override
				public void close() {
					client.close();
				}
				@Override
				public T getResource() {
					return resource;
				}
			};
			
			sessionService.registerResourceSession(session, resourceSession);
		}

		@Override
		public void websocketRejected(Throwable cause) {

			callback.websocketRejected(cause);

			fireResourceSessionOpenFailedEvent(cause, session, resource, hostname, port);
			
		}

		@Override
		public void websocketClosed(WebsocketClient client) {

			callback.websocketClosed(client);
			
			sessionService.unregisterResourceSession(session, resourceSession);
			
			if(!sessionService.hasResourceSession(session, resource)) {
				fireResourceSessionClosedEvent(resource, session, hostname, port, client
								.getTotalBytesIn(), client.getTotalBytesOut());
			}
		}

		public int getPort() {
			return port;
		}

		public String getHostname() {
			return hostname;
		}
	}


}
