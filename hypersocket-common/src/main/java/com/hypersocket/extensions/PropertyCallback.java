package com.hypersocket.extensions;

import java.util.Map;

public interface PropertyCallback {

	void processRemoteProperties(Map<String,String> properties);
}
