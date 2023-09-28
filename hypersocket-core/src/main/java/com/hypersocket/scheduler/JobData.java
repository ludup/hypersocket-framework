package com.hypersocket.scheduler;

import org.quartz.JobDataMap;

import com.hypersocket.resource.RealmResource;

public final class JobData {
	
	public final static String KEY_JOB_NAME = "jobName";
	public final static String KEY_JOB_ARGS = "jobArgs";

	public static JobDataMap ofNamed(String name,  Object... data) {
		return of("genericNamedJob", AbstractSchedulerServiceImpl.RESOURCE_BUNDLE, data);
	}
	
	public static JobDataMap ofResource(RealmResource resource) {
		var data = new PermissionsAwareJobData(resource.getRealm(), "generic", resource.getName());
		data.put("resourceId", resource.getId());
		return data;
	}

	public static JobDataMap of(String resourceKey, Object... data) {
		return of(resourceKey, new Object[0], data);
	}

	public static JobDataMap of(String resourceKey, Object[] args, Object... data) {
		var map = new JobDataMap();
		map.put(KEY_JOB_NAME, resourceKey);
		map.put(KEY_JOB_ARGS, args);
		if(data.length % 2 == 0) {
			for(var i = 0 ; i < data.length ; i += 2) {
				map.put((String)data[i], data[i + 1]);
			}
		}
		else
			throw new IllegalArgumentException("Uneven data, data must be repeating pairs of key followed by data.");
		return map;
	}
}
