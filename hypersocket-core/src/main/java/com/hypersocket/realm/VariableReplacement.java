package com.hypersocket.realm;

import java.util.Set;

public interface VariableReplacement<T> {

	String replaceVariables(T source, String value);

	Set<String> getVariableNames(Realm realm);
}
