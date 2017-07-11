package com.hypersocket.password.history;

import java.util.Collection;

import com.hypersocket.realm.Principal;
import com.hypersocket.repository.AbstractEntityRepository;

public interface PasswordHistoryRepository extends AbstractEntityRepository<PasswordHistory, Long> {

	Collection<PasswordHistory> getPasswordHistory(Principal principal, int previous);

	PasswordHistory getHistoryFor(Principal principal, String password);

	void savePassword(PasswordHistory p);
}
