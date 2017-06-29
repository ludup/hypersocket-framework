package com.hypersocket.migration.mapper.serializers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.migration.execution.MigrationContext;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.resource.AbstractResource;

@Component
public class SerializersUtil {
	
	private static final String TEMPLATE_CACHE = "TEMPLATE_CACHE";
	
	@Autowired
	MigrationContext migrationContext;

	@SuppressWarnings("unchecked")
	public PropertyTemplate getPropertyTemplate(final AbstractResource resource, final String currentName) {
		Map<String, PropertyTemplate> templateCache = (Map<String, PropertyTemplate>) migrationContext.get(TEMPLATE_CACHE);
		if(templateCache == null) {
			templateCache = new HashMap<>();
			migrationContext.put(TEMPLATE_CACHE, templateCache);
		}
		final String cacheKey = String.format("%s_%s", resource.getClass().getName(), currentName);
		PropertyTemplate propertyTemplate = templateCache.get(cacheKey);
		
		if(propertyTemplate == null) {
			String context = WordUtils.uncapitalize(resource.getClass().getSimpleName());
			List<ResourceTemplateRepository> list = ResourceTemplateRepositoryImpl.getPropertyContexts().get(context);
			if(list != null) {
				for (ResourceTemplateRepository resourceTemplateRepository : list) {
					propertyTemplate = resourceTemplateRepository.getPropertyTemplate(resource, currentName);
					if(propertyTemplate != null) {
						templateCache.put(cacheKey, propertyTemplate);
						break;
					} 
				}
			}
		}
		return propertyTemplate;
	}
}
