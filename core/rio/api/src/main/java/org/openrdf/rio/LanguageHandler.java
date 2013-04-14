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
package org.openrdf.rio;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.LiteralUtilException;

/**
 * An interface defining methods related to verification and normalisation of
 * language tags.
 * <p>
 * The language handler may optionally provide normalisation and verification
 * services for string literals based on the language tags, including
 * translation, grammar and spelling checks. However, this behaviour is entirely
 * driven by the user
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public interface LanguageHandler {

	/**
	 * 
	 */
	public static final String RFC3066 = "org.openrdf.rio.languages.RFC3066";

	/**
	 * Checks if the given language tag is recognised by this language handler,
	 * including cases where the language tag is recognised, but is not yet
	 * normalised.
	 * 
	 * @param languageTag
	 * @return
	 * @since 2.7.0
	 */
	public boolean isRecognisedLanguage(String languageTag);

	/**
	 * Verifies that the language tag is valid, optionally including an automated
	 * check on the literal value.
	 * <p>
	 * This method must only be called after verifying that
	 * {@link #isRecognisedLanguage(String)} returns true for the given language
	 * tag.
	 * 
	 * @param literalValue
	 *        Optional literal value. If null, it must be ignored and the result
	 *        defined solely by the language tag.
	 * @param languageTag
	 *        A language tag that matched with
	 * @return True if the language tag is recognised by this language handler,
	 *         and it is verified to be syntactically valid.
	 * @since 2.7.0
	 * @throws LiteralUtilException
	 *         If the language tag was not recognised.
	 */
	public boolean verifyLanguage(String literalValue, String languageTag)
		throws LiteralUtilException;

	/**
	 * Normalise both the language tag and the language if appropriate, and use
	 * the given value factory to generate a literal matching an optional literal
	 * value and language tag.
	 * <p>
	 * This method must only be called after verifying that
	 * {@link #isRecognisedLanguage(String)} returns true for the given language
	 * tag, and {@link #verifyLanguage(String, String)} also returns true for the
	 * given language and (optionally) literal value.
	 * 
	 * @param literalValue
	 *        Optional literal value. If the given literal value is null, then an
	 *        empty string must be used for the resulting literal, and the user
	 *        must only utilise {@link Literal#getLanguage()} on the resulting
	 *        literal to derive the normalised language tag.
	 * @param languageTag
	 * @param valueFactory
	 * @return
	 * @since 2.7.0
	 * @throws LiteralUtilException
	 *         If the language tag was not recognised or verified, or the literal
	 *         value was not null and could not be normalised due to an
	 *         exception.
	 */
	public Literal normaliseLanguage(String literalValue, String languageTag, ValueFactory valueFactory)
		throws LiteralUtilException;

	/**
	 * A unique key for this language handler to identify it in the
	 * LanguageHandlerRegistry.
	 * 
	 * @return A unique string key.
	 */
	public String getKey();

}
