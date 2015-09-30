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
