/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.queryrender.builder;

import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

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
				ValueFactoryImpl.getInstance().createLiteral(theLang)));
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
