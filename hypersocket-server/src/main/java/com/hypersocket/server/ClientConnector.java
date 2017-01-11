package com.hypersocket.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.hypersocket.server.websocket.WebsocketClientCallback;

public interface ClientConnector {

	Integer getWeight();

	boolean handlesConnect(InetSocketAddress addr);

	void connect(InetSocketAddress addr, WebsocketClientCallback callback) throws IOException;

}
