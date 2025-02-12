package com.hypersocket.events;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.AbstractTaskResult;

@SuppressWarnings("serial")
public abstract class SynchronousEvent extends  SystemEvent {

	public final static class SynchronousEventException extends RuntimeException {

		private TaskResult result;

		public SynchronousEventException(TaskResult result) {
			super(result instanceof AbstractTaskResult ? ((AbstractTaskResult)result).getException() : (Throwable)null);
			this.result = result;
		}
		
		public TaskResult getResult() {
			return result;
		}

	}
	
	public SynchronousEvent(Object source, String resourceKey, boolean success, Realm currentRealm) {
		super(source, resourceKey, success, currentRealm);
	}
	
	public SynchronousEvent(Object source, String resourceKey, SystemEventStatus status, Realm currentRealm) {
		super(source, resourceKey, status, currentRealm);
	}
	
	public SynchronousEvent(Object source, String resourceKey, Throwable e, Realm currentRealm) {
		super(source, resourceKey, e, currentRealm);
	}
	
}
