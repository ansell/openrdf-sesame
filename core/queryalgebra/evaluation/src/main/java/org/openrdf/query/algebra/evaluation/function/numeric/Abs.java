/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} ABS, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-abs">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Abs implements Function {

	public String getURI() {
		return FN.NUMERIC_ABS.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("ABS requires exactly 1 argument, got " + args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];

			URI datatype = literal.getDatatype();
			
			// ABS function accepts only numeric literals
			if (datatype != null && XMLDatatypeUtil.isNumericDatatype(datatype))
			{
				if (XMLDatatypeUtil.isDecimalDatatype(datatype)) {
					BigDecimal absoluteValue = literal.decimalValue().abs();
					
					return valueFactory.createLiteral(absoluteValue.toPlainString(), datatype);
				}
				else if (XMLDatatypeUtil.isFloatingPointDatatype(datatype)) {
					double absoluteValue = Math.abs(literal.doubleValue());
					return valueFactory.createLiteral(Double.toString(absoluteValue), datatype);
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
