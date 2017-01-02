package com.hypersocket.realm;

import java.util.Set;

public interface VariableReplacement<T> {

	Set<String> getVariableNames(Realm realm);
}
