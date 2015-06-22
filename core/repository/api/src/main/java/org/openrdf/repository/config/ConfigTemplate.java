/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.config;

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
public class ConfigTemplate {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{%[\\p{Print}&&[^\\}]]+%\\}");

	/*-----------*
	 * Variables *
	 *-----------*/

	private String template;

	private final Map<String, List<String>> variableMap = new LinkedHashMap<String, List<String>>();

	private final Map<String, String> multilineMap = new LinkedHashMap<String, String>();

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

	public final void setTemplate(String template) {
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
			if (var.length() == 0) {
				throw new IllegalArgumentException("Illegal template token: " + matcher.group());
			}
			if (!variableMap.containsKey(var)) {
				variableMap.put(var, tokens.subList(1, tokens.size()));
				int start = matcher.start();
				String before = template.substring(start - 3, start);
				int end = matcher.end();
				if (("'''".equals(before) || "\"\"\"".equals(before))
						&& before.equals(template.substring(end, end + 3)))
				{
					multilineMap.put(var, before);
				}
			}
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
				value = values.isEmpty() ? "" : values.get(0);
			}
			if (!value.isEmpty() && multilineMap.containsKey(var)) {
				value = escapeMultilineQuotes(multilineMap.get(var), value);
			}
			matcher.appendReplacement(result, value);
		}
		matcher.appendTail(result);
		return result.toString();
	}

	/**
	 * Escape Turtle multiline literal quote characters in the given value.
	 * 
	 * @param quoteVariant
	 *        either ''' or """
	 * @param value
	 *        the value to escape properly
	 * @return the value with any needed multiline quote sequences escaped
	 */
	protected static String escapeMultilineQuotes(String quoteVariant, String value) {
		if ("'''".equals(quoteVariant) || "\"\"\"".equals(quoteVariant)) {
			return value.replace(quoteVariant,
					new String(new char[3]).replace("\0", "\\" + quoteVariant.charAt(0)));
		}
		else {
			throw new IllegalArgumentException("Only a valid Turtle multi-line quote delmiter is allowed.");
		}
	}

	public Map<String, String> getMultilineMap() {
		return Collections.unmodifiableMap(multilineMap);
	}
}
