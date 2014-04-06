/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty.websocket;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * A WebSocket client
 * Controls the basic features of a client.
 *
 * To get notified of events, please see {@link WebSocketListener}.
 *
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public interface WebSocket {
	
	public Integer getId();
	
	public void setAttachment(Object attachment);
	
	public Object getAttachment();
	
	public boolean isOpen();
	
    public ChannelFuture connect();

    public ChannelFuture disconnect();

    public ChannelFuture send(WebSocketFrame frame);
}
