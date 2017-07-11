package com.hypersocket.password.history;

import com.hypersocket.realm.Principal;

public interface PasswordHistroyService {

	void recordPassword(Principal principal, String password);

	boolean checkPassword(Principal principal, String password, int previous);

}
