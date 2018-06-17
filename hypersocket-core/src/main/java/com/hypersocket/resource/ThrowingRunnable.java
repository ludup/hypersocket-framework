package com.hypersocket.resource;

@FunctionalInterface
public interface ThrowingRunnable extends Runnable {
	default void run() {
		try {
			runThrows();
		}
		catch(RuntimeException re) {
			throw re;
		}
		catch(Exception e) {
			throw new RuntimeException("Runnable failed.", e);
		}
	}
	
	void runThrows() throws Exception;
	
}
