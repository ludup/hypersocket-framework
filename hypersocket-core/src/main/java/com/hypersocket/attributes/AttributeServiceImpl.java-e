package com.hypersocket.attributes;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.tables.ColumnSort;

@Service
public class AttributeServiceImpl extends AuthenticatedServiceImpl implements
		AttributeService {

	public static final String RESOURCE_BUNDLE = "UserAttributes";

	@Autowired
	I18NService i18nService;

	@Autowired
	AttributeRepository attributeRepository;

	@Autowired
	AttributeCategoryRepository attributeCategoryRepository;

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);
	}

	@Override
	public List<AttributeCategory> getCategories() {
		return attributeCategoryRepository.allEntities();
	}

	@Override
	public List<Attribute> searchAttributes(String searchPattern, int start,
			int length, ColumnSort[] sorting) {
		return attributeRepository.searchAttributes(searchPattern, start,
				length, sorting);
	}

	@Override
	public AttributeCategory getAttributeCategoryById(Long id) {
		return attributeCategoryRepository.getEntityById(id);
	}

	@Override
	public Long getAttributeCount(String searchPattern) {
		return attributeRepository.getAttributeCount(searchPattern);
	}

	@Override
	public Attribute getAttributeById(Long id) {
		return attributeRepository.getEntityById(id);
	}

	@Override
	public AttributeCategory createAttributeCategory(String name,
			String context, int weight) throws ResourceCreationException {

		if (attributeCategoryRepository.nameExists(name)) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"attribute.nameInUse.error", name);
		}

		AttributeCategory attributeCategory = new AttributeCategory();
		attributeCategory.setName(name);
		attributeCategory.setContext(context);
		attributeCategory.setWeight(weight);
		attributeCategoryRepository.saveEntity(attributeCategory);
		return attributeCategory;
	}

	@Override
	public Attribute updateAttribute(Attribute attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, Boolean readOnly, Boolean encrypted,
			String variableName) throws ResourceCreationException {

		attribute.setName(name);
		if (attributeRepository.nameExists(attribute)) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"attribute.nameInUse.error", name);
		}

		attribute.setCategory(attributeCategoryRepository
				.getEntityById(category));
		attribute.setDescription(description);
		attribute.setDefaultValue(defaultValue);
		attribute.setWeight(weight);
		if (type.equals("TEXT")) {
			attribute.setType(AttributeType.TEXT);
		} else {
			attribute.setType(AttributeType.SELECT);
		}
		attribute.setReadOnly(readOnly);
		attribute.setEncrypted(encrypted);
		attribute.setVariableName(variableName);
		attributeRepository.saveEntity(attribute);
		return attribute;
	}

	@Override
	public Attribute createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			Boolean readOnly, Boolean encrypted, String variableName)
			throws ResourceCreationException {

		Attribute attribute = new Attribute();
		attribute.setName(name);

		if (attributeRepository.nameExists(attribute)) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"attribute.nameInUse.error", name);
		}

		attribute.setCategory(attributeCategoryRepository
				.getEntityById(category));
		attribute.setDescription(description);
		attribute.setDefaultValue(defaultValue);
		attribute.setWeight(weight);
		if (type.equals("TEXT")) {
			attribute.setType(AttributeType.TEXT);
		} else {
			attribute.setType(AttributeType.SELECT);
		}
		attribute.setReadOnly(readOnly);
		attribute.setEncrypted(encrypted);
		attribute.setVariableName(variableName);
		attributeRepository.saveEntity(attribute);
		return attribute;
	}

	@Override
	public void deleteAttribute(Attribute attribute) {
		attributeRepository.deleteEntity(attribute);
	}

}
