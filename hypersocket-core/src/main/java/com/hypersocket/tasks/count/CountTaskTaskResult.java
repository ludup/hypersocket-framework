package com.hypersocket.tasks.count;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class CountTaskTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_RESOURCE_KEY = "countTask.result";
	
	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;
	public static final String ATTR_ADJUSTMENT = "attr.adjustment";
	public static final String ATTR_CURRENT_TOTAL = "attr.currentTotal";
	
	public CountTaskTaskResult(Object source, 
			boolean success, Realm currentRealm, Task task, String name, Long count, Long currentTotal) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
		addAttribute(ATTR_RESOURCE_NAME, name);
		addAttribute(ATTR_ADJUSTMENT, count.toString());
		addAttribute(ATTR_CURRENT_TOTAL, currentTotal.toString());
	}

	public CountTaskTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return CountTaskTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
