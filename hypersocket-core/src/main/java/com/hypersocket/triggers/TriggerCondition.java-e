package com.hypersocket.triggers;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name="trigger_resource_conditions")
public class TriggerCondition extends AbstractEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="id")
	Long id;
	
	@OneToOne
	@JoinColumn(name="trigger_id")
	TriggerResource trigger;
	
	@Column(name="type")
	TriggerConditionType type;
	
	@Column(name="attribute_key")
	String attributeKey;
	
	@Column(name="condition_key")
	String conditionKey;
	
	@Column(name="condition_value")
	String conditionValue;
	
	@Override
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	@JsonIgnore
	public TriggerResource getTrigger() {
		return trigger;
	}

	public void setTrigger(TriggerResource trigger) {
		this.trigger = trigger;
	}

	public TriggerConditionType getType() {
		return type;
	}

	public void setType(TriggerConditionType type) {
		this.type = type;
	}

	public String getAttributeKey() {
		return attributeKey;
	}
	
	public void setAttributeKey(String resourceKey) {
		this.attributeKey = resourceKey;
	}

	public String getConditionKey() {
		return conditionKey;
	}

	public void setConditionKey(String conditionKey) {
		this.conditionKey = conditionKey;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public void setConditionValue(String conditionValue) {
		this.conditionValue = conditionValue;
	}

	@Override
	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		builder.append(getAttributeKey());
		builder.append(getConditionKey());
		builder.append(getConditionValue());
	}

	@Override
	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {
		TriggerCondition c = (TriggerCondition) obj;
		builder.append(getAttributeKey(), c.getAttributeKey());
		builder.append(getConditionKey(), c.getConditionKey());
		builder.append(getConditionValue(), c.getConditionValue());
	}
	
	
	
}
