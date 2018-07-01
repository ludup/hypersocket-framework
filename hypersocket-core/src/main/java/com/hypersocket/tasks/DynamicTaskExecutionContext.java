package com.hypersocket.tasks;

public interface DynamicTaskExecutionContext {

	void addResults(TaskResult result);
	
	void flush();

	boolean isTransactional();
}
