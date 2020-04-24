package com.hypersocket.resource;

import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.Realm;

@MappedSuperclass
public abstract class RealmResource extends Resource {

	private static final long serialVersionUID = 7483847758596964348L;
	
	@JsonIgnore
	public Realm getRealm() {
		return doGetRealm();
	}
	
	protected abstract Realm doGetRealm();

	public abstract void setRealm(Realm realm);
	
	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		super.doHashCodeOnKeys(builder);
		Realm realm = getRealm();
		builder.append(realm==null ? -1 : realm.getId());
	}
	
	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {
		super.doEqualsOnKeys(builder, obj);
		builder.append(getId(), ((RealmResource)obj).getId());
	}
}
