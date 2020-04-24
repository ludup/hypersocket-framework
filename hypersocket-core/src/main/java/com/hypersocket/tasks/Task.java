package com.hypersocket.tasks;

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
@Table(name = "tasks")
public abstract class Task extends RealmResource {

	private static final long serialVersionUID = -1025827996129306313L;

	public abstract String getResourceKey();

	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "tasks_cascade_1"))
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
}
