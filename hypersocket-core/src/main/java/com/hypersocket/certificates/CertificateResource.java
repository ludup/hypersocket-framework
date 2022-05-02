package com.hypersocket.certificates;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "ssl_certificates")
public class CertificateResource extends RealmResource {

	private static final long serialVersionUID = 2494558616152991393L;

	@Column(name = "type")
	private CertificateType type;

	@Column(name = "cn")
	private String commonName;

	@Column(name = "ou")
	private String organizationalUnit;

	@Column(name = "o")
	private String organization;

	@Column(name = "l")
	private String location;

	@Column(name = "s")
	private String state;

	@Column(name = "c")
	private String country;

	@Column(name = "private_key", nullable = false)
	@Lob
	private String privateKey;

	@Column(name = "certificate", nullable = true)
	@Lob
	private String certificate;

	@Column(name = "bundle", nullable = true)
	@Lob
	private String bundle;

	@Column(name = "san", nullable = true)
	@Lob
	private String san;

	@Column(name = "signature_type")
	private String signatureAlgorithm;

	@Column(name = "provider")
	private String provider;

	@Temporal(TemporalType.DATE)
	private Date expiryDate;

	@Temporal(TemporalType.DATE)
	private Date issueDate;
	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "ssl_certificates_cascade_1"))
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

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

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

	public String getSan() {
		return san;
	}

	public void setSan(String san) {
		this.san = san;
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

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

}
