package com.hypersocket.tasks;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="tasks")
public abstract class Task extends RealmResource {

	private static final long serialVersionUID = -1025827996129306313L;

	public abstract String getResourceKey();

}
