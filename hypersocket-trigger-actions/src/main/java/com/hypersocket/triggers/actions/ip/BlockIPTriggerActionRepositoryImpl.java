package com.hypersocket.triggers.actions.ip;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class BlockIPTriggerActionRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements BlockIPTriggerActionRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("actions/blockIP.xml");
	}
}

