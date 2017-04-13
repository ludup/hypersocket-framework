package com.hypersocket.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.local.LocalGroup;
import com.hypersocket.local.LocalUser;
import com.hypersocket.migration.importer.MigrationImporter;
import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Realm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component("com.hypersocket.migration.RoleMigrationImporter")
public class RoleMigrationImporter implements MigrationImporter<Role>{

    @Autowired
    MigrationRepository migrationRepository;

    @Override
    public void process(Role entity) {

    }

    @Override
    public void postSave(Role entity) {

    }

    @Override
    public Class<Role> getType() {
        return Role.class;
    }

    @Override
    @Transactional
    public void processCustomOperationsMap(JsonNode jsonNode, Realm realm) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> customOperationsList = mapper.convertValue(jsonNode, Map.class);

        List<Map<String, ?>> users = (List<Map<String, ?>>) customOperationsList.get("users");
        List<Map<String, ?>> groups = (List<Map<String, ?>>) customOperationsList.get("groups");

        if(users != null) {
            for (Map<String, ?> object : users) {
                Long roleId = getLongValue((Number) object.get("roleId"));
                Long userId = getLongValue((Number) object.get("userId"));

                Role role = getRoleResource(realm, roleId);
                LocalUser user = getLocalUserResource(realm, userId);

                role.getPrincipals().add(user);

                migrationRepository.saveOrUpdate(role);
            }
        }

        if(groups != null) {
            for (Map<String, ?> object : groups) {
                Long roleId = getLongValue((Number) object.get("roleId"));
                Long groupId = getLongValue((Number) object.get("groupId"));

                Role role = getRoleResource(realm, roleId);
                LocalGroup group = getLocalGroupResource(realm, groupId);

                role.getPrincipals().add(group);

                migrationRepository.saveOrUpdate(role);
            }
        }
    }

    private Role getRoleResource(Realm realm, Long roleId) {
        Role roleResource = migrationRepository.findEntityByLegacyIdInRealm(Role.class, roleId, realm);
        if(roleResource == null) {
            throw new IllegalStateException(String.format("Role Resource for legacy id %d not found", roleId));
        }

        return roleResource;
    }

    private LocalGroup getLocalGroupResource(Realm realm, Long groupId) {
        LocalGroup localGroupResource = migrationRepository.findEntityByLegacyIdInRealm(LocalGroup.class, groupId, realm);
        if(localGroupResource == null) {
            throw new IllegalStateException(String.format("Local Group Resource for legacy id %d not found", groupId));
        }

        return localGroupResource;
    }

    private LocalUser getLocalUserResource(Realm realm, Long userId) {
        LocalUser localUserResource = migrationRepository.findEntityByLegacyIdInRealm(LocalUser.class, userId, realm);
        if(localUserResource == null) {
            throw new IllegalStateException(String.format("Local User Resource for legacy id %d not found", userId));
        }

        return localUserResource;
    }

    private Long getLongValue(Number number) {
        return number.longValue();
    }
}
