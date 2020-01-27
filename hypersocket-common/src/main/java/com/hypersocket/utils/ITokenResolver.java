package com.hypersocket.utils;

import java.util.Map;

public interface ITokenResolver {

    String resolveToken(String tokenName);

	Map<String,Object> getData();
}
