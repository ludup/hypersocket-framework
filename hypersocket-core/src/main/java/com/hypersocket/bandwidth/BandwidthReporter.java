package com.hypersocket.bandwidth;

public interface BandwidthReporter {

	long getIntervalBytesIn();
	
	long getIntervalBytesOut();
	
	long getTotalBytesIn();
	
	long getTotalBytesOut();
	
	String getResourceKey();
	
	String getResourceBundle();
}
