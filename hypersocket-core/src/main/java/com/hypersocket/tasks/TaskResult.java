package com.hypersocket.tasks;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.events.SystemEventStatus;

public interface TaskResult {

	boolean isPublishable();

	SystemEvent getEvent();

	boolean isSuccess();
	
	SystemEventStatus getStatus();
	
}
