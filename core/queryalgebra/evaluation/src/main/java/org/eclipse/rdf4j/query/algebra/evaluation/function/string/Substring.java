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

import java.util.Optional;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FN;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;
import org.eclipse.rdf4j.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} SUBSTR, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-substr">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class Substring implements Function {

	public String getURI() {
		return FN.SUBSTRING.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length < 2 || args.length > 3) {
			throw new ValueExprEvaluationException("Incorrect number of arguments for SUBSTR: " + args.length);
		}

		Value argValue = args[0];
		Value startIndexValue = args[1];
		Value lengthValue = null;
		if (args.length > 2) {
			lengthValue = args[2];
		}

		if (argValue instanceof Literal) {
			Literal literal = (Literal)argValue;

			// substr function accepts string literals only.
			if (QueryEvaluationUtil.isStringLiteral(literal))
			{
				String lexicalValue = literal.getLabel();

				// determine start index.
				int startIndex = 0;
				if (startIndexValue instanceof Literal) {
					try {
						// xpath:substring startIndex is 1-based.
						startIndex = ((Literal)startIndexValue).intValue() - 1;

						if (startIndex < 0) {
							throw new ValueExprEvaluationException(
									"illegal start index value (expected 1 or larger): " + startIndexValue);
						}
					}
					catch (NumberFormatException e) {
						throw new ValueExprEvaluationException("illegal start index value (expected int value): "
								+ startIndexValue);
					}
				}
				else if (startIndexValue != null) {
					throw new ValueExprEvaluationException("illegal start index value (expected literal value): "
							+ startIndexValue);
				}

				// optionally convert supplied length expression to an end index for
				// the substring.
				int endIndex = lexicalValue.length();
				if (lengthValue instanceof Literal) {
					try {
						int length = ((Literal)lengthValue).intValue();
						endIndex = startIndex + length;
					}
					catch (NumberFormatException e) {
						throw new ValueExprEvaluationException("illegal length value (expected int value): "
								+ lengthValue);
					}
				}
				else if (lengthValue != null) {
					throw new ValueExprEvaluationException("illegal length value (expected literal value): "
							+ lengthValue);
				}

				try {
					Optional<String> language = literal.getLanguage();
					lexicalValue = lexicalValue.substring(startIndex, endIndex);

					if (language.isPresent()) {
						return valueFactory.createLiteral(lexicalValue, language.get());
					}
					else if (XMLSchema.STRING.equals(literal.getDatatype())) {
						return valueFactory.createLiteral(lexicalValue, XMLSchema.STRING);
					}
					else {
						return valueFactory.createLiteral(lexicalValue);
					}
				}
				catch (IndexOutOfBoundsException e) {
					throw new ValueExprEvaluationException("could not determine substring", e);
				}
			}
			else {
				throw new ValueExprEvaluationException("unexpected input value for function substring: "
						+ argValue);
			}
		}
		else {
			throw new ValueExprEvaluationException("unexpected input value for function substring: " + argValue);
		}
	}

}
