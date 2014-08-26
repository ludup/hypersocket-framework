package com.hypersocket.triggers.conditions;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerValidationException;

public class ContainsCondition implements Condition {

	@Override
	public boolean checkCondition(TriggerCondition condition, TriggerResource trigger,
			SystemEvent event) throws TriggerValidationException {
		if (event.hasAttribute(condition.getAttributeKey())) {
			throw new TriggerValidationException("Event "
					+ event.getResourceKey()
					+ " does not have an attribute named "
					+ condition.getAttributeKey());
		}

		return event.getAttribute(condition.getAttributeKey()).toString()
				.contains(condition.getConditionValue());
	}

}
