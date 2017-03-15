package com.hypersocket.migration;

import com.hypersocket.local.LocalGroup;
import com.hypersocket.local.LocalUser;
import com.hypersocket.migration.order.MigrationOrder;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;

import java.util.Arrays;
import java.util.List;

public class CoreMigrationOrder implements MigrationOrder {
    @Override
    public String getGroupName() {
        return "core-hypersocket-migration";
    }

    @Override
    public List<Class<? extends AbstractEntity<Long>>> getOrderList() {
        return Arrays.<Class<? extends AbstractEntity<Long>>>asList(LocalUser.class,
                LocalGroup.class, PermissionCategory.class, Permission.class, Role.class,
                /*DatabaseProperty.class,*/ Realm.class);
    }
}
