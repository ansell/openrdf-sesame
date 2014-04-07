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
package org.openrdf.rio.turtle;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.text.ASCIIUtil;
import info.aduna.text.StringUtil;

/**
 * Utility methods for Turtle encoding/decoding.
 * 
 * @see <a href="http://www.w3.org/TR/turtle/">Turtle: Terse RDF Triple
 *      Language</a>
 */
public class TurtleUtil {

	private static final Logger logger = LoggerFactory.getLogger(TurtleUtil.class);

	public static final char[] LOCAL_ESCAPED_CHARS = new char[] {
			'_',
			'~',
			'.',
			'-',
			'!',
			'$',
			'&',
			'\'',
			'(',
			')',
			'*',
			'+',
			',',
			';',
			'=',
			'/',
			'?',
			'#',
			'@',
			'%' };

	static {
		// sorting array to allow simple binary search for char lookup.
		Arrays.sort(LOCAL_ESCAPED_CHARS);
	}

	/**
	 * Tries to find an index where the supplied URI can be split into a
	 * namespace and a local name that comply with the serialization constraints
	 * of the Turtle format.
	 * 
	 * @param uri
	 *        The URI to split.
	 * @return The index where the supplied URI can be split, or <tt>-1</tt> if
	 *         the URI cannot be split.
	 */
	public static int findURISplitIndex(String uri) {
		int uriLength = uri.length();

		int idx = uriLength - 1;

		// Search last character that is not a name character
		for (; idx >= 0; idx--) {
			if (!TurtleUtil.isNameChar(uri.charAt(idx))) {
				// Found a non-name character
				break;
			}
		}

		idx++;

		// Local names need to start with a 'nameStartChar', skip characters
		// that are not nameStartChar's.
		for (; idx < uriLength; idx++) {
			if (TurtleUtil.isNameStartChar(uri.charAt(idx))) {
				break;
			}
		}

		if (idx > 0 && idx < uriLength) {
			// A valid split index has been found
			return idx;
		}

		// No valid local name has been found
		return -1;
	}

	public static boolean isWhitespace(int c) {
		// Whitespace character are space, tab, newline and carriage return:
		return c == 0x20 || c == 0x9 || c == 0xA || c == 0xD;
	}

	/**
	 * From Turtle Spec:
	 * <p>
	 * http://www.w3.org/TR/turtle/#grammar-production-PN_CHARS_BASE
	 * <p>
	 * [163s] PN_CHARS_BASE ::= [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6]
	 * | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] |
	 * [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] |
	 * [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isPN_CHARS_BASE(int c) {
		return ASCIIUtil.isLetter(c) || c >= 0x00C0 && c <= 0x00D6 || c >= 0x00D8 && c <= 0x00F6 || c >= 0x00F8
				&& c <= 0x02FF || c >= 0x0370 && c <= 0x037D || c >= 0x037F && c <= 0x1FFF || c >= 0x200C
				&& c <= 0x200D || c >= 0x2070 && c <= 0x218F || c >= 0x2C00 && c <= 0x2FEF || c >= 0x3001
				&& c <= 0xD7FF || c >= 0xF900 && c <= 0xFDCF || c >= 0xFDF0 && c <= 0xFFFD || c >= 0x10000
				&& c <= 0xEFFFF;
	}

	/**
	 * From Turtle Spec:
	 * <p>
	 * http://www.w3.org/TR/turtle/#grammar-production-PN_CHARS_U
	 * <p>
	 * [164s] PN_CHARS_U ::= PN_CHARS_BASE | '_'
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isPN_CHARS_U(int c) {
		return isPN_CHARS_BASE(c) || c == '_';
	}

	/**
	 * From Turtle Spec:
	 * <p>
	 * http://www.w3.org/TR/turtle/#grammar-production-PN_CHARS
	 * <p>
	 * [166s] PN_CHARS ::= PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] |
	 * [#x203F-#x2040]
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isPN_CHARS(int c) {
		return isPN_CHARS_U(c) || ASCIIUtil.isNumber(c) || c == '-' || c == 0x00B7 || c >= 0x0300
				&& c <= 0x036F || c >= 0x203F && c <= 0x2040;
	}

	public static boolean isPrefixStartChar(int c) {
		return isPN_CHARS_BASE(c);
	}

	public static boolean isBLANK_NODE_LABEL_StartChar(int c) {
		return isPN_CHARS_U(c) || ASCIIUtil.isNumber(c);
	}

	public static boolean isBLANK_NODE_LABEL_Char(int c) {
		return isPN_CHARS(c) || c == '.';
	}

	public static boolean isBLANK_NODE_LABEL_EndChar(int c) {
		return isPN_CHARS(c);
	}

	public static boolean isNameStartChar(int c) {
		return isPN_CHARS_U(c) || c == ':' || ASCIIUtil.isNumber(c) || c == '\\' || c == '%';
	}

	public static boolean isNameChar(int c) {
		return isPN_CHARS(c) || c == '.' || c == ':' | c == '\\' || c == '%';
	}

	public static boolean isNameEndChar(int c) {
		return isPN_CHARS(c) || c == ':';
	}

	public static boolean isLocalEscapedChar(int c) {
		return Arrays.binarySearch(LOCAL_ESCAPED_CHARS, (char)c) > -1;
	}

	public static boolean isPrefixChar(int c) {
		return isPN_CHARS_BASE(c) || isPN_CHARS(c) || c == '.';
	}

	public static boolean isLanguageStartChar(int c) {
		return ASCIIUtil.isLetter(c);
	}

	public static boolean isLanguageChar(int c) {
		return ASCIIUtil.isLetter(c) || ASCIIUtil.isNumber(c) || c == '-';
	}

	/**
	 * From Turtle Spec:
	 * <p>
	 * http://www.w3.org/TR/turtle/#grammar-production-PN_PREFIX
	 * <p>
	 * [167s] PN_PREFIX ::= PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?
	 * 
	 * @param prefix
	 * @return true iff the supplied prefix conforms to Turtle grammar rules
	 */
	public static boolean isPN_PREFIX(String prefix) {
		// Empty prefixes are not legal, they should always have a colon
		if (prefix.length() == 0) {
			logger.debug("PN_PREFIX was not valid (empty)");
			return false;
		}

		if (!isPN_CHARS_BASE(prefix.charAt(0))) {
			logger.debug("PN_PREFIX was not valid (start character invalid) i=0 nextchar={} prefix=",
					prefix.charAt(0), prefix);
			return false;
		}

		for (int i = 1; i < prefix.length(); i++) {
			if (!isPN_CHARS(prefix.charAt(i)) || (prefix.charAt(i) == '.' && i < (prefix.length() - 1))) {
				logger.debug("PN_PREFIX was not valid (intermediate character invalid) i=" + i
						+ " nextchar={} prefix={}", prefix.charAt(i), prefix);
				return false;
			}

			// Check if the percent encoding was less than two characters from the
			// end of the prefix, in which case it is invalid
			if (prefix.charAt(i) == '%' && (prefix.length() - i) < 2) {
				logger.debug("PN_PREFIX was not valid (percent encoding) i=" + i + " nextchar={} prefix={}",
						prefix.charAt(i), prefix);
				return false;
			}
		}

		return true;
	}

