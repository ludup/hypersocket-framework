package com.hypersocket.attributes;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "realm_attribute_categories")
public abstract class RealmAttributeCategory<A extends AbstractAttribute<?>> extends RealmResource implements AttributeCategory<A> {


	abstract public void setWeight(Integer weight);
	
	abstract public Set<A> getAttributes();
}
