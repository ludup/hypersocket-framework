package com.hypersocket.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.attributes.user.UserAttributeCategory;
import com.hypersocket.auth.AuthenticationModule;
import com.hypersocket.automation.AutomationResource;
import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.dashboard.message.DashboardMessage;
import com.hypersocket.interfaceState.UserInterfaceState;
import com.hypersocket.jobs.JobResource;
import com.hypersocket.json.JsonMapper;
import com.hypersocket.local.LocalGroup;
import com.hypersocket.local.LocalUser;
import com.hypersocket.message.MessageResource;
import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.migration.properties.MigrationProperties;
import com.hypersocket.migration.repository.MigrationLookupCriteriaBuilder;
import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.migration.repository.MigrationExportCriteriaBuilder;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.PrincipalSuspension;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.secret.SecretKeyResource;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.util.SpringApplicationContextProvider;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import java.util.*;

public class CoreMigrationProperties implements MigrationProperties {

    private Map<Class<?>, MigrationExportCriteriaBuilder> criteriaMap = new HashMap<>();

    private Map<Class<?>, MigrationLookupCriteriaBuilder> lookupCriteriaBuilderHashMap = new HashMap<>();

    public CoreMigrationProperties() {
        buildCriteriaFor(AuthenticationModule.class);
        buildLookupCriteriaMapAuthenticationModule(AuthenticationModule.class);
        buildLookupCriteriaMapMessageResource(MessageResource.class);
    }

    @Override
    public Short sortOrder() {
        return 0;
    }

    @Override
    public List<Class<? extends AbstractEntity<Long>>> getOrderList() {
        return Arrays.<Class<? extends AbstractEntity<Long>>>asList(
                Realm.class,
                LocalUser.class,
                LocalGroup.class,
                PermissionCategory.class,
                Permission.class,
                Role.class,
                CertificateResource.class,
                TriggerResource.class,
                AutomationResource.class,
                FileUpload.class,
                SecretKeyResource.class,
                AuthenticationModule.class,
                DashboardMessage.class,
                UserInterfaceState.class,
                JobResource.class,
                MessageResource.class,
                PrincipalSuspension.class,
                UserAttributeCategory.class,
                UserAttribute.class);
    }

    @Override
    public Map<Class<?>, MigrationExportCriteriaBuilder> getExportCriteriaMap() {
        return criteriaMap;
    }

    @Override
    public Map<Class<?>, MigrationLookupCriteriaBuilder> getLookupCriteriaMap() {
        return lookupCriteriaBuilderHashMap;
    }

    private void buildCriteriaFor(final Class<AuthenticationModule> authenticationModuleClass) {
        criteriaMap.put(authenticationModuleClass, new MigrationExportCriteriaBuilder() {

            @Override
            public DetachedCriteria make(Realm realm) {
                MigrationRepository migrationRepository = (MigrationRepository) SpringApplicationContextProvider.
                        getApplicationContext().getBean("migrationRepository");
                DetachedCriteria criteria = migrationRepository.buildCriteriaFor(authenticationModuleClass, "am");
                criteria.createAlias("am.scheme", "scheme");
                criteria.createAlias("scheme.realm", "realm");
                criteria.add(Restrictions.eq("realm.id", realm.getId()));
                return criteria;
            }
        });
    }

    private void buildLookupCriteriaMapAuthenticationModule(final Class<AuthenticationModule> authenticationModuleClass) {
        lookupCriteriaBuilderHashMap.put(authenticationModuleClass, new MigrationLookupCriteriaBuilder() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
            public DetachedCriteria make(Realm realm, LookUpKey lookUpKey, JsonNode node) {
                MigrationRepository migrationRepository = (MigrationRepository) SpringApplicationContextProvider.
                        getApplicationContext().getBean("migrationRepository");

                JsonMapper jsonMapper = (JsonMapper) SpringApplicationContextProvider.
                        getApplicationContext().getBean("jsonMapper");

                Map<String, Object> entityMap = jsonMapper.get().convertValue(node, Map.class);

                String template = (String) entityMap.get("template");
                String name = (String) ((Map) entityMap.get("scheme")).get("name");
                String resourceKey = (String) ((Map) entityMap.get("scheme")).get("resourceKey");

                DetachedCriteria criteria = migrationRepository.buildCriteriaFor(authenticationModuleClass, "am");
                criteria.createAlias("am.scheme", "scheme");
                criteria.createAlias("scheme.realm", "realm");
                criteria.add(Restrictions.eq("realm.id", realm.getId()));
                criteria.add(Restrictions.eq("am.template", template));
                criteria.add(Restrictions.eq("scheme.name", name));
                criteria.add(Restrictions.eq("scheme.resourceKey", resourceKey));
                return criteria;
            }
        });
    }


    private void buildLookupCriteriaMapMessageResource(final Class<MessageResource> messageResourceClass) {
        lookupCriteriaBuilderHashMap.put(messageResourceClass, new MigrationLookupCriteriaBuilder() {
            @SuppressWarnings("unchecked")
			@Override
            public DetachedCriteria make(Realm realm, LookUpKey lookUpKey, JsonNode node) {
                MigrationRepository migrationRepository = (MigrationRepository) SpringApplicationContextProvider.
                        getApplicationContext().getBean("migrationRepository");

                JsonMapper jsonMapper = (JsonMapper) SpringApplicationContextProvider.
                        getApplicationContext().getBean("jsonMapper");

                Map<String, Object> entityMap = jsonMapper.get().convertValue(node, Map.class);

                Integer messageId = (Integer) entityMap.get("messageId");

                DetachedCriteria criteria = migrationRepository.buildCriteriaFor(messageResourceClass, "mr");
                criteria.createAlias("mr.realm", "realm");
                criteria.add(Restrictions.eq("realm.id", realm.getId()));
                criteria.add(Restrictions.eq("mr.messageId", messageId));
                return criteria;
            }
        });
    }

}

