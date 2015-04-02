package com.hypersocket.transactions;

import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface TransactionService {

	<T> T doInTransaction(TransactionCallback<T> transaction)
			throws ResourceException, AccessDeniedException;

}
