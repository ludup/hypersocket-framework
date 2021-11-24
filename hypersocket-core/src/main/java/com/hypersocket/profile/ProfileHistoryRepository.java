package com.hypersocket.profile;

import java.util.Collection;
import java.util.Date;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntityRepository;

public interface ProfileHistoryRepository extends AbstractEntityRepository<ProfileHistory, Long> {

	void report(Realm realm, long count);

	ProfileHistory getReport(Realm realm, Date today);

	Collection<ProfileHistory> getChartReports(Realm realm);

	

}
