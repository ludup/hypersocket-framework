package com.hypersocket.resource;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import com.hypersocket.realm.Realm;

@MappedSuperclass
public abstract class RealmResource extends Resource {


	@ManyToOne
	@JoinColumn(name="realm_id")
	protected Realm realm;

	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}
	
	public Boolean isSystemResource() {
		return realm==null;
	}

}
