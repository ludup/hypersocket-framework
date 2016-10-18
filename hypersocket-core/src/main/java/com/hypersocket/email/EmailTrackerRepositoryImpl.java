package com.hypersocket.email;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractRepositoryImpl;

@Repository
public class EmailTrackerRepositoryImpl extends AbstractRepositoryImpl<Long> implements EmailTrackerRepository
{

	@Transactional
	@Override
	public EmailReceipt trackEmail(String subject, String emailAddress, Realm realm, Principal principal) {
		
		EmailTracker tracker = get("subject", subject, EmailTracker.class);
		if(tracker==null) {
			tracker = new EmailTracker();
			tracker.setSubject(subject);
			tracker.setRealm(realm);
			save(tracker);
		}
		
		EmailReceipt receipt = new EmailReceipt();
		receipt.setEmailAddress(emailAddress);
		receipt.setTracker(tracker);
		receipt.setPrincipal(principal);
		
		save(receipt);
		
		return receipt;
	}
	
	@Transactional(readOnly=true)
	@Override
	public EmailReceipt getReceiptById(Long id) {
		return get("id", id, EmailReceipt.class);
	}

	@Override
	@Transactional
	public void saveReceipt(EmailReceipt receipt) {
		save(receipt);
	}
}
