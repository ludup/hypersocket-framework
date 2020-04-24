package com.hypersocket.batch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "batch_processing_items")
public class BatchItem extends RealmResource {

	private static final long serialVersionUID = -6097805172247469296L;

	@Column(name = "resource_key")
	private String resourceKey;

	@ManyToOne
	@JoinColumn(name= "realm_id", foreignKey = @ForeignKey(name = "batch_processing_items_cascade_1"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	protected Realm realm;

	@Override
	protected Realm doGetRealm() {
		return realm;
	}

	@Override
	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

}
