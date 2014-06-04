package com.hypersocket.attributes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.hypersocket.resource.AssignableResource;

@Entity
@Table(name="attributes")
public class Attribute extends AssignableResource {

	@ManyToOne
	AttributeCategory category;

	@Column(name="default_value", nullable=true, length=8000 /*SQL server limit */)
	String defaultValue;

//	@Column(name="weight", nullable=false)
//	int weight;
//	
	@Column(name="type")
	AttributeType type;

	public AttributeCategory getCategory() {
		return category;
	}

	public void setCategory(AttributeCategory category) {
		this.category = category;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

//	public int getWeight() {
//		return weight;
//	}
//
//	public void setWeight(int weight) {
//		this.weight = weight;
//	}

	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
	}
	
	
}
