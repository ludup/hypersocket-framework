package com.hypersocket.events;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;

@Service
public class EventServiceImpl implements EventService {

	static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	Map<String, EventDefinition> eventDefinitions = new HashMap<String, EventDefinition>();

	public static final String RESOURCE_BUNDLE = "EventService";

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	I18NService i18nService;

	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}
	@Override
	public void registerEvent(Class<? extends SystemEvent> eventClass,
			String resourceBundle) {

		if (log.isInfoEnabled()) {
			log.info("Registering event class " + eventClass.getName());
		}

		try {
			String resourceKey = (String) eventClass.getField(
					"EVENT_RESOURCE_KEY").get(null);

			if (log.isInfoEnabled()) {
				log.info("Process event with resource key " + resourceKey);
			}

			checkResourceKey(resourceKey, resourceBundle);
			checkResourceKey(resourceKey + ".success", resourceBundle);
			checkResourceKey(resourceKey + ".failure", resourceBundle);

			eventDefinitions.put(resourceKey, new EventDefinition(
					resourceBundle, resourceKey));
			for (Field field : eventClass.getFields()) {
				if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC
						&& (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {

					if (field.getType().equals(String.class)
							&& field.getName().startsWith("ATTR_")) {
						try {
							String attributeName = (String) field.get(null);
							checkResourceKey(attributeName, resourceBundle);
							checkResourceKey(attributeName + ".info",
									resourceBundle);
							eventDefinitions.get(resourceKey)
									.getAttributeNames().add(attributeName);
							if (log.isInfoEnabled()) {
								log.info("Added attribute " + attributeName);
							}
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						}
					}
				}
			}
		} catch (Throwable t) {
			log.error("Failed to register event class " + eventClass.getName(),
					t);
		}

	}

	private boolean checkResourceKey(String resourceKey, String resourceBundle) {
		try {
			I18N.getResource(i18nService.getDefaultLocale(), resourceBundle,
					resourceKey);
			return true;
		} catch (Exception e) {

			try {
				I18N.getResource(i18nService.getDefaultLocale(),
						RESOURCE_BUNDLE, resourceKey);
				return true;
			} catch (Exception e2) {
				log.error("Missing resource key " + resourceBundle + "/"
						+ resourceKey);
				return false;
			}
		}
	}

	@Override
	public void publishEvent(SystemEvent event) {
		if (!eventDefinitions.containsKey(event.getResourceKey())) {
			if (log.isWarnEnabled()) {
				log.warn("The system is firing an unregistered event "
						+ event.getResourceKey()
						+ ". Register your event with EventService to remove this message.");
			}
		}
		
		if(!event.isSuccess()) {
			log.error(event.getResourceKey() + " failed", event.getException());
		}
		
		eventPublisher.publishEvent(event);
	}

	@Override
	public EventDefinition getEventDefinition(String resourceKey) {
		return eventDefinitions.get(resourceKey);
	}
}
