package com.hypersocket.bandwidth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BandwidthReportingServiceImpl implements BandwidthReportingService {

	static Logger log = LoggerFactory
			.getLogger(BandwidthReportingServiceImpl.class);

	protected Map<String, Set<BandwidthReporter>> reporters = new HashMap<String, Set<BandwidthReporter>>();

	@Override
	public void startReporting(BandwidthReporter reporter) {
		if (!reporters.containsKey(reporter.getResourceKey())) {
			reporters.put(reporter.getResourceKey(),
					new HashSet<BandwidthReporter>());
		}
		Set<BandwidthReporter> tmp = reporters.get(reporter.getResourceKey());
		synchronized (tmp) {
			tmp.add(reporter);
		}
	}

	@Override
	public Map<String,Set<BandwidthReporter>> getReporters() {
		return reporters;
	}
	
	@Override
	public void stopReporting(BandwidthReporter reporter) {
		Set<BandwidthReporter> tmp = reporters.get(reporter.getResourceKey());
		synchronized (tmp) {
			tmp.remove(reporter);
		}
	}

}
