package com.hypersocket.events;


public interface EventService {

	void registerEvent(Class<? extends SystemEvent> eventClass, String resourceBundle);

	void publishEvent(SystemEvent event);

	EventDefinition getEventDefinition(String resourceKey);

}
