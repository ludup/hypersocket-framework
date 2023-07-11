package com.hypersocket.remoteservices;

public interface RemoteService {

	void setIdentificationProvider(RemoteServiceIdentificationProvider idProvider);
	
	RemoteServiceIdentificationProvider getIdentificationProvider();
}
