package com.hypersocket.realm.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;

public class UserUpdatedEvent extends UserEvent {

	private static final long serialVersionUID = 3984021807869214879L;

	public static final String EVENT_RESOURCE_KEY = "event.userUpdated";

	public static final String ATTR_CHANGES = "attr.changes";

	private Map<String, String[]> allChangedProperties = new HashMap<>();
	private Map<String, String[]> changedProperties = new HashMap<>();
	private Map<String, String> addedProperties = new HashMap<>();

	
	{
		consoleLog = false;
	}
	
	public UserUpdatedEvent(Object source, Session session, Realm realm,
			RealmProvider provider, Principal principal,
			List<? extends Principal> associatedPrincipals, Map<String,String> properties,
			List<? extends Principal> previouslyAssociatedPrincipals, Map<String,String> oldProperties) {
		super(source, "event.userUpdated", session, realm, provider, principal,
				associatedPrincipals, Collections.emptyMap());

		List<String> changes = new ArrayList<>();
		
		// Find property additions or changes
		for(Map.Entry<String, String> en : properties.entrySet()) {
			if(oldProperties.containsKey(en.getKey())) {
				final String oldVal = oldProperties.get(en.getKey());
				if(!Objects.equals(en.getValue(), oldVal)) {
					changes.add(String.format("%s was changed from %s to %s", en.getKey(), friendlyNull(oldVal), friendlyNull(en.getValue())));
					changedProperties.put(en.getKey(), new String[] {oldVal, en.getValue()});
					allChangedProperties.put(en.getKey(), new String[] {oldVal, en.getValue()});
				}
			}
			else {
				changes.add(String.format("%s was added with a value of %s", en.getKey(), friendlyNull(en.getValue())));
				addedProperties.put(en.getKey(), en.getValue());
				allChangedProperties.put(en.getKey(), new String[] { en.getValue() });
			} 
		}
		
		// Find principal additions
		for(Principal p : associatedPrincipals) {
			if(!previouslyAssociatedPrincipals.contains(p)) {
				changes.add(String.format("%s %s was added", p.getPrincipalType(), p.getPrincipalName()));
			} 
		}
		// Find principal removals

		for(Principal p : previouslyAssociatedPrincipals) {
			if(!associatedPrincipals.contains(p)) {
				changes.add(String.format("%s %s was removed", p.getPrincipalType(), p.getPrincipalName()));
			} 
		}

		if(!changes.isEmpty())
			addAttribute(ATTR_CHANGES, String.join("\n", changes));
	}

	private String friendlyNull(String val) {
		return val == null ? "[empty]" : val;
	}

	public UserUpdatedEvent(Object source, Throwable e, Session session,
			Realm realm, RealmProvider provider, String principalName,
			Map<String, String> properties, List<? extends Principal> associatedPrincipals) {
		super(source, "event.userUpdated", e, session, realm, provider,
				principalName, properties, associatedPrincipals);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	public Map<String, String[]> getAllChangedProperties() {
		return allChangedProperties;
	}

	public Map<String, String[]> getChangedProperties() {
		return changedProperties;
	}

	public Map<String, String> getAddedProperties() {
		return addedProperties;
	}

}
