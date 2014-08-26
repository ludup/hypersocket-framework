package com.hypersocket.triggers.actions.email;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
@Transactional
public class EmailTriggerActionRepositoryImpl extends ResourceTemplateRepositoryImpl
		implements EmailTriggerActionRepository {

	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("actions/email-template.xml");
	}

}
