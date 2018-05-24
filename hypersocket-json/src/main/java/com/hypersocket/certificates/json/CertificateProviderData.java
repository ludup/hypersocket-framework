package com.hypersocket.certificates.json;

import com.hypersocket.certificates.CertificateProvider;
import com.hypersocket.properties.NameValuePair;

public class CertificateProviderData extends NameValuePair {

	private boolean requireCertificateDetails;

	public CertificateProviderData(CertificateProvider provider) {
		super("certificateProvider." + provider.getResourceKey(), provider.getResourceKey());
		requireCertificateDetails = provider.isRequireCertificateDetails();
	}

	public CertificateProviderData() {
		super();
	}

	public CertificateProviderData(String name, String value) {
		super(name, value);
	}

	public CertificateProviderData(String pair) {
		super(pair);
	}

	public boolean isRequireCertificateDetails() {
		return requireCertificateDetails;
	}

	public void setRequireCertificateDetails(boolean requireCertificateDetails) {
		this.requireCertificateDetails = requireCertificateDetails;
	}

}
