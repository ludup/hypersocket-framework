package com.hypersocket.triggers.actions.alert;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
@Transactional
public class AlertTriggerActionRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements AlertTriggerActionRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("actions/alert-template.xml");
	}
}
