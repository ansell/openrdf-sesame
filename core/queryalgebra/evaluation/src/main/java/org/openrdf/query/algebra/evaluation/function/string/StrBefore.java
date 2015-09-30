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
package org.openrdf.query.algebra.evaluation.function.string;

import java.util.Optional;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} STRBEFORE, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-substr">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class StrBefore implements Function {

	public String getURI() {
		return FN.SUBSTRING_BEFORE.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException("Incorrect number of arguments for STRBEFORE: " + args.length);
		}

		Value leftArg = args[0];
		Value rightArg = args[1];

		if (leftArg instanceof Literal && rightArg instanceof Literal) {
			Literal leftLit = (Literal)leftArg;
			Literal rightLit = (Literal)rightArg;

			
			if (QueryEvaluationUtil.compatibleArguments(leftLit, rightLit))
			{
				Optional<String> leftLanguage = leftLit.getLanguage();
				IRI leftDt = leftLit.getDatatype();

				String lexicalValue = leftLit.getLabel();
				String substring = rightLit.getLabel();
				
				int index = lexicalValue.indexOf(substring);

				String substringBefore = "";
				if (index > -1) {
					substringBefore = lexicalValue.substring(0, index);
				}
				else {
					// no match, return empty string with no language or datatype
					leftLanguage = Optional.empty();
					leftDt = null;
				}
				
				if (leftLanguage.isPresent()) {
					return valueFactory.createLiteral(substringBefore, leftLanguage.get());
				}
				else if (leftDt != null) {
					return valueFactory.createLiteral(substringBefore, leftDt);
				}
				else {
					return valueFactory.createLiteral(substringBefore);
				}
			}
			else {
				throw new ValueExprEvaluationException("incompatible operands for STRBEFORE: " + leftArg + ", " + rightArg);
			}
		}
		else {
			throw new ValueExprEvaluationException("incompatible operands for STRBEFORE: " + leftArg + ", " + rightArg);
		}
	}
}
