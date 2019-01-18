package com.hypersocket.tasks.user;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class UpdateAccountAddressesRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements UpdateAccountAddressesTaskRepository {

	public UpdateAccountAddressesRepositoryImpl() {
		super(true);
	}
	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/updateAccountAddresses.xml");
	}

}
