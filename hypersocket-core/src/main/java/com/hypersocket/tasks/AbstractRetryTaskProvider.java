package com.hypersocket.tasks;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ValidationException;

public abstract class AbstractRetryTaskProvider extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(AbstractRetryTaskProvider.class);
	
	@Override
	public TaskResult execute(Task task, Realm currentRealm, List<SystemEvent> event) throws ValidationException {
		
		boolean retry = getRepository().getBooleanValue(task, "retry.enabled");
		if(retry) {
			int retries = getRepository().getIntValue(task, "retry.attempts");
			int interval = getRepository().getIntValue(task, "retry.interval");
			if(retries <= 0) {
				throw new ValidationException("retries value must be > 0");
			}
			
			TaskResult result = null;
			for(int i=0;i<retries;i++) {
				if(log.isInfoEnabled()) {
					log.info(String.format("Executing task with error handling retry=%d interval=%d", retries, interval));
				}
				result = onExecute(task, currentRealm, event);
				if(result.isSuccess()) {
					if(log.isInfoEnabled()) {
						log.info("Task was a success, returning");
					}
					break;
				}
				
				if(log.isInfoEnabled()) {
					log.info("Task WAS NOT a success, waiting for %d seconds before retrying", interval);
				}
				
				try {
					Thread.sleep(interval * 1000L);
				} catch (InterruptedException e) {
				}
			}
			return result;
		} else {
			return onExecute(task, currentRealm, event);
		}
	}

	protected abstract TaskResult onExecute(Task task, Realm currentRealm, List<SystemEvent> event) throws ValidationException;
}
