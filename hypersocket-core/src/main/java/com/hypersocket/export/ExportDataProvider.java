package com.hypersocket.export;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public interface ExportDataProvider extends Iterator<Map<String, String>> {

	Collection<String> getHeaders();
 
}
