/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;

/**
 * Compares the string representation of a value expression to a pattern.
 */
public class Regex extends NaryValueOperator {

	private static final long serialVersionUID = 2823066093898975827L;

	private Pattern compiled;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Regex() {
	}

	public Regex(ValueExpr expr, ValueExpr pattern, ValueExpr flags) {
		super(expr, pattern);
		setFlagsArg(flags);
	}

	public Regex(ValueExpr expr, String like, boolean caseSensitive) {
		setArg(expr);
		setLikePattern(like);
		setCaseSensitive(caseSensitive);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setArg(int idx, ValueExpr arg) {
		compiled = null;
		super.setArg(idx, arg);
		ValueExpr patternArg = getPatternArg();
		ValueExpr flagsArg = getFlagsArg();
		if (patternArg instanceof ValueConstant && (flagsArg instanceof ValueConstant || flagsArg == null)) {
			Value pvalue = ((ValueConstant)patternArg).getValue();
			Value fvalue = null;
			if (flagsArg != null) {
				fvalue = ((ValueConstant)flagsArg).getValue();
			}
			compiled = compile(pvalue, fvalue);
		}
	}

	public ValueExpr getArg() {
		return getArg(0);
	}

	public void setArg(ValueExpr leftArg) {
		setArg(0, leftArg);
	}

	public ValueExpr getPatternArg() {
		return getArg(1);
	}

	public void setPatternArg(ValueExpr rightArg) {
		setArg(1, rightArg);
	}

	public ValueExpr getFlagsArg() {
		return getArg(2);
	}

	public void setFlagsArg(ValueExpr flags) {
		setArg(2, flags);
	}

	public Pattern compile(Value pattern, Value flags) {
		if (compiled != null) {
			return compiled;
		}
		int f = 0;
		if (flags != null) {
			for (char c : flags.stringValue().toCharArray()) {
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
						return null;
				}
			}
		}
		try {
			return Pattern.compile(pattern.stringValue(), f);
		}
		catch (PatternSyntaxException e) {
			return null;
		}
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public Regex clone() {
		Regex clone = (Regex)super.clone();
		return clone;
	}

	private void setLikePattern(String pattern) {
		int prevPatternIndex = -1;
		int patternIndex = pattern.indexOf('*');

		if (patternIndex == -1) {
			// No wildcards
			String regex = "^" + escape(pattern, 0, pattern.length()) + "$";
			setPatternArg(new ValueConstant(new LiteralImpl(regex)));
			return;
		}

		StringBuilder regex = new StringBuilder();

		if (patternIndex > 0) {
			// Pattern does not start with a wildcard, first part must match
			regex.append("^");
			regex.append(escape(pattern, 0, patternIndex));

			prevPatternIndex = patternIndex;
			patternIndex = pattern.indexOf('*', patternIndex + 1);
		}

		while (patternIndex != -1) {
			// Get snippet between previous wildcard and this wildcard
			if (regex.length() > 0) {
				regex.append(".*");
			}
			regex.append(escape(pattern, prevPatternIndex + 1, patternIndex));

			prevPatternIndex = patternIndex;
			patternIndex = pattern.indexOf('*', patternIndex + 1);
		}

		// Part after last wildcard
		if (prevPatternIndex != pattern.length() - 1) {
			if (regex.length() > 0) {
				regex.append(".*");
			}
			regex.append(escape(pattern, prevPatternIndex + 1, pattern.length()));
			regex.append("$");
		}

		setPatternArg(new ValueConstant(new LiteralImpl(regex.toString())));
	}

	private String escape(String string, int start, int end) {
		String substring = string.substring(start, end);
		if (substring.matches("^[a-zA-Z]*$"))
			return substring; // safe
		return Pattern.quote(substring);
	}

	private void setCaseSensitive(boolean caseSensitive) {
		if (caseSensitive) {
			setFlagsArg(new ValueConstant(new LiteralImpl("s")));
		} else {
			setFlagsArg(new ValueConstant(new LiteralImpl("is")));
		}
	}
}
