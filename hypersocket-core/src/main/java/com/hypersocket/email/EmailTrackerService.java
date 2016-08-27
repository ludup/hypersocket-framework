package com.hypersocket.email;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;

public interface EmailTrackerService {

	void finaliseReceipt(Long receiptId);

	String generateTrackingUri(String subject, String name, String emailAddress, Realm realm) throws AccessDeniedException;

	String generateNonTrackingUri(String uuid, Realm realm) throws AccessDeniedException;

}
