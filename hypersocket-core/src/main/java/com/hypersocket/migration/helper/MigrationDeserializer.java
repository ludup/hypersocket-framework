package com.hypersocket.migration.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.hypersocket.migration.execution.MigrationContext;
import com.hypersocket.migration.execution.stack.MigrationCurrentInfo;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.migration.mapper.MigrationObjectMapper;
import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.migration.util.MigrationUtil;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.resource.SimpleResource;
import com.hypersocket.resource.Resource;

@Component
public class MigrationDeserializer extends StdDeserializer<AbstractEntity<?>> {

	private static final long serialVersionUID = 2901354269126106098L;

	static Logger log = LoggerFactory.getLogger(MigrationDeserializer.class);

    @Autowired
    MigrationCurrentStack migrationCurrentStack;
    
    @Autowired
    MigrationContext migrationContext;

    @Autowired
    MigrationObjectMapper migrationObjectMapper;

    @Autowired
    MigrationUtil migrationUtil;

    @Autowired
    MigrationRepository migrationRepository;

    protected MigrationDeserializer() {
        this(null);
    }

    protected MigrationDeserializer(Class<Resource> t) {
        super(t);
    }

    @SuppressWarnings("unchecked")
	@Override
    public AbstractEntity<?> deserialize(JsonParser p, DeserializationContext ctxt)  {
        try {
            boolean isCollection = false;
            boolean isValueToUpdateFromDb = false;
            JsonNode node = p.getCodec().readTree(p);
            MigrationCurrentInfo migrationCurrentInfo = migrationCurrentStack.getState();
            Realm realm = migrationContext.getCurrentRealm();
            AbstractEntity<Long> resourceRootBean = (AbstractEntity<Long>) migrationCurrentInfo.getBean();
            String propertyName = migrationCurrentInfo.getPropName();

            String className = node.get("_meta").asText();

            boolean isReference = node.get("reference") != null && node.get("reference").asBoolean();

            if(StringUtils.isBlank(className)) {
                throw new IllegalStateException(String.format("Class type info not found, cannot parse json %s", node.toString()));
            }

            Class<?> resourceClass = MigrationDeserializer.class.getClassLoader().loadClass(className);

            LookUpKey lookUpKey = migrationUtil.captureEntityLookup(node, resourceClass, false);

            log.info("The look up key is {}", lookUpKey);

            Class<?> propertyClass = PropertyUtils.getPropertyType(resourceRootBean, propertyName);
            if(Collection.class.isAssignableFrom(propertyClass)) {
                isCollection = true;
            }

            Object value = PropertyUtils.getProperty(resourceRootBean, propertyName);
            AbstractEntity<?> valueToUpdate = null;

            if (value != null) {
                if (isCollection) {
					Collection<AbstractEntity<?>> resourceCollection = (Collection<AbstractEntity<?>>) value;
                    outer : for (AbstractEntity<?> resource : resourceCollection) {
                        if(lookUpKey.isComposite()) {
                            String[] properties = lookUpKey.getProperties();
                            Object[] values = lookUpKey.getValues();
                            if(properties != null) {
                                boolean match = true;
                                for (int i = 0; i < properties.length; ++i) {
                                    Object property = PropertyUtils.getProperty(resource, properties[i]);
                                    if (property.equals(values[i])) {
                                        continue;
                                    }
                                    match = false;
                                    break;
                                }

                                if(match) {
                                    valueToUpdate = resource;
                                    break outer;
                                }
                            }
                        } else {
                            Object property = PropertyUtils.getProperty(resource, lookUpKey.getProperty());
                            if (property != null && property.equals(lookUpKey.getValue())) {
                                valueToUpdate = resource;
                                break;
                            }
                        }
                    }
                } else {
                    valueToUpdate = (AbstractEntity<?>) value;
                }
            }

            //once again check here if we can find value to update from db
            //may be new item has to be added which is not in current list of root loaded object
            //but present in database
            //book -> author, may be new update adding another author
            //this logic is not for deletes, e.g. it checks db list and sent list is missing an object
            //that means we have to delete tha
            //b1 - a1, a2 from db, sent is b1 - a1(partial update), a3 (new to add)
            //this does not means delete a2
            if(valueToUpdate == null) {
                //lets check db if we have something
                valueToUpdate = (AbstractEntity<?>) migrationRepository.findEntityByLookUpKey(resourceClass, lookUpKey, realm);
                if(SimpleResource.class.isAssignableFrom(resourceClass) && valueToUpdate == null && isReference && lookUpKey.isLegacyId()) {
                	// Some legacy records are bound to show legacy id in export json but in not in DB, due to legacy source code,
                	// for such records we fallback to resource id, just in case, as legacy id in json will map to resource id.
                	LookUpKey lookUpKeyWithResourceId = migrationUtil.captureEntityLookup(node, resourceClass, true);
                	valueToUpdate = (AbstractEntity<Long>) migrationRepository.findEntityByLookUpKey(resourceClass, lookUpKeyWithResourceId, realm);
                	if(valueToUpdate != null) {
                		log.info("The look up key is {} (Resource Id )", lookUpKeyWithResourceId);
                		//For some accidental case where import record shows legacy id which matches a record's resource id, but this record has
                		//its own legacy id, means this is accidental mismatch, should not be processed, match is not correct.
                		//Ideally this should not happen, but a check and log is better.
                		if(valueToUpdate.getLegacyId() != null) {
                			log.info("Resource with id {} has legacy id {}, resource will be set to null", valueToUpdate.getId(), valueToUpdate.getLegacyId());
                			valueToUpdate = null;
                		}
                	}
                }
                if((valueToUpdate == null && isReference) ||
                        (valueToUpdate == null && migrationUtil.isResourceAllowNameOnlyLookUp(resourceClass))) {
                    //if no match with legacy let us see if some thing matching in realm by name
                    valueToUpdate = (AbstractEntity<?>) migrationRepository.findEntityByNameLookUpKey(resourceClass, lookUpKey, realm);
                }
                if (valueToUpdate != null) {
                    isValueToUpdateFromDb = true;
                }
                //if still null means this is new entity to be saved.
            }
            //MigrationBeanDeserializer works on assumption we will not directly update references
            //if collection we need to add to it and refernce we need to merge
            //we do not want jackson to change root object reference
            //role ->* permissions
            //when we enter this logic we need jackson to work on instance permissions collection
            //default is, it will collect all new objects and set it with new instance of collection
            //as we have to respect hibernate, the data it returns we do not want to change references
            //our attempt is to merge into objects from hibernate, collection or reference,
            //i.e add to original collection, merge with original reference
            //if object was not present from hibernate we can set new reference
            //for collection or simple object

            if(valueToUpdate == null) {
                log.info("Resource not found creating new for class {}.", resourceClass);
                valueToUpdate = (AbstractEntity<?>) resourceClass.newInstance();
            } else {
                log.info("Resource found merging to resource with id {} and {}",
                        valueToUpdate.getId(), resourceClass);
            }

            ObjectReader objectReader = migrationObjectMapper.getObjectMapper().readerForUpdating(valueToUpdate);

            AbstractEntity<?> resource = (AbstractEntity<?>) objectReader.treeAsTokens(node).readValueAs(resourceClass);

            if(isCollection) {
                //to begin with there was no collection
                // we need to create one.
                if(value == null) {
                    if(Set.class.isAssignableFrom(propertyClass)) {
                    	value = new HashSet<>();
                    } else if(List.class.isAssignableFrom(propertyClass)) {
                    	value = new ArrayList<>();
                    } else {
                    	value = propertyClass.newInstance();
                    }
                    PropertyUtils.setProperty(resourceRootBean, propertyName, value);
                }

                //value to update is from db, we did not find match in loaded property
                //or value to update is new, no id, we did not find it anywhere
                //this value needs to go back to collection
                if(isValueToUpdateFromDb || valueToUpdate.getId() == null) {
                    ((Collection<AbstractEntity<?>>) value).add(resource);
                    String mappedBy = migrationUtil.getMappedBy(resourceRootBean, propertyName);
                    if(StringUtils.isNotBlank(mappedBy)) {
                        PropertyUtils.setProperty(resource, mappedBy, resourceRootBean);
                    }
                }

            } else if (!isCollection && value == null) {
                //to begin with reference was not there when loaded from db
                //we need to set it back to root property
                PropertyUtils.setProperty(resourceRootBean, propertyName, resource);
                String mappedBy = migrationUtil.getMappedBy(resourceRootBean, propertyName);
                if(StringUtils.isNotBlank(mappedBy)) {
                    PropertyUtils.setProperty(resource, mappedBy, resourceRootBean);
                }
            }

            migrationUtil.fillInRealm(resource);

            return resource;

        }catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }
}
