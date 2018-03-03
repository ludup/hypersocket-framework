package com.hypersocket.html;

import org.springframework.stereotype.Repository;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class HtmlTemplateResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<HtmlTemplateResource> implements
		HtmlTemplateResourceRepository {

	@Override
	protected Class<HtmlTemplateResource> getResourceClass() {
		return HtmlTemplateResource.class;
	}

}
