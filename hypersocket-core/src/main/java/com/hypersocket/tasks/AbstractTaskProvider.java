package com.hypersocket.tasks;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.MediaNotFoundException;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserVariableReplacementService;
import com.hypersocket.realm.events.UserEvent;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.conditions.TriggerAttributeHelper;
import com.hypersocket.util.TextProcessor;
import com.hypersocket.util.TextProcessor.Resolver;

public abstract class AbstractTaskProvider implements TaskProvider {

	static Logger log = LoggerFactory.getLogger(AbstractTaskProvider.class);

	static Pattern eventPattern;
	
	static {
		eventPattern = Pattern.compile("principal(\\d+):(.*)");
	}
	
	@Autowired
	private TriggerResourceService triggerService;
	
	@Autowired
	private RealmService realmService;
	
	@Autowired
	private UserVariableReplacementService userVariableReplacementService; 

	protected String[] processTokenReplacements(String[] values, List<SystemEvent> events) {
		return processTokenReplacements(values, events, false);
	}
	
	public String getDisplayMode() {
		return null;
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
		
		if(evaluateScripts && !events.isEmpty()) {
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
			public String evaluate(String variable, TextProcessor processor) {
				try {			
					Matcher m = eventPattern.matcher(variable);
					if(m.matches()) {
						int index = Integer.parseInt(m.group(1));
						String realAttribute = m.group(2);
						if(index>= 0 && index < events.size()) {
							SystemEvent event = events.get(index);
							if(event.hasAttribute(UserEvent.ATTR_USER_NAME)) {
								String principalName = event.getAttribute(UserEvent.ATTR_USER_NAME);
								Principal principal = realmService.getPrincipalByName(event.getCurrentRealm(), 
										principalName, PrincipalType.USER);
								
								try {
								switch(realAttribute) {
								case "media.phone":
									return realmService.getPrincipalAddress(principal, MediaType.PHONE);
								case "media.email":
									return realmService.getPrincipalAddress(principal, MediaType.EMAIL);
								default:
									return userVariableReplacementService.getVariableValue(principal, realAttribute);
								}
								} catch(MediaNotFoundException e) {
									return null;
								}
							}
						}
					}
				}
				catch(IllegalStateException ise) {
					// No such variable
					if(log.isDebugEnabled())
						log.debug("Failed to get variable value for {}",  variable, ise);
				}
				
				return null;

			}
		});
		
		tp.addResolver(new Resolver() {
			@Override
			public String evaluate(String variable, TextProcessor processor) {
				return TriggerAttributeHelper.getAttribute(variable, events);
			}
		});
		tp.addResolver(new Resolver() {
			@Override
			public String evaluate(String variable, TextProcessor processor) {
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