	// public static boolean isLegalPrefix(String prefix) {
	// // Empty prefixes are not legal, they should always have a colon
	// if (prefix.length() == 0) {
	// System.err.println("prefix was not valid (empty)");
	// return false;
	// }
	// if (!isPrefixStartChar(prefix.charAt(0))) {
	// System.err.println("prefix was not valid (start character invalid) i=" + 0
	// + " nextchar="
	// + prefix.charAt(0) + " prefix=" + prefix);
	// return false;
	// }
	//
	// for (int i = 1; i < prefix.length(); i++) {
	// if (!isPrefixChar(prefix.charAt(i))) {
	// System.err.println("prefix was not valid (intermediate character invalid) i="
	// + i + " nextchar="
	// + prefix.charAt(i) + " prefix=" + prefix);
	// return false;
	// }
	//
	// // Check if the percent encoding was less than two characters from the
	// // end of the prefix, in which case it is invalid
	// if (prefix.charAt(i) == '%' && (prefix.length() - i) < 2) {
	// System.err.println("prefix was not valid i=" + i + " nextchar=" +
	// prefix.charAt(i) + " prefix="
	// + prefix);
	// return false;
	// }
	// }
	//
	// return true;
	// }

	public static boolean isPLX_START(String name) {
		if (name.length() >= 3 && isPERCENT(name.substring(0, 3))) {
			return true;
		}

		if (name.length() >= 2 && isPN_LOCAL_ESC(name.substring(0, 2))) {
			return true;
		}

		return false;
	}

	/**
	 * @param substring
	 * @return
	 */
	public static boolean isPERCENT(String name) {
		if (name.length() != 3) {
			return false;
		}

		if (name.charAt(0) != '%') {
			return false;
		}

		if (!ASCIIUtil.isHex(name.charAt(1)) || !ASCIIUtil.isHex(name.charAt(2))) {
			return false;
		}

		return true;
	}

	public static boolean isPLX_INTERNAL(String name) {
		if (name.length() == 3 && isPERCENT(name)) {
			return true;
		}

		if (name.length() == 2 && isPN_LOCAL_ESC(name)) {
			return true;
		}

		return false;
	}

	public static boolean isPN_LOCAL_ESC(String name) {
		if (name.length() != 2) {
			return false;
		}

		if (!name.startsWith("\\")) {
			return false;
		}

		if (!(Arrays.binarySearch(LOCAL_ESCAPED_CHARS, name.charAt(1)) > -1)) {
			return false;
		}

		return true;
	}

