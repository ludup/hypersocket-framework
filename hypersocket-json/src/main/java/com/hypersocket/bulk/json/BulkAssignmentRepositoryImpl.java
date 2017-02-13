package com.hypersocket.bulk.json;

import com.hypersocket.permissions.Role;
import com.hypersocket.resource.AbstractAssignableResourceRepositoryImpl;
import com.hypersocket.resource.AssignableResource;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public class BulkAssignmentRepositoryImpl extends AbstractAssignableResourceRepositoryImpl<AssignableResource>
        implements BulkAssignmentRepository {
    @Override
    protected Class<AssignableResource> getResourceClass() {
        return AssignableResource.class;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void bulkAssignRolesToResource(List<Long> roleIds, List<Long> resourceIds,
                                          BulkAssignmentMode bulkAssignmentMode) {
        List<AssignableResource> assignableResources = (List<AssignableResource>) createCriteria(AssignableResource.class,
                "ar").add(Restrictions.in("ar.id", resourceIds)).list();

        if(assignableResources == null || assignableResources.isEmpty()) {
            throw new IllegalStateException(String.format("For passed in ids %s no resources were found.", resourceIds));
        }

        if(!allOfSameClass(assignableResources)) {
            throw new IllegalStateException("All objects not of same class type.");
        }

        List<Role> roleList = (List<Role>) createCriteria(Role.class,"role").
                add(Restrictions.in("role.id", roleIds)).list();

        if(roleList == null || roleList.isEmpty()) {
            throw new IllegalStateException(String.format("For passed in ids %s no roles were found.", roleIds));
        }

        for (AssignableResource ar: assignableResources) {
            if(BulkAssignmentMode.OverWrite.equals(bulkAssignmentMode)) {
                ar.getRoles().clear();
                ar.getRoles().addAll(roleList);
            } else {
                Collection<Role> mergedRoles = computeMerge(ar, roleList);
                ar.getRoles().addAll(computeMerge(ar, roleList));
            }
            saveObject(ar);
        }
    }

    /**
     * Method to filter already existing roles, roles not present will only be returned.
     *
     * @param assignableResource
     * @param toMergeRoles
     * @return
     */
    private Collection<Role> computeMerge(AssignableResource assignableResource, List<Role> toMergeRoles) {
        Set<Role> present = assignableResource.getRoles();
        Map<Long, Role> toMergeRoleToIdMap = new HashMap<>();
        for (Role role : toMergeRoles) {
            toMergeRoleToIdMap.put(role.getId(), role);
        }

        for(Role role : present) {
            if(toMergeRoleToIdMap.containsKey(role.getId())) {
                toMergeRoleToIdMap.remove(role.getId());
            }
        }

        return toMergeRoleToIdMap.values();
    }

    private boolean allOfSameClass(List<AssignableResource> assignableResources) {
        Set<Class<?>> clazzs = new HashSet<>();
        for (AssignableResource ar : assignableResources) {
            clazzs.add(ar.getClass());
        }

        return clazzs.size() == 1;
    }
}
