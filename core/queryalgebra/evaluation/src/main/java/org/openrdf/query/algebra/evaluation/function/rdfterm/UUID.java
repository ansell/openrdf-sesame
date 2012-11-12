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
 * The SPARQL built-in {@link Function} UUID, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-uuid">SPARQL Query Language
 * for RDF</a>
 * 
 * @since 2.7.0
 * @author Jeen Broekstra
 */
public class UUID implements Function {

	public String getURI() {
		return "UUID";
	}

	public URI evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length > 0) {
			throw new ValueExprEvaluationException("UUID requires 0 arguments, got " + args.length);
		}

		URI uri = valueFactory.createURI("urn:uuid:" + java.util.UUID.randomUUID().toString());

		return uri;
	}

}
