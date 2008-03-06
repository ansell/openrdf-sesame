/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * Compares the string representation of a value expression to a pattern.
 */
public class Like extends UnaryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

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

	public Like() {
	}

	public Like(ValueExpr expr, String pattern, boolean caseSensitive) {
		super(expr);
		setPattern(pattern, caseSensitive);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setPattern(String pattern, boolean caseSensitive) {
		assert pattern != null : "pattern must not be null";
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

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(128);

		sb.append("LIKE ").append(_pattern);
		if (_caseSensitive) {
			sb.append(" IGNORE CASE");
		}

		return sb.toString();
	}

	public ValueExpr cloneValueExpr() {
		return new Like(getArg().cloneValueExpr(), _pattern, _caseSensitive);
	}
}
