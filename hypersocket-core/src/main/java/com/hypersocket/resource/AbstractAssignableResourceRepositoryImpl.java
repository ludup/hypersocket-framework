/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.bulk.BulkAssignment;
import com.hypersocket.bulk.BulkAssignmentMode;
import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourcePropertyStore;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.HibernateUtils;
import com.hypersocket.session.Session;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

@Repository
public abstract class AbstractAssignableResourceRepositoryImpl<T extends AssignableResource>
		extends ResourceTemplateRepositoryImpl implements
		AbstractAssignableResourceRepository<T> {

	static Logger log = LoggerFactory.getLogger(AbstractAssignableResourceRepositoryImpl.class);
	
	protected EntityResourcePropertyStore entityPropertyStore;

	@Autowired
	private EncryptionService encryptionService;
	
	@PostConstruct
	private void postConstruct() {
		entityPropertyStore = new EntityResourcePropertyStore(encryptionService, getResourceClass().getCanonicalName());
	}

	protected void beforeDelete(T resource) throws ResourceException {
				
	}
	
	protected void afterDelete(T resource) throws ResourceException {
		
	}
	
	protected void beforeSave(T resource, Map<String,String> properties) throws ResourceException {
		
	}
	
	protected void afterSave(T resource, Map<String,String> properties) throws ResourceException {
		
 	}
		 	
	@Override
	protected ResourcePropertyStore getPropertyStore() {
		return entityPropertyStore;
	}
	
	@Override 
	public EntityResourcePropertyStore getEntityStore() {
		return entityPropertyStore;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public Collection<T> getAssignedResources(Role role, CriteriaConfiguration... configs) {

		
		Criteria criteria = createCriteria(getResourceClass());
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		criteria.add(Restrictions.eq("realm", role.getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		
		for(CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("id", role.getId()));
		
		return criteria.list();

	}
	
	@Override
	@Transactional /** LDP it's possible extended versions of this class could need write transaction **/
	public Collection<T> getAssignedResources(List<Principal> principals, CriteriaConfiguration... configs) {
		return getAssignedResources("", principals, configs);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional /** LDP it's possible extended versions of this class could need write transaction **/
	public Collection<T> getAssignedResources(String name, List<Principal> principals, CriteriaConfiguration... configs) {

		
		Criteria criteria = createCriteria(getResourceClass());
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		if(StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.eq("name", name));
		}
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("hidden", false));
		
		for(CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		Set<T> results = new HashSet<T>(criteria.list());
		
		Set<Long> ids = new HashSet<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}

		criteria = createCriteria(getResourceClass());
		
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		if(StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.eq("name", name));
		}
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("hidden", false));
		
		for(CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		criteria.add(Restrictions.in("id", ids));
		
		results.addAll((List<T>) criteria.list());
		
		processAdditionalAssignedResourceResults(results, principals.get(0).getRealm(), name, "name", principals);
		
		return results;
	}
	

	protected void processAdditionalAssignedResourceIds(Set<Long> ids, Realm realm, String searchPattern, String searchColumn, Collection<Principal> principals) {
			
	}
	
	protected void processAdditionalAssignedResourceResults(Set<T> results, Realm realm, String searchPattern, String searchColumn, Collection<Principal> principals) {
			
	}

//	@SuppressWarnings("unchecked")
//	@Transactional(readOnly=true)
//	public T getPersonalResourceByName(String name, Principal principal, CriteriaConfiguration... configs) {
//		
//		/**
//		 * Not sure on the effectiveness of this method. Since assignment was removed it would not
//		 * guarantee to return a users private resource.
//		 */
//		Criteria criteria = createCriteria(getResourceClass());
//		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
//		
//		criteria.add(Restrictions.eq("name", name));
//		
//		for (CriteriaConfiguration c : configs) {
//			c.configure(criteria);
//		}
//
//		criteria.add(Restrictions.eq("realm", principal.getRealm()));
//		criteria.add(Restrictions.eq("deleted", false));
//		criteria.add(Restrictions.eq("hidden", false));
//		
////		criteria = criteria.createCriteria("roles");
////		criteria.add(Restrictions.eq("personalRole", true));
////		criteria.add(Restrictions.in("principals", Arrays.asList(principal.getId())));
//		
//		return (T) criteria.uniqueResult();
//	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional /** LDP it's possible extended versions of this class could need write transaction **/
	public Collection<T> searchAssignedResources(List<Principal> principals,
			final String searchPattern, final String searchColumn, final int start, final int length,
			final ColumnSort[] sorting, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(getResourceClass());
		
		Map<String,Criteria> assosications = new HashMap<String,Criteria>();
		
		if (StringUtils.isNotBlank(searchPattern)) {
			HibernateUtils.configureSearch(searchColumn, searchPattern, criteria, getClass(), assosications);
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("hidden", false));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		Set<Long> ids = new HashSet<Long>(criteria.list());

		
		criteria = createCriteria(getResourceClass());
		
		if (StringUtils.isNotBlank(searchPattern)) {
			HibernateUtils.configureSearch(searchColumn, searchPattern, criteria, getClass(), assosications);
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);

		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("hidden", false));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		
		List<Long> principalIds = new ArrayList<Long>();
		for(Principal p : principals) {
			principalIds.add(p.getId());
		}
		criteria.add(Restrictions.in("id", principalIds));
		
		ids.addAll(criteria.list());
		
		processAdditionalAssignedResourceIds(ids, principals.get(0).getRealm(), searchPattern, searchColumn, principals);
		
		if(ids.size() > 0) {
			
			criteria = createCriteria(getResourceClass());
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.in("id", ids));
	
			criteria.setFirstResult(start);
			criteria.setMaxResults(length);
			
			for (ColumnSort sort : sorting) {
				criteria.addOrder(sort.getSort() == Sort.ASC ? Order.asc(sort
						.getColumn().getColumnName()) : Order.desc(sort.getColumn()
						.getColumnName()));
			}
			
			return ((List<T>) criteria.list());
		}
		
		return new ArrayList<T>();
	};
	

	@Override
	@Transactional(readOnly=true)
	public boolean hasAssignedEveryoneRole(Realm realm, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(getResourceClass());
		criteria.setProjection(Projections.property("id"));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.add(Restrictions.eq("realm", realm));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("hidden", false));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		List<?> everyoneRoles = criteria.list();
		
		return everyoneRoles.size() > 0;
	}

		
	@SuppressWarnings("unchecked")
	@Override
	@Transactional /** LDP it's possible extended versions of this class could need write transaction **/
	public Long getAssignedResourceCount(Collection<Principal> principals,
			final String searchPattern, final String searchColumn, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(getResourceClass());
		
		Map<String,Criteria> assosications = new HashMap<String,Criteria>();
		
		if (StringUtils.isNotBlank(searchPattern)) {
			HibernateUtils.configureSearch(searchColumn, searchPattern, criteria, getClass(), assosications);
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		
		criteria.add(Restrictions.eq("realm", principals.iterator().next().getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("hidden", false));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		Set<Long> ids = new HashSet<Long>(criteria.list());
		
		criteria = createCriteria(getResourceClass());
		
		if (StringUtils.isNotBlank(searchPattern)) {
			HibernateUtils.configureSearch(searchColumn, searchPattern, criteria, getClass(), assosications);
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		criteria.add(Restrictions.eq("realm", principals.iterator().next().getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("hidden", false));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		
		List<Long> principalIds = new ArrayList<Long>();
		for(Principal p : principals) {
			principalIds.add(p.getId());
		}
		criteria.add(Restrictions.in("id", principalIds));
		
		ids.addAll((List<Long>)criteria.list());
		
		processAdditionalAssignedResourceIds(ids, principals.iterator().next().getRealm(), searchPattern, searchColumn, principals);
		
		return new Long(ids.size());
	}


	@Override
	@Transactional /** LDP it's possible extended versions of this class could need write transaction **/
	public Long getAssignableResourceCount(Collection<Principal> principals) {
		return getAssignedResourceCount(principals, "", "");
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public T getResourceByIdAndPrincipals(Long resourceId,
			List<Principal> principals) {

		Set<Long> ids = new HashSet<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}
		Criteria crit = createCriteria(getResourceClass());
		crit.add(Restrictions.eq("id", resourceId));
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit = crit.createCriteria("roles");
		crit = crit.createCriteria("principals");
		crit.add(Restrictions.in("id", ids));

		return (T) crit.uniqueResult();
	}

	protected <K extends AssignableResourceSession<T>> K createResourceSession(
			T resource, Session session, K newSession) {

		newSession.setSession(session);
		newSession.setResource(resource);

		save(newSession);

		return newSession;
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceByName(String name, Realm realm) {
		return get("name", name, getResourceClass(), new DeletedCriteria(false), new RealmRestriction(realm), new PersonalRestriction(false));
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceByName(String name, Realm realm, boolean deleted) {
		return get("name", name, getResourceClass(), new DeletedCriteria(
				deleted), new RealmRestriction(realm), new PersonalRestriction(false));
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceByReference(String reference, Realm realm) {
		if(NumberUtils.isCreatable(reference)) {
			return getResourceById(Long.parseLong(reference));
		} else {
			return getResourceByName(reference, realm);
		}
	}
	
	@Override
	@Transactional(readOnly=true)
	public T getResourceById(Long id) {
		return get("id", id, getResourceClass());
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceByLegacyId(Long id) {
		return get("legacyId", id, getResourceClass());
	}
	
	@Override
	@Transactional
	public void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceException {

		for(TransactionOperation<T> op : ops) {
			op.beforeOperation(resource, null);
		}
		
		beforeDelete(resource);
		
		delete(resource);

		afterDelete(resource);
		
		for(TransactionOperation<T> op : ops) {
			op.afterOperation(resource, null);
		}
	}
	
	@Override
	public List<PropertyChange> populateEntityFields(T resource, Map<String,String> properties) {
		List<PropertyChange> changedProperties = new ArrayList<>();
		if(properties!=null) {
			for(PropertyTemplate template : getPropertyTemplates(resource)) {
				if(properties.containsKey(template.getResourceKey())) {
					/**
					 * Why was this commented out? We have to ensure we only attempt to update
					 * entity properties. Some resources use a mixture of both.
					 */
					if(template.getPropertyStore() instanceof EntityResourcePropertyStore) {
						String val = getValue(resource, template.getResourceKey());
						setValue(resource, template.getResourceKey(), properties.get(template.getResourceKey()));
						
						String newVal = getValue(resource, template.getResourceKey());
						
						/**
						 * LDP - Changed to getValue rather than use property value because the property
						 * value may not be the same as the actual value, for example in the case of enum we
						 * might have ordinal but getValue returns String.
						 */
						if(val == null) {
							val = "";
						}
						if(newVal == null) {
							newVal = "";
						}
						if(!Objects.equals(val, newVal)) {
							changedProperties.add(new PropertyChange(template.getResourceKey(), val, newVal));
						}
						properties.remove(template.getResourceKey());
					}
				}
			}
		}
		return changedProperties;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@SafeVarargs
	@Transactional
	public final List<PropertyChange> saveResource(T resource, Map<String, String> properties, TransactionOperation<T>... ops) throws ResourceException {

		for(TransactionOperation<T> op : ops) {
			op.beforeSetProperties(resource, properties);
		}
		
		List<PropertyChange> changes = populateEntityFields(resource, properties);

		for(TransactionOperation<T> op : ops) {
			op.beforeOperation(resource, properties);
		}
		
		beforeSave(resource, properties);
		
		resource = (T) save(resource);

		// Now set any remaining values
		setValues(resource, properties);
		
		clearPropertyCache(resource);
		
		afterSave(resource, properties);
		
		for(TransactionOperation<T> op : ops) {
			op.afterOperation(resource, properties);
		}
		
		return changes;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<T> getResources(Realm realm) {

		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.setFetchMode("roles", FetchMode.SELECT);
		crit.add(Restrictions.eq("deleted", false));
		crit.add(Restrictions.eq("hidden", false));
		
		new PersonalRestriction(false).configure(crit);
		crit.add(Restrictions.eq("realm", realm));

		return (List<T>) crit.list();
	}
	
	

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<T> allRealmResources(Realm realm) {

		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.setFetchMode("roles", FetchMode.SELECT);
		crit.add(Restrictions.eq("deleted", false));
		crit.add(Restrictions.eq("hidden", false));
		
		crit.add(Restrictions.eq("realm", realm));

		return (List<T>) crit.list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<T> allResources() {

		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.setFetchMode("roles", FetchMode.SELECT);
		crit.add(Restrictions.eq("deleted", false));
		crit.add(Restrictions.eq("hidden", false));
		
		new PersonalRestriction(false).configure(crit);

		return (List<T>) crit.list();
	}

	@Override
	@Transactional(readOnly=true)
	public List<T> search(Realm realm, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting, CriteriaConfiguration... configs) {
		List<T> results = super.search(getResourceClass(), searchColumn, searchPattern, start,
				length, sorting, ArrayUtils.addAll(configs, new DeletedCriteria(false),
						new RoleSelectMode(), new RealmCriteria(realm), new PersonalRestriction(false)));
		return results;
	}
	
	
	@Override
	@Transactional(readOnly=true)
	public long allRealmsResourcesCount() {
		return getCount(getResourceClass(), new DeletedCriteria(false), new PersonalRestriction(false));
	}

	@Override
	@Transactional(readOnly=true)
	public long getResourceCount(Realm realm) {
		return getResourceCount(realm, "name", "");
	}
	
	@Override
	@Transactional(readOnly=true)
	public long getResourceCount(Realm realm, String searchColumn, String searchPattern,
			CriteriaConfiguration... configs) {
		long count = getCount(getResourceClass(), searchColumn, searchPattern,
				ArrayUtils.addAll(configs, new RoleSelectMode(), new DeletedCriteria(false),
						new RealmCriteria(realm), new PersonalRestriction(false)));
		return count;
	}
	
	@Override
	@Transactional(readOnly=true)
	public long getResourceByRoleCount(Realm realm, Role... roles) {
		if(roles.length == 0) {
			return 0L;
		}
		return  getCount(getResourceClass(), new DeletedCriteria(false),
						new RealmCriteria(realm), new RolesCriteria(roles));
	}
	
	@Override
	@Transactional(readOnly=true)
	public Collection<T> getResourcesByRole(Realm realm, Role... roles) {
		return  list(getResourceClass(), new DeletedCriteria(false),
						new RealmCriteria(realm), new RolesCriteria(roles));
	}
	
	protected abstract Class<T> getResourceClass();

	public class RoleSelectMode implements CriteriaConfiguration {

		@Override
		public void configure(Criteria criteria) {
			criteria.setFetchMode("roles", FetchMode.SELECT);
		}
	}
	

	@SuppressWarnings("unchecked")
	protected Collection<T> searchResources(Realm currentRealm, 
			String name, 
			Date createdFrom, Date createdTo, 
			Date lastUpdatedFrom, Date lastUpdatedTo, 
			CriteriaConfiguration... configs) {
	
		Criteria criteria = createCriteria(getResourceClass());
		
		for (String property : resolveCollectionProperties(getResourceClass())) {
			 criteria.setFetchMode(property, org.hibernate.FetchMode.SELECT);
		}
		
		if (!StringUtils.isEmpty(name)) {
			criteria.add(Restrictions.ilike("name", name.replace('*', '%')));
		}
		
		criteria.add(Restrictions.eq("realm", currentRealm));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("hidden", false));
		
		new PersonalRestriction(false).configure(criteria);
		
		if(createdFrom!=null) {
			criteria.add(Restrictions.and(Restrictions.ge("created", createdFrom),
					Restrictions.lt("created", createdTo)));
		}
		
		if(lastUpdatedFrom!=null) {
			criteria.add(Restrictions.and(Restrictions.ge("modifiedDate", lastUpdatedFrom),
					Restrictions.lt("modifiedDate", lastUpdatedTo)));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		return (Collection<T>) criteria.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public void bulkAssignRolesToResource(BulkAssignment bulkAssignment) {
		List<T> assignableResources = createCriteria(getResourceClass(),	"ar").add(Restrictions.in("ar.id",
				bulkAssignment.getResourceIds())).list();

		if(assignableResources == null || assignableResources.isEmpty()) {
			throw new IllegalStateException(String.format("For passed in ids %s no resources were found.",
					bulkAssignment.getResourceIds()));
		}

		List<Role> roleList = createCriteria(Role.class,"role").
				add(Restrictions.in("role.id", bulkAssignment.getRoleIds())).list();

		if(roleList == null || roleList.isEmpty()) {
			throw new IllegalStateException(String.format("For passed in ids %s no roles were found.",
					bulkAssignment.getRoleIds()));
		}

		for (AssignableResource ar: assignableResources) {
			if(BulkAssignmentMode.OverWrite.equals(bulkAssignment.getMode())) {
				ar.getRoles().clear();
				ar.getRoles().addAll(roleList);
			} else {
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
	
	@Transactional
	public void removeAssignments(Role role) {
		
		Collection<T> resources = getResourcesByRole(role.getRealm(), role);
		for(T resource : resources) {
			resource.getRoles().remove(role);
			save(resource);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<T> getResourcesByIds(Long...ids) {
		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("deleted", false));
		
		crit.add(Restrictions.in("id", ids));

		return (List<T>) crit.list();
	}

	@Override
	@Transactional
	public void deleteResources(List<T> resources, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceException {
		for (T resource: resources) {
			deleteResource(resource, ops);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void deleteRealm(Realm realm) {
		
		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.setFetchMode("roles", FetchMode.SELECT);
		crit.add(Restrictions.eq("realm", realm));

		int count = 0;
		for(T t : (List<T>)crit.list()) {
			log.info(String.format("Deleting %s", t.getName()));
			t.getRoles().clear();
			save(t);
			delete(t);
			count++;
		}

		log.info(String.format("Deleted %d %s",count, getResourceClass().getSimpleName()));
		
		flush();
	}
	
	@Override
	public boolean isDeletable() {
		return true;
	}
}
