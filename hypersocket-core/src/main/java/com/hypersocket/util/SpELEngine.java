package com.hypersocket.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpELEngine implements ExpressionLanguageEngine {
	
	private static List<String> whitelistPackages;
	private static List<String> whitelistClasses;
	private static List<String> whitelistResources;
	private static List<String> blacklistPackages;
	private static List<String> blacklistClasses;
	private static List<String> blacklistResources;
	private SpelParserConfiguration config;
	
	static {
		whitelistPackages = Arrays.asList(System.getProperty("spel.whitelistPackages", "org.springframework.expression.spel,java.util,java.lang").split(",")).stream().filter(s -> !s.isBlank()).collect(Collectors.toList());
		whitelistClasses = Arrays.asList(System.getProperty("spel.whitelistClasses", "org.springframework.expression.spel.,java.util.,java.lang.").split(",")).stream().filter(s -> !s.isBlank()).collect(Collectors.toList());
		whitelistResources = Arrays.asList(System.getProperty("spel.whitelistResources", "").split(",")).stream().filter(s -> !s.isBlank()).collect(Collectors.toList());
		blacklistPackages = Arrays.asList(System.getProperty("spel.blacklistPackages", "").split(",")).stream().filter(s -> !s.isBlank()).collect(Collectors.toList());
		blacklistClasses = Arrays.asList(System.getProperty("spel.blacklistClasses", "java.lang.System").split(",")).stream().filter(s -> !s.isBlank()).collect(Collectors.toList());
		blacklistResources = Arrays.asList(System.getProperty("spel.blacklistResources", "").split(",")).stream().filter(s -> !s.isBlank()).collect(Collectors.toList());
	}
	
	public SpELEngine() {
		this(new FilteredClassLoader.Builder(SpELEngine.class.getClassLoader()).denyIf(cl -> {
			if (cl.kind == FilteredClassLoader.Request.Kind.RESOURCE) {
				var denied = isDenied(blacklistResources, whitelistResources, cl.name);
				if(denied.isPresent()) {
					return denied.get();
				}
			} else if (cl.kind == FilteredClassLoader.Request.Kind.PACKAGE) {
				var denied = isDenied(blacklistPackages, whitelistPackages, cl.name);
				if(denied.isPresent()) {
					return denied.get();
				}
			} else {
				var denied = isDenied(blacklistClasses, whitelistClasses, cl.name);
				if(denied.isPresent()) {
					return denied.get();
				}
			}
			return true;
		}).build());
	}
	
	private static Optional<Boolean> isDenied(List<String> blacklist, List<String> whitelist, String name) {
		for (var res : blacklist) {
			if (name.startsWith(res)) {
				return Optional.of(true);
			}
		}
		
		for (var res : whitelist) {
			if (name.startsWith(res)) {
				return Optional.of(false);
			}
		}
		
		return Optional.empty();
	}

	public SpELEngine(FilteredClassLoader fcl) {
		config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, fcl);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object eval(String scriptText, Map<String, ? extends Object> map) throws Exception {
		ExpressionParser parser = new SpelExpressionParser(config);
		StandardEvaluationContext evalContext = new StandardEvaluationContext();
		evalContext.setVariables((Map<String, Object>) map);
		Expression exp = parser.parseExpression(scriptText);
		return exp.getValue(evalContext);
	}
	
}