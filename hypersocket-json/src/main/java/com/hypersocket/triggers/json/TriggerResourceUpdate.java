package com.hypersocket.triggers.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.triggers.TriggerType;

@JsonIgnoreProperties(ignoreUnknown=true)
public class TriggerResourceUpdate {

	Long id;
	String name;
	String event;
	String task;
	String result;
	TriggerType type;
	TriggerActionUpdate[] actions;
	TriggerConditionUpdate[] allConditions;
	TriggerConditionUpdate[] anyConditions;
	PropertyItem[] properties;
	Long parentId;
	boolean allRealms;
	
	public TriggerResourceUpdate() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public TriggerActionUpdate[] getActions() {
		return actions;
	}

	public void setActions(TriggerActionUpdate[] actions) {
		this.actions = actions;
	}

	public TriggerConditionUpdate[] getAllConditions() {
		return allConditions;
	}

	public void setAllConditions(TriggerConditionUpdate[] allConditions) {
		this.allConditions = allConditions;
	}

	public TriggerConditionUpdate[] getAnyConditions() {
		return anyConditions;
	}

	public void setAnyConditions(TriggerConditionUpdate[] anyConditions) {
		this.anyConditions = anyConditions;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public PropertyItem[] getProperties() {
		return properties;
	}

	public void setProperties(PropertyItem[] properties) {
		this.properties = properties;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
	public String getTask() {
		return task;
	}
	
	public void setTask(String task) {
		this.task = task;
	}

	public TriggerType getType() {
		return type;
	}

	public void setType(TriggerType type) {
		this.type = type;
	}

	public boolean isAllRealms() {
		return allRealms;
	}

	public void setAllRealms(boolean allRealms) {
		this.allRealms = allRealms;
	}
	
	
	
	
}
