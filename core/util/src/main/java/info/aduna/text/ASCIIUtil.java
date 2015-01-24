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

package info.aduna.text;

/**
 * Utility methods for ASCII character checking.
 */
public class ASCIIUtil {

	/**
	 * Checks whether the supplied character is a letter or number.
	 * 
	 * @see #isLetter
	 * @see #isNumber
	 */
	public static boolean isLetterOrNumber(int c) {
		return isLetter(c) || isNumber(c);
	}

	/**
	 * Checks whether the supplied character is a letter.
	 */
	public static boolean isLetter(int c) {
		return isUpperCaseLetter(c) || isLowerCaseLetter(c);
	}

	/**
	 * Checks whether the supplied character is an upper-case letter.
	 */
	public static boolean isUpperCaseLetter(int c) {
		return (c >= 65 && c <= 90); // A - Z
	}

	/**
	 * Checks whether the supplied character is an lower-case letter.
	 */
	public static boolean isLowerCaseLetter(int c) {
		return (c >= 97 && c <= 122); // a - z
	}

	/**
	 * Checks whether the supplied character is a number
	 */
	public static boolean isNumber(int c) {
		return (c >= 48 && c <= 57); // 0 - 9
	}

	/**
	 * Check whether the supplied character is a Hexadecimal character.
	 * 
	 * @param c
	 * @return <code>true</code> if c is a hexadecimal character, <code>false</code> otherwise.
	 */
	public static boolean isHex(int c) {
		return isNumber(c) || isUpperCaseHexLetter(c) || isLowerCaseHexLetter(c);
	}

	private static boolean isUpperCaseHexLetter(int c) {
		return (c >= 65 && c <= 70);
	}

	private static boolean isLowerCaseHexLetter(int c) {
		return (c >= 97 && c <= 102);
	}
}
