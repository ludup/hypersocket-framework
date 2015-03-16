package com.hypersocket.role.events;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.events.PrincipalEvent;
import com.hypersocket.session.Session;

public class RoleEvent extends PrincipalEvent {

	private static final long serialVersionUID = -5587201972600545594L;

	public static final String EVENT_RESOURCE_KEY = "role.event";
	public static final String ATTR_ROLE_NAME = "attr.role";
	public static final String ATTR_ASSOCIATED_PRINCIPALS = "attr.associatedPrincipals";
	public static final String ATTR_ASSOCIATED_PERMISSIONS = "attr.associatedPermissions";
	public static final String ATTR_PERMISSIONS = "attr.Permissions";

	public RoleEvent(Object source, String resourceKey, Session session,
			Realm realm, Role role) {
		super(source, resourceKey, true, session, realm);
		addAttribute(ATTR_ROLE_NAME, role.getName());
		addAssociatedPrincipals(role.getPrincipals());
		addAssociatedPermissions(role.getPermissions());
		addPermissions(role.getPermissions());

	}

	public RoleEvent(Object source, String resourceKey, String roleName,
			Throwable e, Session session, Realm realm) {
		super(source, resourceKey, e, session, realm);
		addAttribute(ATTR_ROLE_NAME, roleName);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	private void addAssociatedPrincipals(Set<Principal> associatedPrincipals) {
		StringBuffer buf = new StringBuffer();
		for (Principal p : associatedPrincipals) {
			if (buf.length() > 0) {
				buf.append(',');
			}
			buf.append(p.getPrincipalName());
		}
		addAttribute(ATTR_ASSOCIATED_PRINCIPALS, buf.toString());
	}

	private void addAssociatedPermissions(Set<Permission> associatedPermissions) {
		StringBuffer buf = new StringBuffer();
		for (Permission p : associatedPermissions) {
			if (buf.length() > 0) {
				buf.append(",");
			}
			buf.append(I18NServiceImpl.tagForConversion(
					PermissionService.RESOURCE_BUNDLE, p.getResourceKey()));
		}
		addAttribute(ATTR_ASSOCIATED_PERMISSIONS, buf.toString());
	}

	private void addPermissions(Set<Permission> associatedPermissions) {
		StringBuffer buf = new StringBuffer();
		for (Permission p : associatedPermissions) {
			if (buf.length() > 0) {
				buf.append(',');
			}
			buf.append(p.getResourceKey());
		}
		addAttribute(ATTR_PERMISSIONS, buf.toString());
	}
}
