package com.hypersocket.realm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractVariableReplacementImpl<T> implements VariableReplacement<T> {

	static Logger log = LoggerFactory.getLogger(AbstractVariableReplacementImpl.class);
	
	protected abstract boolean hasVariable(T source, String name);
	
	public abstract String getVariableValue(T source, String name);
	
	@Override
	public String replaceVariables(T source, String value) {
		
		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(value);

		StringBuilder builder = new StringBuilder();
		
		int i = 0;
		while (matcher.find()) {
			String attributeName = matcher.group(1);
			String replacement;
			if(hasVariable(source, attributeName)) {
				replacement = getVariableValue(source, attributeName);
			} else {
				log.warn("Failed to find replacement token " + attributeName);
				continue;	
			}
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
