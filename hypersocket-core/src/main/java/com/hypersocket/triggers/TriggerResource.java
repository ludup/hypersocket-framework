package com.hypersocket.triggers;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hypersocket.tasks.Task;

@Entity
@Table(name = "trigger_resources")
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonDeserialize(using = TriggerResourceDeserializer.class)
public class TriggerResource extends Task {

	private static final long serialVersionUID = 4055884752102231593L;

	@Column(name = "result")
	private TriggerResultType result;
	
	@Column(name="trigger_type")
	private TriggerType triggerType = TriggerType.TRIGGER;
	
	@Column(name="event")
	private String event;

	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "trigger", fetch = FetchType.EAGER)
	private Set<TriggerCondition> conditions = new HashSet<TriggerCondition>();

	@OneToMany(mappedBy = "parentTrigger", fetch = FetchType.EAGER)
	private Set<TriggerResource> childTriggers = new HashSet<>();

	@OneToOne
	private TriggerResource parentTrigger;

	@Column(name = "resource_key")
	private String resourceKey;
	
	@Column(name = "attachment_id")
	private Long attachmentId;
	
	@Column(name="all_realms")
	private Boolean allRealms = Boolean.FALSE;

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

	public Set<TriggerResource> getChildTriggers() {
		return childTriggers;
	}

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(TriggerType triggerType) {
		this.triggerType = triggerType;
	}
	
	public Long getAttachmentId() {
		return attachmentId;
	}
	
	public void setAttachmentId(Long attachmentId) {
		this.attachmentId = attachmentId;
	}
	
	public Boolean getAllRealms() {
		return allRealms==null ? false : allRealms;
	}
	
	public void setAllRealms(Boolean allRealms) {
		this.allRealms = allRealms;
	}

	public void setConditions(Set<TriggerCondition> conditions) {
		this.conditions = conditions;		
	}

	public void setChildTriggers(Set<TriggerResource> childTriggers) {
		this.childTriggers = childTriggers;
	}
	
}
