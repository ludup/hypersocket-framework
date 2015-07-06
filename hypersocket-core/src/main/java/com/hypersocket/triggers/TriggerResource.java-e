package com.hypersocket.triggers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hypersocket.tasks.Task;

@Entity
@Table(name = "trigger_resources")
@JsonDeserialize(using = TriggerResourceDeserializer.class)
public class TriggerResource extends Task {

	@Column(name = "result")
	TriggerResultType result;

	@Column(name = "event")
	String event;

	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "trigger", fetch = FetchType.EAGER)
	Set<TriggerCondition> conditions = new HashSet<TriggerCondition>();

	@OneToMany(mappedBy = "parentTrigger", fetch = FetchType.EAGER)
	List<TriggerResource> childTriggers;

	@OneToOne
	TriggerResource parentTrigger;

	@Column(name = "resource_key")
	String resourceKey;

	public TriggerResultType getResult() {
		return result;
	}

	public void setResult(TriggerResultType result) {
		this.result = result;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	@JsonIgnore
	public Set<TriggerCondition> getConditions() {
		return conditions;
	}

	public Set<TriggerCondition> getAllConditions() {
		Set<TriggerCondition> ret = new HashSet<TriggerCondition>();
		for (TriggerCondition c : conditions) {
			if (c.getType() == TriggerConditionType.ALL) {
				ret.add(c);
			}
		}
		return ret;
	}

	public Set<TriggerCondition> getAnyConditions() {
		Set<TriggerCondition> ret = new HashSet<TriggerCondition>();
		for (TriggerCondition c : conditions) {
			if (c.getType() == TriggerConditionType.ANY) {
				ret.add(c);
			}
		}
		return ret;
	}

	public void setParentTrigger(TriggerResource parentTrigger) {
		this.parentTrigger = parentTrigger;
	}

	@JsonIgnore
	public TriggerResource getParentTrigger() {
		return parentTrigger;
	}

	public boolean getIsRoot() {
		return parentTrigger == null;
	}

	public Long getParentId() {
		return parentTrigger == null ? null : parentTrigger.getId();
	}

	public void setResourceKey(String task) {
		this.resourceKey = task;
	}

	@Override
	public String getResourceKey() {
		return resourceKey;
	}

	public List<TriggerResource> getChildTriggers() {
		return childTriggers;
	}

}
