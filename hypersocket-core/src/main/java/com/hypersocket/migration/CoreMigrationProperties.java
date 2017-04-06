package com.hypersocket.migration;

import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.attributes.user.UserAttributeCategory;
import com.hypersocket.auth.AuthenticationModule;
import com.hypersocket.automation.AutomationResource;
import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.dashboard.message.DashboardMessage;
import com.hypersocket.interfaceState.UserInterfaceState;
import com.hypersocket.jobs.JobResource;
import com.hypersocket.local.LocalGroup;
import com.hypersocket.local.LocalUser;
import com.hypersocket.message.MessageResource;
import com.hypersocket.migration.properties.MigrationProperties;
import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.migration.repository.MigrationCriteriaBuilder;
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
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreMigrationProperties implements MigrationProperties {

    private Map<Class<?>, MigrationCriteriaBuilder> criteriaMap = new HashMap<>();

    public CoreMigrationProperties() {
        buildCriteriaFor(AuthenticationModule.class);
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
    public Map<Class<?>, MigrationCriteriaBuilder> getCriteriaMap() {
        return criteriaMap;
    }

    private void buildCriteriaFor(final Class<AuthenticationModule> authenticationModuleClass) {
        criteriaMap.put(authenticationModuleClass, new MigrationCriteriaBuilder() {

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
}

