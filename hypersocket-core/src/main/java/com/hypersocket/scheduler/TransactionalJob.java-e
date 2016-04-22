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

	protected abstract void onTransactionComplete();

	protected abstract void onTransactionFailure(Throwable t);
	
	protected abstract void onExecute(JobExecutionContext context);

	protected boolean isTransactionRequired() {
		return true;
	}
	
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		
		if(!isTransactionRequired()) {
			onExecute(context);
		} else {
			try {
				TransactionTemplate txnTemplate = new TransactionTemplate(
						transactionManager);
				txnTemplate.afterPropertiesSet();
				txnTemplate.execute(new TransactionCallback<Object>() {
					public Object doInTransaction(TransactionStatus status) {
						onExecute(context);
						return null;
					}
				});
				onTransactionComplete();
			} catch(Throwable t) {
				onTransactionFailure(t);
			}
		}
		
	}

}
