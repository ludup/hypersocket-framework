package com.hypersocket.certificates.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.certificates.CertificateResourceRepository;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;

public class CertificateResourceEvent extends ResourceEvent {
	
	private static final long serialVersionUID = 811365186032132096L;

	public static final String EVENT_RESOURCE_KEY = "cert.event";
	
	public CertificateResourceEvent(Object source, String resourceKey,
			Session session, CertificateResource resource) {
		super(source, resourceKey, true, session, resource);
		addAllAttributes(ApplicationContextServiceImpl.getInstance().getBean(CertificateResourceRepository.class).getProperties(resource));
	}

	public CertificateResourceEvent(Object source, String resourceKey,
			CertificateResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
