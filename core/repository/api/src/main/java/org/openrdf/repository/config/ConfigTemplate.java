/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
