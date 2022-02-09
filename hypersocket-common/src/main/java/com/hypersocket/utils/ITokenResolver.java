package com.hypersocket.utils;

import java.util.HashMap;
import java.util.Map;

public interface ITokenResolver {

    String resolveToken(String tokenName);

	default Map<String,Object> getData() { return new HashMap<>(); };
}
