/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


/**
 * Compares the string representation of a value expression to a pattern.
 */
public class Like extends BooleanExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr _expr;

	private String _pattern;

	private boolean _caseSensitive;

	/**
	 * Operational pattern, equal to _pattern but converted to lower case when
	 * not case sensitive.
	 */
	private String _opPattern;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Like(ValueExpr expr, String pattern, boolean caseSensitive) {
		setValueExpr(expr);
		setPattern(pattern, caseSensitive);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setValueExpr(ValueExpr expr) {
		_expr = expr;
	}

	public ValueExpr getValueExpr() {
		return _expr;
	}

	public void setPattern(String pattern, boolean caseSensitive) {
		_pattern = pattern;
		_caseSensitive = caseSensitive;
		_opPattern = _caseSensitive ? _pattern : _pattern.toLowerCase();
	}

	public String getPattern() {
		return _pattern;
	}

	public boolean isCaseSensitive() {
		return _caseSensitive;
	}

	public String getOpPattern() {
		return _opPattern;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	@Override
	public void visitChildren(QueryModelVisitor visitor)
	{
		_expr.visit(visitor);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(128);

		sb.append("LIKE ").append(_pattern);
		if (_caseSensitive) {
			sb.append(" IGNORE CASE");
		}

		return sb.toString();
	}
}
