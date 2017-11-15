package com.hypersocket.batch;

import java.util.Map;

public interface BatchProcessor {

	void process(BatchItem item, Map<String, String> properties);

}
