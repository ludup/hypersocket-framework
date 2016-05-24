package com.hypersocket.reconcile;

import org.quartz.SchedulerException;

import com.hypersocket.resource.Resource;

public interface AbstractReconcileService<T extends Resource> {

	boolean reconcileNow(T resource);

	void updateResourceSchedule(T resource) throws SchedulerException;

	void unlockResource(T resource);

	void lockResource(T resource);

	boolean isLocked(T resource);

}
