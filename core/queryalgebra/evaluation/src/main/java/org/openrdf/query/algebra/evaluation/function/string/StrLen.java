/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.string;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} STRLEN, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-strlen">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class StrLen implements Function {

	public String getURI() {
		return FN.STRING_LENGTH.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("STRLEN requires 1 argument, got " + args.length);
		}

		Value argValue = args[0];
		if (argValue instanceof Literal) {
			Literal literal = (Literal)argValue;

			// strlen function accepts only string literals 
			if (QueryEvaluationUtil.isStringLiteral(literal)) {

				// TODO we jump through some hoops here to get an xsd:integer
				// literal. Shouldn't createLiteral(int) return an xsd:integer
				// rather than an xsd:int?
				Integer length = literal.getLabel().length();
				return valueFactory.createLiteral(length.toString(), XMLSchema.INTEGER);
			}
			else {
				throw new ValueExprEvaluationException("unexpected input value for strlen function: " + argValue);
			}
		}
		else {
			throw new ValueExprEvaluationException("unexpected input value for strlen function: " + argValue);
		}
	}

}
