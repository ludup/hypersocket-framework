package com.hypersocket.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ResourceUtils {

	public static String[] explodeValues(String values) {
		StringTokenizer t = new StringTokenizer(values, "]|[");
		List<String> ret = new ArrayList<String>();
		while (t.hasMoreTokens()) {
			ret.add(t.nextToken());
		}
		return ret.toArray(new String[0]);
	}

}
