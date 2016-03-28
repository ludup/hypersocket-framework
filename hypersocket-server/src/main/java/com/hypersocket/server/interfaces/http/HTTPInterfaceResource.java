package com.hypersocket.server.interfaces.http;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="http_interfaces")
public class HTTPInterfaceResource extends RealmResource {

	@Column(name="interfaces", length=1024)
	String interfaces;
	
	@Column(name="port")
	Integer port;
	
	@Column(name="protocol")
	HTTPProtocol protocol;
	
	@OneToOne
	CertificateResource certificate;

	@Column(name="redirect_https")
	Boolean redirectHTTPS;
	
	@Column(name="redirect_port")
	Integer redirectPort;
	
	public String getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(String interfaces) {
		this.interfaces = interfaces;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
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
	
	
	
	
}
