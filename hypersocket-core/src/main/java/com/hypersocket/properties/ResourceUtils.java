package com.hypersocket.properties;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.hypersocket.resource.SimpleResource;
import com.hypersocket.resource.Resource;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.utils.HypersocketUtils;

public class ResourceUtils {

	static final String[] DELIMS = { "]|[", "\r\n" };
	static Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
	
	public static String[] explodeValues(String values) {
		if(StringUtils.isBlank(values)) {
			return new String[] { };
		}
		List<String> ret = new ArrayList<>();

		String[] v = StringUtils.splitByWholeSeparator(values, "]|[");
		
		for(String val : v){
			StringTokenizer t2 = new StringTokenizer(val, "\r\n");
			while(t2.hasMoreTokens()) {
				ret.add(t2.nextToken());
			}
		}
	
		return ret.toArray(new String[0]);
	}
	
	public static List<String> explodeCollectionValues(String values) {
		return Arrays.asList(explodeValues(values));
	}
	
	public static String addToValues(String values, String value) {
		List<String> vals;
		if(StringUtils.isNotBlank(value)) {
			vals = new ArrayList<>(explodeCollectionValues(values));
		} else {
			vals = new ArrayList<>();
		}
		vals.add(value);
		return implodeValues(vals);
	}
	
	public static <T extends SimpleResource> String createCommaSeparatedString(Collection<T> resources) {
		return createDelimitedResourceString(resources, ",");
	}
	
	public static <T extends SimpleResource> String createCommaSeparatedString(Collection<T> resources, ResourceNameCallback<T> callback) {
		return createDelimitedResourceString(resources, ",", callback);
	}
	
	public static <T extends SimpleResource> String createDelimitedResourceString(Collection<T> resources, String delimiter) {
		return createDelimitedResourceString(resources, delimiter, new ResourceNameCallback<T>() {

			@Override
			public String getResourceName(T resource) {
				return resource.getName();
			}
			
		});
	}
	public static <T extends SimpleResource> String createDelimitedResourceString(Collection<T> resources, String delimiter, ResourceNameCallback<T> callback) {
		StringBuffer buf = new StringBuffer();
		for(T r : resources) {
			if(buf.length() > 0) {
				buf.append(delimiter);
			}
			buf.append(callback.getResourceName(r));
		}
		return buf.toString();
	}
	
