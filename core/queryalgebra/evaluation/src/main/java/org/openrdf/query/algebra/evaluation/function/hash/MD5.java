/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.hash;

import java.security.NoSuchAlgorithmException;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} MD5, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-md5">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class MD5 extends HashFunction {

	public String getURI() {
		return "MD5";
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("MD5 requires exactly 1 argument, got " + args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];

			if (QueryEvaluationUtil.isSimpleLiteral(literal)) {
				String lexValue = literal.getLabel();

				try {
					return valueFactory.createLiteral(hash(lexValue, "MD5"));
				}
				catch (NoSuchAlgorithmException e) {
					// MD5 should always be available.
					throw new RuntimeException(e);
				}
			}
			else {
				throw new ValueExprEvaluationException("Invalid argument for MD5: " + literal);
			}
		}
		else {
			throw new ValueExprEvaluationException("Invalid argument for Md5: " + args[0]);
		}
	}


}
