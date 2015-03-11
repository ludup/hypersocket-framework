package com.hypersocket.triggers;

import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.tasks.Task;

@Entity
@Table(name="trigger_resource_tasks")
public class TriggerAction extends Task {

	@OneToOne
	@JoinColumn(name="trigger_id")
	TriggerResource trigger;
	
	@Column(name="resource_key")
	String resourceKey;
	
	@OneToMany(mappedBy="parentAction", fetch=FetchType.EAGER)
	List<TriggerResource> postExecutionTrigger;

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

	public List<TriggerResource> getPostExecutionTriggers() {
		return postExecutionTrigger;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}	
}
