package com.hypersocket.template;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
@Transactional
public class TemplateRepositoryImpl extends
		AbstractResourceRepositoryImpl<Template> implements TemplateRepository {

	@Override
	protected Class<Template> getResourceClass() {
		return Template.class;
	}

}
