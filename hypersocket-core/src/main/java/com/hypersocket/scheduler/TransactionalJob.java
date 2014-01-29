package com.hypersocket.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class TransactionalJob implements Job {

	@Autowired
	PlatformTransactionManager transactionManager;
	
	public TransactionalJob() {
	}

	@Override
	public void execute(final JobExecutionContext context)
			throws JobExecutionException {
		
		try {
			TransactionTemplate txnTemplate = new TransactionTemplate(
					transactionManager);
			txnTemplate.afterPropertiesSet();
			txnTemplate.execute(new TransactionCallback<Object>() {
				public Object doInTransaction(TransactionStatus status) {
					return onExecute(context);
				}
			});
			onTransactionComplete();
		} catch(Throwable t) {
			onTransactionFailure(t);
		}

	}
	
	protected abstract void onTransactionComplete();

	protected abstract void onTransactionFailure(Throwable t);
	
	protected abstract Object onExecute(JobExecutionContext context);

}
