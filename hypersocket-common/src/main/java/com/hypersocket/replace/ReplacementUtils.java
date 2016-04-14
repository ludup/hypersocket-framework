package com.hypersocket.replace;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplacementUtils {

	static Logger log = LoggerFactory.getLogger(ReplacementUtils.class);
	
	public static String processTokenReplacements(String value, Map<String,String> replacements) {
		
		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(value);

		StringBuilder builder = new StringBuilder();
		int i = 0;
		while (matcher.find()) {
			String attributeName = matcher.group(1);
			if(!replacements.containsKey(attributeName)) {
				log.debug("Replacement token " + attributeName + " not in list to replace from");
				continue;
			}
		    String replacement = replacements.get(attributeName);
		    builder.append(value.substring(i, matcher.start()));
		    if (replacement == null) {
		        builder.append(matcher.group(0));
		    } else {
		        builder.append(replacement);
		    }
		    i = matcher.end();
		}
		
	    builder.append(value.substring(i, value.length()));
		
		return builder.toString();
	}
}
