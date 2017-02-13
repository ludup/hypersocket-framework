package com.hypersocket.bulk.json;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.realm.events.UserEvent;
import com.hypersocket.session.Session;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class BulkAssignmentEvent extends UserEvent{

    public static final String EVENT_RESOURCE_KEY = "event.bulkAssignmentResource";

    public static final String ATTR_BULK_ASSIGNED_ROLES = "attr.bulkAssignmentRoles";
    public static final String ATTR_BULK_ASSIGNED_RESOURCES = "attr.bulkAssignmentResources";
    public static final String ATTR_BULK_ASSIGNED_MODE = "attr.bulkAssignmentMode";

    public BulkAssignmentEvent(Object source, List<Long> roleIds, List<Long> resourceIds, BulkAssignmentMode bulkAssignmentMode,
                               Session session,
                               Realm realm, RealmProvider provider,
                               Principal principal) {
        super(source, "event.bulkAssignmentResource", session, realm, provider, principal);
        addAttribute(ATTR_BULK_ASSIGNED_ROLES, roleIds);
        addAttribute(ATTR_BULK_ASSIGNED_RESOURCES, resourceIds);
        addAttribute(ATTR_BULK_ASSIGNED_MODE, bulkAssignmentMode);
    }

    public BulkAssignmentEvent(Object source, List<Long> roleIds, List<Long> resourceIds, BulkAssignmentMode bulkAssignmentMode,
                               Throwable e,
                               Session session, Realm realmName, RealmProvider provider,
                               Principal principal) {
        super(source, "event.bulkAssignmentResource", e, session, realmName, provider, principal.getPrincipalName());
        addAttribute(ATTR_BULK_ASSIGNED_ROLES, roleIds);
        addAttribute(ATTR_BULK_ASSIGNED_RESOURCES, resourceIds);
        addAttribute(ATTR_BULK_ASSIGNED_MODE, bulkAssignmentMode);
    }

    public String[] getResourceKeys() {
        return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
    }
}
