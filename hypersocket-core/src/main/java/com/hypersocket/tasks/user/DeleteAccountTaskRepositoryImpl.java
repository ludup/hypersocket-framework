package com.hypersocket.tasks.user;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class DeleteAccountTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements DeleteAccountTaskRepository {

	public DeleteAccountTaskRepositoryImpl() {
		super(true);
	}
	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/deleteAccount.xml");
	}

}
