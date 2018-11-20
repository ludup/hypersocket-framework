package com.hypersocket.tasks.principal.legacy;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class ImportPrincipalLegacyIDTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements ImportPrincipalLegacyIDTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/importPrincipalLegacyIDTask.xml");
	}

}
