package com.hypersocket.realm;

import java.util.Set;

public interface UserVariableReplacement extends
		VariableReplacement<Principal>  {

	String getVariableValue(Principal principal, String variableName);

	Set<String> getVariableNames(Realm realm);
	
	Set<String> getVariableNames(Principal realm);

}
