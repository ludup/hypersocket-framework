package com.hypersocket.tasks;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ValidationException;

public interface TaskProvider {

	String getResourceBundle();
	
	String[] getResourceKeys();

	void validate(Task task, Map<String, String> parameters)
			throws ValidationException;

	TaskResult execute(Task task, Realm currentRealm, List<SystemEvent> event)
			throws ValidationException;
	
	ResourceTemplateRepository getRepository();

	Collection<PropertyCategory> getPropertyTemplate(Task task);

	Collection<PropertyCategory> getProperties(Task task);
	
	Map<String,String> getTaskProperties(Task task);
	
	String getResultResourceKey();
	
	void taskCreated(Task task);
	
	void taskUpdated(Task task);
	
	void taskDeleted(Task task);
	
	boolean supportsAutomation();
	
	boolean supportsTriggers();

	Collection<String> getPropertyNames(Task task);
	
	boolean isSystem();

	boolean isRealmTask();

	boolean isRealmSupported(Realm realm);
	
	String getDisplayMode();
}
