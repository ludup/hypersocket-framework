package com.hypersocket.interfaceState;

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
@Table(name = "interface_state")
public class UserInterfaceState extends RealmResource {

	private static final long serialVersionUID = 5075850205303090191L;

	@Column(name = "preferences", length = 8000)
	private String preferences;
	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "interface_state_cascade_1"))
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

	public UserInterfaceState() {

	}

	public UserInterfaceState(String preferences) {
		this.preferences = preferences;
	}

	public String getPreferences() {
		return preferences;
	}

	public void setPreferences(String preferences) {
		this.preferences = preferences;
	}
}
