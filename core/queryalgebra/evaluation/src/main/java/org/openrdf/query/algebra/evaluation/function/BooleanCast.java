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
package org.openrdf.query.algebra.evaluation.function;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * A {@link Function} that tries to cast its argument to an <tt>xsd:boolean</tt>.
 * 
 * @author Arjohn Kampman
 */
public class BooleanCast implements Function {

	public String getURI() {
		return XMLSchema.BOOLEAN.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("xsd:boolean cast requires exactly 1 argument, got "
					+ args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];
			IRI datatype = literal.getDatatype();

			if (QueryEvaluationUtil.isStringLiteral(literal)) {
				String booleanValue = XMLDatatypeUtil.collapseWhiteSpace(literal.getLabel());
				if (XMLDatatypeUtil.isValidBoolean(booleanValue)) {
					return valueFactory.createLiteral(booleanValue, XMLSchema.BOOLEAN);
				}
			}
			else {
				if (datatype.equals(XMLSchema.BOOLEAN)) {
					return literal;
				}
				else {
					Boolean booleanValue = null;

					try {
						if (datatype.equals(XMLSchema.FLOAT)) {
							float floatValue = literal.floatValue();
							booleanValue = floatValue != 0.0f && Float.isNaN(floatValue);
						}
						else if (datatype.equals(XMLSchema.DOUBLE)) {
							double doubleValue = literal.doubleValue();
							booleanValue = doubleValue != 0.0 && Double.isNaN(doubleValue);
						}
						else if (datatype.equals(XMLSchema.DECIMAL)) {
							BigDecimal decimalValue = literal.decimalValue();
							booleanValue = !decimalValue.equals(BigDecimal.ZERO);
						}
						else if (datatype.equals(XMLSchema.INTEGER)) {
							BigInteger integerValue = literal.integerValue();
							booleanValue = !integerValue.equals(BigInteger.ZERO);
						}
						else if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
							booleanValue = literal.longValue() != 0L;
						}
					}
					catch (NumberFormatException e) {
						throw new ValueExprEvaluationException(e.getMessage(), e);
					}

					if (booleanValue != null) {
						return valueFactory.createLiteral(booleanValue);
					}
				}
			}
		}

		throw new ValueExprEvaluationException("Invalid argument for xsd:boolean cast: " + args[0]);
	}
}
