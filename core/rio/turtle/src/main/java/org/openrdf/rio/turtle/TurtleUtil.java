/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.turtle;

import info.aduna.text.ASCIIUtil;
import info.aduna.text.StringUtil;


/**
 * Utility methods for Turtle encoding/decoding.
 * 
 * @see http://www.w3.org/TR/turtle/ 
 */
public class TurtleUtil {

	/**
	 * Tries to find an index where the suppied URI can be split into a namespace
	 * and a local name that comply with the serialization constraints of the
	 * Turtle format.
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

	public static boolean isPrefixStartChar(int c) {
		return
			ASCIIUtil.isLetter(c) ||
			c >= 0x00C0 && c <= 0x00D6 ||
			c >= 0x00D8 && c <= 0x00F6 ||
			c >= 0x00F8 && c <= 0x02FF ||
			c >= 0x0370 && c <= 0x037D ||
			c >= 0x037F && c <= 0x1FFF ||
			c >= 0x200C && c <= 0x200D ||
			c >= 0x2070 && c <= 0x218F ||
			c >= 0x2C00 && c <= 0x2FEF ||
			c >= 0x3001 && c <= 0xD7FF ||
			c >= 0xF900 && c <= 0xFDCF ||
			c >= 0xFDF0 && c <= 0xFFFD ||
			c >= 0x10000 && c <= 0xEFFFF;
	}
	
	public static boolean isNameStartChar(int c) {
		return c == '_' || c == ':' || c == '%' || ASCIIUtil.isNumber(c) || isPrefixStartChar(c);
	}

	public static boolean isNameChar(int c) {
		return
			isNameStartChar(c) ||
			ASCIIUtil.isNumber(c) ||
			c == '-' ||
			c == 0x00B7 ||
			c >= 0x0300 && c <= 0x036F ||
			c >= 0x203F && c <= 0x2040;
	}

	public static boolean isPrefixChar(int c) {
		return
				c == '_' || 
				ASCIIUtil.isNumber(c) || 
				isPrefixStartChar(c) ||
				c == '-' ||
				c == 0x00B7 ||
				c >= 0x0300 && c <= 0x036F ||
				c >= 0x203F && c <= 0x2040;
	}

	public static boolean isLanguageStartChar(int c) {
		return ASCIIUtil.isLetter(c);
	}

	public static boolean isLanguageChar(int c) {
		return ASCIIUtil.isLetter(c) || ASCIIUtil.isNumber(c) || c == '-';
	}

	public static boolean isLegalPrefix(String prefix) {
		if (prefix.length() == 0) {
			return false;
		}
		if (!isPrefixStartChar(prefix.charAt(0))) {
			return false;
		}
		for (int i = 1; i < prefix.length(); i++) {
			if (!isPrefixChar( prefix.charAt(i) )) {
				return false;
			}
		}
		return true;
	}

	public static boolean isLegalName(String name) {
		if (name.length() == 0) {
			return false;
		}
		if (!isNameStartChar(name.charAt(0))) {
			return false;
		}
		
		// FIXME: Last character cannot be a period '.' character
		for (int i = 1; i < name.length(); i++) {
			if (!isNameChar( name.charAt(i) )) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Encodes the supplied string for inclusion as a 'normal' string in a
	 * Turtle document.
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
	 * @param s An encoded Turtle string.
	 * @return The unencoded string.
	 * @exception IllegalArgumentException If the supplied string is not a
	 * correctly encoded Turtle string.
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
			else if (c == '"') {
				sb.append('"');
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

		sb.append( s.substring(startIdx) );

		return sb.toString();
	}
}
