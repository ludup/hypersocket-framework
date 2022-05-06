package com.hypersocket.profile;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.local.PrincipalTypeRestriction;
import com.hypersocket.realm.DelegationCriteria;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.resource.RealmCriteria;
import com.hypersocket.resource.RealmsCriteria;
import com.hypersocket.tables.ColumnSort;

@Repository
public class ProfileRepositoryImpl extends AbstractEntityRepositoryImpl<Profile, Long> implements ProfileRepository {

	static Logger log = LoggerFactory.getLogger(ProfileRepositoryImpl.class);
	
	@Override
	protected Class<Profile> getEntityClass() {
		return Profile.class;
	}

	@Autowired
	private DelegationCriteria delegationCriteria;
	
	@Override
	@Transactional(readOnly=true)
	public long getCompleteProfileCount(Collection<Realm>  realm) {
		return getCount(Profile.class, new RealmsCriteria(realm), new DeletedCriteria(false), new CriteriaConfiguration(){

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("state", ProfileCredentialsState.COMPLETE));
			} 
		});
	}
	
	@Override
	@Transactional(readOnly=true)
	public long getCompleteProfileOnDateCount(Collection<Realm> realm, final Date date) {
		return getCount(Profile.class, new RealmsCriteria(realm), new DeletedCriteria(false),  new CriteriaConfiguration(){

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("state", ProfileCredentialsState.COMPLETE));
				criteria.add(Restrictions.eq("completed", date));
			} 
		});
	}
	
	@Override
	@Transactional(readOnly=true)
	public long getIncompleteProfileCount(Collection<Realm> realm) {
		return getCount(Profile.class, new RealmsCriteria(realm), new DeletedCriteria(false), new CriteriaConfiguration(){

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("state", ProfileCredentialsState.INCOMPLETE));
			} 
		});
	}
	
	@Override
	@Transactional(readOnly=true)
	public long getPartiallyCompleteProfileCount(Collection<Realm> realm) {
		return getCount(Profile.class, new RealmsCriteria(realm), new DeletedCriteria(false), new CriteriaConfiguration(){

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("state", ProfileCredentialsState.PARTIALLY_COMPLETE));
			} 
		});
	}


	@Override
	public Collection<Profile> getProfilesWithStatus(Collection<Realm> realm, final ProfileCredentialsState...credentialsStates) {
		return list(Profile.class, new RealmsCriteria(realm), new DeletedCriteria(false), new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				if(credentialsStates.length > 0) {
					criteria.add(Restrictions.in("state", credentialsStates));
				}
			}
		});
	}


	@Override
	@Transactional(readOnly=true)
	public List<?> searchIncompleteProfiles(final Realm realm, String searchColumn, String searchPattern, ColumnSort[] sorting, int start, int length) {
		return search(Principal.class, searchColumn, searchPattern, start, length, sorting, 
				new RealmCriteria(realm), 
				new DeletedCriteria(false), 
				new PrincipalTypeRestriction(PrincipalType.USER), delegationCriteria,  new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				DetachedCriteria profileSubquery = DetachedCriteria.forClass(Profile.class, "p")
						.add(Restrictions.eq("p.realm.id", realm.getId()))
						.add(Restrictions.eq("deleted", false))
						.add(Restrictions.in("p.state", 
							new ProfileCredentialsState[] { ProfileCredentialsState.COMPLETE, ProfileCredentialsState.NOT_REQUIRED }))  
					    		.setProjection( Projections.property("p.id"));
				
				criteria.add(Subqueries.propertyNotIn("id", profileSubquery));
			}
			
		});
	}


	@Override
	@Transactional(readOnly=true)
	public Long searchIncompleteProfilesCount(final Realm realm, String searchColumn, String searchPattern) {
		
		return getCount(Principal.class, searchColumn, searchPattern, 
				new RealmCriteria(realm), 
				new DeletedCriteria(false), 
				new PrincipalTypeRestriction(PrincipalType.USER), delegationCriteria, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				DetachedCriteria profileSubquery = DetachedCriteria.forClass(Profile.class, "p")
						.add(Restrictions.eq("p.realm.id", realm.getId()))
						.add(Restrictions.eq("deleted", false))
						.add(Restrictions.in("p.state", 
							new ProfileCredentialsState[] { ProfileCredentialsState.COMPLETE, ProfileCredentialsState.NOT_REQUIRED }))  
					    		.setProjection( Projections.property("p.id"));
				
				criteria.add(Subqueries.propertyNotIn("id", profileSubquery));
			}
			
		});
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<?> searchCompleteProfiles(final Realm realm, String searchColumn, String searchPattern, ColumnSort[] sorting, int start, int length) {
		return search(Principal.class, searchColumn, searchPattern, start, length, sorting, new RealmCriteria(realm), 
				new DeletedCriteria(false), 
				new PrincipalTypeRestriction(PrincipalType.USER), delegationCriteria, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				DetachedCriteria profileSubquery = DetachedCriteria.forClass(Profile.class, "p")
						.add(Restrictions.eq("p.realm.id", realm.getId()))
						.add(Restrictions.eq("deleted", false))
						.add(Restrictions.in("p.state",
							new ProfileCredentialsState[] { ProfileCredentialsState.COMPLETE }))  
					    		.setProjection( Projections.property("p.id"));
				
				criteria.add(Subqueries.propertyIn("id", profileSubquery));
			}
			
		});
	}


	@Override
	@Transactional(readOnly=true)
	public Long searchCompleteProfilesCount(final Realm realm, String searchColumn, String searchPattern) {
		
		return getCount(Principal.class, searchColumn, searchPattern, new RealmCriteria(realm), 
				new DeletedCriteria(false), 
				new PrincipalTypeRestriction(PrincipalType.USER), delegationCriteria, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				DetachedCriteria profileSubquery = DetachedCriteria.forClass(Profile.class, "p")
						.add(Restrictions.eq("p.realm.id", realm.getId()))
						.add(Restrictions.eq("deleted", false))
						.add(Restrictions.in("p.state", 
							new ProfileCredentialsState[] { ProfileCredentialsState.COMPLETE }))  
					    		.setProjection( Projections.property("p.id"));
				
				criteria.add(Subqueries.propertyIn("id", profileSubquery));
			}
			
		});
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<?> searchNeverVisitedProfiles(final Realm realm, String searchColumn, String searchPattern, ColumnSort[] sorting, int start, int length) {
		return search(Principal.class, searchColumn, searchPattern, start, length, sorting, new RealmCriteria(realm), 				
				new DeletedCriteria(false), 
				new PrincipalTypeRestriction(PrincipalType.USER), delegationCriteria, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				DetachedCriteria profileSubquery = DetachedCriteria.forClass(Profile.class, "p")
						.add(Restrictions.eq("p.realm.id", realm.getId()))
						.add(Restrictions.eq("deleted", false))
						.setProjection( Projections.property("p.id"));
				
				criteria.add(Subqueries.propertyNotIn("id", profileSubquery));
			}
			
		});
	}


	@Override
	@Transactional(readOnly=true)
	public Long searchNeverVisitedProfilesCount(final Realm realm, String searchColumn, String searchPattern) {
		
		return getCount(Principal.class, searchColumn, searchPattern, new RealmCriteria(realm), 
				new DeletedCriteria(false), 
				new PrincipalTypeRestriction(PrincipalType.USER), delegationCriteria, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				DetachedCriteria profileSubquery = DetachedCriteria.forClass(Profile.class, "p") 
								.add(Restrictions.eq("p.realm.id", realm.getId()))
								.add(Restrictions.eq("deleted", false))
					    		.setProjection( Projections.property("p.id"));
				
				criteria.add(Subqueries.propertyNotIn("id", profileSubquery));
			}
			
		});
	}


	@Override
	@Transactional(readOnly=true)
	public boolean hasCompletedProfile(Principal principal) {
		return get("id", principal.getId(), Profile.class, new DeletedCriteria(false), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("state", ProfileCredentialsState.COMPLETE));
			}
			
		}) != null;
	}
	
	@Override
	@Transactional(readOnly=true)
	public boolean hasPartiallyCompletedProfile(Principal principal) {
		return get("id", principal.getId(), Profile.class, new DeletedCriteria(false), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("state", ProfileCredentialsState.PARTIALLY_COMPLETE));
			}
			
		}) != null;
	}

	@Override
	@Transactional(readOnly=true)
	public boolean isPrincipalActive(Principal principal) {
		return get("id", principal.getId(), Profile.class, new DeletedCriteria(false), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				Disjunction dj = Restrictions.disjunction();
				dj.add(Restrictions.eq("state", ProfileCredentialsState.COMPLETE));
				dj.add(Restrictions.eq("state", ProfileCredentialsState.PARTIALLY_COMPLETE));
				criteria.add(dj);
			}
			
		}) != null;
	}

	@Override
	@Transactional
	public void deleteRealm(Realm realm) {
		Query q2 = createQuery("delete from ProfileCredentials where profile in (select id from Profile where realm = :r)", true);
		q2.setParameter("r", realm);
		log.info(String.format("Deleted %d Profile Credentials", q2.executeUpdate()));
		
		Query q = createQuery("delete from Profile where realm = :r", true);
		q.setParameter("r", realm);
		log.info(String.format("Deleted %d Profile", q.executeUpdate()));
	}
	
}
