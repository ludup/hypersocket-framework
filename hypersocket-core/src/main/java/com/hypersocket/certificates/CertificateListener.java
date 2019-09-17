package com.hypersocket.certificates;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface CertificateListener {

	
	void onCreateCertificate(CertificateResource cert) throws ResourceException, AccessDeniedException;
	
	void onUpdateCertificate(CertificateResource cert) throws ResourceException, AccessDeniedException;
	
	void onDeleteCertificate(CertificateResource cert) throws ResourceException, AccessDeniedException;
	
}
