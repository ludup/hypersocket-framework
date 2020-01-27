package com.hypersocket.utils;

import java.util.HashMap;
import java.util.Map;

public class StaticResolver implements ITokenResolver {

	private Map<String,Object> tokens = new HashMap<String,Object>();
	
	public void addToken(String name, Object value) {
		tokens.put(name, value);
	}
	
	@Override
	public final String resolveToken(String tokenName) {
		Object obj = tokens.get(tokenName);
		if(obj==null) {
			return "";
		}
		return obj.toString();
	}

	public Map<String,Object> getData() {
		return  tokens;
	}
}
