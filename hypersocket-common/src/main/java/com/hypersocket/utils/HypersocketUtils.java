package com.hypersocket.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HypersocketUtils {

	
	static Logger log = LoggerFactory.getLogger(HypersocketUtils.class);
	
	static ThreadLocal<Long> times = new ThreadLocal<Long>();
	
	
	public static void resetInterval() {
		times.set(System.currentTimeMillis());
	}
	
	public static void logInterval(String msg) {
		
		log.info("REMOVE ME: " + msg + ": " + (System.currentTimeMillis() - times.get()));
		resetInterval();
	}
	
	
}
