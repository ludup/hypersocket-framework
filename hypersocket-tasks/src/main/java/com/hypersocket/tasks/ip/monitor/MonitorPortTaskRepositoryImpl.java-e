package com.hypersocket.tasks.ip.monitor;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class MonitorPortTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements MonitorPortTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/monitorPort.xml");
	}
}
