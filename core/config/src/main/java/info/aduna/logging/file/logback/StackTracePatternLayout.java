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
	 * @see <a
	 *      href="https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html">
	 *      Information on Log4J conversion patterns.</a>
	 */
	public StackTracePatternLayout(String conversionPattern) {
		super();
		this.setPattern(conversionPattern);
	}
}
