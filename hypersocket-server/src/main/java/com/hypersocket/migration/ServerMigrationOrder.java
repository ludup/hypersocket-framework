package com.hypersocket.migration;

import com.hypersocket.auth.AuthenticationModule;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.automation.AutomationResource;
import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.dashboard.message.DashboardMessage;
import com.hypersocket.local.LocalGroup;
import com.hypersocket.local.LocalUser;
import com.hypersocket.migration.order.MigrationOrder;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.secret.SecretKeyResource;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.upload.FileUpload;

import java.util.Arrays;
import java.util.List;

public class ServerMigrationOrder implements MigrationOrder {
    @Override
    public String getGroupName() {
        return "core-server-hypersocket-migration";
    }

    @Override
    public List<Class<? extends AbstractEntity<Long>>> getOrderList() {
        return Arrays.<Class<? extends AbstractEntity<Long>>>asList(
                HTTPInterfaceResource.class);
    }
}
