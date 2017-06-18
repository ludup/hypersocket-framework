package com.hypersocket.alert;

public interface AlertService {

	<T> T processAlert(String resourceKey, 
			String alertKey, 
			int delay, 
			int threshold, 
			int timeout,
			AlertCallback<T> callback);

}
