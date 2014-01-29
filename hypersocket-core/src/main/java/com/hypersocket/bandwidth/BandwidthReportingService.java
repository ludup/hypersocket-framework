package com.hypersocket.bandwidth;

import java.util.Map;
import java.util.Set;

public interface BandwidthReportingService {

	public void startReporting(BandwidthReporter reporter);

	void stopReporting(BandwidthReporter reporter);

	Map<String, Set<BandwidthReporter>> getReporters();
}
