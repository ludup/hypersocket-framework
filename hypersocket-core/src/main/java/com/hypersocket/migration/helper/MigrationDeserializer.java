package com.hypersocket.migration.helper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.hypersocket.migration.execution.stack.MigrationCurrentInfo;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.mapper.MigrationObjectMapper;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.Resource;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class MigrationDeserializer extends StdDeserializer<Resource> {

    @Autowired
    MigrationCurrentStack migrationCurrentStack;

    @Autowired
    MigrationObjectMapper migrationObjectMapper;

    @Autowired
    RealmService realmService;

    @Autowired
    PermissionService permissionService;

    protected MigrationDeserializer() {
        this(null);
    }

    protected MigrationDeserializer(Class<Resource> t) {
        super(t);
    }

    @Override
    public Resource deserialize(JsonParser p, DeserializationContext ctxt)  {
        try {
            boolean isCollection = false;
            boolean isValueToUpdateFromDb = false;
            JsonNode node = p.getCodec().readTree(p);
            MigrationCurrentInfo migrationCurrentInfo = migrationCurrentStack.getState();
            Resource resourceRootBean = (Resource) migrationCurrentInfo.getBean();
            String propertyName = migrationCurrentInfo.getPropName();

            System.out.println("The bean " + migrationCurrentInfo.getBean());
            System.out.println("The propname " + migrationCurrentInfo.getPropName());

            String className = node.get("_meta").asText();
            //we need to check here if we have key name or not
            //else we need to have a switch of key lookups
            //Permissions have resourceKey, whats next ????
            String entityName = node.get("name").asText();

            Class resourceClass = Class.forName(className);

            Class propertyClass = PropertyUtils.getPropertyType(resourceRootBean, propertyName);
            if(Collection.class.isAssignableFrom(propertyClass)) {
                isCollection = true;
            }
            Object value = PropertyUtils.getProperty(resourceRootBean, propertyName);
            Object valueToUpdate = null;

            if (value != null) {
                if (isCollection) {
                    Collection<Resource> resourceCollection = (Collection<Resource>) value;
                    for (Resource resource : resourceCollection) {
                        if (resource.getName().equals(entityName)) {
                            valueToUpdate = resource;
                            break;
                        }
                    }
                } else {
                    valueToUpdate = value;
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
                valueToUpdate = realmService.findResourceInRealmByName(resourceClass, entityName);
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

            ObjectReader objectReader = null;
            if (valueToUpdate != null) {
                objectReader = migrationObjectMapper.getObjectMapper().readerForUpdating(valueToUpdate);
            } else {
                objectReader = migrationObjectMapper.getObjectMapper().readerFor(resourceClass);
            }

            Resource resource = (Resource) objectReader.treeAsTokens(node).readValueAs(resourceClass);

            if(isCollection) {
                //to begin with there was no collection
                // we need to create one.
                if(value == null) {
                    value = propertyClass.newInstance();
                    PropertyUtils.setProperty(resourceRootBean, propertyName, value);
                }

                //value to update is from db, we did not find match in loaded property
                //or value to update is null, we did not find it anywhere
                //this value needs to go back to collection
                if(isValueToUpdateFromDb || valueToUpdate == null) {
                    ((Collection) value).add(resource);
                }

            } else if (!isCollection && value == null) {
                //to begin with reference was not there when loaded from db
                //we need to set it back to root property
                PropertyUtils.setProperty(resourceRootBean, propertyName, resource);
            }

            return resource;

        }catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }
}
