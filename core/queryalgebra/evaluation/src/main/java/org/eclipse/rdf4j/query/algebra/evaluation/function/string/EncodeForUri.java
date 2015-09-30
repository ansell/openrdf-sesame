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
package org.eclipse.rdf4j.query.algebra.evaluation.function.string;

import java.io.UnsupportedEncodingException;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FN;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;
import org.eclipse.rdf4j.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} ENCODE_FOR_URI, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-encode">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 * @author Arjohn Kampman
 */
public class EncodeForUri implements Function {

	public String getURI() {
		return FN.ENCODE_FOR_URI.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("ENCODE_FOR_URI requires exactly 1 argument, got "
					+ args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];

			if (QueryEvaluationUtil.isStringLiteral(literal)) {
				String lexValue = literal.getLabel();

				return valueFactory.createLiteral(encodeUri(lexValue));
			}
			else {
				throw new ValueExprEvaluationException("Invalid argument for ENCODE_FOR_URI: " + literal);
			}
		}
		else {
			throw new ValueExprEvaluationException("Invalid argument for ENCODE_FOR_URI: " + args[0]);
		}
	}

	private String encodeUri(String uri) {
				
		StringBuilder buf = new StringBuilder(uri.length() * 2);

		int uriLen = uri.length();
		for (int i = 0; i < uriLen; i++) {
			char c = uri.charAt(i);

			if (isUnreserved(c)) {
				buf.append(c);
			}
			else {
				// use UTF-8 hex encoding for character.
				try {
					byte[] utf8 = Character.toString(c).getBytes("UTF-8");
					
					for (byte b: utf8) {
						// Escape character
						buf.append('%');
						
						char cb = (char)(b & 0xFF);
						
						String hexVal = Integer.toHexString(cb).toUpperCase();

						// Ensure use of two characters
						if (hexVal.length() == 1) {
							buf.append('0');
						}

						buf.append(hexVal);
					}
						
				}
				catch (UnsupportedEncodingException e) {
					// UTF-8 is always supported
					throw new RuntimeException(e);
				}
			}
		}

		return buf.toString();
	}

	private final boolean isUnreserved(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '-' || c == '.'
				|| c == '_' || c == '~';
	}
}
