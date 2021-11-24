package com.hypersocket.profile;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.json.utils.HypersocketUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;

@Repository
public class ProfileHistoryRepositoryImpl extends AbstractEntityRepositoryImpl<ProfileHistory, Long> implements ProfileHistoryRepository {

	static Logger log = LoggerFactory.getLogger(ProfileHistoryRepositoryImpl.class);
	
	@Override
	protected Class<ProfileHistory> getEntityClass() {
		return ProfileHistory.class;
	}

	@Transactional
	@Override
	public void report(Realm realm, long count) {
	
		Date today = HypersocketUtils.today();
		
		ProfileHistory report = getReport(realm, today);
		
		if(report==null) {
			report = new ProfileHistory();
			report.setRealmId(realm.getId());
			report.setReportDate(today);
			report.setProfileCount(count);
			
			save(report);
		} else {
			
			if(report.getProfileCount() < count) {
				report.setProfileCount(count);
				save(report);
			}
		}
	}

	@Transactional(readOnly = true)
	@Override
	public ProfileHistory getReport(Realm realm, Date today) {
		return get("reportDate", today, ProfileHistory.class, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("realmId", realm.getId()));
			}
		});
	}
	
	@Transactional(readOnly = true)
	@Override
	public Collection<ProfileHistory> getChartReports(Realm realm) {
		return list(ProfileHistory.class, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DAY_OF_MONTH, -90);
				
				criteria.add(Restrictions.eq("realmId", realm.getId()));
				criteria.add(Restrictions.gt("reportDate", c.getTime()));
			}
			
		});
	}

}
