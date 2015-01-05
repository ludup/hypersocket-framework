package com.hypersocket.triggers.conditions;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.ValidationException;

public class NotEqualsCondition implements Condition {

	@Override
	public boolean checkCondition(TriggerCondition condition, TriggerResource trigger,
			SystemEvent event) throws ValidationException {
		if (event.hasAttribute(condition.getAttributeKey())) {
			throw new ValidationException("Event "
					+ event.getResourceKey()
					+ " does not have an attribute named "
					+ condition.getAttributeKey());
		}

		return !event.getAttribute(condition.getAttributeKey()).equals(
				condition.getConditionValue());
	}

}
