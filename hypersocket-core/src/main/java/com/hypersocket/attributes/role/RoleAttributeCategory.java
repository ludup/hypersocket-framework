package com.hypersocket.attributes.role;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.attributes.AbstractAttributeCategory;

@Entity
@Table(name = "role_attribute_categories")

public class RoleAttributeCategory extends AbstractAttributeCategory<RoleAttribute> {

	private static final long serialVersionUID = 5468378074378807120L;
	
	@OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
	protected Set<RoleAttribute> attributes;

	@Override
	@JsonIgnore
	public Set<RoleAttribute> getAttributes() {
		return attributes;
	}

}
