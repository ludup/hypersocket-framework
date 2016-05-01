package com.hypersocket.attributes.user;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user_attribute_categories")
public class UserAttributeCategory extends AbstractAttributeCategory<UserAttribute> {

	@OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
	protected Set<UserAttribute> attributes;
	
	@Override
	@JsonIgnore
	public Set<UserAttribute> getAttributes() {
		return attributes;
	}

}
