/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.Comparator;

import info.aduna.lang.ObjectUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * A comparator that compares values according the SPARQL value ordering as
 * specified in <A
 * href="http://www.w3.org/TR/rdf-sparql-query/#modOrderBy">SPARQL Query
 * Language for RDF</a>.
 * 
 * @author james
 * @author Arjohn Kampman
 */
public class ValueComparator implements Comparator<Value> {

	public int compare(Value o1, Value o2) {
		// check equality
		if (ObjectUtil.nullEquals(o1, o2)) {
			return 0;
		}

		// 1. (Lowest) no value assigned to the variable
		if (o1 == null) {
			return -1;
		}
		if (o2 == null) {
			return 1;
		}

		// 2. Blank nodes
		boolean b1 = o1 instanceof BNode;
		boolean b2 = o2 instanceof BNode;
		if (b1 && b2) {
			return 0;
		}
		if (b1) {
			return -1;
		}
		if (b2) {
			return 1;
		}

		// 3. IRIs
		boolean u1 = o1 instanceof URI;
		boolean u2 = o2 instanceof URI;
		if (u1 && u2) {
			return o1.toString().compareTo(o2.toString());
		}
		if (u1) {
			return -1;
		}
		if (u2) {
			return 1;
		}

		// 4. RDF literals
		return compareLiterals((Literal)o1, (Literal)o2);
	}

	private int compareLiterals(Literal leftLit, Literal rightLit) {
		// Additional constraint for ORDER BY: "A plain literal is lower
		// than an RDF literal with type xsd:string of the same lexical
		// form."

		if (!QueryEvaluationUtil.isStringLiteral(leftLit) || !QueryEvaluationUtil.isStringLiteral(rightLit)) {
			try {
				boolean isSmaller = QueryEvaluationUtil.compareLiterals(leftLit, rightLit, CompareOp.LT);

				if (isSmaller) {
					return -1;
				}
				else {
					return 1;
				}
			}
			catch (ValueExprEvaluationException e) {
				// literals cannot be compared using the '<' operator, continue
				// below
			}
		}

		// Sort by label first
		int result = leftLit.getLabel().compareTo(rightLit.getLabel());

		if (result == 0) {
			// Labels are equal, enforce order plain literal < datatyped literal
			URI leftDatatype = leftLit.getDatatype();
			URI rightDatatype = rightLit.getDatatype();

			if (leftDatatype != null) {
				if (rightDatatype != null) {
					result = compare(leftDatatype, rightDatatype);
				}
				else {
					result = 1;
				}
			}
			else if (rightDatatype != null) {
				result = -1;
			}
		}

		if (result == 0) {
			// Both datatypes are null, enforce order simple literal < lang-literal
			String leftLanguage = leftLit.getLanguage();
			String rightLanguage = rightLit.getLanguage();

			if (leftLanguage != null) {
				if (rightLanguage != null) {
					result = leftLanguage.compareTo(rightLanguage);
				}
				else {
					result = 1;
				}
			}
			else if (rightLanguage != null) {
				result = -1;
			}
		}

		return result;
	}
}