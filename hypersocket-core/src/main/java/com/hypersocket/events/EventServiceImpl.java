package com.hypersocket.events;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationChangedEvent;
import com.hypersocket.config.ConfigurationServiceImpl;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.i18n.Message;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.resource.AssignableResourceEvent;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.util.BufferedSerializer;

@Service
public class EventServiceImpl implements EventService {

	static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	Map<String, EventDefinition> eventDefinitions = new HashMap<String, EventDefinition>();
	Set<String> attributeNames = new TreeSet<String>();
	public static final String RESOURCE_BUNDLE = "EventService";

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private I18NService i18nService;

	private ThreadLocal<List<Boolean>> isDelayingEvents = new ThreadLocal<List<Boolean>>();
	private ThreadLocal<BufferedSerializer<SystemEvent>> delayedEvents = new ThreadLocal<BufferedSerializer<SystemEvent>>();
	private ThreadLocal<SystemEvent> lastResult = new ThreadLocal<SystemEvent>();
	private List<EventExtender> extenders = new ArrayList<EventExtender>();
	private List<Runnable> publishCallbacks = Collections.synchronizedList(new ArrayList<>());
	private Set<String> dynamicEvents = new HashSet<>();
	
	@Override
	public void undelayEvents() {
		List<Boolean> was = isDelayingEvents.get();
		if(was == null)
			throw new IllegalStateException("Not delaying events.");
		was.remove(was.size() - 1);
		if(was.isEmpty())
			isDelayingEvents.remove();
	}
	
	@Override
	public void delayEvents() {
		List<Boolean> was = isDelayingEvents.get();
		if(was == null)
			isDelayingEvents.set(new LinkedList<>(Arrays.asList(Boolean.TRUE)));
		else
			was.add(Boolean.TRUE);
	}

	@Override
	public void registerExtender(EventExtender extender) {
		extenders.add(extender);
	}

	@Override
	public void onDelayedEventsPublished(Runnable r) {
		publishCallbacks.add(r);
	}

	@Override
	public void publishDelayedEvents() {

		if (log.isDebugEnabled()) {
			log.debug("Publish delayed events [delay " + isDelayEvent() + "]");
		}
		
		try {
			if(isDelayEvent())
				/* Defer until no longer delaying in the thread */
				return;
			
			BufferedSerializer<SystemEvent> events = delayedEvents.get();
			if (events != null) {
				synchronized (events) {
					Iterator<SystemEvent> it = events.iterator();
					while (it.hasNext()) {
						doPublishEvent(it.next());
					}
					events.close();
					delayedEvents.set(null);
				}
			}
			
		} catch (Throwable t) {
			log.error("Failed to process delayed events", t);
		}

		for(Runnable r : publishCallbacks) {
			r.run();
		}
		publishCallbacks.clear();
	}

	@Override
	public void rollbackDelayedEvents(boolean fireFailedEvents) {

		if (log.isDebugEnabled()) {
			log.debug("Rolling back delayed events [" + fireFailedEvents + ": delay " + isDelayEvent() + "]");
		}

		if(isDelayEvent())
			/* Defer until no longer delaying in the thread */
			return;

		if (delayedEvents.get() == null) {
			return;
		}
		BufferedSerializer<SystemEvent> events = delayedEvents.get();
		synchronized (events) {
			if (fireFailedEvents) {
				Iterator<SystemEvent> it = events.iterator();
				while (it.hasNext()) {
					SystemEvent event = it.next();
					if (!event.isSuccess()) {
						publishEvent(event);
					}
				}
			}
			try {
				delayedEvents.get().close();
			} catch (IOException e) {
			}
			delayedEvents.set(null);
		}
	}

