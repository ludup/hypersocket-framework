package com.hypersocket.tasks.user.password.change;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class ChangePasswordTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements ChangePasswordTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/changePasswordTask.xml");
	}

}
