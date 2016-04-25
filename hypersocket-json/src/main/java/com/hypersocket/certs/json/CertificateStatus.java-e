/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.certs.json;

import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.certificates.CertificateResource;

@XmlRootElement(name="certificateState")
public class CertificateStatus {

	boolean installedCertificate;
	boolean matchingCertificate;
	
	boolean success = true;
	String message;
	CertificateResource resource;
	
	public CertificateStatus(CertificateResource resource) {
		this.resource = resource;
	}
	
	public CertificateStatus() {
	}

	public boolean isInstalledCertificate() {
		return installedCertificate;
	}
	
	public void setInstalledCertificate(boolean installedCertificate) {
		this.installedCertificate = installedCertificate;
	}

	public boolean isMatchingCertificate() {
		return matchingCertificate;
	}

	public void setMatchingCertificate(boolean matchingCertificate) {
		this.matchingCertificate = matchingCertificate;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public CertificateResource getResource() {
		return resource;
	}
	
	public void setResource(CertificateResource resource) {
		this.resource = resource;
	}

}
