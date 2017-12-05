package com.hypersocket.local;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class LocalPrincipalTemplateRepositoryImpl 
				extends ResourceTemplateRepositoryImpl 
				implements LocalPrincipalTemplateRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("local-principalTemplate.xml");
	}
}
