/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.queryrender.builder;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.And;
import org.eclipse.rdf4j.query.algebra.Bound;
import org.eclipse.rdf4j.query.algebra.Compare;
import org.eclipse.rdf4j.query.algebra.Lang;
import org.eclipse.rdf4j.query.algebra.LangMatches;
import org.eclipse.rdf4j.query.algebra.Not;
import org.eclipse.rdf4j.query.algebra.Or;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;

/**
 * <p>
 * Collection of utility methods for building the various ValueExpr objects in
 * the Sesame query API.
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public class ValueExprFactory {

	public static LangMatches langMatches(String theVar, String theLang) {
		return new LangMatches(new Lang(new Var(theVar)), new ValueConstant(
				SimpleValueFactory.getInstance().createLiteral(theLang)));
	}

	public static Bound bound(String theVar) {
		return new Bound(new Var(theVar));
	}

	public static Not not(ValueExpr theExpr) {
		return new Not(theExpr);
	}

	public static Or or(ValueExpr theLeft, ValueExpr theRight) {
		return new Or(theLeft, theRight);
	}

	public static And and(ValueExpr theLeft, ValueExpr theRight) {
		return new And(theLeft, theRight);
	}

	public static Compare lt(String theVar, String theOtherVar) {
		return compare(theVar, theOtherVar, Compare.CompareOp.LT);
	}

	public static Compare lt(String theVar, Value theValue) {
		return compare(theVar, theValue, Compare.CompareOp.LT);
	}

	public static Compare gt(String theVar, String theOtherVar) {
		return compare(theVar, theOtherVar, Compare.CompareOp.GT);
	}

	public static Compare gt(String theVar, Value theValue) {
		return compare(theVar, theValue, Compare.CompareOp.GT);
	}

	public static Compare eq(String theVar, String theOtherVar) {
		return compare(theVar, theOtherVar, Compare.CompareOp.EQ);
	}

	public static Compare eq(String theVar, Value theValue) {
		return compare(theVar, theValue, Compare.CompareOp.EQ);
	}

	public static Compare ne(String theVar, String theOtherVar) {
		return compare(theVar, theOtherVar, Compare.CompareOp.NE);
	}

	public static Compare ne(String theVar, Value theValue) {
		return compare(theVar, theValue, Compare.CompareOp.NE);
	}

	public static Compare le(String theVar, String theOtherVar) {
		return compare(theVar, theOtherVar, Compare.CompareOp.LE);
	}

	public static Compare le(String theVar, Value theValue) {
		return compare(theVar, theValue, Compare.CompareOp.LE);
	}

	public static Compare ge(String theVar, String theOtherVar) {
		return compare(theVar, theOtherVar, Compare.CompareOp.GE);
	}

	public static Compare ge(String theVar, Value theValue) {
		return compare(theVar, theValue, Compare.CompareOp.GE);
	}

	private static Compare compare(String theVar, Value theValue, Compare.CompareOp theOp) {
		return compare(new Var(theVar), new ValueConstant(theValue), theOp);
	}

	private static Compare compare(String theVar, String theValue, Compare.CompareOp theOp) {
		return compare(new Var(theVar), new Var(theValue), theOp);
	}

	private static Compare compare(ValueExpr theLeft, ValueExpr theRight, Compare.CompareOp theOp) {
		return new Compare(theLeft, theRight, theOp);
	}
}
