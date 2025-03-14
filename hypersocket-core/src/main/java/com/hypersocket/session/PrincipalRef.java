package com.hypersocket.session;

import java.util.Objects;

import com.hypersocket.realm.Realm;

public class PrincipalRef {

	private final Realm realm;
	private final String principalName;

	public PrincipalRef(Realm realm, String principalName) {
		super();
		this.realm = realm;
		this.principalName = principalName;
	}

	public Realm getRealm() {
		return realm;
	}

	public String getPrincipalName() {
		return principalName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(principalName, realm);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrincipalRef other = (PrincipalRef) obj;
		return Objects.equals(principalName, other.principalName) && Objects.equals(realm, other.realm);
	}

	@Override
	public String toString() {
		return "PrincipalRef [realm=" + realm + ", principalName=" + principalName + "]";
	}
}
