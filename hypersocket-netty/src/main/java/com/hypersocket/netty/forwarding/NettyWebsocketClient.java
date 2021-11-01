package com.hypersocket.netty.forwarding;

import com.hypersocket.server.websocket.WebsocketClient;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;

public interface NettyWebsocketClient extends WebsocketClient {
	
	public static final AttributeKey<NettyWebsocketClient> WEBSOCKET_CLIENT = AttributeKey.newInstance(NettyWebsocketClient.class.getSimpleName());

	void setWebsocketChannel(Channel channel);

	void frameReceived(WebSocketFrame msg);
}
