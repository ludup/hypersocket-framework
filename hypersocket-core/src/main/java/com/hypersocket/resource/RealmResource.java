package com.hypersocket.resource;

import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.Realm;

@MappedSuperclass
public abstract class RealmResource extends Resource {

	private static final long serialVersionUID = 7483847758596964348L;

	@OneToOne
	@JoinColumn(name="realm_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	protected Realm realm;
	
	@JsonIgnore
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
