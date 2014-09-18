package com.hypersocket.certificates.events;

import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.session.Session;

public class CertificateResourceDeletedEvent extends
		CertificateResourceEvent {

	private static final long serialVersionUID = -5905990185868544055L;
	public static final String EVENT_RESOURCE_KEY = "certificate.deleted";

	public CertificateResourceDeletedEvent(Object source,
			Session session, CertificateResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public CertificateResourceDeletedEvent(Object source,
			CertificateResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

}
