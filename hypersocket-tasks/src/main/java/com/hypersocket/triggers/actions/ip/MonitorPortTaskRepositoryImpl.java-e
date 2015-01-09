package com.hypersocket.triggers.actions.ip;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class MonitorPortTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements MonitorPortTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/monitorPort.xml");
	}
}
