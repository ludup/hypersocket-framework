package com.hypersocket.attributes.user;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.attributes.AttributeCategory;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "user_attribute_categories")
public class UserAttributeCategory extends RealmResource implements AttributeCategory {
	
	@Column(name = "weight", nullable = false)
	Integer weight = new Integer(0);

	@OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
	protected Set<UserAttribute> attributes;

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	@JsonIgnore
	public Set<UserAttribute> getAttributes() {
		return attributes;
	}
	
	public String getFilter() {
		return "custom";
	}
	
}
