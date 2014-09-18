package com.hypersocket.certificates.events;

import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.realm.events.RealmResourceEvent;
import com.hypersocket.session.Session;

public class CertificateResourceEvent extends RealmResourceEvent {
	
	private static final long serialVersionUID = 811365186032132096L;

	public CertificateResourceEvent(Object source, String resourceKey,
			Session session, CertificateResource resource) {
		super(source, resourceKey, true, session, resource);

		/**
		 * TODO add attributes of your resource here. Make sure all attributes
		 * have a constant string definition like the commented out example above,
		 * its important for its name to start with ATTR_ as this is picked up during 
		 * the registration process
		 */
	}

	public CertificateResourceEvent(Object source, String resourceKey,
			CertificateResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
		

	}

}
