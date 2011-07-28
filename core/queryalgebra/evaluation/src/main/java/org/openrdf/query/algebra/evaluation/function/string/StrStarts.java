/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.builtin;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} STRSTARTS, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-strstarts">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class StrStarts implements Function {

	public String getURI() {
		return "STRSTARTS";
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException("STRSTARTS requires 2 arguments, got "
					+ args.length);
		}
		
		Value leftVal = args[0];
		Value rightVal = args[1];
		
		if (leftVal instanceof Literal && rightVal instanceof Literal) {
			Literal leftLit = (Literal)leftVal;
			Literal rightLit = (Literal)rightVal;

			if (leftLit.getLanguage() != null) {
				if (rightLit.getLanguage() == null || rightLit.getLanguage().equals(leftLit.getLanguage())) {

					String leftLexVal = leftLit.getLabel();
					String rightLexVal = rightLit.getLabel();

					return BooleanLiteralImpl.valueOf(leftLexVal.startsWith(rightLexVal));
				}
				else {
					throw new ValueExprEvaluationException("incompatible operands for STRSTARTS function");
				}
			}
			else if (QueryEvaluationUtil.isStringLiteral(leftLit)) {
				if (QueryEvaluationUtil.isStringLiteral(rightLit)) {
					String leftLexVal = leftLit.getLabel();
					String rightLexVal = rightLit.getLabel();

					return BooleanLiteralImpl.valueOf(leftLexVal.startsWith(rightLexVal));
				}
				else {
					throw new ValueExprEvaluationException("incompatible operands for STRSTARTS function");
				}
			}
			else {
				throw new ValueExprEvaluationException("incompatible operands for STRSTARTS function");
			}
		}
		else {
			throw new ValueExprEvaluationException("STRSTARTS function expects literal operands");
		}

		}

}
