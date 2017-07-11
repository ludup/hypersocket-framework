package com.hypersocket.message;

import com.hypersocket.properties.ResourceTemplateRepository;

public interface MessageTemplateRepository extends ResourceTemplateRepository {

	public void onCreated(MessageResource resource);
	
	public void onUpdated(MessageResource resource);
	
	public void onDeleted(MessageResource resource);
}
