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
import com.hypersocket.migration.exception.MigrationProcessRealmAlreadyExistsThrowable;
import com.hypersocket.migration.exception.MigrationProcessRealmNotFoundException;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.exporter.MigrationExporter;
import com.hypersocket.migration.importer.MigrationImporter;
import com.hypersocket.migration.info.MigrationHelperClassesInfoProvider;
import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.migration.mapper.MigrationObjectMapper;
import com.hypersocket.migration.repository.MigrationExportCriteriaBuilder;
import com.hypersocket.migration.repository.MigrationLookupCriteriaBuilder;
import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.migration.util.MigrationUtil;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.resource.AbstractResource;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.upload.FileUploadService;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

    @Autowired
    FileUploadService fileUploadService;

    private Map<Class<?>, MigrationImporter> migrationImporterMap;

    private Map<Class<?>, MigrationExporter> migrationExporterMap;

    private Map<Class<?>, MigrationExportCriteriaBuilder> migrationExportCriteriaBuilderMap;

    private Map<Class<?>, MigrationLookupCriteriaBuilder> migrationLookupCriteriaBuilderMap;

    @PostConstruct
    private void postConstruct() {
        migrationImporterMap = migrationHelperClassesInfoProvider.getMigrationImporterMap();
        migrationExporterMap = migrationHelperClassesInfoProvider.getMigrationExporterMap();
        migrationExportCriteriaBuilderMap = migrationHelperClassesInfoProvider.getMigrationExportCriteriaBuilder();
        migrationLookupCriteriaBuilderMap = migrationHelperClassesInfoProvider.getMigrationLookupCriteriaBuilder();
    }

    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void importJson(String group, ArrayNode arrayNode, MigrationRealm migrationRealm, MigrationExecutorTracker migrationExecutorTracker) throws MigrationProcessRealmAlreadyExistsThrowable {
        try{
            log.info("Processing import for group {}.", group);
            Realm realm = migrationRealm.realm;

            if(realm != null) {
                log.info("Processing import in realm {}", realm.getName());
                migrationCurrentStack.addRealm(realm);
            }

            ObjectMapper objectMapper = migrationObjectMapper.getObjectMapper();

            Iterator<JsonNode> jsonNodeIterator = arrayNode.iterator();
            int record = 0;
            while (jsonNodeIterator.hasNext()) {
                JsonNode node = null;
                try {
                    JsonNode nodeObjectPack = jsonNodeIterator.next();

                    node = nodeObjectPack.get("entity");

                    ++record;

                    String className = node.get("_meta").asText();

                    if (StringUtils.isBlank(className)) {
                        throw new IllegalStateException(String.format("Class type info not found, cannot parse json %s",
                                node.toString()));
                    }

                    Class resourceClass = MigrationExecutor.class.getClassLoader().loadClass(className);
                    LookUpKey lookUpKey = migrationUtil.captureEntityLookup(node, resourceClass);

                    log.info("The look up key is {}", lookUpKey);

                    AbstractEntity resource = null;

                    if (realm != null) {
                        if (migrationLookupCriteriaBuilderMap.containsKey(resourceClass)) {
                            MigrationLookupCriteriaBuilder migrationLookupCriteriaBuilder = migrationLookupCriteriaBuilderMap.get(resourceClass);
                            DetachedCriteria detachedCriteria = migrationLookupCriteriaBuilder.make(realm, lookUpKey, node);
                            List<?> list = migrationRepository.executeCriteria(detachedCriteria);
                            resource = list != null && !list.isEmpty() ? (AbstractEntity) list.get(0) : null;
                        } else {
                            resource = (AbstractEntity) migrationRepository.findEntityByLookUpKey(resourceClass, lookUpKey, realm);
                        }
                    } else if (realm == null && Realm.class.equals(resourceClass)) {
                        resource = migrationRepository.findRealm(lookUpKey);
                    }

                    if (resource == null && migrationUtil.isResourceAllowNameOnlyLookUp(resourceClass)) {
                        //if no match with legacy let us see if some thing matching in realm by name
                        resource = (AbstractEntity) migrationRepository.findEntityByNameLookUpKey(resourceClass, lookUpKey, realm);
                    }

                    if (resource == null) {
                        log.info("Resource not found creating new.");
                        resource = (AbstractEntity) resourceClass.newInstance();
                    } else {
                        log.info("Resource found merging to resource with id {}", resource.getId());
                    }

                    ObjectReader objectReader = objectMapper.readerForUpdating(resource);

                    resource = (AbstractEntity) objectReader.treeAsTokens(node).readValueAs(resourceClass);

                    if (Realm.class.equals(resourceClass)) {
                        migrationRealm.realm = (Realm) resource;
                        realm = migrationRealm.realm;
                        //mergeData is false and realm by same name exists, throw error
                        if(!migrationRealm.mergeData && realmService.getRealmByName(realm.getName()) != null) {
                            throw new MigrationProcessRealmAlreadyExistsThrowable();
                        }
                    }

                    migrationUtil.fillInRealm(resource);

                    handleTransientLocalGroup(resource);

                    if (migrationImporterMap.containsKey(resourceClass)) {
                        MigrationImporter migrationImporter = migrationImporterMap.get(resourceClass);
                        log.info("Found migration importer for class {} as {}", resourceClass.getCanonicalName(),
                                migrationImporter.getClass().getCanonicalName());
                        migrationImporter.process(resource);
                    }

                    migrationRepository.saveOrUpdate(resource);

                    if (migrationImporterMap.containsKey(resourceClass)) {
                        MigrationImporter migrationImporter = migrationImporterMap.get(resourceClass);
                        log.info("Performing post save operation for class {} as {}", resourceClass.getCanonicalName(),
                                migrationImporter.getClass().getCanonicalName());
                        migrationImporter.postSave(resource);
                    }

                    handleDatabaseProperties(nodeObjectPack, resource);

                    migrationExecutorTracker.incrementSuccess();
                }catch (Exception e) {
                    log.error("Problem in importing record number {} of group {}", record, group, e);
                    if(node != null) {
                        migrationExecutorTracker.incrementFailure();
                        migrationExecutorTracker.addErrorNode(String.format("%s_%d", group, record), node);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Problem in importing group {}", group, e);
        } finally {
            migrationCurrentStack.clearState();
        }
    }

    @SuppressWarnings("unchecked")
    public void startRealmExport(OutputStream outputStream, Realm realm, Set<String> entities) {
        JsonGenerator jsonGenerator = null;
        ZipOutputStream zos = (ZipOutputStream) outputStream;
        try {
            JsonFactory jsonFactory = new JsonFactory();
            jsonGenerator = jsonFactory.createGenerator(outputStream);
            jsonGenerator.setCodec(migrationObjectMapper.getObjectMapper());

            if (Boolean.getBoolean("hypersocket.development")) {
                jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
            }
            boolean filtered = entities.size() > 0;

            MultiValueMap migrationOrderMap = (MultiValueMap) migrationHelperClassesInfoProvider.getMigrationOrderMap();

            Set<Short> keys = migrationOrderMap.keySet();
            for (Short key : keys) {
                Collection migrationClassesList = migrationOrderMap.getCollection(key);
                for (Object migrationClasses : migrationClassesList) {
                    for (Class<? extends AbstractEntity<Long>> aClass : (List<Class<? extends AbstractEntity<Long>>>) migrationClasses) {
                        if(filtered && !entities.contains(aClass.getSimpleName())) {
                            log.info("Class for export {} filtered out", aClass.getCanonicalName());
                            continue;
                        }
                        log.info("Processing class for export {}", aClass.getCanonicalName());
                        ZipEntry anEntry = new ZipEntry(String.format("%s.json", aClass.getSimpleName()));
                        zos.putNextEntry(anEntry);
                        List<AbstractEntity<Long>> objectList = null;
                        if (migrationExportCriteriaBuilderMap.containsKey(aClass)) {
                            log.info("Criteria builder found for class {}", aClass.getSimpleName());
                            MigrationExportCriteriaBuilder migrationExportCriteriaBuilder = migrationExportCriteriaBuilderMap.get(aClass);
                            DetachedCriteria criteria = migrationExportCriteriaBuilder.make(realm);
                            objectList = migrationRepository.executeCriteria(criteria);
                        } else {
                            objectList = (List<AbstractEntity<Long>>) migrationRepository.findAllResourceInRealmOfType(aClass, realm);
                        }

                        List<MigrationObjectWithMeta> migrationObjectWithMetas = new ArrayList<>();

                        for (AbstractEntity abstractEntity : objectList) {
                            List<DatabaseProperty> databaseProperties;
                            if (abstractEntity instanceof AbstractResource) {
                                databaseProperties = migrationRepository.findAllDatabaseProperties((AbstractResource) abstractEntity);
                            } else {
                                databaseProperties = Collections.emptyList();
                            }
                            MigrationObjectWithMeta migrationObjectWithMeta = new MigrationObjectWithMeta(abstractEntity, databaseProperties);
                            migrationObjectWithMetas.add(migrationObjectWithMeta);
                        }

                        ObjectPack objectPack = new ObjectPack(aClass.getCanonicalName(),
                                migrationObjectWithMetas);

                        if (migrationExporterMap.containsKey(aClass)) {
                            MigrationExporter migrationExporter = migrationExporterMap.get(aClass);
                            log.info("Found migration exporter for class {} as {}", aClass.getCanonicalName(),
                                    migrationExporter.getClass().getCanonicalName());
                            Map<String, List<Map<String, ?>>> customOperationsMap =
                                    migrationExporter.produceCustomOperationsMap(realm);
                            objectPack.setCustomOperationsMap(customOperationsMap);
                        }

                        jsonGenerator.writeObject(objectPack);
                        zos.closeEntry();
                        jsonGenerator.flush();
                    }
                }
            }
        }catch (IOException e) {
            log.error("Problem in import process.", e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    public MigrationExecutorTracker startRealmImport(InputStream inputStream, boolean mergeData) throws AccessDeniedException,
            ResourceCreationException, MigrationProcessRealmAlreadyExistsThrowable {
        JsonParser jsonParser = null;
        MigrationExecutorTracker migrationExecutorTracker = new MigrationExecutorTracker();
        try {
            MigrationRealm migrationRealm = new MigrationRealm();
            migrationRealm.mergeData = mergeData;

            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry ze = zipInputStream.getNextEntry();
            /**
             * In this process logic json files should be processed first, with realm file as the first entry,
             * as all other entries will go in that realm.
             */
            while(ze!=null) {
                String fileName = ze.getName();
                if(!"Realm.json".equals(fileName) && migrationRealm.realm == null ) {
                    if(mergeData) {
                        migrationRealm.realm = realmService.getCurrentRealm();
                    } else {
                        throw new MigrationProcessRealmNotFoundException();
                    }
                }
                if(fileName.contains("uploadedFiles\\")) {
                    //store file
                    String fileNameStripped = fileName.substring(fileName.indexOf("\\") + 1);
                    //file upload service createFile method closes input stream hence we need to extract content into byte array
                    //and pass in that,
                    byte[] data = IOUtils.toByteArray(zipInputStream);
                    ByteArrayInputStream dataByteStream = new ByteArrayInputStream(data);
                    fileUploadService.createFile(dataByteStream, fileNameStripped, migrationRealm.realm, true);
                }else {
                    //process json
                    ObjectMapper objectMapper = migrationObjectMapper.getObjectMapper();
                    jsonParser = objectMapper.getFactory().createParser(zipInputStream);
                    jsonParser.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
                    JsonToken jsonToken;
                    while ((jsonToken = jsonParser.nextToken()) != null) {
                        if (jsonToken == JsonToken.START_OBJECT) {
                            ObjectNode objectPack = jsonParser.readValueAsTree();
                            importJson(objectPack.get("group").asText(),
                                    (ArrayNode) objectPack.get("objectList"),
                                    migrationRealm, migrationExecutorTracker);
                            importCustomOperations(objectPack.get("group").asText(),
                                    objectPack.get("customOperationsMap"),
                                    migrationRealm, migrationExecutorTracker);
                        }
                    }
                }

                zipInputStream.closeEntry();
                ze = zipInputStream.getNextEntry();
            }
        }catch (IOException e) {
            log.error("Problem in import process.", e);
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

        return migrationExecutorTracker;
    }

    private void importCustomOperations(String group, JsonNode jsonNode, MigrationRealm migrationRealm, MigrationExecutorTracker migrationExecutorTracker) {
        try {
            Class resourceClass = MigrationExecutor.class.getClassLoader().loadClass(group);

            if(migrationImporterMap.containsKey(resourceClass)) {
                MigrationImporter migrationImporter = migrationImporterMap.get(resourceClass);
                log.info("Found migration importer for class {} as {}", resourceClass.getCanonicalName(),
                        migrationImporter.getClass().getCanonicalName());
                migrationImporter.processCustomOperationsMap(jsonNode, migrationRealm.realm);
                migrationExecutorTracker.incrementCustomOperationSuccess();
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Problem in custom import operation for {}", group, e);
            migrationExecutorTracker.incrementCustomOperationFailure();
            migrationExecutorTracker.addErrorNode(String.format("%s_custom_operation", group), jsonNode);
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

    public static class MigrationRealm {
        public Realm realm;
        public boolean mergeData;
    }
}
