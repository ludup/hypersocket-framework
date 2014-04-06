/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.websocket;

import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;


public interface WebSocketListener {

    public void onConnect(WebSocket client);

    public void onDisconnect(WebSocket client);

    public void onMessage(WebSocket client, WebSocketFrame frame);

    public void onError(Throwable t);
}
