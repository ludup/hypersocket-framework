package com.hypersocket.util;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TextProcessor {
	
	public static class ObjectNotation implements Resolver {
		
		@Override
		public String evaluate(String variable, TextProcessor processor) {
			try {
				return BeanUtils.getProperty(processor.bindings, variable);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				return null;
			}
		}
		
	}

	final static String START_EXPR_TAG = "$(expr";
	final static String END_EXPR_TAG = "$(/expr)";

	public interface Resolver {
		String evaluate(String variable, TextProcessor processor);
	}

	final static Log LOG = LogFactory.getLog(TextProcessor.class);

	protected Map<String, Object> bindings = new HashMap<String, Object>();
	protected Map<String, String> map = new HashMap<String, String>();
	protected Map<String, List<Map<String, String>>> listMap = new HashMap<String, List<Map<String, String>>>();
	protected Map<String, Boolean> conditions = new HashMap<String, Boolean>();
	protected List<Resolver> resolvers = new LinkedList<>();

	protected Locale locale;
	private boolean unknownVariablesAreBlank = true;
	private boolean evaluateScripts;
	private boolean captureScriptErrors = true;
	private String defaultExprType = "spel";

	public TextProcessor() {
		this(Locale.getDefault());
	}

	public TextProcessor(Locale locale) {
		this.locale = locale;
	}
	
	public String getDefaultExprType() {
		return defaultExprType;
	}

	public void setDefaultExprType(String defaultExprType) {
		this.defaultExprType = defaultExprType;
	}

	public List<Map<String, String>> addList(String listName) {
		List<Map<String, String>> m = new ArrayList<Map<String, String>>();
		listMap.put(listName, m);
		return m;
	}

	public boolean isCaptureScriptErrors() {
		return captureScriptErrors;
	}

	public void setCaptureScriptErrors(boolean captureScriptErrors) {
		this.captureScriptErrors = captureScriptErrors;
	}

	public boolean isEvaluateScripts() {
		return evaluateScripts;
	}

	public void setEvaluateScripts(boolean evaluateScripts) {
		this.evaluateScripts = evaluateScripts;
	}

	public void enable(String key) {
		conditions.put(key, Boolean.TRUE);
	}

	public void disable(String key) {
		conditions.put(key, Boolean.FALSE);
	}

	public void clearLists() {
		listMap.clear();
	}

	public void addResolver(Resolver resolver) {
		resolvers.add(resolver);
	}

	public Map<String, String> addListRow(String listName) {
		List<Map<String, String>> l = listMap.get(listName);
		if (l == null) {
			l = addList(listName);
		}
		Map<String, String> vars = new HashMap<String, String>();
		l.add(vars);
		return vars;
	}

	public void addVariable(String variable, String value, Map<String, String> vars) {
		vars.put(variable, StringUtils.defaultString(value));
	}

	public void addBindings(String name, Object value) {
		bindings.put(name, value);
	}

	public void addVariable(String variable, String value) {
		addVariable(variable, value, map);
	}

	public boolean isUnknownVariablesAreBlank() {
		return unknownVariablesAreBlank;
	}

	public void setUnknownVariablesAreBlank(boolean unknownVariablesAreBlank) {
		this.unknownVariablesAreBlank = unknownVariablesAreBlank;
	}

	public String process(String text) {
		if (text == null) {
			// text = "!no text!";
			text = "";
		}

		// First look for conditional blocks and remove them if conditions are
		// not met
		while (true) {
			int openStart = text.indexOf("={");
			if (openStart == -1) {
				// No more conditions
				break;
			}
			int openEnd = text.indexOf("}", openStart);
			String conditionName = text.substring(openStart + 2, openEnd);
			boolean match = true;
			if (conditionName.startsWith("!")) {
				match = false;
				conditionName = conditionName.substring(1);
			}

			// Now look for the end of the condition
			String closeTag = "={/" + conditionName + "}";
			int closeStart = text.indexOf(closeTag);
			if (closeStart == -1) {
				LOG.warn("Invalid pattern in message template, no closing tabfor ={" + conditionName + "} was found");
				break;
			}

			if ((match && Boolean.TRUE.equals(conditions.get(conditionName)))
					|| (!match && !Boolean.TRUE.equals(conditions.get(conditionName)))) {
				text = text.substring(0, openStart) + text.substring(openEnd + 1, closeStart)
						+ text.substring(closeStart + closeTag.length());
			} else {
				text = text.substring(0, openStart) + text.substring(closeStart + closeTag.length());
			}
		}

		// First look for the repeating blocks and replace the content inside
		// those first
		for (String listName : listMap.keySet()) {

			// Repeat each list until no more occurrences
			while (true) {
				int start = text.indexOf("#{" + listName + "}");
				if (start != -1) {
					int end = text.indexOf("#{/" + listName + "}");
					if (end == -1) {
						LOG.warn("Invalid pattern in message template, no closing tag for #{" + listName
								+ "} was found");
						break;
					} else {
						StringBuilder bui = new StringBuilder();
						bui.append(text.substring(0, start));
						int i = text.indexOf('}', start);
						if (i == -1) {
							LOG.warn("Invalid pattern in message template, no closing bracket for #{" + listName
									+ "} was found");
							break;
						} else {
							String block = text.substring(i + 1, end);

							for (Map<String, String> v : listMap.get(listName)) {
								bui.append(processText(block, v));
							}

							int i2 = text.indexOf('}', end);
							if (i2 == -1) {
								LOG.warn("Invalid pattern in message template, no closing bracket for #{/" + listName
										+ "} was found");
								break;
							} else {
								bui.append(text.substring(i2 + 1));
							}

							text = bui.toString();

						}
					}
				} else {
					// No more occurrences of this list name
					break;
				}
			}
		}

		// Now the main content
		text = processText(text, map);
		
		// Evaluate scripts first so variable patterns in scripts are replaced
		// TODO provide a way to execute scripts BEFORE variable replacement
		Map<String, ExpressionLanguageEngine> exprEngines = new HashMap<>();
		Map<String, Map<String, Object>> exprBindings = new HashMap<>();
		text = evaluateExpressions(text, exprEngines, exprBindings);

		return text;
	}
	private String evaluateExpressions(String text, Map<String, ExpressionLanguageEngine> engines,
			Map<String, Map<String, Object>> bindings) {
		// Now look for script fragments
		if (evaluateScripts) {
			int startIndx = 0;
			while (true) {
				int openStart = text.indexOf(START_EXPR_TAG, startIndx);
				if (openStart == -1) {
					// No more conditions
					break;
				}

				int openStartEnd = text.indexOf(")", openStart);
				if (openStartEnd == -1) {
					startIndx = START_EXPR_TAG.length() + 1;
					continue;
				}

				String lang = text.substring(openStart + START_EXPR_TAG.length(), openStartEnd).trim();
				if (StringUtils.isBlank(lang))
					lang = defaultExprType;
				
				if(!lang.equals("spel")) {
					throw new IllegalArgumentException("Spring Expression Language parser is currently the only supported expression language in text processing.");
				}

				ExpressionLanguageEngine eng = engines.get(lang);
				if (eng == null) {
					if (lang.equals("spel")) {
						eng = new SpELEngine();
					} else {
						throw new IllegalArgumentException(
									String.format("No known script engine for type %s", lang));
					}
					engines.put(lang, eng);
					Map<String, Object> binding = new HashMap<>();
					binding.put("log", LOG);
					binding.put("processor", this);
					binding.put("attributes", new HashMap<String, Object>(map));
					binding.putAll(this.bindings);
					bindings.put(lang, binding);
				}

				int openEnd = text.indexOf(END_EXPR_TAG, openStart);
				if (openEnd != -1) {
					String scriptText = text.substring(openStartEnd + 1, openEnd);
					if (scriptText.startsWith("// <![CDATA[")) {
						scriptText = scriptText.substring(12);
					}
					if (scriptText.endsWith("// ]]")) {
						scriptText = scriptText.substring(0, scriptText.length() - 5);
					}
					Object val = null;
					if (captureScriptErrors) {
						try {
							val = eng.eval(scriptText, bindings.get(lang));
						} catch (Exception e) {
							LOG.error("Embedded expression failed.", e);
							val = "[Error:" + e.getMessage() + "]";
						}
					} else {
						try {
							val = eng.eval(scriptText, bindings.get(lang));
						} catch (Exception e) {
							throw new IllegalArgumentException("Failed to evaluate expression '" + scriptText + "' for '" + "'.", e);
						}
					}
					String str = val == null ? "" : val.toString();
					text = text.substring(0, openStart) + str + text.substring(openEnd + END_EXPR_TAG.length());
					startIndx = openEnd + END_EXPR_TAG.length();
				} else {
					LOG.warn("Unclosed $(expr) tag in message template.");
					startIndx = openStartEnd;
				}
			}
		}
		return text;
	}
	private String processText(String text, Map<String, String> map) {
		final Map<String, String> allVars = new HashMap<>(map);
		buildAllVars(allVars);
		List<Resolver> r = new LinkedList<>(resolvers);
		r.add(new Resolver() {
			@Override
			public String evaluate(String variable, TextProcessor processor) {
				return allVars.get(variable);
			}
		});

		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(text);
		StringBuilder builder = new StringBuilder();
		int i = 0;
		while (matcher.find()) {
			String attributeName = matcher.group(1);
			String replacement = null;
			for (Resolver resolver : r) {
				replacement = resolver.evaluate(attributeName, this);
				if (replacement != null) {
					break;
				}
			}
			builder.append(text.substring(i, matcher.start()));
			if (replacement == null) {
				if (!unknownVariablesAreBlank)
					builder.append(matcher.group(0));
			} else {
				builder.append(replacement);
			}
			i = matcher.end();

		}
		builder.append(text.substring(i, text.length()));
		text = builder.toString();
		return text;
	}

	protected void buildAllVars(Map<String, String> allVars) {
	}
}