	public static String createDelimitedString(Collection<String> resources, String delimiter) {
		StringBuffer buf = new StringBuffer();
		for(String r : resources) {
			if(buf.length() > 0) {
				buf.append(delimiter);
			}
			buf.append(r);
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

	public static String[] getNamePairArray(String[] source) {
		if(source==null) {
			return null;
		}
		String[] dest = new String[source.length];
		for(int i=0;i<source.length;i++) {
			dest[i] = HypersocketUtils.urlDecode(source[i]);
		}
		return dest;
	}
	
	public static boolean isReplacementVariable(String value) {
		return value.startsWith("${") && value.endsWith("}");
	}
	
	public static boolean containsReplacementVariable(String value) {
		Matcher m = pattern.matcher(value);
		return m.find();
	}
	
	public static String getReplacementVariableName(String value) {
		Matcher m = pattern.matcher(value);
		m.find();
		return m.group(1);
	}

	public static boolean isEncrypted(String value) {
		return value!=null && (value.startsWith(getEncryptedTag()) || value.startsWith(getUUIDEncryptedTag()));
	}
	
	public static boolean isEncryptedUUIDType(String value) {
		return value!=null && value.startsWith(getUUIDEncryptedTag());
	}
	
	public static String getUUIDEncryptedTag() {
		return "!ENU!";
	}

	public static <T extends SimpleResource> String implodeResourceValues(Collection<T> entities) {
		
		StringBuilder buf = new StringBuilder();
		for(SimpleResource e : entities) {
			if(buf.length() > 0) {
				buf.append("]|[");
			}
			buf.append(e.getId());
		}
		return buf.toString();
	}
	
	public static <T extends SimpleResource> String implodeResourceNames(Collection<T> entities,
			ResourceNameCallback<T> callback) {
		
		StringBuilder buf = new StringBuilder();
		for(T e : entities) {
			if(buf.length() > 0) {
				buf.append("]|[");
			}
			buf.append(callback.getResourceName(e));
		}
		return buf.toString();
	}
	
	public static <T extends SimpleResource> String implodeNamePairValues(Collection<T> entities) {
		return implodeNamePairValues(new NameValueImploder<T>() {
			@Override
			public String getId(T t) {
				return String.valueOf(t.getId());
			}
			@Override
			public String getName(T t) {
				return t.getName();
			}
		}, entities);
	}
	
	public static String implodeNamePairs(Collection<NameValuePair> pairs) {
		StringBuilder buf = new StringBuilder();
		for(NameValuePair pair : pairs) {
			if(buf.length() > 0) {
				buf.append("]|[");
			}
			buf.append(pair.getName());
			buf.append("=");
			buf.append(HypersocketUtils.urlEncode(pair.getValue()));
		}
		return buf.toString();
	}
	
	public static <T extends SimpleResource> String implodeNamePairValues(NameValueImploder<T> imploder, Collection<T> entities) {
		
		StringBuilder buf = new StringBuilder();
		for(T e : entities) {
			if(buf.length() > 0) {
				buf.append("]|[");
			}
			buf.append(imploder.getId(e));
			buf.append("=");
			buf.append(HypersocketUtils.urlEncode(imploder.getName(e)));
		}
		return buf.toString();
	}

	public static List<NameValuePair> explodeNamePairs(String values) {
		
		String[] pairs = explodeValues(values);
		List<NameValuePair> result = new ArrayList<>();
		for(String pair : pairs) {
			result.add(new NameValuePair(pair));
		}
		return result;
	}

	public static <T extends Enum<?>> String implodeEnumValues(Collection<T> collection) {
		StringBuilder buf = new StringBuilder();
		if(collection != null) {
			for(Enum<?> e : collection) {
				if(buf.length() > 0) {
					buf.append("]|[");
				}
				buf.append(e.ordinal());
			}
		}
		return buf.toString();
	}

	public static String getEncryptedTag() {
		return "!ENC!";
	}
	
	public static String addEncryptedTag(String value) {
		if(value==null) {
			return value;
		}
		return getUUIDEncryptedTag() + value;
	}
	
	public static String removeEncryptedTag(String value) {
		if(value==null) {
			return value;
		}
		return value.replaceFirst(getEncryptedTag(), "").replaceFirst(getUUIDEncryptedTag(), "");
	}
	
	public static boolean isNamePair(String id) {
		return id.indexOf("=") > -1;
	}

	public static String encodeNamePair(String key, String value) {
		return key + "=" + value;
	}

	public static Long[] createResourceIdArray(Resource... resources) {
		return createResourceIdArray(Arrays.asList(resources));
	}
	
	public static Long[] createResourceIdArray(Collection<? extends Resource> resources) {
		List<Long> ids = new ArrayList<>();
		for(Resource r : resources) {
			ids.add(r.getId());
		}
		return ids.toArray(new Long[0]);
	}

	public static Map<String,String> filterResourceProperties(Collection<PropertyTemplate> templates, Map<String,String> properties) {
		
		Map<String,String> ret = new HashMap<>();
		for(PropertyTemplate t : templates) {
			if(!(t.getPropertyStore() instanceof EntityResourcePropertyStore)) {
				String value = properties.get(t.getResourceKey());
				if(value!=null) {
					ret.put(t.getResourceKey(), value);
				}
			}
		}
		return ret;
	}

	public static String implodeObjectValues(Collection<?> array) {
		return StringUtils.join(array.toArray(new Object[0]), "]|[");	
	}

	public static String implodeFileUploads(FileUpload[] attachments) {
		StringBuilder buf = new StringBuilder();
		if(attachments != null) {
			for(FileUpload e : attachments) {
				if(buf.length() > 0) {
					buf.append("]|[");
				}
				buf.append(e.getUUID());
			}
		}
		return buf.toString();
	}
}
