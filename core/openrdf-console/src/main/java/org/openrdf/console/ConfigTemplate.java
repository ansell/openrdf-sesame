/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Arjohn Kampman
 */
class ConfigTemplate {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{%[\\p{Print}&&[^\\}]]+%\\}");

	/*-----------*
	 * Variables *
	 *-----------*/

	private String template;

	private Map<String, List<String>> variableMap = new LinkedHashMap<String, List<String>>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ConfigTemplate(String template) {
		setTemplate(template);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		if (template == null) {
			throw new IllegalArgumentException("template must not be null");
		}

		this.template = template;

		parseTemplate();
	}

	private void parseTemplate() {
		Matcher matcher = TOKEN_PATTERN.matcher(template);
		while (matcher.find()) {
			String group = matcher.group();
			String[] tokensArray = group.substring(2, group.length() - 2).split("\\|");
			List<String> tokens = Arrays.asList(tokensArray);

			String var = tokens.get(0).trim();
			List<String> values = tokens.subList(1, tokens.size());

			if (var.length() == 0) {
				throw new IllegalArgumentException("Illegal template token: " + matcher.group());
			}

			variableMap.put(var, values);
		}
	}

	public Map<String, List<String>> getVariableMap() {
		return Collections.unmodifiableMap(variableMap);
	}

	public String render(Map<String, String> valueMap) {
		StringBuffer result = new StringBuffer(template.length());

		Matcher matcher = TOKEN_PATTERN.matcher(template);
		while (matcher.find()) {
			String group = matcher.group();
			String[] tokensArray = group.substring(2, group.length() - 2).split("\\|");
			
			String var = tokensArray[0].trim();

			String value = valueMap.get(var);
			if (value == null) {
				List<String> values = variableMap.get(var);
				if (!values.isEmpty()) {
					value = values.get(0);
				}
			}

			if (value == null) {
				throw new IllegalArgumentException("No value specified for variable " + var);
			}

			matcher.appendReplacement(result, value);
		}

		matcher.appendTail(result);

		return result.toString();
	}
}
