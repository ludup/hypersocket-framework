package com.hypersocket.jobs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="jobs")
public class JobResource extends RealmResource {

	@OneToOne
	JobResource parentJob;

	@Column(name="state")
	JobState state;
	
	@Column(name="result", length=8000)
	String result;
	
	public JobResource getParentJob() {
		return parentJob;
	}

	public void setParentJob(JobResource parentJob) {
		this.parentJob = parentJob;
	}

	public JobState getState() {
		return state;
	}

	public void setState(JobState state) {
		this.state = state;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	
	
}
