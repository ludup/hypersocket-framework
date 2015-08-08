package com.hypersocket.events;

import java.util.List;

public interface EventService  {

	void registerEvent(Class<? extends SystemEvent> eventClass,
			String resourceBundle,
			EventPropertyCollector propertyCollector);

	void registerEvent(Class<? extends SystemEvent> eventClass,
			String resourceBundle);

	void publishEvent(SystemEvent event);

	EventDefinition getEventDefinition(String resourceKey);

	List<EventDefinition> getEvents();

	void registerEventDefinition(EventDefinition def);

	void publishDelayedEvents();

	void delayEvents(Boolean val);

	void rollbackDelayedEvents(boolean fireFailedEvents);

}
