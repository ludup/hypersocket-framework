package com.hypersocket.migration.execution;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hypersocket.local.LocalGroup;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.importer.MigrationImporter;
import com.hypersocket.migration.info.MigrationHelperClassesInfoProvider;
import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.migration.mapper.MigrationObjectMapper;
import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.migration.util.MigrationUtil;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.resource.AbstractResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Service
public class MigrationExecutor {

    static Logger log = LoggerFactory.getLogger(MigrationExecutor.class);

    @Autowired
    MigrationObjectMapper migrationObjectMapper;

    @Autowired
    MigrationCurrentStack migrationCurrentStack;

    @Autowired
    MigrationUtil migrationUtil;

    @Autowired
    MigrationRepository migrationRepository;

    @Autowired
    RealmService realmService;

    @Autowired
    RealmRepository realmRepository;

    @Autowired
    MigrationHelperClassesInfoProvider migrationHelperClassesInfoProvider;

    private Map<String, MigrationImporter> migrationImporterMap;

    @PostConstruct
    private void postConstruct() {
        migrationImporterMap = migrationHelperClassesInfoProvider.getMigrationImporterMap();
    }

    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void importJson(String group, ArrayNode arrayNode, Realm realm) {
        int record = 0;
        try{
            if(realm == null) {
                realm = realmService.getCurrentRealm();
            }

            log.info("Processing import for group {} in realm {}", group, realm.getName());
            migrationCurrentStack.addRealm(realm);

            ObjectMapper objectMapper = migrationObjectMapper.getObjectMapper();

            Iterator<JsonNode> jsonNodeIterator = arrayNode.iterator();
            while (jsonNodeIterator.hasNext()) {
                JsonNode nodeObjectPack = jsonNodeIterator.next();

                JsonNode node = nodeObjectPack.get("entity");

                String className = node.get("_meta").asText();
                log.info("Meta class name found as {}", className);

                if (StringUtils.isBlank(className)) {
                    throw new IllegalStateException(String.format("Class type info not found, cannot parse json %s",
                            node.toString()));
                }

                Class resourceClass = MigrationExecutor.class.getClassLoader().loadClass(className);
                LookUpKey lookUpKey = migrationUtil.captureEntityLookup(node, resourceClass);

                ++record;

                AbstractEntity resource = (AbstractEntity) migrationRepository.findEntityByLookUpKey(resourceClass, lookUpKey, realm);
                if (resource == null) {
                    resource = (AbstractEntity) resourceClass.newInstance();
                }
                ObjectReader objectReader = objectMapper.readerForUpdating(resource);

                resource = (AbstractEntity) objectReader.treeAsTokens(node).readValueAs(resourceClass);

                migrationUtil.fillInRealm(resource);

                handleTransientLocalGroup(resource);

                if(migrationImporterMap.containsKey(resourceClass.getCanonicalName())) {
                    MigrationImporter migrationImporter = migrationImporterMap.get(resourceClass.getCanonicalName());
                    log.info("Found migration importer for class {} as {}", resourceClass.getCanonicalName(),
                            migrationImporter.getClass().getCanonicalName());
                    migrationImporter.process(resource);
                }

                migrationRepository.saveOrUpdate(resource);

                handleDatabaseProperties(nodeObjectPack, resource);
            }
        } catch (Exception e) {
            log.error("Problem in importing record number {} of group {}", record, group, e);
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            migrationCurrentStack.clearState();
        }
    }

