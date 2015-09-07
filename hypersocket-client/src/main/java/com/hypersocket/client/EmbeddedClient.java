/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedClient extends HypersocketClient<Object> {

	static Logger log = LoggerFactory.getLogger(EmbeddedClient.class);
	
	public EmbeddedClient(HypersocketClientTransport transport)
			throws IOException {
		super(transport, Locale.getDefault());
	}

	@Override
	protected Map<String, String> showLogin(List<Prompt> prompts, int attempt, boolean success) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void showWarning(String msg) {
		log.warn(msg);
	}

	@Override
	public void showError(String msg) {
		log.error(msg);
	}

	@Override
	protected void onDisconnect() {
		
	}

	@Override
	protected void onDisconnecting() {
		
	}

	@Override
	protected void onConnected() {
		
	}


}
