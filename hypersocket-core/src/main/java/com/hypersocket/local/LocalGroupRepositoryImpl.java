/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.ResourcePropertyStore;
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
import com.hypersocket.util.PrincipalIterator;

@Repository
public class LocalGroupRepositoryImpl extends ResourceTemplateRepositoryImpl implements LocalGroupRepository {

	private static Logger LOG = LoggerFactory
			.getLogger(LocalGroupRepositoryImpl.class);
	
	@Autowired
	private ApplicationContext applicationContext;	
	@Autowired
	private SystemConfigurationService systemConfigurationService;

	private EntityResourcePropertyStore entityPropertyStore;

	private Object idLock = new Object();
	
	@PostConstruct
	private void postConstruct() {
		entityPropertyStore = new EntityResourcePropertyStore(applicationContext, "localGroupRepository");
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

	@Transactional
	public LocalGroup createGroup(String name, Realm realm) {
		
		LocalGroup group = new LocalGroup();
		group.setName(name);
		group.setRealm(realm);
		group.setPrincipalType(PrincipalType.GROUP);
		group.setPosixId(getNextPosixId(realm));
		save(group);
		return group;
	}
	
	protected LocalGroup getGroup(String column, Object value, Realm realm, boolean deleted) {
		return get(column, value, LocalGroup.class, JOIN_USERS, new DeletedCriteria(deleted), new RealmRestriction(realm));
	}
	
	@Transactional(readOnly=true)
	public LocalGroup getGroupByName(String name, Realm realm) {
		return getGroup("name", name, realm, false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Iterator<LocalGroup> iterateGroups(Realm realm, ColumnSort[] sorting) {
		return new PrincipalIterator<LocalGroup>(sorting, realm) { 
			@Override
			protected void remove(LocalGroup principal) {
				deleteGroup(principal);
			}

			@SuppressWarnings("unchecked")
			@Override
			protected List<LocalGroup> listUsers(Realm realm, int start, int iteratorPageSize, ColumnSort[] sorting) {
				return (List<LocalGroup>) getGroups(realm, "", "", start, iteratorPageSize, sorting);
			}
		};
	}

	@Override
	@Transactional(readOnly=true)
	public List<LocalGroup> allGroups(Realm realm) {
		return allEntities(LocalGroup.class, new DistinctRootEntity(), new RealmRestriction(realm), new HiddenCriteria(false));
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
	@Transactional(readOnly=true)
	public Principal getGroupById(Long id, Realm realm, boolean deleted) {
		return getGroup("id", id, realm, deleted);
	}

	@Override
	@Transactional
	public void deleteGroup(LocalGroup group) {
		group.getUsers().clear();
		group.setDeleted(true);
		save(group);
	}

	@Override
	@Transactional(readOnly=true)
	public Long countGroups(final Realm realm, final String searchColumn, final String searchPattern) {
		return getCount(LocalGroup.class, searchColumn, searchPattern, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("realm", realm));
				criteria.add(Restrictions.eq("deleted", false));
				criteria.add(Restrictions.eq("hidden", false));
			}
		});
	};

	@Override
	@Transactional(readOnly=true)
	public List<?> getGroups(final Realm realm, String searchColumn, final String searchPattern, final int start,
			final int length, final ColumnSort[] sorting) {
		
		return search(LocalGroup.class, searchColumn, searchPattern, start, length, sorting, new CriteriaConfiguration() {

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
	public Collection<? extends Principal> getGroupsByGroup(final LocalGroup principal) {
		return list(LocalGroup.class, false, new CriteriaConfiguration() {
		
			@Override
			public void configure(Criteria criteria) {
				criteria = criteria.createCriteria("groups");
				criteria.add(Restrictions.eq("id", principal.getId()));
			}
		});
	}

	@Override
	public void resetRealm(Iterator<Principal> admins) {
	}
	
	@Override
	@Transactional
	public void deleteRealm(Realm realm) {
		
		Query q2 = createQuery("delete from LocalGroup where realm = :r", true);
		q2.setParameter("r", realm);
		q2.executeUpdate();

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
			
			Criteria c = getCurrentSession().createCriteria(LocalGroup.class);
			c.addOrder(Order.desc("posixId"));
			c.setMaxResults(1);
			c.add(Restrictions.eq("deleted", false));
			c.add(Restrictions.eq("realm", realm));
			c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			@SuppressWarnings("unchecked")
			List<LocalGroup> items = c.list();
			int minGid = systemConfigurationService.getIntValue("security.minGeneratedGid");
			if(items.isEmpty()) {
				return minGid;
			}
			else {
				int posixId = ((LocalGroup)items.get(0)).getPosixId();
				posixId++;
				if(posixId >= Integer.MAX_VALUE) {
					/* BUG: What now? We should probably look for the next 
					 * free Posix ID */
					LOG.warn("Exhausted all Posix IDs for group creation.");
					posixId = Integer.MAX_VALUE - 1;
				}
				if(posixId < minGid) {
					posixId = minGid;
				}
				return (int)posixId;
			}
		}
	}
}
