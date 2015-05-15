package com.hypersocket.scheduler;

import java.util.Date;

import com.hypersocket.resource.AssignableResource;

public class SchedulerResource extends AssignableResource {

	String started;
	int intervals;
	Date end;
	String jobId;
	String lastExecuted;
	String nextExecute;
	
	public String getStarted() {
		return started;
	}

	public void setStarted(String start) {
		this.started = start;
	}

	public int getIntervals() {
		return intervals;
	}

	public void setIntervals(int intervals) {
		this.intervals = intervals;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getLastExecuted() {
		return lastExecuted;
	}

	public void setLastExecuted(String lastExecuted) {
		this.lastExecuted = lastExecuted;
	}

	public String getNextExecute() {
		return nextExecute;
	}

	public void setNextExecute(String nextExecute) {
		this.nextExecute = nextExecute;
	}

}
