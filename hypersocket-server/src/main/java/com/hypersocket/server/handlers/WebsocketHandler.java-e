/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.server.websocket.WebsocketClientCallback;

public interface WebsocketHandler {

	public boolean handlesRequest(HttpServletRequest request);
	
	public void acceptWebsocket(HttpServletRequest request, 
			HttpServletResponse nettyResponse, 
			WebsocketClientCallback callback, 
			HttpResponseProcessor processor) throws UnauthorizedException, AccessDeniedException;
}
