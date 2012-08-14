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
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} SHA512, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-sha512">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class SHA512 extends HashFunction {

	public String getURI() {
		return "SHA512";
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("SHA512 requires exactly 1 argument, got " + args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];

			if (QueryEvaluationUtil.isSimpleLiteral(literal) || XMLSchema.STRING.equals(literal.getDatatype())) {
				String lexValue = literal.getLabel();

				try {
					return valueFactory.createLiteral(hash(lexValue, "SHA-512"));
				}
				catch (NoSuchAlgorithmException e) {
					// SHA512 should always be available.
					throw new RuntimeException(e);
				}
			}
			else {
				throw new ValueExprEvaluationException("Invalid argument for SHA512: " + literal);
			}
		}
		else {
			throw new ValueExprEvaluationException("Invalid argument for SHA512: " + args[0]);
		}
	}

}
