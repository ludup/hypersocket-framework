package com.hypersocket.triggers;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.tasks.Task;

@Entity
@Table(name="trigger_actions")
public class TriggerAction extends Task {

	@OneToOne
	@JoinColumn(name="trigger_id")
	TriggerResource trigger;
	
	@Column(name="resource_key")
	String resourceKey;
	
	@OneToOne
	@JoinColumn(name="post_exec_trigger_id")
	TriggerResource postExecutionTrigger;

	@Transient
	Map<String,String> properties;
	
	@JsonIgnore
	public TriggerResource getTrigger() {
		return trigger;
	}
	
	public void setTrigger(TriggerResource trigger) {
		this.trigger = trigger;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public TriggerResource getPostExecutionTrigger() {
		return postExecutionTrigger;
	}

	public void setPostExecutionTrigger(TriggerResource postExecutionTrigger) {
		this.postExecutionTrigger = postExecutionTrigger;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}	
}
