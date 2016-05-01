package com.hypersocket.attributes.user;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.hypersocket.attributes.AbstractAttribute;

@Entity
@Table(name = "user_attributes")
public class UserAttribute extends AbstractAttribute<UserAttributeCategory> {


	@ManyToOne
	UserAttributeCategory category;
	
	@Override
	public UserAttributeCategory getCategory() {
		return category;
	}

	@Override
	public void setCategory(UserAttributeCategory category) {
		this.category = category;		
	}

}
