package com.hypersocket.properties;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import com.hypersocket.resource.Resource;

public class ResourceUtils {

	static final String[] DELIMS = { "]|[", "\r\n" };
	
	public static String[] explodeValues(String values) {
		if(StringUtils.isBlank(values)) {
			return new String[] { };
		}
		String delim = null;
		for(String str : DELIMS) {
			if(values.contains(str)) {
				delim = str;
			}
		}
		
		List<String> ret = new ArrayList<String>();
		
		if(delim!=null) {
			StringTokenizer t = new StringTokenizer(values, delim);
			
			while (t.hasMoreTokens()) {
				ret.add(t.nextToken());
			}
		} else {
			ret.add(values);
		}
		return ret.toArray(new String[0]);
	}
	
	public static List<String> explodeCollectionValues(String values) {
		return Arrays.asList(explodeValues(values));
	}
	
	public static String addToValues(String values, String value) {
		List<String> vals;
		if(StringUtils.isNotBlank(value)) {
			vals = new ArrayList<String>(explodeCollectionValues(values));
		} else {
			vals = new ArrayList<String>();
		}
		vals.add(value);
		return implodeValues(vals);
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
	
	public static String implodeValues(String... array) {
		return StringUtils.join(array, "]|[");	
	}
	
	public static String implodeValues(Collection<String> array) {
		return StringUtils.join(array.toArray(new String[0]), "]|[");	
	}

	public static String getNamePairValue(String element) {	
		int idx = element.indexOf('=');
		if(idx > -1) {
			try {
				return URLDecoder.decode(element.substring(idx+1), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("Unsupported UTF-8 encoding?!?!");
			}
		}
		return "";
	}
	
	public static String getNamePairKey(String element) {	
		int idx = element.indexOf('=');
		if(idx > -1) {
			return element.substring(0, idx);
		}
		return element;
	}

	public static boolean isReplacementVariable(String value) {
		return value.startsWith("${") && value.endsWith("}");
	}

	public static boolean isEncrypted(String value) {
		return value.startsWith("!ENC!");
	}

	public static <T extends Resource> String implodeResourceValues(Collection<T> entities) {
		
		StringBuilder buf = new StringBuilder();
		for(Resource e : entities) {
			if(buf.length() > 0) {
				buf.append("]|[");
			}
			buf.append(e.getId());
		}
		return buf.toString();
	}

	public static List<NameValuePair> explodeNamePairs(String values) {
		
		String[] pairs = explodeValues(values);
		List<NameValuePair> result = new ArrayList<NameValuePair>();
		for(String pair : pairs) {
			result.add(new NameValuePair(pair));
		}
		return result;
	}

}
