package com.hypersocket.message;

import com.hypersocket.properties.ResourceTemplateRepository;

public interface MessageTemplateRepository extends ResourceTemplateRepository {

	void onCreated(MessageResource resource);
	
	void onUpdated(MessageResource resource);
	
	void onDeleted(MessageResource resource);
}
