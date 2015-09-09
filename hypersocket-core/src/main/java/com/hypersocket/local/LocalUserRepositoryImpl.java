/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.repository.HiddenCriteria;
import com.hypersocket.tables.ColumnSort;

@Repository
public class LocalUserRepositoryImpl extends ResourceTemplateRepositoryImpl implements LocalUserRepository {

	@Autowired
	PermissionService permissionService;
	
	final static CriteriaConfiguration JOIN_GROUPS = new CriteriaConfiguration() {
		
		@Override
		public void configure(Criteria criteria) {
			criteria.setFetchMode("groups", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);		
		}
	};
	
	final static CriteriaConfiguration JOIN_USERS = new CriteriaConfiguration() {
		
		@Override
		public void configure(Criteria criteria) {
			criteria.setFetchMode("users", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);		
		}
	};
	
	@Override
	@Transactional
	public LocalUser createUser(String username, Realm realm) {
		
		LocalUser user = new LocalUser();
		user.setName(username);
		user.setResourceCategory(LocalRealmProviderImpl.USER_RESOURCE_CATEGORY);
		user.setRealm(realm);
		save(user);
		
		return user;

	}
	
	protected void save(LocalUserCredentials creds) {
		save(creds, creds.getId()==null);
	}
	
	@Transactional
	public LocalGroup createGroup(String name, Realm realm) {
		
		LocalGroup group = new LocalGroup();
		group.setName(name);
		group.setRealm(realm);
		save(group);
		return group;
	}
	
	protected LocalUser getUser(String column, Object value, Realm realm, PrincipalType type) {
		return get(column, value, LocalUser.class, JOIN_GROUPS, new RealmRestriction(realm), new DeletedCriteria(false), new PrincipalTypeRestriction(type));
	}
	
	@Transactional(readOnly=true)
	public LocalUser getUserByName(String username, Realm realm) {
		return getUser("name", username, realm, PrincipalType.USER);
	}
	
	protected LocalGroup getGroup(String column, Object value, Realm realm) {
		return get(column, value, LocalGroup.class, JOIN_USERS, new DeletedCriteria(false), new RealmRestriction(realm));
	}
	
	@Transactional(readOnly=true)
	public LocalGroup getGroupByName(String name, Realm realm) {
		return getGroup("name", name, realm);
	}

	@Override
	@Transactional
	public void assign(LocalUser user, LocalGroup group) {
		
		if(!user.getRealm().equals(group.getRealm()))
			throw new IllegalArgumentException("Cannot assign: user and group must both belong to the same realm!");
		
		user.getGroups().add(group);
		save(user);
		flush();
		
	}

	@Override
	@Transactional
	public void unassign(LocalUser user, LocalGroup group) {
		
		if(!user.getRealm().equals(group.getRealm()))
			throw new IllegalArgumentException("Cannot unassign: user and group must both belong to the same realm!");
		
		user.getGroups().remove(group);
		save(user);
		flush();

		
	}

	@Override
	@Transactional(readOnly=true)
	public List<LocalUser> allUsers(Realm realm) {
		return allEntities(LocalUser.class, JOIN_GROUPS, new DistinctRootEntity(), new RealmRestriction(realm), new HiddenCriteria(false));
	}

	@Override
	@Transactional(readOnly=true)
	public List<LocalGroup> allGroups(Realm realm) {
		return allEntities(LocalGroup.class, new DistinctRootEntity(), new RealmRestriction(realm), new HiddenCriteria(false));
	}
	
	@Override
	@Transactional(readOnly=true)
	public LocalUserCredentials getCredentials(LocalUser user) {
		return get("user", user, LocalUserCredentials.class, new DistinctRootEntity());
	}
	
	@Override
	@Transactional
	public void saveGroup(LocalGroup group) {
		
		if(group.getRealm()==null) {
			throw new IllegalArgumentException("No realm set for new group " + group.getName());
		}

		save(group);
		
	}

	@Override
	@Transactional
	public void saveUser(LocalUser user, Map<String,String> properties) {
		
		if(user.getRealm()==null) {
			throw new IllegalArgumentException("No realm set for new user " + user.getPrincipalName());
		}

		save(user);
		
		// Save properties
		if (properties != null) {
			for (Map.Entry<String, String> e : properties.entrySet()) {
				setValue(user,
						e.getKey(),
						e.getValue());
			}
		}
	}

