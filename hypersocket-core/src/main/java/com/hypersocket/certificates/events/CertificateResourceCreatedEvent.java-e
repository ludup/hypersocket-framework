package com.hypersocket.certificates.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.session.Session;

public class CertificateResourceCreatedEvent extends
		CertificateResourceEvent {

	private static final long serialVersionUID = 7538727039181170321L;

	public static final String EVENT_RESOURCE_KEY = "certificate.created";
	
	public CertificateResourceCreatedEvent(Object source,
			Session session,
			CertificateResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public CertificateResourceCreatedEvent(Object source,
			CertificateResource resource, Throwable e,
			Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
