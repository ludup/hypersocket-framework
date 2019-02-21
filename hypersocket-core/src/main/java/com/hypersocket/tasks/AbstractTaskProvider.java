package com.hypersocket.tasks;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.conditions.TriggerAttributeHelper;
import com.hypersocket.util.TextProcessor;
import com.hypersocket.util.TextProcessor.Resolver;

public abstract class AbstractTaskProvider implements TaskProvider {

	static Logger log = LoggerFactory.getLogger(AbstractTaskProvider.class);

	@Autowired
	EventService eventService;

	@Autowired
	RealmService realmService;

	@Autowired
	TriggerResourceService triggerService;


	protected String[] processTokenReplacements(String[] values, List<SystemEvent> events) {
		return processTokenReplacements(values, events, false);
	}

	protected String[] processTokenReplacements(String[] values, List<SystemEvent> events, boolean evaluateScripts) {
		for (int i = 0; i < values.length; i++) {
			values[i] = processTokenReplacements(values[i], events, evaluateScripts, true);
		}
		return values;
	}

	protected String processTokenReplacements(String value, final List<SystemEvent> events) {
		return processTokenReplacements(value, events, false, true);
	}
	
	protected String processTokenReplacements(String value, final List<SystemEvent> events, boolean evaluateScripts) {
		return processTokenReplacements(value, events, evaluateScripts, true);
	}
	
	protected String processTokenReplacements(String value, final List<SystemEvent> events, boolean evaluateScripts, boolean replaceUnkown) {

		if (value == null) {
			return null;
		}
		final Set<String> defaultAttributes = triggerService.getDefaultVariableNames();
		TextProcessor tp = new TextProcessor();
		tp.setUnknownVariablesAreBlank(replaceUnkown);
		tp.setEvaluateScripts(evaluateScripts);
		
		if(evaluateScripts) {
			/* When evaluating scripts, it should be possible to access the property
			 * values from the bindings. This means the pattern based approach for
			 * resolving variables won't work, so we have to add them all.
			 * 
			 * We might as well expose the entire event object itself.
			 * 
			 * The value is also made available
			 */
			SystemEvent lastEvent = events.get(events.size()-1);
			tp.addBindings("event", lastEvent);
			tp.addBindings("value", value);
			tp.addBindings("events", events);
		}
		
		tp.addResolver(new Resolver() {
			@Override
			public String evaluate(String variable) {
				return TriggerAttributeHelper.getAttribute(variable, events);
			}
		});
		tp.addResolver(new Resolver() {
			@Override
			public String evaluate(String variable) {
				if(defaultAttributes.contains(variable)) {
					return triggerService.getDefaultVariableValue(variable);
				}
				return null;
			}
		});
		return tp.process(value);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(Task task) {
		return getRepository().getPropertyCategories(task);
	}

	@Override
	public Collection<PropertyCategory> getProperties(Task task) {
		return getRepository().getPropertyCategories(task);
	}

	@Override
	public Map<String, String> getTaskProperties(Task task) {
		return getRepository().getProperties(task);
	}

	@Override
	public Collection<String> getPropertyNames(Task task) {
		return getRepository().getPropertyNames(task);
	}

	@Override
	public void taskCreated(Task task) {

	}

	@Override
	public void taskUpdated(Task task) {

	}

	@Override
	public void taskDeleted(Task task) {
		getRepository().deletePropertiesForResource(task);
	}

	@Override
	public boolean supportsAutomation() {
		return true;
	}

	@Override
	public boolean supportsTriggers() {
		return true;
	}

	@Override
	public boolean isRealmTask() {
		return false;
	}

	@Override
	public boolean isRealmSupported(Realm realm) {
		return false;
	}
}
