/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.builtin;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} STRLANG, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-strlang">SPARQL Query
 * Language for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class StrLang implements Function {

	public String getURI() {
		return "STRLANG";
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException("STRLANG requires 2 arguments, got " + args.length);
		}

		Value lexicalValue = args[0];
		Value languageValue = args[1];

		if (QueryEvaluationUtil.isSimpleLiteral(lexicalValue)) {
			Literal lit = (Literal)lexicalValue;

			if (languageValue instanceof Literal) {
				return valueFactory.createLiteral(lit.getLabel(), ((Literal)languageValue).getLabel());
			}
			else {
				throw new ValueExprEvaluationException("illegal value for operand: " + languageValue);
			}
		}
		else {
			throw new ValueExprEvaluationException("illegal value for operand: " + lexicalValue);
		}

	}

}
