package com.hypersocket.bulk;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class BulkAssignmentEvent extends SessionEvent {

	private static final long serialVersionUID = -2366608864198706771L;

	public static final String EVENT_RESOURCE_KEY = "event.bulkAssignmentResource";

    public static final String ATTR_BULK_ASSIGNED_ROLES = "attr.bulkAssignmentRoles";
    public static final String ATTR_BULK_ASSIGNED_RESOURCES = "attr.bulkAssignmentResources";
    public static final String ATTR_BULK_ASSIGNED_MODE = "attr.bulkAssignmentMode";

    public BulkAssignmentEvent(Object source, List<Long> roleIds, List<Long> resourceIds, BulkAssignmentMode bulkAssignmentMode,
                               Session session,
                               Realm realm, RealmProvider provider,
                               Principal principal) {
        super(source, "event.bulkAssignmentResource", true, session);
        addAttribute(ATTR_BULK_ASSIGNED_ROLES, roleIds);
        addAttribute(ATTR_BULK_ASSIGNED_RESOURCES, resourceIds);
        addAttribute(ATTR_BULK_ASSIGNED_MODE, bulkAssignmentMode);
    }

    public BulkAssignmentEvent(Object source, List<Long> roleIds, List<Long> resourceIds, BulkAssignmentMode bulkAssignmentMode,
                               Throwable e,
                               Session session, Realm realmName, RealmProvider provider,
                               Principal principal) {
        super(source, "event.bulkAssignmentResource", e, session);
        addAttribute(ATTR_BULK_ASSIGNED_ROLES, roleIds);
        addAttribute(ATTR_BULK_ASSIGNED_RESOURCES, resourceIds);
        addAttribute(ATTR_BULK_ASSIGNED_MODE, bulkAssignmentMode);
    }

    public String[] getResourceKeys() {
        return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
    }
}
