package com.hypersocket.attributes;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractAttributeCategory<A extends AbstractAttribute<?>> extends RealmAttributeCategory<A> {

	private static final long serialVersionUID = -8751695909133262852L;

	@Column(name = "weight", nullable = false)
	private Integer weight = Integer.valueOf(0);

	@Column(name="visibility_depends_on")
	private String visibilityDependsOn;
	
	@Column(name="visibility_depends_value")
	private String visibilityDependsValue;
	
	public AbstractAttributeCategory() {
		super();
	}
	
	public String getCategoryNamespace() {
		return null;
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

	public String getVisibilityDependsOn() {
		return visibilityDependsOn;
	}

	public String getVisibilityDependsValue() {
		return visibilityDependsValue;
	}

}