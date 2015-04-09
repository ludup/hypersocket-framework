package com.hypersocket.attributes;

import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.events.AttributeCategoryCreatedEvent;
import com.hypersocket.attributes.events.AttributeCategoryDeletedEvent;
import com.hypersocket.attributes.events.AttributeCategoryEvent;
import com.hypersocket.attributes.events.AttributeCategoryUpdatedEvent;
import com.hypersocket.attributes.events.AttributeCreatedEvent;
import com.hypersocket.attributes.events.AttributeDeletedEvent;
import com.hypersocket.attributes.events.AttributeEvent;
import com.hypersocket.attributes.events.AttributeUpdatedEvent;
import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.tables.ColumnSort;

@Service
public class AttributeServiceImpl extends AbstractAuthenticatedServiceImpl implements
		AttributeService {

	public static final String RESOURCE_BUNDLE = "UserAttributes";

	@Autowired
	I18NService i18nService;

	@Autowired
	AttributeRepository attributeRepository;

	@Autowired
	AttributeCategoryRepository attributeCategoryRepository;

	@Autowired
	EventService eventService;

	@PostConstruct
	private void postConstruct() {

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.attributes");

		for (AttributePermission p : AttributePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(AttributeEvent.class, RESOURCE_BUNDLE);
		eventService
				.registerEvent(AttributeCreatedEvent.class, RESOURCE_BUNDLE);
		eventService
				.registerEvent(AttributeUpdatedEvent.class, RESOURCE_BUNDLE);
		eventService
				.registerEvent(AttributeDeletedEvent.class, RESOURCE_BUNDLE);

		eventService.registerEvent(AttributeCategoryEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(AttributeCategoryCreatedEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(AttributeCategoryUpdatedEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(AttributeCategoryDeletedEvent.class,
				RESOURCE_BUNDLE);
	}

	@Override
	public List<AttributeCategory> getCategories() throws AccessDeniedException {
		assertPermission(AttributePermission.READ);
		return attributeCategoryRepository.allEntities();
	}

	@Override
	public List<Attribute> searchAttributes(String searchPattern, int start,
			int length, ColumnSort[] sorting) throws AccessDeniedException {
		assertPermission(AttributePermission.READ);
		return attributeRepository.searchAttributes(searchPattern, start,
				length, sorting);
	}

	@Override
	public List<AttributeCategory> searchAttributeCategories(
			String searchPattern, int start, int length, ColumnSort[] sorting)
			throws AccessDeniedException {
		assertPermission(AttributePermission.READ);
		return attributeCategoryRepository.searchAttributeCategories(
				searchPattern, start, length, sorting);
	}

	@Override
	public Collection<String> getContexts() {
		return ResourceTemplateRepositoryImpl.getContextNames();
	}

	@Override
	public AttributeCategory getAttributeCategoryById(Long id)
			throws AccessDeniedException {
		assertPermission(AttributePermission.READ);
		return attributeCategoryRepository.getEntityById(id);
	}

	@Override
	public Long getAttributeCount(String searchPattern)
			throws AccessDeniedException {
		assertPermission(AttributePermission.READ);
		return attributeRepository.getAttributeCount(searchPattern);
	}

	@Override
	public Long getAttributeCategoryCount(String searchPattern)
			throws AccessDeniedException {
		assertPermission(AttributePermission.READ);
		return attributeCategoryRepository
				.getAttributeCategoryCount(searchPattern);
	}

	@Override
	public Attribute getAttributeById(Long id) throws AccessDeniedException {
		assertPermission(AttributePermission.READ);
		return attributeRepository.getEntityById(id);
	}

	@Override
	public AttributeCategory createAttributeCategory(String name,
			String context, int weight) throws ResourceCreationException,
			AccessDeniedException {
		assertPermission(AttributePermission.CREATE);
		AttributeCategory attributeCategory = new AttributeCategory();
		try {
			attributeCategory.setName(name);
			if (attributeCategoryRepository.nameExists(attributeCategory)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"attribute.catInUse.error", name);
			}

			attributeCategory.setName(name);
			attributeCategory.setContext(context);
			attributeCategory.setWeight(weight);
			attributeCategoryRepository.saveCategory(attributeCategory);

			eventService.publishEvent(new AttributeCategoryCreatedEvent(this,
					getCurrentSession(), attributeCategory));
			return attributeCategory;
		} catch (Exception e) {
			eventService.publishEvent(new AttributeCategoryCreatedEvent(this,
					e, getCurrentSession(), attributeCategory));
			throw e;
		}
	}

	@Override
	public AttributeCategory updateAttributeCategory(
			AttributeCategory category, Long id, String name, String context,
			int weight) throws ResourceChangeException, AccessDeniedException {
		assertPermission(AttributePermission.UPDATE);
		String oldName = category.getName();
		try {
			if (attributeCategoryRepository.nameExists(category)) {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
						"attribute.nameInUse.error", name);
			}

			category.setId(id);
			category.setName(name);
			category.setContext(context);
			category.setWeight(weight);
			attributeCategoryRepository.saveCategory(category);
			eventService.publishEvent(new AttributeCategoryUpdatedEvent(this,
					getCurrentSession(), oldName, category));
			return category;

		} catch (ResourceChangeException ex) {
			eventService.publishEvent(new AttributeCategoryUpdatedEvent(this,
					ex, getCurrentSession(), category));
			throw ex;
		} catch (Throwable t) {
			eventService.publishEvent(new AttributeCategoryUpdatedEvent(this,
					t, getCurrentSession(), category));
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.failedToUpdateCategory", t.getMessage());
		}

	}

	@Override
	public Attribute updateAttribute(Attribute attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, Boolean readOnly, Boolean encrypted,
			String variableName) throws ResourceChangeException,
			AccessDeniedException {
		String oldName = attribute.getName();
		try {
			assertPermission(AttributePermission.UPDATE);

			attribute.setName(name);
			if (attributeRepository.nameExists(attribute)) {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
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
				attribute.setType(AttributeType.PASSWORD);
			}

			attribute.setReadOnly(readOnly);
			attribute.setEncrypted(encrypted);
			attribute.setVariableName(variableName);

			attributeRepository.saveAttribute(attribute);

			eventService.publishEvent(new AttributeUpdatedEvent(this,
					getCurrentSession(), oldName, attribute));
			return attribute;
		} catch (ResourceChangeException ex) {
			eventService.publishEvent(new AttributeUpdatedEvent(this, ex,
					getCurrentSession(), attribute));
			throw ex;
		} catch (Throwable t) {
			eventService.publishEvent(new AttributeUpdatedEvent(this, t,
					getCurrentSession(), attribute));
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.failedToUpdateAttribute", t.getMessage());
		}

	}

	@Override
	public Attribute createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			Boolean readOnly, Boolean encrypted, String variableName)
			throws ResourceCreationException, AccessDeniedException {
		Attribute attribute = new Attribute();
		try {
			assertPermission(AttributePermission.CREATE);

			attribute.setName(name);

			if (attributeRepository.nameExists(attribute)) {

				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"attribute.nameInUse.error", name);
			}

			AttributeCategory cat = attributeCategoryRepository
					.getEntityById(category);
			attribute.setCategory(cat);
			attribute.setDescription(description);
			attribute.setDefaultValue(defaultValue);
			attribute.setWeight(weight);
			attribute.setType(AttributeType.valueOf(type));
			attribute.setReadOnly(readOnly);
			attribute.setEncrypted(encrypted);
			attribute.setVariableName(variableName);

			attributeRepository.saveAttribute(attribute);

			ResourceTemplateRepositoryImpl.registerNewAttribute(
					cat.getContext(), attribute);

			eventService.publishEvent(new AttributeCreatedEvent(this,
					getCurrentSession(), attribute));
			return attribute;
		} catch (ResourceCreationException ex) {
			eventService.publishEvent(new AttributeCreatedEvent(this, ex,
					getCurrentSession(), attribute));
			throw ex;
		} catch (Throwable t) {
			eventService.publishEvent(new AttributeCreatedEvent(this, t,
					getCurrentSession(), attribute));
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.failedToCreateAttribute", t.getMessage());
		}
	}

	@Override
	public void deleteAttribute(Attribute attribute)
			throws AccessDeniedException {
		try {
			assertPermission(AttributePermission.DELETE);
			attributeRepository.deleteEntity(attribute);
			eventService.publishEvent(new AttributeDeletedEvent(this,
					getCurrentSession(), attribute));
		} catch (Throwable e) {
			eventService.publishEvent(new AttributeDeletedEvent(this, e,
					getCurrentSession(), attribute));
			throw e;
		}

	}

	@Override
	public void deleteAttributeCategory(AttributeCategory category)
			throws AccessDeniedException {
		try {
			assertPermission(AttributePermission.DELETE);
			attributeCategoryRepository.deleteEntity(category);
			eventService.publishEvent(new AttributeCategoryDeletedEvent(this,
					getCurrentSession(), category));
		} catch (Throwable e) {
			eventService.publishEvent(new AttributeCategoryDeletedEvent(this,
					e, getCurrentSession(), category));
			throw e;
		}

	}

	@Override
	public Attribute getAttributeByName(String name) throws AccessDeniedException {

		assertPermission(AttributePermission.READ);
		return attributeRepository.getAttributeByName(name);
	}
	
	@Override
	public AttributeCategory getCategoryByName(String name) throws AccessDeniedException {

		assertPermission(AttributePermission.READ);
		return attributeCategoryRepository.getCategoryByName(name);
	}

	@Override
	public Long getMaximumCategoryWeight() throws AccessDeniedException {
		assertPermission(AttributePermission.READ);
		return attributeCategoryRepository.getMaximumCategoryWeight();
	}

	@Override
	public Long getMaximumAttributeWeight(AttributeCategory cat) throws AccessDeniedException {
		assertPermission(AttributePermission.READ);
		return attributeRepository.getMaximumAttributeWeight(cat);
	}

}
