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
package org.eclipse.rdf4j.query.algebra.evaluation.function;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * A {@link Function} that tries to cast its argument to an <tt>xsd:string</tt>.
 * 
 * @author Arjohn Kampman
 */
public class StringCast implements Function {

	public String getURI() {
		return XMLSchema.STRING.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("xsd:string cast requires exactly 1 argument, got "
					+ args.length);
		}

		Value value = args[0];
		if (value instanceof IRI) {
			return valueFactory.createLiteral(value.toString(), XMLSchema.STRING);
		}
		else if (value instanceof Literal) {
			Literal literal = (Literal)value;
			IRI datatype = literal.getDatatype();

			if (QueryEvaluationUtil.isSimpleLiteral(literal)) {
				return valueFactory.createLiteral(literal.getLabel(), XMLSchema.STRING);
			}
			else if (!Literals.isLanguageLiteral(literal)) {
				if (datatype.equals(XMLSchema.STRING)) {
					return literal;
				}
				else if (XMLDatatypeUtil.isNumericDatatype(datatype) || datatype.equals(XMLSchema.BOOLEAN)
						|| datatype.equals(XMLSchema.DATETIME))
				{
					// FIXME Slightly simplified wrt the spec, we just always use the
					// canonical value of the
					// source literal as the target lexical value. This is not 100%
					// compliant with handling of
					// some date-related datatypes.
					//
					// See
					// http://www.w3.org/TR/xpath-functions/#casting-from-primitive-to-primitive
					if (XMLDatatypeUtil.isValidValue(literal.getLabel(), datatype)) {
						String normalizedValue = XMLDatatypeUtil.normalize(literal.getLabel(), datatype);
						return valueFactory.createLiteral(normalizedValue, XMLSchema.STRING);
					}
					else {
						return valueFactory.createLiteral(literal.getLabel(), XMLSchema.STRING);
					}
				}
				else {
					// for unknown datatypes, just use the lexical value.
					return valueFactory.createLiteral(literal.getLabel(), XMLSchema.STRING);
				}
			}
		}

		throw new ValueExprEvaluationException("Invalid argument for xsd:string cast: " + value);
	}
}
