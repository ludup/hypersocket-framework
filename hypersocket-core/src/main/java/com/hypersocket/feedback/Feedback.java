package com.hypersocket.feedback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Feedback implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 106680764431465306L;
	
	private int index;
	private boolean finished;
	private String resourceKey;
	private FeedbackStatus status;
	private String error;
	private List<String> args;
	private String bundle;
	private String[] options;
	
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
	
	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
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
