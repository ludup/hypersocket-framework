package com.hypersocket.alert;

import com.hypersocket.events.CoreStartedEvent;
import com.hypersocket.triggers.TriggerResource;

public interface AlertService {

	<T> T processAlert(String resourceKey, 
			String alertKey, 
			int delay, 
			int threshold, 
			int timeout,
			AlertCallback<T> callback);

	void onStartup(CoreStartedEvent event);

	void registerDynamicEvent(TriggerResource trigger);

}
