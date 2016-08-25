package com.hypersocket.email;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractRepository;

public interface EmailTrackerRepository extends AbstractRepository<Long> {

	EmailReceipt trackEmail(String subject, String emailAddress, Realm realm, Principal principal);

	EmailReceipt getReceiptById(Long id);

	void saveReceipt(EmailReceipt receipt);

}
