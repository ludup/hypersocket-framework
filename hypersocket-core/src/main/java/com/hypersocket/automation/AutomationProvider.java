package com.hypersocket.automation;

import com.hypersocket.properties.ResourceTemplateRepository;

public interface AutomationProvider {

	String[] getResourceKeys();
	
	ResourceTemplateRepository getRepository();
	
	void performTask(AutomationResource resource);


}
