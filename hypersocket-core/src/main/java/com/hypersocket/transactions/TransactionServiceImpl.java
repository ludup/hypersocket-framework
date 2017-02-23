package com.hypersocket.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

@Service
public class TransactionServiceImpl implements TransactionService {

	static Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
	
	@Autowired
	@Qualifier("transactionManager")
	PlatformTransactionManager txManager;
	
	@Autowired
	EventService eventService;
	
	@Override
	public <T> T doInTransaction(TransactionCallback<T> transaction)
			throws ResourceException, AccessDeniedException {

		TransactionTemplate tmpl = new TransactionTemplate(txManager);
		eventService.delayEvents(true);
		try {
			T result = tmpl.execute(transaction);
			eventService.publishDelayedEvents();
			return result;
		} catch (Throwable e) {
			log.error("Error in transaction", e);
			eventService.rollbackDelayedEvents(true);
			if(transaction instanceof TransactionCallbackWithError) {
				((TransactionCallbackWithError<T>)transaction).doTransacationError(e);
			}
			if(e.getCause() instanceof ResourceException) {
				throw (ResourceException) e.getCause();
			}else if(e.getCause() instanceof AccessDeniedException) {
				throw (AccessDeniedException) e.getCause();
			}
			
			throw new ResourceException(AuthenticationService.RESOURCE_BUNDLE, "error.transactionFailed", e.getMessage());
		} finally {
			eventService.delayEvents(false);
		}
		
		
	}
	
	@Override
	public <T> T doInTransaction(TransactionCallbackWithError<T> transaction) throws ResourceException {
		
		TransactionTemplate tmpl = new TransactionTemplate(txManager);
		eventService.delayEvents(true);
		try {
			T result = tmpl.execute(transaction);
			eventService.publishDelayedEvents();
			return result;
		} catch (Throwable e) {
			log.error("Error in transaction", e);
			eventService.rollbackDelayedEvents(true);
			transaction.doTransacationError(e);
			throw new ResourceException(AuthenticationService.RESOURCE_BUNDLE, "error.transactionFailed", e.getMessage());
		} finally {
			eventService.delayEvents(false);
		}
		
		
	}

}
