package com.hypersocket.telemetry;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface TelemetryService {

	void registerProducer(TelemetryProducer producer);

	String collect() throws AccessDeniedException, ResourceException;
}
