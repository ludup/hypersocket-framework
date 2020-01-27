package com.hypersocket.tasks;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.events.SystemEventStatus;

public class TaskResultHolder implements TaskResult {

	private SystemEvent event;
	private boolean publish;
	
	public TaskResultHolder(SystemEvent event, boolean publish) {
		this.event = event;
		this.publish = publish;
	}
	
	@Override
	public boolean isPublishable() {
		return publish;
	}

	@Override
	public SystemEvent getEvent() {
		return event;
	}

	@Override
	public boolean isSuccess() {
		return event.isSuccess();
	}

	@Override
	public SystemEventStatus getStatus() {
		return event.getStatus();
	}

}
