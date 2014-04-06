package com.hypersocket.client.rmi;

public interface ConnectionStatus {

	final static int DISCONNECTED = 0;
	final static int CONNECTING = 1;
	final static int CONNECTED = 2;
	Connection getConnection();
	
	int getStatus();
}
