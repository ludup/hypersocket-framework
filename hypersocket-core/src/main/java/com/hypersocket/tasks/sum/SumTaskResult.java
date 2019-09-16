package com.hypersocket.tasks.sum;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class SumTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_RESOURCE_KEY = "sum.result";
	
	public static final String ATTR_SUM = "attr.sum";
	
	public SumTaskResult(Object source, 
			boolean success, Realm currentRealm, Task task, Long sum, Map<String,Long> counts) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
		addAttribute(ATTR_SUM, sum.toString());
		for(Map.Entry<String,Long> e : counts.entrySet()) {
			addAttribute(e.getKey(), e.getValue().toString());
		}
	}

	public SumTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return SumTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
