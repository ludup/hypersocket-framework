package com.hypersocket.attributes;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.hypersocket.resource.RealmResource;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class RealmAttributeCategory<A extends AbstractAttribute<?>> extends RealmResource implements AttributeCategory<A> {


	abstract public void setWeight(Integer weight);
	
	abstract public Set<A> getAttributes();
}
