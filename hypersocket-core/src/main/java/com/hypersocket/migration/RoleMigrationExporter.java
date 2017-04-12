package com.hypersocket.migration;

import com.hypersocket.local.LocalGroup;
import com.hypersocket.local.LocalUser;
import com.hypersocket.migration.exporter.MigrationExporter;
import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("com.hypersocket.migration.RoleMigrationExporter")
public class RoleMigrationExporter implements MigrationExporter<Role>{

    @Autowired
    MigrationRepository migrationRepository;

    @Override
    public Class<Role> getType() {
        return Role.class;
    }

    @Override
    public Map<String, List<Map<String, ?>>> produceCustomOperationsMap(Realm realm) {
        List<Role> roleResources = migrationRepository.findAllResourceInRealmOfType(getType(), realm);

        List<Map<String, ?>> users = new ArrayList<>();
        List<Map<String, ?>> groups = new ArrayList<>();

        Map<String, List<Map<String, ?>>> customOperationsList = new HashMap<>();

        customOperationsList.put("users", users);
        customOperationsList.put("groups", groups);

        for (Role role : roleResources) {
            Set<Principal> principals = role.getPrincipals();
            for (Principal principal : principals) {
                if(principal instanceof LocalUser) {
                    Map<String, Long> userEntry = new HashMap<>();
                    userEntry.put("roleId",role.getId());
                    userEntry.put("userId", principal.getId());
                    users.add(userEntry);
                } else if (principal instanceof LocalGroup) {
                    Map<String, Long> groupEntry = new HashMap<>();
                    groupEntry.put("roleId", role.getId());
                    groupEntry.put("groupId", principal.getId());
                    groups.add(groupEntry);
                }
            }
        }

        return customOperationsList;
    }
}
