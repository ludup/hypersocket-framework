package com.hypersocket.feedback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Feedback {

	int index;
	boolean finished;
	String resourceKey;
	FeedbackStatus status;
	String error;
	List<String> args;
	
	public Feedback() {
		
	}
	
	public Feedback(int index, FeedbackStatus status, String resourceKey, boolean finished, Throwable t, String... args) {
		this.index = index;
		this.status = status;
		this.resourceKey = resourceKey;
		this.finished = finished;
		if(t!=null) {
			error = t.getMessage();
		}
		this.args = new ArrayList<String>(Arrays.asList(args));
	}

	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public FeedbackStatus getStatus() {
		return status;
	}

	public void setStatus(FeedbackStatus status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	public List<String> getArgs() {
		return args;
	}
	

	
}
