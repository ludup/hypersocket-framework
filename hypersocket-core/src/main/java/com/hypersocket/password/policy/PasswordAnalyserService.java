package com.hypersocket.password.policy;

import java.io.IOException;
import java.util.Locale;

public interface PasswordAnalyserService {

	float analyse(Locale locale, String username, char[] password, PasswordPolicyResource characteristics)
			throws PasswordPolicyException, IOException;

}
 