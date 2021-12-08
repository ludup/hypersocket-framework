package com.hypersocket.server.interfaces.http;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.realm.Realm;
import com.hypersocket.server.interfaces.InterfaceResource;

@Entity
@Table(name="http_interfaces")
public class HTTPInterfaceResource extends InterfaceResource {
	
	private static final long serialVersionUID = 7449184297753501756L;

	@Column(name="protocol")
	private HTTPProtocol protocol;
	
	@ManyToOne
	private CertificateResource certificate;

	@Column(name="redirect_https")
	private Boolean redirectHTTPS;
	
	@Column(name="redirect_port")
	private Integer redirectPort;

	@ManyToMany(fetch=FetchType.EAGER)
	private Set<CertificateResource> additionalCertificates = new HashSet<CertificateResource>();

	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "http_interfaces_cascade_1"))
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
	
	public HTTPProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = HTTPProtocol.valueOf(protocol.toUpperCase());
	}

	public CertificateResource getCertificate() {
		return certificate;
	}

	public void setCertificate(CertificateResource certificate) {
		this.certificate = certificate;
	}

	public Boolean getRedirectHTTPS() {
		return redirectHTTPS!=null && redirectHTTPS;
	}

	public Integer getRedirectPort() {
		return redirectPort;
	}

	public void setRedirectHTTPS(Boolean redirectHTTPS) {
		this.redirectHTTPS = redirectHTTPS;
	}

	public void setRedirectPort(Integer redirectPort) {
		this.redirectPort = redirectPort;
	}

	@Override
	protected Integer getDefaultPort() {
		return 80;
	}

	public Set<CertificateResource> getAdditionalCertificates() {
		return additionalCertificates;
	}

	public void setAdditionalCertificates(Set<CertificateResource> additionalCertificates) {
		this.additionalCertificates = additionalCertificates;
	}
}
