package com.hypersocket.scheduler;

public enum SchedulerJobState {
	WAITING, BLOCKED, RUNNING, PAUSED, COMPLETE, COMPLETE_WITH_ERRORS, MISSING, CANCELLING, WAITING_TO_RETRY;


	public boolean isRunning() {
		return this.equals(CANCELLING) || this.equals(RUNNING);
	}

	public boolean isCancelAllowed() {
		return this.equals(WAITING) || this.equals(BLOCKED) || this.equals(RUNNING) || this.equals(PAUSED);
	}
}
