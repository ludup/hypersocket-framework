package com.hypersocket.utils;

import java.util.Map;

public interface ITokenResolver {

    public String resolveToken(String tokenName);

	public Map<String,Object> getData();
}
