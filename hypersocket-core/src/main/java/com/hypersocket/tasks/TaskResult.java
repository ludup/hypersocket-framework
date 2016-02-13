package com.hypersocket.tasks;

import com.hypersocket.events.SystemEvent;

public interface TaskResult {

	boolean isPublishable();

	SystemEvent getEvent();

}