	@Override
	@Transactional
	public void saveCredentials(LocalUserCredentials creds) {
		
		if(creds.getUser()==null) {
			throw new IllegalArgumentException("No user is associated with the credentials!");
		}
		
		save(creds);

	}

	@Override
	@Transactional(readOnly=true)
	public Principal getUserByNameAndType(String principalName, Realm realm,
			PrincipalType type) {
		return getUser("name", principalName, realm, type);
	}

	@Override
	@Transactional(readOnly=true)
	public Principal getUserById(Long id, Realm realm) {
		return getUser("id", id, realm, PrincipalType.USER);
	}

	@Override
	@Transactional(readOnly=true)
	public Principal getGroupById(Long id, Realm realm) {
		return getGroup("id", id, realm);
	}

	@Override
	@Transactional(readOnly=true)
	public Principal getUserByIdAndType(Long id, Realm realm,
			PrincipalType type) {
		return getUser("id", id, realm, type);
	}

	@Override
	@Transactional
	public void deleteGroup(LocalGroup group) {
		group.getUsers().clear();
		group.setDeleted(true);
		save(group);
	}

	@Override
	@Transactional
	public void deleteUser(LocalUser usr) {
		usr.getGroups().clear();
		usr.setDeleted(true);
		save(usr);
	};
	
	@Override
	@Transactional(readOnly=true)
	public Long countUsers(final Realm realm, final String searchPattern) {
		return getCount(LocalUser.class, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("realm", realm));
				criteria.add(Restrictions.eq("deleted", false));
				criteria.add(Restrictions.eq("hidden", false));
				if(!StringUtils.isEmpty(searchPattern)) {
					criteria.add(Restrictions.ilike("name", searchPattern));
				}
				
			}
		});
	}

	@Override
	@Transactional(readOnly=true)
	public Long countGroups(final Realm realm, final String searchPattern) {
		return getCount(LocalGroup.class, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("realm", realm));
				criteria.add(Restrictions.eq("deleted", false));
				criteria.add(Restrictions.eq("hidden", false));
				if(!StringUtils.isEmpty(searchPattern)) {
					criteria.add(Restrictions.ilike("name", searchPattern));
				}
			}
		});
	};
	

	@Override
	@Transactional(readOnly=true)
	public List<?> getUsers(final Realm realm, final String searchPattern, final int start,
			final int length, final ColumnSort[] sorting) {
		return search(LocalUser.class, "name", searchPattern, start, length, sorting, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("realm", realm));
				criteria.add(Restrictions.eq("deleted", false));
				criteria.add(Restrictions.eq("hidden", false));
				criteria.setFetchMode("groups", FetchMode.SELECT);
				criteria.setFetchMode("roles", FetchMode.SELECT);
				criteria.setFetchMode("properties", FetchMode.SELECT);
			}
		});
	}

	@Override
	@Transactional(readOnly=true)
	public List<?> getGroups(final Realm realm, final String searchPattern, final int start,
			final int length, final ColumnSort[] sorting) {
		
		return search(LocalGroup.class, "name", searchPattern, start, length, sorting, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("realm", realm));
				criteria.add(Restrictions.eq("deleted", false));
				criteria.add(Restrictions.eq("hidden", false));
				criteria.setFetchMode("users", FetchMode.SELECT);
				criteria.setFetchMode("roles", FetchMode.SELECT);
				criteria.setFetchMode("properties", FetchMode.SELECT);
			}
		});
	}
	

	@Override
	@Transactional(readOnly=true)
	public Collection<? extends Principal> getGroupsByUser(final LocalUser principal) {
		return list(LocalGroup.class, false, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria = criteria.createCriteria("users");
				criteria.add(Restrictions.eq("id", principal.getId()));
			}
		});
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<? extends Principal> getUsersByGroup(final LocalGroup principal) {
		return list(LocalUser.class, false, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria = criteria.createCriteria("groups");
				criteria.add(Restrictions.eq("id", principal.getId()));
			}
		});
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<? extends Principal> getGroupsByGroup(final LocalGroup principal) {
		return list(LocalGroup.class, false, new CriteriaConfiguration() {
		
			@Override
			public void configure(Criteria criteria) {
				criteria = criteria.createCriteria("groups");
				criteria.add(Restrictions.eq("id", principal.getId()));
			}
		});
	}
}
