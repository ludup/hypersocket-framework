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
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="trigger_resource")
public class TriggerResource extends RealmResource {

	@Column(name="result")
	TriggerResultType result;
	
	@Column(name="event")
	String event;
	
	@OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, mappedBy="trigger", fetch=FetchType.EAGER)
	Set<TriggerCondition> conditions = new HashSet<TriggerCondition>();
	
	@OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, mappedBy="trigger", fetch=FetchType.EAGER)
	Set<TriggerAction> actions  = new HashSet<TriggerAction>();

	@OneToOne
	TriggerAction parentAction;
	
	Boolean fireEvent;
	
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

	public Set<TriggerAction> getActions() {
		return actions;
	}
	
	public Boolean getFireEvent() {
		return fireEvent;
	}

	public void setFireEvent(Boolean fireEvent) {
		this.fireEvent = fireEvent;
	}

	@JsonIgnore
	public Set<TriggerCondition> getConditions() {
		return conditions;
	}
	
	public Set<TriggerCondition> getAllConditions() {
		Set<TriggerCondition> ret = new HashSet<TriggerCondition>();
		for(TriggerCondition c : conditions) {
			if(c.getType()==TriggerConditionType.ALL) {
				ret.add(c);
			}
		}
		return ret;
	}
	
	public Set<TriggerCondition> getAnyConditions() {
		Set<TriggerCondition> ret = new HashSet<TriggerCondition>();
		for(TriggerCondition c : conditions) {
			if(c.getType()==TriggerConditionType.ANY) {
				ret.add(c);
			}
		}
		return ret;
	}

	public void setParentAction(TriggerAction parentAction) {
		this.parentAction = parentAction;
	}
	
	@JsonIgnore
	public TriggerAction getParentAction() {
		return parentAction;
	}
	
}
