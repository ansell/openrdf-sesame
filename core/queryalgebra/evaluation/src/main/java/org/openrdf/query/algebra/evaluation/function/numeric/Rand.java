/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.numeric;

import java.util.Random;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * The SPARQL built-in {@link Function} RAND, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-rand">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Rand implements Function {

	public String getURI() {
		return "RAND";
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 0) {
			throw new ValueExprEvaluationException("RAND requires 0 arguments, got " + args.length);
		}

		Random randomGenerator = new Random();
		double randomValue = randomGenerator.nextDouble();
		
		return valueFactory.createLiteral(randomValue);
	}

}
