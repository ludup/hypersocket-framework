package com.hypersocket.client;

public interface HypersocketClientListener<T> {

	void connectStarted(HypersocketClient<T> client);
	
	void connectFailed(HypersocketClient<T> client);
	
	void connected(HypersocketClient<T> client);
	
	void disconnected(HypersocketClient<T> client, boolean onError);

}
