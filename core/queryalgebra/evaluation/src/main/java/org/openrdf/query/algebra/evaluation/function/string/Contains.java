/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.string;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} CONTAINS, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-contains">SPARQL Query
 * Language for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Contains implements Function {

	public String getURI() {
		return FN.CONTAINS.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException("CONTAINS requires 2 arguments, got " + args.length);
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

					return BooleanLiteralImpl.valueOf(leftLexVal.contains(rightLexVal));
				}
				else {
					throw new ValueExprEvaluationException("incompatible operands for CONTAINS function");
				}
			}
			else if (QueryEvaluationUtil.isSimpleLiteral(leftLit)) {
				if (QueryEvaluationUtil.isSimpleLiteral(rightLit)) {
					String leftLexVal = leftLit.getLabel();
					String rightLexVal = rightLit.getLabel();

					return BooleanLiteralImpl.valueOf(leftLexVal.contains(rightLexVal));
				}
				else {
					throw new ValueExprEvaluationException("incompatible operands for CONTAINS function");
				}
			}
			else {
				throw new ValueExprEvaluationException("incompatible operands for CONTAINS function");
			}
		}
		else {
			throw new ValueExprEvaluationException("CONTAINS function expects literal operands");
		}

	}

}
