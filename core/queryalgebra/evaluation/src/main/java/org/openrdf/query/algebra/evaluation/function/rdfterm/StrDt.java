/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.rdfterm;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} STRDT, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-strdt">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class StrDt implements Function {

	public String getURI() {
		return "STRDT";
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException("STRDT requires 2 arguments, got "
					+ args.length);
		}

		Value lexicalValue = args[0];
		Value datatypeValue = args[1];
		
		if (QueryEvaluationUtil.isSimpleLiteral(lexicalValue)) {
			Literal lit = (Literal)lexicalValue;
			if (datatypeValue instanceof URI) {
				return valueFactory.createLiteral(lit.getLabel(), (URI)datatypeValue);
			}
			else {
				throw new ValueExprEvaluationException("illegal value for operand: " + datatypeValue);
			}
		}
		else {
			throw new ValueExprEvaluationException("illegal value for operand: " + lexicalValue);
		}
	}

}
