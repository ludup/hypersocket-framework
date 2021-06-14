package com.hypersocket.delegation;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.AssignableResource;

@Entity
@Table(name="delegate_resources")
public class UserDelegationResource extends AssignableResource {

	private static final long serialVersionUID = 1421882511392182761L;

	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "delegate_roles", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = {
			@JoinColumn(name = "role_id") })
	private Set<Role> roleDelegates = new HashSet<Role>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "delegate_groups", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = {
			@JoinColumn(name = "role_id") })
	private Set<Principal> groupDelegates = new HashSet<Principal>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "delegate_users", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = {
			@JoinColumn(name = "role_id") })
	private Set<Principal> userDelegates = new HashSet<Principal>();

	public Set<Role> getRoleDelegates() {
		return roleDelegates;
	}

	public void setRoleDelegates(Set<Role> roleDelegates) {
		this.roleDelegates = roleDelegates;
	}

	public Set<Principal> getGroupDelegates() {
		return groupDelegates;
	}

	public void setGroupDelegates(Set<Principal> groupDelegates) {
		this.groupDelegates = groupDelegates;
	}

	public Set<Principal> getUserDelegates() {
		return userDelegates;
	}

	public void setUserDelegates(Set<Principal> userDelegates) {
		this.userDelegates = userDelegates;
	}
	
	
}
