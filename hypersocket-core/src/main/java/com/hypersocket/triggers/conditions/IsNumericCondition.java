package com.hypersocket.triggers.conditions;

import org.apache.commons.lang3.math.NumberUtils;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.ValidationException;

public class IsNumericCondition implements Condition {

	@Override
	public boolean checkCondition(TriggerCondition condition, TriggerResource trigger,
			SystemEvent event) throws ValidationException {
		if (!event.hasAttribute(condition.getAttributeKey())) {
			return false;
		}

		return NumberUtils.isNumber(event.getAttribute(condition.getAttributeKey()).toString());
	}

	@Override
	public boolean isValueRequired() {
		return false;
	}
}
