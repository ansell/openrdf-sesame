/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public abstract class ASTPathExprTail extends SimpleNode {

	private boolean _isBranch = false;

	public ASTPathExprTail(int id) {
		super(id);
	}

	public ASTPathExprTail(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	public boolean isBranch() {
		return _isBranch;
	}

	public void setBranch(boolean isBranch) {
		_isBranch = isBranch;
	}

	public boolean hasNextTail() {
		return getNextTail() != null;
	}

	/**
	 * Gets the path epxression tail following this part of the path expression,
	 * if any.
	 * 
	 * @return The next part of the path expression, or <tt>null</tt> if this
	 *         is the last part of the path expression.
	 */
	public abstract ASTPathExprTail getNextTail();

	public String toString() {
		String result = super.toString();

		if (_isBranch) {
			result += " (branch)";
		}

		return result;
	}
}
