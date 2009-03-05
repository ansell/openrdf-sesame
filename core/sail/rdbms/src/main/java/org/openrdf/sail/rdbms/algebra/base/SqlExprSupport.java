/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.base;

import java.sql.Types;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.sail.rdbms.algebra.DoubleValue;
import org.openrdf.sail.rdbms.algebra.FalseValue;
import org.openrdf.sail.rdbms.algebra.SqlAbs;
import org.openrdf.sail.rdbms.algebra.SqlAnd;
import org.openrdf.sail.rdbms.algebra.SqlCase;
import org.openrdf.sail.rdbms.algebra.SqlCast;
import org.openrdf.sail.rdbms.algebra.SqlCompare;
import org.openrdf.sail.rdbms.algebra.SqlConcat;
import org.openrdf.sail.rdbms.algebra.SqlEq;
import org.openrdf.sail.rdbms.algebra.SqlIsNull;
import org.openrdf.sail.rdbms.algebra.SqlLike;
import org.openrdf.sail.rdbms.algebra.SqlLowerCase;
import org.openrdf.sail.rdbms.algebra.SqlMathExpr;
import org.openrdf.sail.rdbms.algebra.SqlNot;
import org.openrdf.sail.rdbms.algebra.SqlNull;
import org.openrdf.sail.rdbms.algebra.SqlOr;
import org.openrdf.sail.rdbms.algebra.SqlRegex;
import org.openrdf.sail.rdbms.algebra.StringValue;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Support method to create SQL expressions.
 * 
 * @author James Leigh
 */
public class SqlExprSupport {

	public static SqlExpr abs(SqlExpr arg) {
		return new SqlAbs(arg);
	}

	public static SqlExpr and(SqlExpr... args) {
		return new SqlAnd(args);
	}

	public static SqlExpr cmp(SqlExpr left, CompareOp op, SqlExpr right) {
		return new SqlCompare(left, op, right);
	}

	public static SqlExpr coalesce(SqlExpr... exprs) {
		SqlCase sqlCase = new SqlCase();
		for (SqlExpr expr : exprs) {
			sqlCase.when(isNotNull(expr.clone()), expr);
		}
		return sqlCase;
	}

	public static SqlExpr concat(SqlExpr left, SqlExpr right) {
		return new SqlConcat(left, right);
	}

	public static SqlExpr eq(SqlExpr left, SqlExpr right) {
		return new SqlEq(left, right);
	}

	public static SqlExpr eqComparingNull(SqlExpr left, SqlExpr right) {
		SqlExpr leftIsNull = isNull(left.clone());
		SqlExpr rightIsNull = isNull(right.clone());
		SqlExpr bothNull = and(leftIsNull, rightIsNull);
		SqlExpr bothNotNull = and(not(leftIsNull), not(rightIsNull));
		return or(bothNull, and(bothNotNull, eq(left, right)));
	}

	public static SqlExpr eqIfNotNull(SqlExpr left, SqlExpr right) {
		SqlExpr leftIsNotNull = isNotNull(left.clone());
		SqlExpr rightIsNotNull = isNotNull(right.clone());
		SqlExpr bothNotNull = and(leftIsNotNull, rightIsNotNull);
		return and(bothNotNull, eq(left, right));
	}

	public static SqlExpr eqOrBothNull(SqlExpr left, SqlExpr right) {
		SqlExpr leftIsNull = isNull(left.clone());
		SqlExpr rightIsNull = isNull(right.clone());
		SqlExpr bothNull = and(leftIsNull, rightIsNull);
		return or(bothNull, eq(left, right));
	}

	public static SqlExpr eqOrSimpleType(SqlExpr left, SqlExpr right) {
		SqlExpr bothSimple = and(simple(left), simple(right));
		return or(eq(left.clone(), right.clone()), bothSimple);
	}

	public static SqlExpr ge(SqlExpr left, SqlExpr right) {
		return new SqlCompare(left, CompareOp.GE, right);
	}

	public static SqlExpr gt(SqlExpr left, SqlExpr right) {
		return new SqlCompare(left, CompareOp.GT, right);
	}

	public static SqlExpr in(SqlExpr compare, SqlExpr... values) {
		SqlExpr expr = null;
		for (SqlExpr value : values) {
			if (expr == null) {
				expr = new SqlEq(compare, value);
			}
			else {
				expr = or(expr, new SqlEq(compare.clone(), value));
			}
		}
		if (expr == null) {
			return new FalseValue();
		}
		return expr;
	}

	public static SqlExpr isNotNull(SqlExpr arg) {
		return not(isNull(arg));
	}

	public static SqlExpr isNull(SqlExpr arg) {
		return new SqlIsNull(arg);
	}

	public static SqlExpr le(SqlExpr left, SqlExpr right) {
		return new SqlCompare(left, CompareOp.LE, right);
	}

	public static SqlExpr like(SqlExpr left, SqlExpr right) {
		return new SqlLike(left, right);
	}

	public static SqlExpr lowercase(SqlExpr arg) {
		return new SqlLowerCase(arg);
	}

	public static SqlExpr lt(SqlExpr left, SqlExpr right) {
		return new SqlCompare(left, CompareOp.LT, right);
	}

	public static SqlExpr neq(SqlExpr left, SqlExpr right) {
		return new SqlNot(new SqlEq(left, right));
	}

	public static SqlExpr neqComparingNull(SqlExpr left, SqlExpr right) {
		SqlExpr leftIsNull = isNull(left.clone());
		SqlExpr rightIsNull = isNull(right.clone());
		SqlExpr onlyLeftIsNull = and(not(leftIsNull), rightIsNull.clone());
		SqlExpr onlyRightIsNull = and(leftIsNull.clone(), not(rightIsNull));
		SqlExpr compareNull = or(onlyRightIsNull, onlyLeftIsNull);
		return or(not(eq(left, right)), compareNull);
	}

	public static SqlExpr not(SqlExpr arg) {
		return new SqlNot(arg);
	}

	public static SqlExpr num(double value) {
		return new DoubleValue(value);
	}

	public static SqlExpr or(SqlExpr... args) {
		return new SqlOr(args);
	}

	public static SqlExpr regex(SqlExpr value, SqlExpr pattern) {
		return new SqlRegex(value, pattern);
	}

	public static SqlExpr regex(SqlExpr value, SqlExpr pattern, SqlExpr flags) {
		return new SqlRegex(value, pattern, flags);
	}

	public static SqlExpr simple(SqlExpr arg) {
		SqlExpr isString = eq(arg.clone(), str(XMLSchema.STRING));
		return or(isNull(arg.clone()), isString);
	}

	public static SqlExpr sqlNull() {
		return new SqlNull();
	}

	public static SqlExpr str(String str) {
		if (str == null) {
			return sqlNull();
		}
		return new StringValue(str);
	}

	public static SqlExpr str(URI uri) {
		return new StringValue(uri.stringValue());
	}

	public static SqlExpr sub(SqlExpr left, SqlExpr right) {
		return new SqlMathExpr(left, MathExpr.MathOp.MINUS, right);
	}

	public static SqlExpr text(SqlExpr arg) {
		return new SqlCast(arg, Types.VARCHAR);
	}

	public static UnsupportedRdbmsOperatorException unsupported(Object arg) {
		return new UnsupportedRdbmsOperatorException(arg.toString());
	}

	private SqlExprSupport() {
		// no constructor
	}

}
