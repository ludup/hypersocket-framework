package com.hypersocket.triggers;

import com.hypersocket.events.SystemEvent;

public interface TriggerConditionProvider {

	String getResourceBundle();

	String[] getResourceKeys();

	boolean checkCondition(TriggerCondition condition, TriggerResource trigger,
			SystemEvent event) throws ValidationException;

}
