package com.hypersocket.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.transactions.TransactionCallbackWithError;
import com.hypersocket.transactions.TransactionService;

public abstract class TransactionalJob implements Job {

	static Logger log = LoggerFactory.getLogger(TransactionalJob.class);
	
	@Autowired
	TransactionService transactionService;
	
	boolean transactionFailed = false;
	
	public TransactionalJob() {
	}

	protected abstract void onTransactionComplete();

	protected abstract void onTransactionFailure(Throwable t);
	
	protected abstract void onExecute(JobExecutionContext context);

	protected void beforeTransaction() { 
		
	}
	
	protected void afterTransaction(boolean transactionFailed) {
		
	}
	
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {

		beforeTransaction();
		
		
	
		try {
			transactionService.doInTransaction(new TransactionCallbackWithError<Void>() {

				@Override
				public Void doInTransaction(TransactionStatus status) {
					onExecute(context);
					return null;
				}

				@Override
				public void doTransacationError(Throwable e) {
					onTransactionFailure(e);
					transactionFailed = true;
					
				}
			});
		} catch (ResourceException e) {
			log.error("Job transaction failed", e);
		}
		
		if(!transactionFailed) {
			onTransactionComplete();
		}

		afterTransaction(transactionFailed);
	}
}
