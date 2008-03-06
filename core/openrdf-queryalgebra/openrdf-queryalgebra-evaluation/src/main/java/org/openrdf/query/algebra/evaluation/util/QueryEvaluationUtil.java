/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author Arjohn Kampman
 */
public class QueryEvaluationUtil {

	/**
	 * Determines the effective boolean value (EBV) of the supplied value as
	 * defined in the <a href="http://www.w3.org/TR/rdf-sparql-query/#ebv">SPARQL
	 * specification</a>:
	 * <ul>
	 * <li>The EBV of any literal whose type is xsd:boolean or numeric is false
	 * if the lexical form is not valid for that datatype (e.g.
	 * "abc"^^xsd:integer).
	 * <li>If the argument is a typed literal with a datatype of xsd:boolean,
	 * the EBV is the value of that argument.
	 * <li>If the argument is a plain literal or a typed literal with a datatype
	 * of xsd:string, the EBV is false if the operand value has zero length;
	 * otherwise the EBV is true.
	 * <li>If the argument is a numeric type or a typed literal with a datatype
	 * derived from a numeric type, the EBV is false if the operand value is NaN
	 * or is numerically equal to zero; otherwise the EBV is true.
	 * <li> All other arguments, including unbound arguments, produce a type
	 * error.
	 * </ul>
	 * 
	 * @param value
	 *        Some value.
	 * @return The EBV of <tt>value</tt>.
	 * @throws ValueExprEvaluationException
	 *         In case the application of the EBV algorithm results in a type
	 *         error.
	 */
	public static boolean getEffectiveBooleanValue(Value value)
		throws ValueExprEvaluationException
	{
		if (value instanceof Literal) {
			Literal literal = (Literal)value;
			String label = literal.getLabel();
			URI datatype = literal.getDatatype();

			if (datatype == null || datatype.equals(XMLSchema.STRING)) {
				return label.length() > 0;
			}
			else if (datatype.equals(XMLSchema.BOOLEAN)) {
				if ("true".equals(label) || "1".equals(label)) {
					return true;
				}
				else {
					// also false for illegal values
					return false;
				}
			}
			else if (datatype.equals(XMLSchema.DECIMAL)) {
				try {
					String normDec = XMLDatatypeUtil.normalizeDecimal(label);
					return !normDec.equals("0.0");
				}
				catch (IllegalArgumentException e) {
					return false;
				}
			}
			else if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
				try {
					String normInt = XMLDatatypeUtil.normalize(label, datatype);
					return !normInt.equals("0");
				}
				catch (IllegalArgumentException e) {
					return false;
				}
			}
			else if (XMLDatatypeUtil.isFloatingPointDatatype(datatype)) {
				try {
					String normFP = XMLDatatypeUtil.normalize(label, datatype);
					return !normFP.equals("0.0E0") && !normFP.equals("NaN");
				}
				catch (IllegalArgumentException e) {
					return false;
				}
			}
		}

		throw new ValueExprEvaluationException();
	}

	/**
	 * Checks whether the supplied value is a "simple literal" as defined in the
	 * SPARQL spec. A "simple literal" is a literal without a language tag or a
	 * datatype.
	 */
	public static boolean isSimpleLiteral(Value v) {
		if (v instanceof Literal) {
			return isSimpleLiteral((Literal)v);
		}

		return false;
	}

	/**
	 * Checks whether the supplied literal is a "simple literal" as defined in
	 * the SPARQL spec. A "simple literal" is a literal without a language tag or
	 * a datatype.
	 */
	public static boolean isSimpleLiteral(Literal l) {
		return l.getLanguage() == null && l.getDatatype() == null;
	}

	/**
	 * Checks whether the supplied literal is a "string literal". A "string
	 * literal" is either a {@link #isSimpleLiteral(Literal) simple literal} or a
	 * literal with datatype {@link XMLSchema#STRING xsd:string}.
	 */
	public static boolean isStringLiteral(Literal l) {
		URI datatype = l.getDatatype();

		if (datatype == null) {
			return l.getLanguage() == null;
		}
		else {
			return datatype.equals(XMLSchema.STRING);
		}
	}
}
