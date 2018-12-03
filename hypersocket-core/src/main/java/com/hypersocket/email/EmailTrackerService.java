package com.hypersocket.email;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceNotFoundException;

public interface EmailTrackerService {

	void finaliseReceipt(Long receiptId);

	String generateTrackingUri(String uuid, String subject, String name, String emailAddress, Realm realm) throws AccessDeniedException, ResourceNotFoundException;

	String generateNonTrackingUri(String uuid, Realm realm) throws AccessDeniedException, ResourceNotFoundException;

}
