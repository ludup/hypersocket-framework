package com.hypersocket.transactions;

import org.springframework.transaction.support.TransactionCallback;

public interface TransactionCallbackWithError<T> extends TransactionCallback<T> {

	void doTransacationError(Throwable e);
}