	protected void delayEvent(SystemEvent event) {

		if (log.isDebugEnabled()) {
			log.debug("Delaying event " + event.getResourceKey());
		}

		if (delayedEvents.get() == null) {
			try {
				delayedEvents.set(new BufferedSerializer<SystemEvent>(File.createTempFile("evt", ".buf")));
			} catch (IOException e) {
				throw new IllegalStateException("Cannot create temporary file for buffered events.");
			}
		}

		BufferedSerializer<SystemEvent> events = delayedEvents.get();
		events.add(event);
		lastResult.set(event);
	}

	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);

		registerEvent(ConfigurationChangedEvent.class, ConfigurationServiceImpl.RESOURCE_BUNDLE);
		registerEvent(ResourceEvent.class, RESOURCE_BUNDLE);
		registerEvent(AssignableResourceEvent.class, RESOURCE_BUNDLE);
	}

	@Override
	public void registerEvent(Class<? extends SystemEvent> eventClass, String resourceBundle) {
		registerEvent(eventClass, resourceBundle, null);
	}

	@Override
	public void registerEventDefinition(EventDefinition def) {

		if (log.isDebugEnabled()) {
			log.debug("Registering event definition " + def.getResourceKey());
		}

		eventDefinitions.put(def.getResourceKey(), def);
		attributeNames.addAll(def.getAttributeNames());
	}

	@Override
	public void registerDynamicEvent(String resourceKey, String name, Set<String> attributeNames, String successMessage,
			String failureMessage, String warningMessage) {

		I18N.overrideMessage(Locale.ENGLISH,
				new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE, resourceKey, name, name));

		I18N.overrideMessage(Locale.ENGLISH, new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE,
				resourceKey + ".success", successMessage, successMessage));

		I18N.overrideMessage(Locale.ENGLISH, new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE,
				resourceKey + ".failure", failureMessage, failureMessage));

		I18N.overrideMessage(Locale.ENGLISH, new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE,
				resourceKey + ".warning", failureMessage, failureMessage));

		Set<String> variableNames = new HashSet<String>();

		for (String attributeName : attributeNames) {
			String tmp = WordUtils.capitalize(StringUtils.replaceChars(attributeName, ".-_", "   "));
			String var = resourceKey + "." + StringUtils.replaceChars(attributeName, "-_ ", "...");

			I18N.overrideMessage(Locale.ENGLISH,
					new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE, var, tmp, tmp));
			variableNames.add(var);
		}

		EventDefinition def = new EventDefinition(TriggerResourceServiceImpl.RESOURCE_BUNDLE, resourceKey, "", null);

		def.getAttributeNames().addAll(variableNames);
		dynamicEvents.add(resourceKey);
		registerEventDefinition(def);
	}

	@Override
	public void registerEvent(Class<? extends SystemEvent> eventClass, String resourceBundle,
			EventPropertyCollector propertyCollector) {

		if (log.isDebugEnabled()) {
			log.debug("Registering event class " + eventClass.getName());
		}

		try {
			String resourceKey = (String) eventClass.getField("EVENT_RESOURCE_KEY").get(null);

			String i18nNamespace = "";

			try {
				i18nNamespace = (String) eventClass.getField("EVENT_NAMESPACE").get(null);
			} catch (NoSuchFieldException e) {
			}

			if (log.isDebugEnabled()) {
				log.debug("Process event with resource key " + resourceKey);
			}

			eventDefinitions.put(resourceKey,
					new EventDefinition(resourceBundle, resourceKey, i18nNamespace, propertyCollector));

			for (Field field : eventClass.getFields()) {
				if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC
						&& (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {

					if (field.getType().equals(String.class) && field.getName().startsWith("ATTR_")) {
						try {
							String attributeName = (String) field.get(null);
							eventDefinitions.get(resourceKey).getAttributeNames().add(attributeName);
							attributeNames.add(attributeName);
							if (log.isDebugEnabled()) {
								log.debug("Added attribute " + attributeName);
							}
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						}
					}
				}
			}
		} catch (Throwable t) {
			throw new IllegalStateException("Failed to register event class " + eventClass.getName(), t);
		}

	}

	@Override
	public void deregisterEvent(Class<? extends SystemEvent> eventClass) {

		if (log.isDebugEnabled()) {
			log.debug("De-registering event class " + eventClass.getName());
		}

		try {
			String resourceKey = (String) eventClass.getField("EVENT_RESOURCE_KEY").get(null);
			if (log.isDebugEnabled()) {
				log.debug("Process event with resource key " + resourceKey);
			}
			eventDefinitions.remove(resourceKey);

			for (Field field : eventClass.getFields()) {
				if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC
						&& (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {

					if (field.getType().equals(String.class) && field.getName().startsWith("ATTR_")) {
						try {
							String attributeName = (String) field.get(null);
							attributeNames.remove(attributeName);
							if (log.isDebugEnabled()) {
								log.debug("Removed attribute " + attributeName);
							}
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						}
					}
				}
			}
		} catch (Throwable t) {
			throw new IllegalStateException("Failed to de-register event class " + eventClass.getName(), t);
		}

	}

	@Override
	public void publishEvent(SystemEvent event) {

		if (isDelayEvent()) {
			delayEvent(event);
		} else {
			doPublishEvent(event);
		}
	}

	protected void doPublishEvent(SystemEvent event) {
		if (!eventDefinitions.containsKey(event.getResourceKey())) {
			if (log.isWarnEnabled()) {
				log.warn("The system is firing an unregistered event " + event.getResourceKey()
						+ ". Register your event with EventService to remove this message.");
			}
		}

		if (!event.isSuccess()) {
			log.error(event.getResourceKey() + " failed", event.getException());
		}

		try {
			eventPublisher.publishEvent(extendEvent(event));
			lastResult.set(event);
		} catch(Throwable t) { 
			log.error("Trapped unhandled error from event {}", event.getResourceKey(), t);
		}
	}

	private SystemEvent extendEvent(SystemEvent event) {
		for (EventExtender e : extenders) {
			e.extendEvent(event);
		}
		return event;
	}

	private boolean isDelayEvent() {
		return isDelayingEvents.get() != null;
	}

	@Override
	public EventDefinition getEventDefinition(String resourceKey) {
		return eventDefinitions.get(resourceKey);
	}

	@Override
	public List<EventDefinition> getEvents() {
		return new ArrayList<EventDefinition>(eventDefinitions.values());
	}

	@Override
	public Set<String> getAttributeNames() {
		return attributeNames;
	}

	@Override
	public SystemEvent getLastResult() {
		return lastResult.get();
	}

	@Override
	public boolean isDynamicEvent(String resourceKey) {
		return dynamicEvents.contains(resourceKey);
	}
}
