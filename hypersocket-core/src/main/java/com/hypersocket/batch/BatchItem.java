package com.hypersocket.batch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="batch_processing_items")
public class BatchItem extends RealmResource {

	private static final long serialVersionUID = -6097805172247469296L;

	@Column(name="resource_key")
	String resourceKey;
	
	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

}
