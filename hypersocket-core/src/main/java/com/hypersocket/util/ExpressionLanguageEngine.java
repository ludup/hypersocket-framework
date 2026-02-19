package com.hypersocket.util;

import java.util.Map;

public interface ExpressionLanguageEngine {
	Object eval(String scriptText, Map<String, ? extends Object> map) throws Exception;
}