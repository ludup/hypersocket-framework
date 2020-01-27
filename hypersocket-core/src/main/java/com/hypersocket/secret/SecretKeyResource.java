package com.hypersocket.secret;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="secret_keys")
public class SecretKeyResource extends RealmResource {

	private static final long serialVersionUID = 5968935969005380460L;

	@Column(name="keydata")
	@Lob
	private String keydata;
	
	@Column(name="iv")
	@Lob
	private String iv;
	
	@Column(name="keylength")
	private Integer keylength;
	
	public String getKeydata() {
		return keydata;
	}
	
	public void setKeydata(String keydata) {
		this.keydata = keydata;
	}
	
	public String getIv() {
		return iv;
	}
	
	public void setIv(String iv) {
		this.iv = iv;
	}

	public Integer getKeylength() {
		return keylength == null ? 256 : Math.min(keylength, 256);
	}

	public void setKeylength(Integer keylength) {
		this.keylength = keylength;
	}
	
	

}
