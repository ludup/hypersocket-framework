package com.hypersocket.attributes.role;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.hypersocket.attributes.AbstractAttribute;

@Entity
@Table(name = "role_attributes")
public class RoleAttribute extends AbstractAttribute<RoleAttributeCategory> {

	private static final long serialVersionUID = 9069451084265854269L;

	@ManyToOne
	RoleAttributeCategory category;
	
	@Override
	public RoleAttributeCategory getCategory() {
		return category;
	}

	@Override
	public void setCategory(RoleAttributeCategory category) {
		this.category = category;		
	}

}
