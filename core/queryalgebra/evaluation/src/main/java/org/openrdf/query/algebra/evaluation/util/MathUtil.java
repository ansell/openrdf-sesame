/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.DecimalLiteralImpl;
import org.openrdf.model.impl.IntegerLiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * A utility class for evaluation of mathematical expressions on RDF literals. 
 * 
 * @author Jeen Broekstra
 */
public class MathUtil {

	/**
	 * Computes the result of applying the supplied math operator on the supplied
	 * left and right operand.
	 * 
	 * @param leftLit
	 *        a numeric datatype literal
	 * @param rightLit
	 *        a numeric datatype literal
	 * @param op
	 *        a mathematical operator, as definied by MathExpr.MathOp.
	 * @return a numeric datatype literal containing the result of the operation.
	 *         The result will be datatype according to the most specific data
	 *         type the two operands have in common per the SPARQL/XPath spec.
	 * @throws ValueExprEvaluationException
	 */
	public static Literal compute(Literal leftLit, Literal rightLit, MathOp op)
		throws ValueExprEvaluationException
	{
		URI leftDatatype = leftLit.getDatatype();
		URI rightDatatype = rightLit.getDatatype();

		// Only numeric value can be used in math expressions
		if (leftDatatype == null || !XMLDatatypeUtil.isNumericDatatype(leftDatatype)) {
			throw new ValueExprEvaluationException("Not a number: " + leftLit);
		}
		if (rightDatatype == null || !XMLDatatypeUtil.isNumericDatatype(rightDatatype)) {
			throw new ValueExprEvaluationException("Not a number: " + rightLit);
		}

		// Determine most specific datatype that the arguments have in common,
		// choosing from xsd:integer, xsd:decimal, xsd:float and xsd:double as
		// per the SPARQL/XPATH spec
		URI commonDatatype;

		if (leftDatatype.equals(XMLSchema.DOUBLE) || rightDatatype.equals(XMLSchema.DOUBLE)) {
			commonDatatype = XMLSchema.DOUBLE;
		}
		else if (leftDatatype.equals(XMLSchema.FLOAT) || rightDatatype.equals(XMLSchema.FLOAT)) {
			commonDatatype = XMLSchema.FLOAT;
		}
		else if (leftDatatype.equals(XMLSchema.DECIMAL) || rightDatatype.equals(XMLSchema.DECIMAL)) {
			commonDatatype = XMLSchema.DECIMAL;
		}
		else if (op == MathOp.DIVIDE) {
			// Result of integer divide is decimal and requires the arguments to
			// be handled as such, see for details:
			// http://www.w3.org/TR/xpath-functions/#func-numeric-divide
			commonDatatype = XMLSchema.DECIMAL;
		}
		else {
			commonDatatype = XMLSchema.INTEGER;
		}

		// Note: Java already handles cases like divide-by-zero appropriately
		// for floats and doubles, see:
		// http://www.particle.kth.se/~lindsey/JavaCourse/Book/Part1/Tech/
		// Chapter02/floatingPt2.html

		try {
			if (commonDatatype.equals(XMLSchema.DOUBLE)) {
				double left = leftLit.doubleValue();
				double right = rightLit.doubleValue();

				switch (op) {
					case PLUS:
						return new NumericLiteralImpl(left + right);
					case MINUS:
						return new NumericLiteralImpl(left - right);
					case MULTIPLY:
						return new NumericLiteralImpl(left * right);
					case DIVIDE:
						return new NumericLiteralImpl(left / right);
					default:
						throw new IllegalArgumentException("Unknown operator: " + op);
				}
			}
			else if (commonDatatype.equals(XMLSchema.FLOAT)) {
				float left = leftLit.floatValue();
				float right = rightLit.floatValue();

				switch (op) {
					case PLUS:
						return new NumericLiteralImpl(left + right);
					case MINUS:
						return new NumericLiteralImpl(left - right);
					case MULTIPLY:
						return new NumericLiteralImpl(left * right);
					case DIVIDE:
						return new NumericLiteralImpl(left / right);
					default:
						throw new IllegalArgumentException("Unknown operator: " + op);
				}
			}
			else if (commonDatatype.equals(XMLSchema.DECIMAL)) {
				BigDecimal left = leftLit.decimalValue();
				BigDecimal right = rightLit.decimalValue();

				switch (op) {
					case PLUS:
						return new DecimalLiteralImpl(left.add(right));
					case MINUS:
						return new DecimalLiteralImpl(left.subtract(right));
					case MULTIPLY:
						return new DecimalLiteralImpl(left.multiply(right));
					case DIVIDE:
						// Divide by zero handled through NumberFormatException
						BigDecimal result = null;
						try {
							// try to return the exact quotient if possible.
							result = left.divide(right, MathContext.UNLIMITED);
						}
						catch (ArithmeticException e) {
							// non-terminating decimal expansion in quotient, using scaling and rounding.
							result = left.setScale(24, RoundingMode.HALF_UP).divide(right, RoundingMode.HALF_UP);
						}
						
						
						return new DecimalLiteralImpl(result);
					default:
						throw new IllegalArgumentException("Unknown operator: " + op);
				}
			}
			else { // XMLSchema.INTEGER
				BigInteger left = leftLit.integerValue();
				BigInteger right = rightLit.integerValue();

				switch (op) {
					case PLUS:
						return new IntegerLiteralImpl(left.add(right));
					case MINUS:
						return new IntegerLiteralImpl(left.subtract(right));
					case MULTIPLY:
						return new IntegerLiteralImpl(left.multiply(right));
					case DIVIDE:
						throw new RuntimeException("Integer divisions should be processed as decimal divisions");
					default:
						throw new IllegalArgumentException("Unknown operator: " + op);
				}
			}
		}
		catch (NumberFormatException e) {
			throw new ValueExprEvaluationException(e);
		}
		catch (ArithmeticException e) {
			throw new ValueExprEvaluationException(e);
		}
	}

}
