package com.hypersocket.realm.ou;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="ous")
public class OrganizationalUnit extends RealmResource {

	private static final long serialVersionUID = -1269723999215576537L;
	@Column(name="dn")
	@Lob
	String dn;
	
	public String getDn() {
		return dn;
	}
	
	public void setDn(String dn) {
		this.dn = dn;
	}

}
