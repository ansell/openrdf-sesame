/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * A {@link Function} that tries to cast its argument to an
 * <tt>xsd:dateTime</tt>.
 * 
 * @author Arjohn Kampman
 */
public class DateTimeCast implements Function {

	public String getURI() {
		return XMLSchema.DATETIME.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("xsd:dateTime cast requires exactly 1 argument, got "
					+ args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];
			URI datatype = literal.getDatatype();

			if (QueryEvaluationUtil.isStringLiteral(literal)) {
				String dateTimeValue = XMLDatatypeUtil.collapseWhiteSpace(literal.getLabel());
				if (XMLDatatypeUtil.isValidDateTime(dateTimeValue)) {
					return valueFactory.createLiteral(dateTimeValue, XMLSchema.DATETIME);
				}
			}
			else if (datatype != null) {
				if (datatype.equals(XMLSchema.DATETIME)) {
					return literal;
				}
			}
		}

		throw new ValueExprEvaluationException("Invalid argument for xsd:dateTime cast: " + args[0]);
	}
}
