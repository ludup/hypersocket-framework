package com.hypersocket.tasks.user;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class EnableAccountTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements EnableAccountTaskRepository {

	public EnableAccountTaskRepositoryImpl() {
		super(true);
	}
	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/enableAccount.xml");
	}

}
