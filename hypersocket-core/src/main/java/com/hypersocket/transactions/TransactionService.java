package com.hypersocket.transactions;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface TransactionService {

	<T> T doInTransaction(TransactionCallback<T> transaction)
			throws ResourceException, AccessDeniedException;

	<T> T doInTransaction(TransactionCallbackWithError<T> transaction) throws ResourceException;
	
	<T> T doInNewTransaction(TransactionCallback<T> transaction)
			throws ResourceException, AccessDeniedException;

	<T> T doInNewTransaction(TransactionCallbackWithError<T> transaction) throws ResourceException;
	
	<T> T doInIsolationReadCommittedTransaction(TransactionCallback<T> transaction)
			throws ResourceException, AccessDeniedException;

	<T> T doInIsolationReadCommittedTransaction(TransactionCallbackWithError<T> transaction) throws ResourceException;
	
	<T> T doInTransactionRequiresWithTransactionDefinition(TransactionCallback<T> transaction, TransactionDefinition transactionDefinition)
			throws ResourceException, AccessDeniedException;
	
	<T> T doInTransactionRequiresWithTransactionDefinition(TransactionCallbackWithError<T> transaction, TransactionDefinition transactionDefinition)
			throws ResourceException;

}
