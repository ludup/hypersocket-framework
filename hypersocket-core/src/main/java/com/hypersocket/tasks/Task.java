package com.hypersocket.tasks;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="tasks")
public abstract class Task extends RealmResource {

	public abstract String getResourceKey();

}
