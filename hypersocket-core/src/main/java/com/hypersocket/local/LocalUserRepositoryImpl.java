/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourcePropertyStore;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.DelegationCriteria;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.repository.HiddenCriteria;
import com.hypersocket.resource.RealmCriteria;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.util.PrincipalIterator;

@Repository
public class LocalUserRepositoryImpl extends ResourceTemplateRepositoryImpl implements LocalUserRepository {

	private static Logger LOG = LoggerFactory
			.getLogger(LocalUserRepositoryImpl.class);
	
	@Autowired
	private EncryptionService encryptionService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Autowired
	private DelegationCriteria delegationCriteria;
	
	private EntityResourcePropertyStore entityPropertyStore;

	private Object idLock = new Object();
	
	@PostConstruct
	private void postConstruct() {
		entityPropertyStore = new EntityResourcePropertyStore(encryptionService, "localUserRepository");
	}
	
	@Override
	protected ResourcePropertyStore getPropertyStore() {
		return entityPropertyStore;
	}
	
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
		user.setPrincipalType(PrincipalType.USER);
		user.setPosixId(getNextPosixId(realm));
		save(user);
		
		return user;

	}
	
	protected void save(LocalUserCredentials creds) {
		save(creds, creds.getId()==null);
	}
	
	protected LocalUser getUser(String column, Object value, Realm realm, PrincipalType type, boolean deleted) {
		return get(column, value, LocalUser.class, JOIN_GROUPS, new RealmRestriction(realm), new DeletedCriteria(deleted), new PrincipalTypeRestriction(type));
	}
	
	protected LocalUser getUser(String column, Object value, Realm realm, boolean deleted) {
		return get(column, value, LocalUser.class, JOIN_GROUPS, new RealmRestriction(realm), new DeletedCriteria(deleted));
	}
	
	@Transactional(readOnly=true)
	public LocalUser getUserByName(String username, Realm realm) {
		return getUser("name", username, realm, PrincipalType.USER, false);
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
	@Transactional(readOnly = true)
	public Iterator<LocalUser> iterateUsers(Realm realm, ColumnSort[] sorting) {
		return new PrincipalIterator<LocalUser>(sorting, realm) { 
			@Override
			protected void remove(LocalUser principal) {
				deleteUser(principal);
			}

			@SuppressWarnings("unchecked")
			@Override
			protected List<LocalUser> listUsers(Realm realm, int start, int iteratorPageSize, ColumnSort[] sorting) {
				return (List<LocalUser>) getUsers(realm, "", "", start, iteratorPageSize, sorting);
			}
		};
	}

	@Override
	@Transactional(readOnly=true)
	public List<LocalUser> allUsers(Realm realm) {
		return allEntities(LocalUser.class, JOIN_GROUPS, new DistinctRootEntity(), new RealmRestriction(realm), new HiddenCriteria(false));
	}
	
	@Override
	@Transactional(readOnly=true)
	public LocalUserCredentials getCredentials(LocalUser user) {
		return get("user", user, LocalUserCredentials.class, new DistinctRootEntity());
	}

	@Override
	@Transactional
	public void saveUser(LocalUser user, Map<String,String> properties) {
		
		if(user.getRealm()==null) {
			throw new IllegalArgumentException("No realm set for new user " + user.getPrincipalName());
		}

		Map<String,String> dbProperties = new HashMap<String,String>();
		Map<String,String> entityProperties = new HashMap<String,String>();
		if (properties != null) {
			for(PropertyTemplate t : getPropertyTemplates(user.getId()==null ? null : user)) {
				if(properties.containsKey(t.getResourceKey())) {
					if(t.getPropertyStore().isDefaultStore()) {
						dbProperties.put(t.getResourceKey(), properties.get(t.getResourceKey()));
					} else {
						entityProperties.put(t.getResourceKey(), properties.get(t.getResourceKey()));
					}
				}
			};
		}
		// Save properties
		for (Map.Entry<String, String> e : entityProperties.entrySet()) {
			setValue(user,
					e.getKey(),
					e.getValue());
		}
				
		save(user);
		
		for (Map.Entry<String, String> e : dbProperties.entrySet()) {
			setValue(user,
					e.getKey(),
					e.getValue());
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
		return getUser("name", principalName, realm, type, false);
	}

	@Override
	@Transactional(readOnly=true)
	public Principal getUserById(Long id, Realm realm, boolean deleted) {
		return getUser("id", id, realm, deleted);
	}

	@Override
	@Transactional(readOnly=true)
	public Principal getUserByIdAndType(Long id, Realm realm,
			PrincipalType type) {
		return getUser("id", id, realm, type, false);
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
	public Long countUsers(final Realm realm, String searchColumn, final String searchPattern) {
		return getCount(LocalUser.class, searchColumn, searchPattern, delegationCriteria, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("realm", realm));
				criteria.add(Restrictions.eq("deleted", false));
				criteria.add(Restrictions.eq("hidden", false));				
			}
		});
	}

	@Override
	@Transactional(readOnly=true)
	public List<?> getUsers(final Realm realm, String searchColumn, final String searchPattern, final int start,
			final int length, final ColumnSort[] sorting) {
		return search(LocalUser.class, searchColumn, searchPattern, start, length, sorting, delegationCriteria, new CriteriaConfiguration() {

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
	public Principal getUserByEmail(String email, Realm realm) {
		return get("email", email, LocalUser.class, new DeletedCriteria(false), new RealmCriteria(realm));
	}

	@Override
	@Transactional(readOnly=true)
	public UserPrincipal<?> getUserByFullName(String fullName, Realm realm) {
		return get("description", fullName, LocalUser.class, new DeletedCriteria(false), new RealmCriteria(realm));
	}

	@Override
	public void resetRealm(Iterator<Principal> admins) {
	}
	
	@Override
	@Transactional
	public void deleteRealm(Realm realm) {
		
		Query q3 = createQuery("delete from LocalUserCredentials where user.id in (select user.id from LocalUser user where realm = :r)", true);
		q3.setParameter("r", realm);
		q3.executeUpdate();
		
		Query q = createQuery("delete from LocalUser where realm = :r", true);
		q.setParameter("r", realm);
		q.executeUpdate();
		

	}

	@Override
	public Collection<LocalUserCredentials> allCredentials() {
		return list(LocalUserCredentials.class);
	}

	@Override
	@Transactional
	public int getNextPosixId(Realm realm) {
		synchronized(idLock) {
			/* NOTE: I really don't like this. It will be hard to optimise should it 
			 * be required. We can't use standard Identity columns because of the 
			 * requirement to make the ID unique within a realm (at least i can't find
			 * how to do this in hibernate - any ideas?).
			 * 
			 *   Reference: https://stackoverflow.com/questions/3900105/get-record-with-max-id-using-hibernate-criteria
			 *   
			 *  NOTE: Can't seem to use DetachedCriteria method for this. Any ideas?
			 */
			
			Criteria c = getCurrentSession().createCriteria(LocalUser.class);
			c.addOrder(Order.desc("posixId"));
			c.setMaxResults(1);
			c.add(Restrictions.eq("deleted", false));
			c.add(Restrictions.eq("realm", realm));
			c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			@SuppressWarnings("unchecked")
			List<LocalUser> items = c.list();
			int minUid = systemConfigurationService.getIntValue("security.minGeneratedUid");
			if(items.isEmpty()) {
				return minUid;
			}
			else {
				int posixId = ((LocalUser)items.get(0)).getPosixId();
			
				posixId++;
				if(posixId >= Integer.MAX_VALUE) {
					/* BUG: What now? We should probably look for the next 
					 * free Posix ID */
					LOG.warn("Exhausted all Posix IDs for user creation.");
					posixId = Integer.MAX_VALUE - 1; 
				}
				if(posixId < minUid) {
					posixId = minUid;
				}
				return (int)posixId;
			}
		}
	}
}
