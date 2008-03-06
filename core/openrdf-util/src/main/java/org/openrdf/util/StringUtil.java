/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util;

public class StringUtil {

	/**
	 * Performs a "global substitute" of all occurrences of <tt>target</tt>
	 * with <tt>substitute</tt> in <tt>text</tt>. This method does not
	 * create a new string if <tt>text</tt> doesn't contain <tt>target</tt>,
	 * the <tt>text</tt> object itself is returned in that case.
	 * <p>
	 * Equivalent functionality is offered by
	 * {@link String#replace(java.lang.CharSequence, java.lang.CharSequence)}
	 * (since JDK 1.5), but a number of quick performance test indicate that that
	 * method is about five times slower than this one.
	 * 
	 * @param target
	 *        The string to be substituted.
	 * @param substitute
	 *        The substitute string.
	 * @param text
	 *        The string in which the substitution is done.
	 * @return A new string containing the substitutions, or a reference to
	 *         <tt>text</tt> itself if no substitutions were made.
	 */
	public static String gsub(String target, String substitute, String text) {
		if (target == null || target.length() == 0) {
			// Nothing to substitute.
			return text;
		}

		if (substitute == null) {
			substitute = "";
		}

		// Search for any occurences of 'target'.
		int targetIndex = text.indexOf(target);

		if (targetIndex == -1) {
			// Nothing to substitute.
			return text;
		}

		// We're going to do some substitutions.

		int sbLength = text.length();
		if (substitute.length() > target.length()) {
			// Resulting string will be longer than the original
			sbLength *= 2;
		}

		StringBuilder sb = new StringBuilder(sbLength);
		int prevIndex = 0;

		while (targetIndex >= 0) {
			// First, add the text between the previous and the current occurence
			sb.append(text.substring(prevIndex, targetIndex));

			// Then add the substition string
			sb.append(substitute);

			// Remember the index for the next loop
			prevIndex = targetIndex + target.length();

			// Search for the next occurence
			targetIndex = text.indexOf(target, prevIndex);
		}

		// Add the part after the last occurence
		sb.append(text.substring(prevIndex));

		return sb.toString();
	}

	/**
	 * Appends the specified character <tt>n</tt> times to the supplied
	 * StringBuilder.
	 * 
	 * @param c
	 *        The character to append.
	 * @param n
	 *        The number of times the character should be appended.
	 * @param sb
	 *        The StringBuilder to append the character(s) to.
	 */
	public static void appendN(char c, int n, StringBuilder sb) {
		for (int i = n; i > 0; i--) {
			sb.append(c);
		}
	}

	/**
	 * Searches for the first occurrence of a word, which must be surrounded by
	 * whitespace characters, in a text.
	 */
	public static int indexOfWord(String text, String word) {
		return indexOfWord(text, word, 0);
	}

	/**
	 * Searches for the first occurrence of a word, which must be surrounded by
	 * whitespace characters, that appears in a text after 'fromIndex'.
	 */
	public static int indexOfWord(String text, String word, int fromIndex) {
		int wordIdx = fromIndex - 1;

		while (true) {
			wordIdx = text.indexOf(word, wordIdx + 1);

			if (wordIdx == -1) {
				// word not found
				break;
			}

			// Verify that the character before the word is whitespace
			int beforeIdx = wordIdx - 1;

			if (beforeIdx >= 0 && !Character.isWhitespace(text.charAt(beforeIdx))) {
				// character was not whitespace
				continue;
			}

			// Verify that the character after the word is whitespace
			int afterIdx = wordIdx + word.length();

			if (afterIdx < text.length() && !Character.isWhitespace(text.charAt(afterIdx))) {
				// character was not whitespace
				continue;
			}

			// All checks passed,
			break;
		}

		return wordIdx;
	}

	/**
	 * Searches for the last occurrence of a word, which must be surrounded by
	 * whitespace characters, in a text.
	 */
	public static int lastIndexOfWord(String text, String word) {
		return lastIndexOfWord(text, word, text.length());
	}

	/**
	 * Searches for the last occurrence of a word, which must be surrounded by
	 * whitespace characters, in a text. The algorithm searches the text
	 * backwards, starting from 'fromIndex'.
	 */
	public static int lastIndexOfWord(String text, String word, int fromIndex) {
		int wordIdx = fromIndex + 1;

		while (true) {
			wordIdx = text.lastIndexOf(word, wordIdx - 1);

			if (wordIdx == -1) {
				// word not found
				break;
			}

			// Verify that the character before the word is whitespace
			int beforeIdx = wordIdx - 1;

			if (beforeIdx >= 0 && !Character.isWhitespace(text.charAt(beforeIdx))) {
				// character was not whitespace
				continue;
			}

			// Verify that the character after the word is whitespace
			int afterIdx = wordIdx + word.length();

			if (afterIdx < text.length() && !Character.isWhitespace(text.charAt(afterIdx))) {
				// character was not whitespace
				continue;
			}

			// All checks passed,
			break;
		}

		return wordIdx;
	}

	/**
	 * Removes the double quote from the start and end of the supplied string if
	 * it starts and ends with this character. This method does not create a new
	 * string if <tt>text</tt> doesn't start and end with double quotes, the
	 * <tt>text</tt> object itself is returned in that case.
	 * 
	 * @param text
	 *        The string to remove the double quotes from.
	 * @return The trimmed string, or a reference to <tt>text</tt> if it did
	 *         not start and end with double quotes.
	 */
	public static String trimDoubleQuotes(String text) {
		int textLength = text.length();

		if (textLength >= 2 && text.charAt(0) == '"' && text.charAt(textLength - 1) == '"') {
			return text.substring(1, textLength - 1);
		}

		return text;
	}

} // end class
