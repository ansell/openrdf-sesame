/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
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
