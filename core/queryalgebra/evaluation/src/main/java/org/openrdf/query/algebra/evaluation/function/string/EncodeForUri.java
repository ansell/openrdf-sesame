/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.string;

import info.aduna.net.UriUtil;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} ENCODE_FOR_URI, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-encode">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class EncodeForUri implements Function {

	public String getURI() {
		return FN.ENCODE_FOR_URI.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("ENCODE_FOR_URI requires exactly 1 argument, got "
					+ args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];

			if (QueryEvaluationUtil.isStringLiteral(literal)) {
				String lexValue = literal.getLabel();

				return valueFactory.createLiteral(UriUtil.encodeUri(lexValue));
			}
			else {
				throw new ValueExprEvaluationException("Invalid argument for ENCODE_FOR_URI: " + literal);
			}
		}
		else {
			throw new ValueExprEvaluationException("Invalid argument for ENCODE_FOR_URI: " + args[0]);
		}
	}

}
