package com.hypersocket.migration.execution;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.local.LocalGroup;
import com.hypersocket.migration.customized.MigrationCustomExport;
import com.hypersocket.migration.customized.MigrationCustomImport;
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
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.resource.AbstractResource;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.upload.FileUploadService;

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
    
    @Autowired
    EncryptionService encryptionService;

	@Autowired
    MigrationContext migrationContext;


    private Map<Class<?>, MigrationImporter<AbstractEntity<Long>>> migrationImporterMap;

    private Map<Class<?>, MigrationExporter<AbstractEntity<Long>>> migrationExporterMap;

    private Map<Class<?>, MigrationExportCriteriaBuilder> migrationExportCriteriaBuilderMap;

    private Map<Class<?>, MigrationLookupCriteriaBuilder> migrationLookupCriteriaBuilderMap;
    
    private Map<String, MigrationCustomImport<?>> migrationCustomImports;
    
    private Collection<MigrationCustomExport<?>> migrationCustomExports;

    @PostConstruct
    private void postConstruct() {
        migrationImporterMap = migrationHelperClassesInfoProvider.getMigrationImporterMap();
        migrationExporterMap = migrationHelperClassesInfoProvider.getMigrationExporterMap();
        migrationExportCriteriaBuilderMap = migrationHelperClassesInfoProvider.getMigrationExportCriteriaBuilder();
        migrationLookupCriteriaBuilderMap = migrationHelperClassesInfoProvider.getMigrationLookupCriteriaBuilder();
        migrationCustomImports = migrationHelperClassesInfoProvider.getMigrationCustomImports();
        migrationCustomExports = migrationHelperClassesInfoProvider.getMigrationCustomExports();
    }

    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void importJson(String group, ArrayNode arrayNode, MigrationRealm migrationRealm, MigrationExecutorTracker migrationExecutorTracker) throws MigrationProcessRealmAlreadyExistsThrowable {
        try{
            log.info("Processing import for group {}.", group);
            Realm realm = migrationRealm.realm;

            if(realm != null) {
                log.info("Processing import in realm {}", realm.getName());
                migrationContext.addRealm(realm);
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

                    Class<?> resourceClass = MigrationExecutor.class.getClassLoader().loadClass(className);
                    LookUpKey lookUpKey = migrationUtil.captureEntityLookup(node, resourceClass, false);

                    log.info("The look up key is {}", lookUpKey);

                    AbstractEntity<Long> resource = null;

                    if (realm != null) {
                        if (migrationLookupCriteriaBuilderMap.containsKey(resourceClass)) {
                        	//explicit look up provided, we will use that
                            MigrationLookupCriteriaBuilder migrationLookupCriteriaBuilder = migrationLookupCriteriaBuilderMap.get(resourceClass);
                            DetachedCriteria detachedCriteria = migrationLookupCriteriaBuilder.make(realm, lookUpKey, node);
                            List<?> list = migrationRepository.executeCriteria(detachedCriteria);
                            resource = list != null && !list.isEmpty() ? (AbstractEntity<Long>) list.get(0) : null;
                        } else {
                            resource = (AbstractEntity<Long>) migrationRepository.findEntityByLookUpKey(resourceClass, lookUpKey, realm);
                            
                            if(AbstractResource.class.isAssignableFrom(resourceClass) && resource == null && lookUpKey.isLegacyId()) {
                            	// Some legacy records are bound to show legacy id in export json but in not in DB, due to legacy source code,
                            	// for such records we fallback to resource id, just in case, as legacy id in json will map to resource id.
                            	LookUpKey lookUpKeyWithResourceId = migrationUtil.captureEntityLookup(node, resourceClass, true);
                            	resource = (AbstractEntity<Long>) migrationRepository.findEntityByLookUpKey(resourceClass, lookUpKeyWithResourceId, realm);
                            	if(resource != null) {
                            		log.info("The look up key is {} (Resource Id )", lookUpKeyWithResourceId);
                            		//For some accidental case where import record shows legacy id which matches a record's resource id, but this record has
                            		//its own legacy id, means this is accidental mismatch, should not be processed, match is not correct.
                            		//Ideally this should not happen, but a check and log is better.
                            		if(resource.getLegacyId() != null) {
                            			log.info("Resource with id {} has legacy id {}, resource will be set to null", resource.getId(), resource.getLegacyId());
                            			resource = null;
                            		}
                            	}
                            }
                        }
                    } else if (realm == null && Realm.class.equals(resourceClass)) {
                        resource = migrationRepository.findRealm(lookUpKey);
                    }

                    if (resource == null && migrationUtil.isResourceAllowNameOnlyLookUp(resourceClass)) {
                        //if no match with legacy or resource id let us see if some thing matching in realm by name and if 
                    	//it is explicitly marked with annotation for name only look up
                        resource = (AbstractEntity<Long>) migrationRepository.findEntityByNameLookUpKey(resourceClass, lookUpKey, realm);
                    }

                    if (resource == null) {
                    	//nothing there for this, lets create new
                        log.info("Resource not found creating new.");
                        resource = (AbstractEntity<Long>) resourceClass.newInstance();
                    } else {
                        log.info("Resource found merging to resource with id {}", resource.getId());
                    }

                    ObjectReader objectReader = objectMapper.readerForUpdating(resource);

                    resource = (AbstractEntity<Long>) objectReader.treeAsTokens(node).readValueAs(resourceClass);

                    if (Realm.class.equals(resourceClass)) {
                    	Realm realmInProcess = (Realm) resource;
                        migrationRealm.realm = realmInProcess;
                        realm = migrationRealm.realm;
                        //mergeData is false and realm by same name exists, throw error
                        if(!migrationRealm.mergeData && realmService.getRealmByName(realm.getName()) != null) {
                            throw new MigrationProcessRealmAlreadyExistsThrowable();
                        }
                        
                        if(StringUtils.isBlank(realmInProcess.getUuid())) {
                        	realmInProcess.setUuid(UUID.randomUUID().toString());
                        }
                    }

                    migrationUtil.fillInRealm(resource);

                    handleTransientLocalGroup(resource);

                    if (migrationImporterMap.containsKey(resourceClass)) {
                        MigrationImporter<AbstractEntity<Long>> migrationImporter = migrationImporterMap.get(resourceClass);
                        log.info("Found migration importer for class {}", resourceClass.getCanonicalName());
                        migrationImporter.process(resource);
                    }

                    migrationRepository.saveOrUpdate(resource);

                    if (migrationImporterMap.containsKey(resourceClass)) {
                        MigrationImporter<AbstractEntity<Long>> migrationImporter = migrationImporterMap.get(resourceClass);
                        log.info("Performing post save operation for class {} ", resourceClass.getCanonicalName());
                        migrationImporter.postSave(resource);
                    }

                    handleDatabaseProperties(nodeObjectPack, resource, migrationRealm.realm);

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
        	migrationContext.initExport();
        	migrationContext.addRealm(realm);

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
                Collection<?> migrationClassesList = migrationOrderMap.getCollection(key);
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

                        for (AbstractEntity<Long> abstractEntity : objectList) {
                            List<DatabaseProperty> databaseProperties;
                            if (abstractEntity instanceof AbstractResource) {
                                databaseProperties = migrationRepository.findAllDatabaseProperties((AbstractResource) abstractEntity);
                                processEncrypted(databaseProperties, (AbstractResource) abstractEntity, realm);
                            } else {
                                databaseProperties = Collections.emptyList();
                            }
                            MigrationObjectWithMeta migrationObjectWithMeta = new MigrationObjectWithMeta(abstractEntity, databaseProperties);
                            migrationObjectWithMetas.add(migrationObjectWithMeta);
                        }

                        ObjectPack objectPack = new ObjectPack(aClass.getCanonicalName(),
                                migrationObjectWithMetas);

                        if (migrationExporterMap.containsKey(aClass)) {
                            MigrationExporter<AbstractEntity<Long>> migrationExporter = migrationExporterMap.get(aClass);
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
            
            for (MigrationCustomExport<?> migrationCustomExport : migrationCustomExports) {
            	if(migrationCustomExport.include(entities)) {
            		migrationCustomExport.export(realm, zos);
            	}
			} 
        }catch (IOException e) {
            log.error("Problem in export process.", e);
            throw new IllegalStateException(e.getMessage(), e);
        }finally {
			migrationCurrentStack.clearState();
			migrationContext.clearContext();
		}
    }

    
    public void processEncrypted(List<DatabaseProperty> databaseProperties, AbstractResource resource, Realm realm) {
    	if(databaseProperties == null) {
    		return;
    	}
    	for (DatabaseProperty databaseProperty : databaseProperties) {
			String value = databaseProperty.getValue();
			if(ResourceUtils.isEncrypted(value)) {
				databaseProperty.setValue(realmService.getProviderForRealm(realm)
						.getDecryptedValue(resource, databaseProperty.getResourceKey()));
			}
		}
    }
    
    public MigrationExecutorTracker startRealmImport(InputStream inputStream, boolean mergeData) throws AccessDeniedException,
            ResourceCreationException, MigrationProcessRealmAlreadyExistsThrowable {
        JsonParser jsonParser = null;
        MigrationExecutorTracker migrationExecutorTracker = new MigrationExecutorTracker();
        try {
        	migrationContext.initImport();
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
                } else if(fileName.startsWith("_custom")) {
                	//e.g. _custom_FolderTreePath.json
                	String classSimpleName = fileName.replace(".json", "").split("_")[2];
                	MigrationCustomImport<?> migrationCustomImport = migrationCustomImports.get(classSimpleName);
                	if(migrationCustomImport == null) {
                		throw new IllegalStateException("We have a custom migration import file but no corresponding MigrationCustomImport");
                	} else {
                		log.info("Found custom migration file {}", classSimpleName);
                		migrationCustomImport._import(fileName, migrationRealm.realm, zipInputStream, migrationExecutorTracker);
                	}
                } else {
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
            migrationContext.clearContext();
        }

        return migrationExecutorTracker;
    }

    private void importCustomOperations(String group, JsonNode jsonNode, MigrationRealm migrationRealm, MigrationExecutorTracker migrationExecutorTracker) {
        try {
            Class<?> resourceClass = MigrationExecutor.class.getClassLoader().loadClass(group);

            if(migrationImporterMap.containsKey(resourceClass)) {
                MigrationImporter<AbstractEntity<Long>> migrationImporter = migrationImporterMap.get(resourceClass);
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

    private void handleTransientLocalGroup(AbstractEntity<Long> resource) {
        if(resource instanceof LocalGroup) {
            processTransientLocalGroup((LocalGroup) resource);
        }
    }

    private void handleDatabaseProperties(JsonNode nodeObjectPack, AbstractEntity<Long> resource, Realm realm) throws IOException {
        if(resource instanceof AbstractResource) {
            JsonNode databasePropertiesObjectPack = nodeObjectPack.get("databaseProperties");
            Iterator<JsonNode> databasePropertiesIterator = databasePropertiesObjectPack.iterator();
            while (databasePropertiesIterator.hasNext()) {
                JsonNode databaseProperties = databasePropertiesIterator.next();
                String key = databaseProperties.get("resourceKey").asText();
                String value = databaseProperties.get("value") == null ? null : databaseProperties.get("value").asText();
                log.info("Recieved database property {}", key);
                realmService.getProviderForRealm(realm).setValue((AbstractResource) resource, key, value);
            }
        }
    }

    public static class MigrationRealm {
        public Realm realm;
        public boolean mergeData;
    }
}
