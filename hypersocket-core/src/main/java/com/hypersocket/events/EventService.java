package com.hypersocket.events;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;

public interface EventService extends AuthenticatedService {

	void registerEvent(Class<? extends SystemEvent> eventClass,
			String resourceBundle,
			EventPropertyCollector propertyCollector);

	void registerEvent(Class<? extends SystemEvent> eventClass,
			String resourceBundle);

	void publishEvent(SystemEvent event);

	EventDefinition getEventDefinition(String resourceKey);

	List<EventDefinition> getEvents();

	void registerEventDefinition(EventDefinition def);

}
