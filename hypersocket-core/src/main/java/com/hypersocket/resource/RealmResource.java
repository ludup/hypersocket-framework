package com.hypersocket.resource;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		super.doHashCodeOnKeys(builder);
		builder.append(realm==null ? -1 : realm.getId());
	}
	
	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {
		super.doEqualsOnKeys(builder, obj);
		builder.append(getId(), ((RealmResource)obj).getId());
	}
}
