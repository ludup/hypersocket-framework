/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HttpResponseProcessor {

	void sendResponse(HttpServletRequest request, HttpServletResponse response, boolean chunked);

	void send404(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException;

	void send500(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
