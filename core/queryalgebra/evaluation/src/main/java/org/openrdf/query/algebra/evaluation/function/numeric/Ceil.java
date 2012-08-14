/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.numeric;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * The SPARQL built-in {@link Function} CEIL, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-ceil">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Ceil implements Function {

	public String getURI() {
		return FN.NUMERIC_CEIL.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("CEIL requires exactly 1 argument, got " + args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];

			URI datatype = literal.getDatatype();
			
			// CEIL function accepts only numeric literals
			if (datatype != null && XMLDatatypeUtil.isNumericDatatype(datatype))
			{
				if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
					return literal;
				}
				else if (XMLDatatypeUtil.isDecimalDatatype(datatype)) {
					BigDecimal ceilingValue = literal.decimalValue().setScale(0, RoundingMode.CEILING);
					return valueFactory.createLiteral(ceilingValue.toPlainString(), datatype);
				}
				else if (XMLDatatypeUtil.isFloatingPointDatatype(datatype)) {
					double ceilingValue = Math.ceil(literal.doubleValue());
					return valueFactory.createLiteral(Double.toString(ceilingValue), datatype);
				}
				else {
					throw new ValueExprEvaluationException("unexpected datatype for function operand: " + args[0]);
				}
			}
			else {
				throw new ValueExprEvaluationException("unexpected input value for function: " + args[0]);
			}
		}
		else {
			throw new ValueExprEvaluationException("unexpected input value for function: " + args[0]);
		}

	}

}
