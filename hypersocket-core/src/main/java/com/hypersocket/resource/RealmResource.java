package com.hypersocket.resource;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.hypersocket.realm.Realm;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
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
