package com.hypersocket.password.history;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;

@Repository
public class PasswordHistoryRepositoryImpl extends AbstractEntityRepositoryImpl<PasswordHistory,Long> 
			implements PasswordHistoryRepository {

	@Override
	protected Class<PasswordHistory> getEntityClass() {
		return PasswordHistory.class;
	}


	@Override
	@Transactional(readOnly=true)
	public Collection<PasswordHistory> getPasswordHistory(Principal principal, final int previous) {
		return list("principal", principal, PasswordHistory.class, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.setMaxResults(previous);
				criteria.addOrder(Order.desc("created"));
			}
		});
	}
	
	
	@Override
	@Transactional(readOnly=true)
	public PasswordHistory getHistoryFor(Principal principal, final String password) {
		return get("principal", principal, PasswordHistory.class, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("encodedPassword", password));
				criteria.setMaxResults(1);
				criteria.addOrder(Order.desc("created"));
			}
		});
	}


	@Override
	@Transactional
	public void savePassword(PasswordHistory password) {
		save(password, true);
	}


	@Override
	public void deleteRealm(Realm realm) {
		Query q3 = createQuery("delete from PasswordHistory where principal.id in (select principal.id from Principal principal where realm = :r)", true);
		q3.setParameter("r", realm);
		q3.executeUpdate();
		flush();
	}

	
}
