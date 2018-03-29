package com.hypersocket.alert;

import org.springframework.context.event.ContextStartedEvent;

import com.hypersocket.triggers.TriggerResource;

public interface AlertService {

	<T> T processAlert(String resourceKey, 
			String alertKey, 
			int delay, 
			int threshold, 
			int timeout,
			AlertCallback<T> callback);

	void onStartup(ContextStartedEvent event);

	void registerDynamicEvent(TriggerResource trigger);

}
