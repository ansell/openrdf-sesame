/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.regex.Pattern;

/**
 * Compares the string representation of a value expression to a pattern.
 */
public class Regex extends UnaryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Pattern _pattern;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Regex() {
	}

	public Regex(ValueExpr expr, String pattern, String flags) {
		super(expr);
		setPattern(pattern, flags);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setPattern(String pattern, String flags) {
		assert pattern != null : "pattern must not be null";
		int f = 0;
		for (char c : flags.toCharArray()) {
			switch (c) {
				case 's':
					f |= Pattern.DOTALL;
					break;
				case 'm':
					f |= Pattern.MULTILINE;
					break;
				case 'i':
					f |= Pattern.CASE_INSENSITIVE;
					break;
				case 'x':
					f |= Pattern.COMMENTS;
					break;
				case 'd':
					f |= Pattern.UNIX_LINES;
					break;
				case 'u':
					f |= Pattern.UNICODE_CASE;
					break;
				default:
					throw new IllegalArgumentException(flags);
			}
		}
		_pattern = Pattern.compile(pattern, f);
	}

	public String getPattern() {
		if (_pattern == null)
			return "";
		return _pattern.toString();
	}

	public String getFlags() {
		if (_pattern == null)
			return "";
		int flags = _pattern.flags();
		StringBuffer f = new StringBuffer(6);
		if ((flags & Pattern.DOTALL) == Pattern.DOTALL)
			f.append('s');
		if ((flags & Pattern.MULTILINE) == Pattern.MULTILINE)
			f.append('m');
		if ((flags & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE)
			f.append('i');
		if ((flags & Pattern.COMMENTS) == Pattern.COMMENTS)
			f.append('x');
		if ((flags & Pattern.UNIX_LINES) == Pattern.UNIX_LINES)
			f.append('d');
		if ((flags & Pattern.UNICODE_CASE) == Pattern.UNICODE_CASE)
			f.append('u');
		return f.toString();
	}

	public Pattern getOpPattern() {
		return _pattern;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(128);

		sb.append("REGEX ").append(_pattern);

		return sb.toString();
	}

	public ValueExpr cloneValueExpr() {
		return new Regex(getArg().cloneValueExpr(), getPattern(), getFlags());
	}
}
