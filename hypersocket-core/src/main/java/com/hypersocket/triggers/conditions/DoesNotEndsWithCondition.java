package com.hypersocket.triggers.conditions;

import java.util.List;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.ValidationException;

public class DoesNotEndsWithCondition implements Condition {

	@Override
	public boolean checkCondition(TriggerCondition condition, TriggerResource trigger,
			List<SystemEvent> sourceEvents) throws ValidationException {

		String value = TriggerAttributeHelper.getAttribute(condition.getAttributeKey(), sourceEvents);
		
		if (value==null) {
			return false;
		}

		return !value.endsWith(condition.getConditionValue());
	}

	@Override
	public boolean isValueRequired() {
		return true;
	}
}
