package com.hypersocket.attributes;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractAttributeCategory<A extends AbstractAttribute<?>> extends RealmAttributeCategory<A> {

	@Column(name = "weight", nullable = false)
	Integer weight = new Integer(0);

	public AbstractAttributeCategory() {
		super();
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public String getFilter() {
		return "custom";
	}

}