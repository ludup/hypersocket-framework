package com.hypersocket.events;

import java.util.List;


public interface EventService {

	void registerEvent(Class<? extends SystemEvent> eventClass, String resourceBundle);

	void publishEvent(SystemEvent event);

	EventDefinition getEventDefinition(String resourceKey);

	List<EventDefinition> getEvents();

}
