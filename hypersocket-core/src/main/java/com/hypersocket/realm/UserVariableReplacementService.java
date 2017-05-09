package com.hypersocket.realm;

import java.util.Set;

public interface UserVariableReplacementService {

	void registerReplacement(UserVariableReplacement replacement);

	String getVariableValue(Principal principal, String variableName);

	Set<String> getVariableNames(Realm realm);
	
	Set<String> getVariableNames(Principal realm);

	String replaceVariables(Principal principal, String value);

}
