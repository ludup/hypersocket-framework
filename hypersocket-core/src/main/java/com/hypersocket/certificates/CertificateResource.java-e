package com.hypersocket.certificates;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="ssl_certificates")
public class CertificateResource extends RealmResource {

	@Column(name="type")
	CertificateType type;
	
	@Column(name="cn")
	String commonName;
	
	@Column(name="ou")
	String organizationalUnit;
	
	@Column(name="o")
	String organization;
	
	@Column(name="l")
	String location;
	
	@Column(name="s")
	String state;
	
	@Column(name="c")
	String country;

	@Column(name="private_key", nullable=false, length=8000 /*SQL server limit */)
	String privateKey;
	
	@Column(name="certificate", nullable=true, length=8000 /*SQL server limit */)
	String certificate;
	
	@Column(name="bundle", nullable=true, length=8000 /*SQL server limit */)
	String bundle;
	
	@Column(name="signature_type")
	String signatureAlgorithm;
	
	public CertificateType getCertType() {
		return type;
	}

	public void setCertType(String type) {
		this.type = CertificateType.valueOf(type);
	}

	public CertificateType getType() {
		return type;
	}

	public void setType(CertificateType type) {
		this.type = type;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getOrganizationalUnit() {
		return organizationalUnit;
	}

	public void setOrganizationalUnit(String organizationalUnit) {
		this.organizationalUnit = organizationalUnit;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@JsonIgnore
	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(String signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}
	
}
