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

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.vocabulary.FN;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;

/**
 * The SPARQL built-in {@link Function} CONCAT, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-concat">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Concat implements Function {

	public String getURI() {
		return FN.CONCAT.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length == 0) {
			throw new ValueExprEvaluationException("CONCAT requires at least 1 argument, got " + args.length);
		}

		StringBuilder concatBuilder = new StringBuilder();
		String languageTag = null;

		boolean useLanguageTag = true;
		boolean useDatatype = true;

		for (Value arg : args) {
			if (arg instanceof Literal) {
				Literal lit = (Literal)arg;

				// verify that every literal argument has the same language tag. If
				// not, the operator result should not use a language tag.
				if (useLanguageTag && Literals.isLanguageLiteral(lit)) {
					if (languageTag == null) {
						languageTag = lit.getLanguage().get();
					}
					else if (!languageTag.equals(lit.getLanguage())) {
						languageTag = null;
						useLanguageTag = false;
					}
				}
				else {
					useLanguageTag = false;
				}

				// check datatype: concat only expects plain, language-tagged or
				// string-typed literals. If all arguments are of type xsd:string,
				// the result also should be,
				// otherwise the result will not have a datatype.
				if (lit.getDatatype() == null) {
					useDatatype = false;
				}
				else if (!lit.getDatatype().equals(XMLSchema.STRING)) {
					throw new ValueExprEvaluationException("unexpected data type for concat operand: " + arg);
				}

				concatBuilder.append(lit.getLabel());
			}
			else {
				throw new ValueExprEvaluationException("unexpected argument type for concat operator: " + arg);
			}
		}

		Literal result = null;

		if (useDatatype) {
			result = valueFactory.createLiteral(concatBuilder.toString(), XMLSchema.STRING);
		}
		else if (useLanguageTag) {
			result = valueFactory.createLiteral(concatBuilder.toString(), languageTag);
		}
		else {
			result = valueFactory.createLiteral(concatBuilder.toString());
		}

		return result;

	}

}
