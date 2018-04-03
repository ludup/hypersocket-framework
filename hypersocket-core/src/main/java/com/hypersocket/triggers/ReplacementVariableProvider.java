package com.hypersocket.triggers;

import java.util.Set;

public interface ReplacementVariableProvider {

	Set<String> getReplacementVariableNames();
	
	String getReplacementValue(String variable);
}
