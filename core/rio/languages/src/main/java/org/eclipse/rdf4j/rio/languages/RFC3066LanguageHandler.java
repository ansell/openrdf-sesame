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
package org.eclipse.rdf4j.rio.languages;

import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.LiteralUtilException;
import org.eclipse.rdf4j.rio.LanguageHandler;

/**
 * A language handler that can verify RFC3066 formatted language tags.
 * <p>
 * This language handler normalises language tags to lower-case.
 * 
 * @see <a href="http://www.ietf.org/rfc/rfc3066.txt">RFC 3066</a>
 * @author Peter Ansell
 * @since 2.7.0
 */
public class RFC3066LanguageHandler implements LanguageHandler {

	/**
	 * Language tag is RFC3066-conformant if it matches this regex:
	 * [a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*
	 */
	protected final Pattern matcher = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*");

	/**
	 * Default constructor.
	 */
	public RFC3066LanguageHandler() {
	}

	@Override
	public boolean isRecognizedLanguage(String languageTag) {
		if (languageTag == null) {
			throw new NullPointerException("Language tag cannot be null");
		}

		// language tag is RFC3066-conformant if it matches this regex:
		// [a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*
		boolean result = matcher.matcher(languageTag).matches();

		return result;
	}

	@Override
	public boolean verifyLanguage(String literalValue, String languageTag)
		throws LiteralUtilException
	{
		if (isRecognizedLanguage(languageTag)) {
			if (literalValue == null) {
				throw new NullPointerException("Literal value cannot be null");
			}

			return matcher.matcher(languageTag).matches();
		}

		throw new LiteralUtilException("Could not verify RFC3066 language tag");
	}

	@Override
	public Literal normalizeLanguage(String literalValue, String languageTag, ValueFactory valueFactory)
		throws LiteralUtilException
	{
		if (isRecognizedLanguage(languageTag)) {
			if (literalValue == null) {
				throw new NullPointerException("Literal value cannot be null");
			}

			// TODO Implement normalisation more effectively than this
			return valueFactory.createLiteral(literalValue, languageTag.toLowerCase().intern());
		}

		throw new LiteralUtilException("Could not normalize RFC3066 language tag");
	}

	@Override
	public String getKey() {
		return LanguageHandler.RFC3066;
	}

}
