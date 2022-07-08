package com.hypersocket.scheduler.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.listeners.TriggerListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.scheduler.AllowOneJobConcurrently;

public class AppJobTriggerListener extends TriggerListenerSupport {

	private static Logger log = LoggerFactory.getLogger(AppJobTriggerListener.class);
	
	private static final Set<String> JOB_KEYS = new HashSet<>(); 
	
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
	private final Lock rLock = rwLock.readLock();
	private final Lock wLock = rwLock.writeLock();
	
	public boolean hasKey(String key) {
		rLock.lock();
		try {
			return JOB_KEYS.contains(key);
		} finally {
			rLock.unlock();
		}
	}
	
	public boolean addKeyIfNotPresent(String key) {
		wLock.lock();
		try {
			// double check as read is allowed for all racing threads, they might get impression
			// that key is not present so right before add check again as write is synchronized
			// basically a kind of double check
			if (hasKey(key)) {
				return false;
			}
			
			JOB_KEYS.add(key);
			
			return true;
		} finally {
			wLock.unlock();
		}
	}
	
	public void removeKey(String key) {
		wLock.lock();
		try {
			JOB_KEYS.remove(key);
		} finally {
			wLock.unlock();
		}
	}
	
	@Override
	public String getName() {
		return AppJobTriggerListener.class.getName();
	}
	
	@Override
	public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
		Job job = context.getJobInstance();
		
		if (job instanceof AllowOneJobConcurrently) {
			String key = ((AllowOneJobConcurrently)job).jobId(trigger.getJobDataMap());
			
			if (hasKey(key)) {
				log.info("Job vetoed for class {} and key {}", job.getClass(), context.getJobDetail().getKey());
				return true;
			}
		
			boolean added = addKeyIfNotPresent(key);
			
			if (!added) {
				log.info("Job vetoed for class {} and key {} on double check add.", job.getClass(), context.getJobDetail().getKey());
				return true;
			}
			
			return false;
		}
		
		return false;
	}
	
	
	@Override
	public void triggerComplete(Trigger trigger, JobExecutionContext context,
			CompletedExecutionInstruction triggerInstructionCode) {
		
		Job job = context.getJobInstance();	
		
		if (job instanceof AllowOneJobConcurrently) {
			String key = ((AllowOneJobConcurrently)job).jobId(trigger.getJobDataMap());
			removeKey(key);
		}
	}

}
