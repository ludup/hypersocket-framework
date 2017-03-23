package com.hypersocket.migration;

import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.attributes.user.UserAttributeCategory;
import com.hypersocket.auth.AuthenticationModule;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.automation.AutomationResource;
import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.dashboard.message.DashboardMessage;
import com.hypersocket.interfaceState.UserInterfaceState;
import com.hypersocket.jobs.JobResource;
import com.hypersocket.local.LocalGroup;
import com.hypersocket.local.LocalUser;
import com.hypersocket.message.MessageResource;
import com.hypersocket.migration.order.MigrationOrder;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.PrincipalSuspension;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.secret.SecretKeyResource;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.upload.FileUpload;

import java.util.Arrays;
import java.util.List;

public class CoreMigrationOrder implements MigrationOrder {
    @Override
    public Short sortOrder() {
        return 0;
    }

    @Override
    public List<Class<? extends AbstractEntity<Long>>> getOrderList() {
        return Arrays.<Class<? extends AbstractEntity<Long>>>asList(
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
                AuthenticationScheme.class,
                AuthenticationModule.class,
                DashboardMessage.class,
                UserInterfaceState.class,
                JobResource.class,
                MessageResource.class,
                PrincipalSuspension.class,
                UserAttributeCategory.class,
                UserAttribute.class,
                Realm.class);
    }
}
