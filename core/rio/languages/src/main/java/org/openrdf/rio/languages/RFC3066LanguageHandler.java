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
package org.openrdf.rio.languages;

import java.util.regex.Pattern;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.LiteralUtilException;
import org.openrdf.rio.LanguageHandler;

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
	public boolean isRecognisedLanguage(String languageTag) {
		// language tag is RFC3066-conformant if it matches this regex:
		// [a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*
		boolean result = matcher.matcher(languageTag).matches();

		return result;
	}

	@Override
	public boolean verifyLanguage(String literalValue, String languageTag)
		throws LiteralUtilException
	{
		boolean result = matcher.matcher(languageTag).matches();

		return result;
	}

	@Override
	public Literal normaliseLanguage(String literalValue, String languageTag, ValueFactory valueFactory)
		throws LiteralUtilException
	{
		// TODO Implement normalisation more effectively than this
		return valueFactory.createLiteral(literalValue, languageTag.toLowerCase());
	}

	@Override
	public String getKey() {
		return LanguageHandler.RFC3066;
	}

}
