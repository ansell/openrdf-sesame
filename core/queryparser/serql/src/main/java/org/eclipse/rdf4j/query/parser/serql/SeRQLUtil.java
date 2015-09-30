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
package org.eclipse.rdf4j.query.parser.serql;

import org.eclipse.rdf4j.common.text.StringUtil;

/**
 * SeRQL-related utility methods.
 * 
 * @author Arjohn Kampman
 */
public class SeRQLUtil {

	/**
	 * Encodes the supplied string for inclusion as a 'normal' string in a SeRQL
	 * query.
	 */
	public static String encodeString(String s) {
		s = StringUtil.gsub("\\", "\\\\", s);
		s = StringUtil.gsub("\t", "\\t", s);
		s = StringUtil.gsub("\n", "\\n", s);
		s = StringUtil.gsub("\r", "\\r", s);
		s = StringUtil.gsub("\b", "\\b", s);
		s = StringUtil.gsub("\f", "\\f", s);
		s = StringUtil.gsub("\"", "\\\"", s);
		return s;
	}

	/**
	 * Decodes an encoded SeRQL string. Any \-escape sequences are substituted
	 * with their decoded value.
	 * 
	 * @param s
	 *        An encoded SeRQL string.
	 * @return The unencoded string.
	 * @exception IllegalArgumentException
	 *            If the supplied string is not a correctly encoded SeRQL
	 *            string.
	 */
	public static String decodeString(String s) {
		int backSlashIdx = s.indexOf('\\');

		if (backSlashIdx == -1) {
			// No escaped characters found
			return s;
		}

		int startIdx = 0;
		int sLength = s.length();
		StringBuilder sb = new StringBuilder(sLength);

		while (backSlashIdx != -1) {
			sb.append(s.substring(startIdx, backSlashIdx));

			if (backSlashIdx + 1 >= sLength) {
				throw new IllegalArgumentException("Unescaped backslash in: " + s);
			}

			char c = s.charAt(backSlashIdx + 1);

			if (c == 't') {
				sb.append('\t');
				startIdx = backSlashIdx + 2;
			}
			else if (c == 'n') {
				sb.append('\n');
				startIdx = backSlashIdx + 2;
			}
			else if (c == 'r') {
				sb.append('\r');
				startIdx = backSlashIdx + 2;
			}
			else if (c == 'b') {
				sb.append('\b');
				startIdx = backSlashIdx + 2;
			}
			else if (c == 'f') {
				sb.append('\f');
				startIdx = backSlashIdx + 2;
			}
			else if (c == '"') {
				sb.append('"');
				startIdx = backSlashIdx + 2;
			}
			else if (c == '\\') {
				sb.append('\\');
				startIdx = backSlashIdx + 2;
			}
			else if (c == 'u') {
				// \\uxxxx
				if (backSlashIdx + 5 >= sLength) {
					throw new IllegalArgumentException(
							"Incomplete Unicode escape sequence in: " + s);
				}
				String xx = s.substring(backSlashIdx + 2, backSlashIdx + 6);

				try {
					c = (char)Integer.parseInt(xx, 16);
					sb.append( c );

					startIdx = backSlashIdx + 6;
				}
				catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"Illegal Unicode escape sequence '\\u" + xx + "' in: " + s);
				}
			}
			else if (c == 'U') {
				// \\Uxxxxxxxx
				if (backSlashIdx + 9 >= sLength) {
					throw new IllegalArgumentException(
							"Incomplete Unicode escape sequence in: " + s);
				}
				String xx = s.substring(backSlashIdx + 2, backSlashIdx + 10);

				try {
					c = (char)Integer.parseInt(xx, 16);
					sb.append( c );

					startIdx = backSlashIdx + 10;
				}
				catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"Illegal Unicode escape sequence '\\U" + xx + "' in: " + s);
				}
			}
			else {
				throw new IllegalArgumentException("Unescaped backslash in: " + s);
			}

			backSlashIdx = s.indexOf('\\', startIdx);
		}

		sb.append(s.substring(startIdx));

		return sb.toString();
	}
}
