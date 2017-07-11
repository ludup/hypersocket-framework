package com.hypersocket.password.policy;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.transactions.TransactionService;

@Service
public class DictionaryServiceImpl implements DictionaryService {
	
	static Logger log = LoggerFactory.getLogger(DictionaryServiceImpl.class);
	
	@Autowired
	DictionaryRepository dictionaryRepository;

	@Autowired
	TransactionService transactionService; 
	
	@PostConstruct
	private void postConstruct() {
		new Thread() {
			public void run() {
				try {
					transactionService.doInTransaction(new TransactionCallback<Void>() {

						@Override
						public Void doInTransaction(TransactionStatus arg0) {
							dictionaryRepository.setup();
							return null;
						}
						
					});
				} catch (ResourceException e) {
					log.error("Could not setup dictionary", e);
				} catch (AccessDeniedException e) {
					log.error("Could not setup dictionary", e);
				}
				
			}
		}.start();
	}

	@Override
	public String randomWord(Locale locale) {
		return dictionaryRepository.randomWord(locale);
	}

	@Override
	public boolean containsWord(Locale locale, String word) {
		return dictionaryRepository.containsWord(locale, word);
	}
}