    public void startRealmExport(OutputStream outputStream, Realm realm) {
        JsonGenerator jsonGenerator = null;
        try {
            if(realm == null) {
                realm = realmService.getCurrentRealm();
            }
            JsonFactory jsonFactory = new JsonFactory();
            jsonGenerator = jsonFactory.createGenerator(outputStream);
            jsonGenerator.setCodec(migrationObjectMapper.getObjectMapper());

            if (Boolean.getBoolean("hypersocket.development")) {
                jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
            }

            Map<Short, List<Class<? extends AbstractEntity<Long>>>> migrationOrderMap = migrationHelperClassesInfoProvider.getMigrationOrderMap();

            jsonGenerator.writeStartArray();
            Set<Short> keys = migrationOrderMap.keySet();
            for (Short key : keys) {
                List<Class<? extends AbstractEntity<Long>>> migrationClasses = migrationOrderMap.get(key);
                for (Class<? extends AbstractEntity<Long>> aClass : migrationClasses) {
                    List<AbstractEntity> objectList = migrationRepository.findAllResourceInRealmOfType(aClass, realm);
                    List<MigrationObjectWithMeta> migrationObjectWithMetas = new ArrayList<>();

                    for (AbstractEntity abstractEntity : objectList) {
                        List<DatabaseProperty> databaseProperties;
                        if(abstractEntity instanceof AbstractResource) {
                            databaseProperties = migrationRepository.findAllDatabaseProperties((AbstractResource) abstractEntity);
                        } else {
                            databaseProperties = Collections.emptyList();
                        }
                        MigrationObjectWithMeta migrationObjectWithMeta = new MigrationObjectWithMeta(abstractEntity, databaseProperties);
                        migrationObjectWithMetas.add(migrationObjectWithMeta);
                    }

                    ObjectPack objectPack = new ObjectPack(aClass.getCanonicalName(),
                            migrationObjectWithMetas);
                    jsonGenerator.writeObject(objectPack);
                    jsonGenerator.flush();
                }
            }
            jsonGenerator.writeEndArray();
        }catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }finally {
            if(jsonGenerator != null) {
                try {
                    jsonGenerator.close();
                }catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    public void startRealmImport(InputStream inputStream, Realm realm) {
        JsonParser jsonParser = null;
        try {
            ObjectMapper objectMapper = migrationObjectMapper.getObjectMapper();
            jsonParser = objectMapper.getFactory().createParser(inputStream);
            JsonToken jsonToken;
            while((jsonToken = jsonParser.nextToken()) != null) {
                if(jsonToken == JsonToken.START_OBJECT) {
                    ObjectNode objectPack = jsonParser.readValueAsTree();
                    importJson(objectPack.get("group").asText(),
                            (ArrayNode) objectPack.get("objectList"),
                            realm);
                }
            }
        }catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            if(jsonParser != null) {
                try {
                    jsonParser.close();
                }catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    /**
     * {@link LocalGroup} can have many to many reference back to a {@link LocalGroup}, in such a case it might be the
     * the mapped set is having transient instance when processing an instance, this method wals the collection,
     * and saves all transient instances.
     *
     * <br />
     *
     * For instance, group 'Super' has 'Local', 'Admin' groups under it, while saving 'Super' for the first time,
     * it might be case, 'Local' and 'Admin' both are new too, hence we need to save them too in current transaction,
     * else Hibernate complaints for save Transient before.
     *
     * @param localGroup
     */
    private void processTransientLocalGroup(LocalGroup localGroup) {
        Set<LocalGroup> localGroups = localGroup.getGroups();
        for (LocalGroup group : localGroups) {
            processTransientLocalGroup(group);
        }

        if(localGroup.getId() == null) {
            migrationRepository.saveOrUpdate(localGroup);
        }
    }

    private void handleTransientLocalGroup(AbstractEntity resource) {
        if(resource instanceof LocalGroup) {
            processTransientLocalGroup((LocalGroup) resource);
        }
    }

    private void handleDatabaseProperties(JsonNode nodeObjectPack, AbstractEntity resource) {
        if(resource instanceof AbstractResource) {
            //currently associated if any
            List<DatabaseProperty> databasePropertyList = realmRepository.getPropertiesForResource((AbstractResource) resource);

            Map<String, String> databasePropertiesMap = new HashMap<>();
            JsonNode databasePropertiesObjectPack = nodeObjectPack.get("databaseProperties");
            Iterator<JsonNode> databasePropertiesIterator = databasePropertiesObjectPack.iterator();
            while (databasePropertiesIterator.hasNext()) {
                JsonNode databaseProperties = databasePropertiesIterator.next();
                String key = databaseProperties.get("resourceKey").asText();
                String value = databaseProperties.get("value") == null ? null : databaseProperties.get("value").asText();
                log.info("Recieved database property as {} and {}", key, value);
                databasePropertiesMap.put(key, value);
            }

            //matching found :: update
            for (DatabaseProperty databaseProperty : databasePropertyList) {
                String key = databaseProperty.getResourceKey();
                String value = databasePropertiesMap.get(key);
                if(databasePropertiesMap.containsKey(key)) {
                    databaseProperty.setValue(value);
                    databasePropertiesMap.remove(key);
                }
            }

            //no match new values : insert
            Set<String> keySet = databasePropertiesMap.keySet();
            for (String key: keySet) {
                DatabaseProperty databaseProperty = new DatabaseProperty();
                databaseProperty.setResourceKey(key);
                databaseProperty.setValue(databasePropertiesMap.get(key));
                databaseProperty.setResourceId((Long) resource.getId());
                databasePropertyList.add(databaseProperty);
            }

            //save or update all
            for (DatabaseProperty databaseProperty : databasePropertyList) {
                migrationRepository.saveOrUpdate(databaseProperty);
            }
        }
    }
}
