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
import com.hypersocket.migration.annotation.LookUpKeys;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.migration.mapper.MigrationObjectMapper;
import com.hypersocket.migration.order.MigrationOrderInfoProvider;
import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.migration.util.MigrationUtil;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.resource.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.UniqueConstraint;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
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
    MigrationOrderInfoProvider migrationOrderInfoProvider;

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
                JsonNode node = jsonNodeIterator.next();
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

                migrationRepository.saveOrUpdate(resource);
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

            Map<String, List<Class<? extends AbstractEntity<Long>>>> migrationOrderMap = migrationOrderInfoProvider.getMigrationOrderMap();

            jsonGenerator.writeStartArray();
            Set<String> keys = migrationOrderMap.keySet();
            for (String key : keys) {
                List<Class<? extends AbstractEntity<Long>>> migrationClasses = migrationOrderMap.get(key);
                for (Class<? extends AbstractEntity<Long>> aClass : migrationClasses) {
                    List<AbstractEntity> objectList = migrationRepository.findAllResourceInRealmOfType(aClass, realm);
                    ObjectPack objectPack = new ObjectPack(aClass.getCanonicalName(),
                            objectList);
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
}
