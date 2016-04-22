package com.hypersocket.netty.forwarding;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;

import com.hypersocket.server.websocket.WebsocketClient;

public interface NettyWebsocketClient extends WebsocketClient {

	void setWebsocketChannel(Channel channel);

	void frameReceived(WebSocketFrame msg);
}
