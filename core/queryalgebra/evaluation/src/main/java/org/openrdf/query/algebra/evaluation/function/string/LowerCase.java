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

/**
 * The SPARQL built-in {@link Function} LCASE, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-lcase">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class LowerCase implements Function {

	public String getURI() {
		return FN.LOWER_CASE.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 0) {
			throw new ValueExprEvaluationException("LCASE requires exactly 1 argument, got " + args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];

			String language = literal.getLanguage();

			// LowerCase function accepts only plain literals (optionally
			// language-tagged) or string-typed literals.
			if (language != null
					|| (literal.getDatatype() == null || XMLSchema.STRING.equals(literal.getDatatype())))
			{
				String lexicalValue = literal.getLabel().toLowerCase();

				if (language != null) {
					return valueFactory.createLiteral(lexicalValue, language);
				}
				else if (XMLSchema.STRING.equals(literal.getDatatype())) {
					return valueFactory.createLiteral(lexicalValue, XMLSchema.STRING);
				}
				else {
					return valueFactory.createLiteral(lexicalValue);
				}
			}
			else {
				throw new ValueExprEvaluationException("unexpected input value for function: " + args[0]);
			}
		}
		else {
			throw new ValueExprEvaluationException("unexpected input value for function: " + args[0]);
		}

	}

}
