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

package info.aduna.logging.file.logback;

import java.util.regex.Pattern;

import ch.qos.logback.classic.PatternLayout;

/**
 * PatternLayout that also prints stacktraces.
 * 
 * @author Herko ter Horst
 */
public class StackTracePatternLayout extends PatternLayout {

	static final String DEFAULT_CONVERSION_PATTERN = "[%-5p] %d{ISO8601} [%t] %m%n%ex";

	public static final Pattern DEFAULT_PARSER_PATTERN = Pattern.compile("\\[([^\\]]*)\\] ([^\\[]*)\\[([^\\]]*)\\] (.*)");

	/**
	 * Construct a StacktracePatternLayout with the default conversion pattern.
	 */
	public StackTracePatternLayout() {
		this(DEFAULT_CONVERSION_PATTERN);
	}

	/**
	 * Construct a StacktracePatternLayout with the specified conversion pattern.
	 * 
	 * @param conversionPattern
	 *        the conversion pattern to use
	 * @see org.apache.log4j.PatternLayout for information on conversion
	 *      patterns.
	 */
	public StackTracePatternLayout(String conversionPattern) {
		super();
		this.setPattern(conversionPattern);
	}
}
