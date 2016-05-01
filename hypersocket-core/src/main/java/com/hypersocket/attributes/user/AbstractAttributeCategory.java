package com.hypersocket.attributes.user;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hypersocket.attributes.AbstractAttribute;
import com.hypersocket.attributes.RealmAttributeCategory;

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