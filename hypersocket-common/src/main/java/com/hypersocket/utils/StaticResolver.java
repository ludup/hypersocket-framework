package com.hypersocket.utils;

import java.util.HashMap;
import java.util.Map;

public class StaticResolver implements ITokenResolver {

	Map<String,String> tokens = new HashMap<String,String>();
	public StaticResolver() {
		
	}
	
	protected void addToken(String name, String value) {
		tokens.put(name, value);
	}
	
	@Override
	public final String resolveToken(String tokenName) {
		return tokens.get(tokenName);
	}

}
