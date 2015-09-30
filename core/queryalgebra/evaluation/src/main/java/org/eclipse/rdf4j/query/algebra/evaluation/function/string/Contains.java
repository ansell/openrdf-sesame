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
import org.eclipse.rdf4j.model.impl.BooleanLiteral;
import org.eclipse.rdf4j.model.vocabulary.FN;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;
import org.eclipse.rdf4j.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} CONTAINS, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-contains">SPARQL Query
 * Language for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Contains implements Function {

	public String getURI() {
		return FN.CONTAINS.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException("CONTAINS requires 2 arguments, got " + args.length);
		}
		Value leftVal = args[0];
		Value rightVal = args[1];

		if (leftVal instanceof Literal && rightVal instanceof Literal) {
			Literal leftLit = (Literal)leftVal;
			Literal rightLit = (Literal)rightVal;

			if (leftLit.getLanguage().isPresent()) {
				if (!rightLit.getLanguage().isPresent() || rightLit.getLanguage().equals(leftLit.getLanguage())) {

					String leftLexVal = leftLit.getLabel();
					String rightLexVal = rightLit.getLabel();

					return BooleanLiteral.valueOf(leftLexVal.contains(rightLexVal));
				}
				else {
					throw new ValueExprEvaluationException("incompatible operands for CONTAINS function");
				}
			}
			else if (QueryEvaluationUtil.isStringLiteral(leftLit)) {
				if (QueryEvaluationUtil.isStringLiteral(rightLit)) {
					String leftLexVal = leftLit.getLabel();
					String rightLexVal = rightLit.getLabel();

					return BooleanLiteral.valueOf(leftLexVal.contains(rightLexVal));
				}
				else {
					throw new ValueExprEvaluationException("incompatible operands for CONTAINS function");
				}
			}
			else {
				throw new ValueExprEvaluationException("incompatible operands for CONTAINS function");
			}
		}
		else {
			throw new ValueExprEvaluationException("CONTAINS function expects literal operands");
		}

	}

}