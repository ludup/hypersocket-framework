package com.hypersocket.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import com.hypersocket.resource.Resource;

public class ResourceUtils {

	public static String[] explodeValues(String values) {
		StringTokenizer t = new StringTokenizer(values, "]|[");
		List<String> ret = new ArrayList<String>();
		while (t.hasMoreTokens()) {
			ret.add(t.nextToken());
		}
		return ret.toArray(new String[0]);
	}
	
	public static <T extends Resource> String createCommaSeparatedString(Collection<T> resources) {
		return createDelimitedString(resources, ",");
	}
	
	public static <T extends Resource> String createDelimitedString(Collection<T> resources, String delimiter) {
		StringBuffer buf = new StringBuffer();
		for(Resource r : resources) {
			if(buf.length() > 0) {
				buf.append(delimiter);
			}
			buf.append(r.getName());
		}
		return buf.toString();
	}

	public static String implodeValues(String[] array) {
		return StringUtils.join(array, "]|[");	
	}
	
	public static String implodeValues(List<String> array) {
		return StringUtils.join(array.toArray(new String[0]), "]|[");	
	}

}