	public static boolean isPN_LOCAL(String name) {
		// Empty names are legal
		if (name.length() == 0) {
			return true;
		}

		if (!isPN_CHARS_U(name.charAt(0)) && name.charAt(0) != ':' && !ASCIIUtil.isNumber(name.charAt(0))
				&& !isPLX_START(name))
		{
			System.err.println("PN_LOCAL was not valid (start characters invalid) i=" + 0 + " nextchar="
					+ name.charAt(0) + " name=" + name);
			return false;
		}

		if (!isNameStartChar(name.charAt(0))) {
			System.err.println("name was not valid (start character invalid) i=" + 0 + " nextchar="
					+ name.charAt(0) + " name=" + name);
			return false;
		}

		for (int i = 1; i < name.length(); i++) {
			if (!isNameChar(name.charAt(i))) {
				System.err.println("name was not valid (intermediate character invalid) i=" + i + " nextchar="
						+ name.charAt(i) + " name=" + name);
				return false;
			}

			// Check if the percent encoding was less than two characters from the
			// end of the prefix, in which case it is invalid
			if (name.charAt(i) == '%' && (name.length() - i) < 3) {
				System.err.println("name was not valid (short percent escape) i=" + i + " nextchar="
						+ name.charAt(i) + " name=" + name);
				return false;
			}
		}

		return true;
	}

	// public static boolean isLegalName(String name) {
	// return isPN_LOCAL(name);
	// }

	/**
	 * Encodes the supplied string for inclusion as a 'normal' string in a Turtle
	 * document.
	 */
	public static String encodeString(String s) {
		s = StringUtil.gsub("\\", "\\\\", s);
		s = StringUtil.gsub("\t", "\\t", s);
		s = StringUtil.gsub("\n", "\\n", s);
		s = StringUtil.gsub("\r", "\\r", s);
		s = StringUtil.gsub("\"", "\\\"", s);
		return s;
	}

	/**
	 * Encodes the supplied string for inclusion as a long string in a Turtle
	 * document.
	 **/
	public static String encodeLongString(String s) {
		// TODO: not all double quotes need to be escaped. It suffices to encode
		// the ones that form sequences of 3 or more double quotes, and the ones
		// at the end of a string.
		s = StringUtil.gsub("\\", "\\\\", s);
		s = StringUtil.gsub("\"", "\\\"", s);
		return s;
	}

	/**
	 * Encodes the supplied string for inclusion as a (relative) URI in a Turtle
	 * document.
	 **/
	public static String encodeURIString(String s) {
		s = StringUtil.gsub("\\", "\\\\", s);
		s = StringUtil.gsub(">", "\\>", s);
		return s;
	}

	/**
	 * Decodes an encoded Turtle string. Any \-escape sequences are substituted
	 * with their decoded value.
	 * 
	 * @param s
	 *        An encoded Turtle string.
	 * @return The unencoded string.
	 * @exception IllegalArgumentException
	 *            If the supplied string is not a correctly encoded Turtle
	 *            string.
	 **/
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
			else if (c == 'r') {
				sb.append('\r');
				startIdx = backSlashIdx + 2;
			}
			else if (c == 'n') {
				sb.append('\n');
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
			else if (c == '\'') {
				sb.append('\'');
				startIdx = backSlashIdx + 2;
			}
			else if (c == '>') {
				sb.append('>');
				startIdx = backSlashIdx + 2;
			}
			else if (c == '\\') {
				sb.append('\\');
				startIdx = backSlashIdx + 2;
			}
			else if (c == 'u') {
				// \\uxxxx
				if (backSlashIdx + 5 >= sLength) {
					throw new IllegalArgumentException("Incomplete Unicode escape sequence in: " + s);
				}
				String xx = s.substring(backSlashIdx + 2, backSlashIdx + 6);

				try {
					c = (char)Integer.parseInt(xx, 16);
					sb.append(c);

					startIdx = backSlashIdx + 6;
				}
				catch (NumberFormatException e) {
					throw new IllegalArgumentException("Illegal Unicode escape sequence '\\u" + xx + "' in: " + s);
				}
			}
			else if (c == 'U') {
				// \\Uxxxxxxxx
				if (backSlashIdx + 9 >= sLength) {
					throw new IllegalArgumentException("Incomplete Unicode escape sequence in: " + s);
				}
				String xx = s.substring(backSlashIdx + 2, backSlashIdx + 10);

				try {
					c = (char)Integer.parseInt(xx, 16);
					sb.append(c);

					startIdx = backSlashIdx + 10;
				}
				catch (NumberFormatException e) {
					throw new IllegalArgumentException("Illegal Unicode escape sequence '\\U" + xx + "' in: " + s);
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
